package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer


object PublicInfoRequest : Request("/profile/getPublicInfo") {

    override fun onExecute(query: Map<String, String>): RequestAnswer {
        val id = query["id"]?.toIntOrNull() ?: return Error.WRONG_ARGS

        return DataBase.getPublicInfo(id)
    }
}