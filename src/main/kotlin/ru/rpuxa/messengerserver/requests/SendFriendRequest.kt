package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer

object SendFriendRequest : TokenRequest("/friends/sendRequest") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val friendLogin = query["login"] ?: return Error.WRONG_ARGS

        return DataBase.sendFriendRequest(token, friendLogin)
    }
}