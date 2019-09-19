package ru.rpuxa.messengerserver

import java.io.*
import java.io.FileInputStream
import kotlin.random.Random
import kotlin.random.nextUInt


object IconStorage {

    private lateinit var folder: File

    private const val PROTOCOL = "http"

    const val ICONS_PREFIX = "icons"
    private const val AVATAR_PREFIX = "ava"

    fun connect() {
        folder = File(ICONS_PREFIX)
        if (!folder.exists())
            folder.mkdir()
    }

    fun saveAvatar(stream: InputStream, size: Int, id: Int): String {
        val randomNumber = Random.nextUInt()
        val fileName = "$ICONS_PREFIX/$AVATAR_PREFIX${id}_$randomNumber.png"

        save(stream, size, fileName)

        return "$PROTOCOL://$serverIp/$fileName"
    }

    private fun save(stream: InputStream, size: Int, name: String) {
        FileOutputStream(name).use { fos ->
            repeat(size) {
                fos.write(stream.read())
            }
        }
    }

    fun load(name: String): ByteArray? {
        val file = File(name)
        if (!file.exists()) return null
        val data = ByteArray(file.length().toInt())
        val dis = DataInputStream(FileInputStream(file))
        dis.readFully(data)
        dis.close()
        return data
    }
}