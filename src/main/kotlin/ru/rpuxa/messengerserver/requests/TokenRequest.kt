package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer

abstract class TokenRequest(path: String) : Request(path) {

    final override fun requestAnswer(
        query: Map<String, String>,
        exchange: HttpExchange
    ): RequestAnswer {
        val token = query["token"] ?: return Error.WRONG_ARGS
        return onExecuteWithToken(token, query, exchange)
    }

    abstract fun onExecuteWithToken(
        token: String,
        query: Map<String, String>,
        exchange: HttpExchange
    ): RequestAnswer
}