package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.*
import ru.rpuxa.messengerserver.answers.UploadIconAnswer
import java.io.*

object SetAvatarRequest : Request("/profile/setAvatar") {

    override fun requestAnswer(query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val token = query["token"] ?: return Error.WRONG_ARGS

        val input = exchange.requestBody
        val builder = StringBuilder()
        var size: Int? = null
        try {
            while (true) {
                val char = input.read().toChar()
                if (char == '\n') {
                    if (builder.startsWith("Content-Length: ")) {
                        size = builder.substring(0, builder.lastIndex).substring(16).toIntOrNull()
                            ?: return Error.WRONG_ARGS
                        break
                    }
                    builder.clear()
                } else {
                    builder.append(char)
                }
            }
        } catch (e: EOFException) {
        }
        val first = input.read().toChar()
        val second = input.read().toChar()
        if (size == null || first != '\r' || second != '\n') {
            return Error.WRONG_ARGS
        }

        val id = DataBase.getIdByToken(token) ?: return Error.UNKNOWN_TOKEN
        val link = IconStorage.saveAvatar(input, size, id)
        DataBase.setAvatar(id, link)

        return UploadIconAnswer(link)
    }
}