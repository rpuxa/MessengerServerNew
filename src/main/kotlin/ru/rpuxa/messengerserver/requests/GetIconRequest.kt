package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.IconStorage
import ru.rpuxa.messengerserver.Request

object GetIconRequest : Request("/${IconStorage.ICONS_PREFIX}") {

    override fun execute(exchange: HttpExchange): ByteArray? {
        val icon = exchange.requestURI.toString().substring(1)
        return IconStorage.load(icon)
    }
}