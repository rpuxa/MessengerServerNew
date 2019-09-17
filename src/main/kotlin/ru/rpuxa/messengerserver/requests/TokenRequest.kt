package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer

abstract class TokenRequest(path: String) : Request(path) {

    final override fun onExecute(query: Map<String, String>): RequestAnswer {
        val token = query["token"] ?: return Error.WRONG_ARGS
        return onExecuteWithToken(token, query)
    }

    abstract fun onExecuteWithToken(token: String, query: Map<String, String>): RequestAnswer
}