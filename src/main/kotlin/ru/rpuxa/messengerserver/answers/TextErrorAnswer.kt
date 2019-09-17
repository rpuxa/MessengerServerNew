package ru.rpuxa.messengerserver.answers

import com.google.gson.annotations.SerializedName

class TextErrorAnswer(
    code: String,
    @SerializedName("error_text")
    private val text: String
) : ErrorAnswer(code)