package ru.rpuxa.messengerserver

enum class Error(val code: Int, val text: String? = null) : RequestAnswer {
    NO_ERROR(0),
    WRONG_ARGS(1),
    UNKNOWN_TOKEN(2),
    UNKNOWN_ID(3),

    // Registration
    LOGIN_ALREADY_EXISTS(100, "Данный логин занят"),
    WRONG_LOGIN_OR_PASSWORD(101, "Неверный логин или пароль"),
    LOGIN_TOO_SHORT(102, "Логин должен содержать как минимум 4 символа"),
    LOGIN_TOO_LONG(103, "Логин должен содержать максимум 16 символов"),
    LOGIN_CONTAINS_WRONG_SYMBOLS(104, "Логин должен состоять из следующих символов: (A-Za-z1-9_-)"),
    PASSWORD_TOO_SHORT(105, "Пароль должен содержать минимум 4 символа"),
    NAME_WRONG_LENGTH(106, "Имя должно содержать от 1 до 32 символов"),
    NAME_CONTAINS_WRONG_SYMBOLS(107, "Имя должно состоять из букв латинского или кириллического алфавитов"),
    SURNAME_WRONG_LENGTH(108, "Фамилия должна содержать от 1 до 32 символов"),
    SURNAME_CONTAINS_WRONG_SYMBOLS(109, "Фамилия должна состоять из букв латинского или кириллического алфавитов"),


    // Profile
    UNKNOWN_USER_FIELD(200),
    CURRENT_PASSWORD_NEEDED(201),
    CURRENT_PASSWORD_WRONG(202, "Текущий пароль введен неверно"),


    // Icons
    ICON_NOT_FOUND(300),

    // Friends
    ACCOUNT_IS_NOT_FOUND(400),
    ALREADY_IN_FRIENDS(401),
    ALREADY_SENT_REQUEST(402),
    REQUEST_NOT_FOUND(403),
    CANT_SEND_REQUEST_TO_YOURSELF(404),
}