package ru.rpuxa.messengerserver.requests

import ru.rpuxa.messengerserver.DataBase
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer
import ru.rpuxa.messengerserver.answers.SetInfoAnswer

object SetInfoRequest : TokenRequest("/profile/setInfo") {

    private const val CURRENT_PASSWORD = "current_pass"

    override fun onExecuteWithToken(token: String, query: Map<String, String>): RequestAnswer {
        var currentPass: String? = null
        for ((k, v) in query) {
            if (k == CURRENT_PASSWORD) {
                currentPass = v
                break
            }
        }

       val errors = query
            .filter { it.key != DataBase.TOKEN && it.key != CURRENT_PASSWORD }
            .map { (name, value) ->
                DataBase.setUserField(token, currentPass, name, value)
            }

        val codes = errors.map { it.code }
        val errorTexts = errors.flatMap {
            if (it.text == null) {
                emptyList()
            } else {
                listOf(it.code to it.text)
            }
        }.toMap()

        return SetInfoAnswer(codes, errorTexts)
    }
}