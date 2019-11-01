@file:Suppress("SqlNoDataSourceInspection")

package ru.rpuxa.messengerserver

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import ru.rpuxa.messengerserver.answers.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.*
import kotlin.math.max
import kotlin.math.min

object DataBase {

    const val PATH = "messenger.db"

    private const val USERS_TABLE = "users"


    const val USER_ID = "id"
    const val TOKEN = "token"
    const val LOGIN = "login"
    const val PASSWORD = "pass"
    const val NAME = "name"
    const val SURNAME = "surname"
    const val BIRTHDAY = "birthday"
    const val AVATAR = "avatar"

    private val BIRTHDAY_NOT_INITIALIZED: String? = null
    private val ICON_NOT_INITIALIZED: String? = null

    private const val ACTIONS_TABLE = "act"
    private const val ACTION_ID = "act_id"
    private const val ACTION_TYPE = "act_type"

    private const val FRIENDS_TABLE = "fr"
    private const val FRIENDS_ID = "fr_id"

    private const val FRIENDS_REQUESTS_TABLE = "frReq"
    private const val DIALOG_TABLE = "dialog"

    private const val DIALOG_ID = "id"
    private const val DIALOG_RANDOM_UUID = "uuid"
    private const val DIALOG_TEXT = "text"
    private const val DIALOG_TIME = "time"
    private const val DIALOG_SENDER = "sender"

    private lateinit var connection: Connection
    private lateinit var statement: Statement

    fun connect() {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$PATH")
        statement = connection.createStatement()

        statement.execute(
            """CREATE TABLE IF NOT EXISTS $USERS_TABLE
(
  '$USER_ID'       INTEGER PRIMARY KEY AUTOINCREMENT,
  '$TOKEN'    TEXT,
  '$LOGIN'    TEXT,
  '$PASSWORD'     BLOB,
  '$NAME'     TEXT,
  '$SURNAME'  TEXT,
  '$BIRTHDAY' TEXT,
  '$AVATAR'    TEXT
);"""
        )
    }

    fun createNewUser(login: String, pass: String, name: String, surname: String): RequestAnswer {
        if (userByLogin(login) != null) return Error.LOGIN_ALREADY_EXISTS

        val encryptedPass = encrypt(pass)
        val token = randomToken()

        connection.prepareStatement(
            """ INSERT INTO $USERS_TABLE ($TOKEN, $LOGIN, $PASSWORD, $NAME, $SURNAME, $BIRTHDAY, $AVATAR) VALUES (?, ?, ?, ?, ?, ?, ?) """
        )
            .apply {
                setString(1, token)
                setString(2, login)
                setBytes(3, encryptedPass)
                setString(4, name)
                setString(5, surname)
                setString(6, BIRTHDAY_NOT_INITIALIZED)
                setString(7, ICON_NOT_INITIALIZED)
                executeUpdate()
            }

        val id = userByToken(token)!!.getInt(USER_ID)

        statement.execute(
            """CREATE TABLE IF NOT EXISTS $ACTIONS_TABLE$id ('$ACTION_ID' INTEGER PRIMARY KEY AUTOINCREMENT, '$ACTION_TYPE' INTEGER);"""
        )

        statement.execute(
            """CREATE TABLE IF NOT EXISTS $FRIENDS_TABLE$id ('$FRIENDS_ID' INTEGER PRIMARY KEY);"""
        )

        statement.execute(
            """CREATE TABLE IF NOT EXISTS $FRIENDS_REQUESTS_TABLE$id ('$FRIENDS_ID' INTEGER PRIMARY KEY);"""
        )



        return TokenAnswer(token)
    }

    fun login(login: String, pass: String): RequestAnswer {
        val encryptedPass = encrypt(pass)

        val set = userByLogin(login) ?: return Error.WRONG_LOGIN_OR_PASSWORD
        val currentPass = set.getBytes(PASSWORD)

        if (!encryptedPass.contentEquals(currentPass)) return Error.WRONG_LOGIN_OR_PASSWORD

        return TokenAnswer(set.getString(TOKEN))
    }

    fun getPrivateInfo(token: String): RequestAnswer {
        val set = userByToken(token) ?: return Error.UNKNOWN_TOKEN

        val id = set.getInt(USER_ID)
        val lastActionId = statement.executeQuery("SELECT * FROM $ACTIONS_TABLE$id ORDER BY $ACTION_ID DESC LIMIT 1")

        return PrivateProfileInfo(
            id,
            if (lastActionId.next()) lastActionId.getInt(ACTION_ID) else 0,
            set.getString(LOGIN),
            set.getString(NAME),
            set.getString(SURNAME),
            set.getString(BIRTHDAY),
            set.getString(AVATAR)
        )
    }


    fun getPublicInfo(id: Int): RequestAnswer {
        val set = userById(id) ?: return Error.UNKNOWN_ID

        return PublicProfileInfo(
            set.getString(LOGIN),
            set.getString(NAME),
            set.getString(SURNAME),
            set.getString(BIRTHDAY),
            set.getString(AVATAR)
        )
    }

    fun setUserField(token: String, currentPassword: String?, fieldName: String, value: String): Error {
        if (userByToken(token) == null) return Error.UNKNOWN_TOKEN

        fun setField() = connection.prepareStatement("UPDATE $USERS_TABLE SET $fieldName = ? WHERE $TOKEN = ?").apply {
            setString(2, token)
        }

        fun checkPass(): Boolean {
            val statement = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $TOKEN = ? AND $PASSWORD = ?")
            statement.setString(1, token)
            statement.setBytes(2, encrypt(currentPassword!!))

            return statement.executeQuery().next()
        }

        when (fieldName) {
            LOGIN -> {
                if (currentPassword == null) return Error.CURRENT_PASSWORD_NEEDED
                if (!checkPass()) return Error.CURRENT_PASSWORD_WRONG

                UserDataConditions.checkLogin(value)?.also { return it }

                if (userByLogin(value) != null) return Error.LOGIN_ALREADY_EXISTS

                setField().apply { setString(1, value) }.executeUpdate()
            }

            PASSWORD -> {
                if (currentPassword == null) return Error.CURRENT_PASSWORD_NEEDED
                if (!checkPass()) return Error.CURRENT_PASSWORD_WRONG

                UserDataConditions.checkPassword(value)?.also { return it }
                setField().apply { setBytes(1, encrypt(value)) }.executeUpdate()
            }

            NAME -> {
                UserDataConditions.checkName(value)?.also { return it }

                setField().apply { setString(1, value) }.executeUpdate()
            }

            SURNAME -> {
                UserDataConditions.checkSurname(value)?.also { return it }

                setField().apply { setString(1, value) }.executeUpdate()
            }

            BIRTHDAY -> {
                UserDataConditions.checkBirthday(value)?.also { return it }
                setField().apply { setString(1, value) }.executeUpdate()
            }

            else -> Error.UNKNOWN_USER_FIELD
        }

        return Error.NO_ERROR
    }

    fun setAvatar(id: Int, link: String) {
        connection.prepareStatement("UPDATE $USERS_TABLE SET $AVATAR = ? WHERE $USER_ID = ?").apply {
            setString(1, link)
            setInt(2, id)
            executeUpdate()
        }
    }

    fun sendFriendRequest(token: String, login: String): RequestAnswer {
        val friend = userByLogin(login) ?: return Error.ACCOUNT_IS_NOT_FOUND
        val user = userByToken(token) ?: return Error.UNKNOWN_TOKEN
        val friendId = friend.getInt(USER_ID)
        val userId = user.getInt(USER_ID)

        if (friendId == userId) {
            return Error.CANT_SEND_REQUEST_TO_YOURSELF
        }

        if (connection.prepareStatement("SELECT * FROM $FRIENDS_TABLE$friendId WHERE $FRIENDS_ID = ?").run {
                setInt(1, userId)
                executeQuery()
            }.next()
        ) {
            return Error.ALREADY_IN_FRIENDS
        }

        if (connection.prepareStatement("SELECT * FROM $FRIENDS_REQUESTS_TABLE$friendId WHERE $FRIENDS_ID = ?").run {
                setInt(1, userId)
                executeQuery()
            }.next()
        ) {
            return Error.ALREADY_SENT_REQUEST
        }

        connection.prepareStatement("INSERT INTO $FRIENDS_REQUESTS_TABLE$friendId ($FRIENDS_ID) VALUES (?)").run {
            setInt(1, userId)
            executeUpdate()
        }

        onAction(friendId, ActionType.FRIEND_REQUEST_RECEIVED)

        return Error.NO_ERROR
    }

    fun getFriendRequests(token: String): RequestAnswer {
        val id = getIdByToken(token) ?: return Error.UNKNOWN_TOKEN

        val set = statement.executeQuery("SELECT * FROM $FRIENDS_REQUESTS_TABLE$id")

        val list = ArrayList<Int>()

        while (set.next()) {
            list.add(set.getInt(FRIENDS_ID))
        }

        return IdsAnswer(list)
    }

    fun answerOnFriendRequest(token: String, friendId: Int, accept: Boolean): RequestAnswer {
        val id = getIdByToken(token) ?: return Error.UNKNOWN_TOKEN

        if (connection.prepareStatement("SELECT * FROM $FRIENDS_REQUESTS_TABLE$id WHERE $FRIENDS_ID = ?").run {
                setInt(1, friendId)
                !executeQuery().next()
            }) {
            return Error.REQUEST_NOT_FOUND
        }

        connection.prepareStatement("DELETE FROM $FRIENDS_REQUESTS_TABLE$id WHERE $FRIENDS_ID = ?").run {
            setInt(1, friendId)
            executeUpdate()
        }

        if (accept) {
            connection.prepareStatement("INSERT INTO $FRIENDS_TABLE$friendId ($FRIENDS_ID) VALUES (?)").run {
                setInt(1, id)
                executeUpdate()
            }

            connection.prepareStatement("INSERT INTO $FRIENDS_TABLE$id ($FRIENDS_ID) VALUES (?)").run {
                setInt(1, friendId)
                executeUpdate()
            }

            statement.execute(
                """CREATE TABLE IF NOT EXISTS $DIALOG_TABLE${min(id, friendId)}_${max(friendId, id)} (
  $DIALOG_ID     INTEGER PRIMARY KEY AUTOINCREMENT,
  $DIALOG_RANDOM_UUID   TEXT,
  $DIALOG_TEXT   TEXT,
  $DIALOG_TIME   BIGINT,
  $DIALOG_SENDER INTEGER
);"""
            )


            onAction(friendId, ActionType.FRIEND_REQUEST_ACCEPTED)
        }

        return Error.NO_ERROR
    }

    fun getAllFriends(token: String): RequestAnswer {
        val id = getIdByToken(token) ?: return Error.UNKNOWN_TOKEN
        val set = statement.executeQuery("SELECT * FROM $FRIENDS_TABLE$id")

        val list = ArrayList<Int>()
        while (set.next()) {
            list.add(set.getInt(FRIENDS_ID))
        }

        return IdsAnswer(list)
    }

    fun sendMessage(token: String, toId: Int, text: String, randomUUID: String): RequestAnswer {
        val id = getIdByToken(token) ?: return Error.UNKNOWN_TOKEN
        val table = "$DIALOG_TABLE${min(id, toId)}_${max(id, toId)}"
        fun messageWithRandomUUID() =
            connection.prepareStatement("SELECT * FROM $table WHERE $DIALOG_RANDOM_UUID = ?").run {
                setString(1, randomUUID)
                executeQuery()
            }

        val set = messageWithRandomUUID()

        if (set.next()) {
            return SendMessageAnswer(set.getInt(DIALOG_ID))
        }

        connection.prepareStatement("INSERT INTO $table ($DIALOG_RANDOM_UUID, $DIALOG_TEXT, $DIALOG_TIME, $DIALOG_SENDER) VALUES (?, ?, ?, ?)")
            .run {
                setString(1, randomUUID)
                setString(2, text)
                setLong(3, System.currentTimeMillis())
                setInt(4, id)
                executeUpdate()
            }


        onAction(toId, ActionType.NEW_MESSAGE)

        return SendMessageAnswer(messageWithRandomUUID().getInt(DIALOG_ID))
    }

    fun getMessages(token: String, toId: Int, messageId: Int, limit: Int): RequestAnswer {
        val id = getIdByToken(token) ?: return Error.UNKNOWN_TOKEN
        val table = "$DIALOG_TABLE${min(id, toId)}_${max(id, toId)}"
        val set = connection.prepareStatement("SELECT * FROM $table WHERE $DIALOG_ID <= ? AND $DIALOG_ID > ?").run {
            setInt(1, messageId)
            setInt(2, messageId - limit)
            executeQuery()
        }

        val array = MessagesAnswer()

        while (set.next()) {
            array.add(
                Message(
                    set.getInt(DIALOG_ID),
                    set.getString(DIALOG_RANDOM_UUID),
                    set.getString(DIALOG_TEXT),
                    set.getInt(DIALOG_SENDER)
                )
            )
        }

        return array
    }

    val actionChannel = HashMap<Int, Channel<Unit>>()

    private fun onAction(userId: Int, type: ActionType) {
        connection.prepareStatement("INSERT INTO $ACTIONS_TABLE$userId ($ACTION_TYPE) VALUES (?)").run {
            setInt(1, type.id)
            executeUpdate()
        }

        runBlocking {
            actionChannel[userId]?.send(Unit)
        }
    }

    fun getNewActions(userId: Int, actionId: Int): List<Action> {
        val set = connection.prepareStatement("SELECT * FROM $ACTIONS_TABLE$userId WHERE $ACTION_ID > ?").run {
            setInt(1, actionId)
            executeQuery()
        }

        val list = ArrayList<Action>()

        while (set.next()) {
            list.add(
                Action(
                    set.getInt(ACTION_ID),
                    set.getInt(ACTION_TYPE)
                )
            )
        }

        return list
    }

    fun getIdByToken(token: String) = userByToken(token)?.getInt(USER_ID)

    fun disconnect() {
        try {
            connection.close()
        } catch (e: SQLException) {
        }
        try {
            statement.close()
        } catch (e: SQLException) {
        }
    }

    private fun userByToken(token: String): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $TOKEN = ?").run {
            setString(1, token)
            executeQuery()
        }

        return if (set.next()) set else null
    }

    private fun userByLogin(login: String): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $LOGIN = ?").run {
            setString(1, login)
            executeQuery()
        }

        return if (set.next()) set else null
    }

    private fun userById(id: Int): ResultSet? {
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $USER_ID = ?").run {
            setInt(1, id)
            executeQuery()
        }

        return if (set.next()) set else null
    }


    private val digest = MessageDigest.getInstance("SHA-256")
    private val random = SecureRandom()

    private fun encrypt(s: String): ByteArray = digest.digest(s.toByteArray())

    private fun randomToken() = buildString {
        repeat(32) {
            val code =
                if (random.nextBoolean()) {
                    'a'.toInt() + random.nextInt('z' - 'a')
                } else {
                    'A'.toInt() + random.nextInt('Z' - 'A')
                }

            append(code.toChar())
        }
    }

    fun removeFriend(token: String, friendId: Int): RequestAnswer {
        val myId = getIdByToken(token) ?: return Error.UNKNOWN_TOKEN
        connection.prepareStatement("DELETE FROM $FRIENDS_TABLE$myId WHERE $FRIENDS_ID = ?").run {
            setInt(1, friendId)
            executeUpdate()
        }

        return Error.NO_ERROR
    }


}