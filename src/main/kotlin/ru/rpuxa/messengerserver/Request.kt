package ru.rpuxa.messengerserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.answers.ErrorAnswer


abstract class Request(val path: String) {

    open fun execute(exchange: HttpExchange): ByteArray? {
        val map = HashMap<String, String>()

        (exchange.requestURI.query ?: "").split('&').forEach {
            if ('=' in it) {
                val (name, value) = it.split('=')
                if (name.isNotBlank())
                    map[name] = value
            }
        }

       return byteArrayAnswer(map, exchange)
    }

    open fun byteArrayAnswer(
        query: Map<String, String>,
        exchange: HttpExchange
    ): ByteArray {
        var answer: RequestAnswer = requestAnswer(query, exchange)
        if (answer is Error) {
            answer = ErrorAnswer(answer.code.toString(), answer.text)
        }
        return gson.toJson(answer).toByteArray()
    }

    open fun requestAnswer(
        query: Map<String, String>,
        exchange: HttpExchange
    ): RequestAnswer {
        throw UnsupportedOperationException()
    }

    companion object {
        private val gson: Gson = GsonBuilder().create()
    }
}