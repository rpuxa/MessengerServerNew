package ru.rpuxa.messengerserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.answers.ErrorAnswer
import ru.rpuxa.messengerserver.answers.TextErrorAnswer
import java.io.BufferedReader
import java.io.InputStreamReader


abstract class Request(val path: String) {

    fun execute(exchange: HttpExchange): String {
        val map = HashMap<String, String>()

        val streamReader = InputStreamReader(exchange.requestBody, "utf-8")
        val bufferedReader = BufferedReader(streamReader)

        val builder = StringBuilder(exchange.requestURI.query ?: "")
        var first = true
        while (true) {
            val b = bufferedReader.read()
            if (b == -1) break
            if (first) {
                first = false
                builder.append('&')
            }
            builder.append(b.toChar())
        }

        bufferedReader.close()
        streamReader.close()

        println("Request: $builder")

        builder.split('&').forEach {
            if ('=' in it) {
                val (name, value) = it.split('=')
                if (name.isNotBlank())
                    map[name] = value
            }
        }

        var onExecute: RequestAnswer = onExecute(map)
        if (onExecute is Error) {
            val text = onExecute.text
            val code = onExecute.code.toString()
            onExecute = if (text == null) {
                ErrorAnswer(code)
            } else {
                TextErrorAnswer(code, text)
            }
        }
        return gson.toJson(onExecute)
    }

    abstract fun onExecute(query: Map<String, String>): RequestAnswer

    companion object {
        private val gson: Gson = GsonBuilder().create()
    }
}