package ru.rpuxa.messengerserver

import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import ru.rpuxa.messengerserver.requests.*
import java.lang.Exception


class HttpServer(private val ip: String, private val port: Int) : Runnable, AutoCloseable {


    private var server: HttpServer? = null

    override fun run() {
        println("Starting server at $ip:$port...")
        val server = HttpServer.create(
            InetSocketAddress(
                ip,
                port
            ), 0
        )
        this.server = server
        for (request in ALL_REQUESTS) {
            server.createContext(request.path) {
                val answer = try {
                    request.execute(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    "ERROR: ${e.message}"
                }
                val bytes = answer.toByteArray()
                it.sendResponseHeaders(200, bytes.size.toLong())
                val os = it.responseBody
                os.write(bytes)
                os.close()
            }

        }
        server.executor = null
        server.start()

        println("Server started!")
    }

    override fun close() {
        server?.stop(10)
    }

    companion object {

        private val ALL_REQUESTS = arrayOf(
            WelcomeRequest,
            RegisterRequest,
            LoginRequest,
            PrivateInfoRequest,
            PublicInfoRequest,
            SetInfoRequest
        )
    }
}