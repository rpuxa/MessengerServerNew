package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer

object RemoveFriendRequest : TokenRequest("/friends/remove") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val id = query["id"]?.toIntOrNull() ?: return Error.WRONG_ARGS
        return DataBase.removeFriend(token, id)
    }
}