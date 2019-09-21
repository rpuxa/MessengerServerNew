package ru.rpuxa.messengerserver.requests

import com.sun.net.httpserver.HttpExchange
import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.Request
import ru.rpuxa.messengerserver.RequestAnswer

object AnswerOnFriendRequest : TokenRequest("/friends/answer") {

    override fun onExecuteWithToken(token: String, query: Map<String, String>, exchange: HttpExchange): RequestAnswer {
        val friendId = query["id"]?.toInt() ?: return Error.WRONG_ARGS
        val accept = query["accept"]?.toInt() ?: return Error.WRONG_ARGS
        return DataBase.answerOnFriendRequest(token, friendId, accept != 0)
    }
}