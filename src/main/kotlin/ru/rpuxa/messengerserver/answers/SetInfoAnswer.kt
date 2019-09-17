package ru.rpuxa.messengerserver.answers

import com.google.gson.annotations.SerializedName
import ru.rpuxa.messengerserver.Error
import ru.rpuxa.messengerserver.RequestAnswer

class SetInfoAnswer(
    val errors: List<Int>,
    @SerializedName("error_texts")
    val errorsTexts: Map<Int, String>
) : RequestAnswer