package ru.rpuxa.messengerserver

import java.io.File
import kotlin.system.exitProcess

private const val DEFAULT_IP = "176.57.217.44"// "62.109.3.22"
private const val DEFAULT_PORT = 1488

var serverIp = DEFAULT_IP
    private set

fun main() {
    println("Server is running4...")
    var server = HttpServer(DEFAULT_IP, DEFAULT_PORT)

    while (true) {
        val line = readLine()!!
        val commands = line.split(' ').filterNot { it.isBlank() }
        val args = if (commands.size == 1) emptyList() else commands.subList(1, commands.size)
        when (commands.first()) {
            "start" -> {
                IconStorage.connect()
                DataBase.connect()
                Thread(server).start()
            }

            "stop" -> {
                DataBase.disconnect()
                server.close()
                println("Server stopped")
            }

            "ip" -> {
                serverIp = args.first()
                server = HttpServer(serverIp, DEFAULT_PORT)
                println("Ip: $serverIp set")
            }

            "cleardb" -> {
                println(if (File(DataBase.PATH).delete() && File(IconStorage.ICONS_PREFIX).deleteRecursively()) "Database cleared!" else "Error while clearing database")
            }

            "exit" -> {
                exitProcess(0)
            }

            else -> println("Unknown command")
        }
    }
}