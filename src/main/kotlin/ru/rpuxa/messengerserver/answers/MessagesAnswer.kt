package ru.rpuxa.messengerserver.answers

import ru.rpuxa.messengerserver.RequestAnswer

class MessagesAnswer : MutableList<Message> by ArrayList(), RequestAnswer

class Message(val id: Int, val randomUUID: String, val text: String, val sender: Int)