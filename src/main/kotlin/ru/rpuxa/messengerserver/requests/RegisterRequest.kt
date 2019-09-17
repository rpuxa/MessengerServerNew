package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.*

object RegisterRequest : Request("/reg") {

    override fun onExecute(query: Map<String, String>): RequestAnswer {
        val login = query["login"] ?: return Error.WRONG_ARGS
        val pass = query["pass"] ?: return Error.WRONG_ARGS
        val name = query["name"] ?: return Error.WRONG_ARGS
        val surname = query["surname"] ?: return Error.WRONG_ARGS

        return UserDataConditions.checkAll(login, pass, name, surname) ?: DataBase.createNewUser(login, pass, name, surname)
    }
}