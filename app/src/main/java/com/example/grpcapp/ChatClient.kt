package com.example.grpcapp.com.example.grpcapp

import com.example.grpcapp.proto.ChatServiceGrpc
import com.example.grpcapp.proto.ChatProto.*
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
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
        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()
        asyncStub = ChatServiceGrpc.newStub(channel)
        blockingStub = ChatServiceGrpc.newBlockingStub(channel)
    }

    fun sendMessage(username: String, message: String): Boolean {
        return try {
            val chatMessage = ChatMessage.newBuilder()
                .setUsername(username)
                .setMessage(message)
                .build()

            val response = blockingStub?.sendMessage(chatMessage)
            response?.success ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun joinChat(username: String): Flow<ChatMessage> = flow {
        val userInfo = UserInfo.newBuilder()
            .setUsername(username)
            .build()

        val responseChannel = Channel<ChatMessage>(Channel.UNLIMITED)

        asyncStub?.joinChat(userInfo, object : StreamObserver<ChatMessage> {
            override fun onNext(message: ChatMessage) {
                responseChannel.trySend(message)
            }

            override fun onError(throwable: Throwable) {
                responseChannel.close(throwable)
            }

            override fun onCompleted() {
                responseChannel.close()
            }
        })

        try {
            for (message in responseChannel) {
                emit(message)
            }
        } finally {
            responseChannel.close()
        }
    }

    fun disconnect() {
        channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
    }
}