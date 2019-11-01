package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer

object SendMessageRequest : TokenRequest("/dialogs/sendMessage") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val id = query["id"]?.toIntOrNull() ?: return Error.WRONG_ARGS
        val text = query["text"] ?: return Error.WRONG_ARGS
        val randomUUID = query["randomUUID"] ?: return Error.WRONG_ARGS

        return DataBase.sendMessage(token, id, text, randomUUID)
    }
}