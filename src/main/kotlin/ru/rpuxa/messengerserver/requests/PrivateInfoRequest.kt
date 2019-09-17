package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.RequestAnswer

object PrivateInfoRequest : TokenRequest("/profile/getPrivateInfo") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>): RequestAnswer {
        return DataBase.getPrivateInfo(token)
    }
}