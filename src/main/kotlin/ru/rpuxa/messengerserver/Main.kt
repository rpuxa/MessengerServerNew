package ru.rpuxa.messengerserver

import java.io.File
import kotlin.system.exitProcess

const val DEFAULT_IP = "176.57.217.44"
const val DEFAULT_PORT = 80

fun main(__u: Array<String>) {
    println("Server running3...")
    var server = HttpServer(DEFAULT_IP, DEFAULT_PORT)

    while (true) {
        val line = readLine()!!
        val commands = line.split(' ').filterNot { it.isBlank() }
        val args = if (commands.size == 1) emptyList() else commands.subList(1, commands.size)
        when (commands.first()) {
            "start" -> {
                DataBase.connect()
                Thread(server).start()
            }

            "stop" -> {
                DataBase.disconnect()
                server.close()
                println("Server stopped")
            }

            "ip" -> {
                val ip = args.first()
                server = HttpServer(ip, DEFAULT_PORT)
                println("Ip: $ip set")
            }

            "cleardb" -> {
                println(if (File(DataBase.PATH).delete()) "Database cleared!" else "Error while clearing database")

            }

            "exit" -> {
                exitProcess(0)
            }

            else -> println("Unknown command")
        }
    }
}