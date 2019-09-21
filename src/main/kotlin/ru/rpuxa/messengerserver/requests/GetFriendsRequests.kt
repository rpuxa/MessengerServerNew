package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.RequestAnswer

object GetFriendsRequests : TokenRequest("friends/getRequests") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        return DataBase.getFriendRequests(token)
    }
}