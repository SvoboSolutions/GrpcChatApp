package com.example.grpcapp

import com.example.grpc.chat.ChatServiceGrpc
import com.example.grpc.chat.*
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class ChatClient {
    private var channel: ManagedChannel? = null
    private var asyncStub: ChatServiceGrpc.ChatServiceStub? = null
    private var blockingStub: ChatServiceGrpc.ChatServiceBlockingStub? = null

    fun connect(host: String, port: Int) {
        println("🔄 Verbinde zu $host:$port...")

        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .maxInboundMessageSize(1024 * 1024) // 1MB
            .build()

        asyncStub = ChatServiceGrpc.newStub(channel)

        // Kein Deadline für blockingStub!
        blockingStub = ChatServiceGrpc.newBlockingStub(channel)

        println("✅ Channel erstellt")
    }

    fun sendMessage(username: String, message: String): Boolean {
        return try {
            println("📤 Sende Nachricht: '$message' von $username")

            val chatMessage = ChatMessage.newBuilder()
                .setUsername(username)
                .setMessage(message)
                .build()

            // Mit individuellem Timeout pro Call
            val response = blockingStub
                ?.withDeadlineAfter(30, TimeUnit.SECONDS) // 30 Sekunden nur für diesen Call
                ?.sendMessage(chatMessage)

            val success = response?.success ?: false
            println("📤 Antwort: success=$success")
            success

        } catch (e: StatusRuntimeException) {
            println("❌ gRPC Fehler: ${e.status} - ${e.message}")
            false
        } catch (e: Exception) {
            println("❌ Unbekannter Fehler: ${e.message}")
            false
        }
    }

    fun joinChat(username: String): Flow<ChatMessage> = flow {
        println("🔄 Trete Chat bei als: $username")

        val userInfo = UserInfo.newBuilder()
            .setUsername(username)
            .build()

        val responseChannel = Channel<ChatMessage>(Channel.UNLIMITED)

        try {
            asyncStub?.joinChat(userInfo, object : StreamObserver<ChatMessage> {
                override fun onNext(message: ChatMessage) {
                    println("📥 Stream: ${message.username}: ${message.message}")
                    responseChannel.trySend(message)
                }

                override fun onError(throwable: Throwable) {
                    println("❌ Stream Fehler: $throwable")
                    responseChannel.close(throwable)
                }

                override fun onCompleted() {
                    println("✅ Stream beendet")
                    responseChannel.close()
                }
            })

            for (message in responseChannel) {
                emit(message)
            }

        } catch (e: Exception) {
            println("❌ JoinChat Fehler: ${e.message}")
            throw e
        } finally {
            responseChannel.close()
        }
    }

    fun disconnect() {
        try {
            channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
            println("✅ Verbindung geschlossen")
        } catch (e: Exception) {
            println("❌ Fehler beim Schließen: ${e.message}")
        }
    }
}