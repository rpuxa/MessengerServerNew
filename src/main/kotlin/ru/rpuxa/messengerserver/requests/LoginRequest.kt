package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer

object LoginRequest : Request("/login") {

    override fun requestAnswer(
        query: Map<String, String>,
        exchange: HttpExchange
    ): RequestAnswer {
        val login = query["login"] ?: return Error.WRONG_ARGS
        val pass = query["pass"] ?: return Error.WRONG_ARGS

        return DataBase.login(login, pass)
    }
}