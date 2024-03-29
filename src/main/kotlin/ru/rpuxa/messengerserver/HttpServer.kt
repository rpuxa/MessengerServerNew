package ru.rpuxa.messengerserver

import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import ru.rpuxa.messengerserver.requests.*
import java.lang.Exception
import java.lang.Runnable


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
                GlobalScope.launch(Dispatchers.IO) {
                    val answer = try {
                        request.execute(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                    if (answer == null) {
                        it.sendResponseHeaders(404, 0)
                    } else {
                        it.sendResponseHeaders(200, answer.size.toLong())
                        val os = it.responseBody
                        os.write(answer)
                        os.close()
                    }
                }
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

        private val ALL_REQUESTS = setOf(
            WelcomeRequest,
            RegisterRequest,
            LoginRequest,
            PrivateInfoRequest,
            PublicInfoRequest,
            SetInfoRequest,
            SetAvatarRequest,
            GetIconRequest,
            GetActions,
            AnswerOnFriendRequest,
            GetFriendsRequests,
            SendFriendRequest,
            GetAllFriendsRequest,
            RemoveFriendRequest,
            SendMessageRequest,
            GetMessagesRequest,
            GetLastActionIdRequest
        )
    }
}