package ru.rpuxa.messengerserver.answers

import ru.rpuxa.messengerserver.RequestAnswer

open class PublicProfileInfo(val login: String, val name: String, val surname: String, val birthday: String?) : RequestAnswer

class PrivateProfileInfo(val id: Int, login: String, name: String, surname: String, birthday: String?) :
    PublicProfileInfo(login, name, surname, birthday)