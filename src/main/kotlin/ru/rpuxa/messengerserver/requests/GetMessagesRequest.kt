package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer

object GetMessagesRequest : TokenRequest("dialogs/getMessages") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val id = query["id"]?.toIntOrNull() ?: return Error.WRONG_ARGS
        val messageId = query["messageId"]?.toIntOrNull() ?: return Error.WRONG_ARGS
        val limit = query["limit"]?.toIntOrNull() ?: return Error.WRONG_ARGS

        return DataBase.getMessages(token, id, messageId, limit)
    }
}