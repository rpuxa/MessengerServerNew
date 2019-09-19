package ru.rpuxa.messengerserver.answers

import com.google.gson.annotations.SerializedName
import ru.rpuxa.messengerserver.RequestAnswer

open class ErrorAnswer(
    val error: String,
    @SerializedName("error_text")
    val text: String?
) : RequestAnswer