package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.RequestAnswer

object GetLastActionIdRequest : TokenRequest("/getLastActionId") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer =
        DataBase.getLastActionId(token)
}