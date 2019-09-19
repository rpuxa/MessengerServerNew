package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.answers.WelcomeAnswer

object WelcomeRequest : Request("/welcome") {
    override fun requestAnswer(
        query: Map<String, String>,
        exchange: HttpExchange
    ) = WelcomeAnswer()
}
