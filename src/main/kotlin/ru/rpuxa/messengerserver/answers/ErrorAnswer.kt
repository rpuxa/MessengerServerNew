package ru.rpuxa.messengerserver.answers

import ru.rpuxa.messengerserver.RequestAnswer

open class ErrorAnswer(private val error: String) : RequestAnswer