package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer
import ru.rpuxa.messengerserver.answers.ActionsAnswer

object GetActions : TokenRequest("/actions/get") {

    private const val DEFAULT_TIMEOUT = 45_000L

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val lastAction = query["last"]?.toInt() ?: return Error.WRONG_ARGS
        val timeout = query["timeout"]?.toLong() ?: DEFAULT_TIMEOUT
        val id = DataBase.getIdByToken(token) ?: return Error.UNKNOWN_TOKEN

        val result = runBlocking(Dispatchers.IO) {
            withTimeoutOrNull(timeout) {
                DataBase.getNewActions(id, lastAction).let {
                    if (it.isNotEmpty()) return@withTimeoutOrNull it
                }
                val channel = Channel<Unit>()
                try {
                    DataBase.actionChannel[id] = channel
                    channel.receive()
                    DataBase.getNewActions(id, lastAction)
                } finally {
                    channel.cancel()
                    DataBase.actionChannel.remove(id)
                }
            } ?: emptyList()
        }

        return ActionsAnswer(result)
    }
}