package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer
import ru.rpuxa.messengerserver.answers.WelcomeAnswer

object WelcomeRequest : Request("/welcome") {
    override fun onExecute(query: Map<String, String>) = WelcomeAnswer()
}
