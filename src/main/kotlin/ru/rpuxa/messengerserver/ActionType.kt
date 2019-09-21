package ru.rpuxa.messengerserver

enum class ActionType(val id: Int) {
    FRIEND_REQUEST_RECEIVED(1),
    FRIEND_REQUEST_ACCEPTED(2),
    MESSAGE(3),
}