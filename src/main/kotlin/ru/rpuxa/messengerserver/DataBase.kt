package ru.rpuxa.messengerserver

import ru.rpuxa.messengerserver.answers.PrivateProfileInfo
import ru.rpuxa.messengerserver.answers.PublicProfileInfo
import ru.rpuxa.messengerserver.answers.TokenAnswer
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.*

object DataBase {

    const val PATH = "messenger.db"

    private const val USERS_TABLE = "users"

    const val ID = "id"
    const val TOKEN = "token"
    const val LOGIN = "login"
    const val PASSWORD = "pass"
    const val NAME = "name"
    const val SURNAME = "surname"
    const val BIRTHDAY = "birthday"
    const val AVATAR = "avatar"

    private val BIRTHDAY_NOT_INITIALIZED: String? = null
    private val ICON_NOT_INITIALIZED: String? = null

    private lateinit var connection: Connection
    private lateinit var statement: Statement

    fun connect() {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$PATH")
        statement = connection.createStatement()

        statement.execute(
            """CREATE TABLE IF NOT EXISTS $USERS_TABLE
(
  '$ID'       INTEGER PRIMARY KEY AUTOINCREMENT,
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
            """INSERT INTO users ($TOKEN, $LOGIN, $PASSWORD, $NAME, $SURNAME, $BIRTHDAY, $AVATAR)
VALUES (?, ?, ?, ?, ?, ?, ?)"""
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

        return PrivateProfileInfo(
            set.getInt(ID),
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
        connection.prepareStatement("UPDATE $USERS_TABLE SET $AVATAR = ? WHERE $ID = ?").apply {
            setString(1, link)
            setInt(2, id)
            executeQuery()
        }
    }

    fun getIdByToken(token: String) = userByToken(token)?.getInt(ID)

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
        val set = connection.prepareStatement("SELECT * FROM $USERS_TABLE WHERE $ID = ?").run {
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


}