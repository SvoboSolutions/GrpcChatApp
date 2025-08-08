package com.example.grpcapp.com.example.grpcapp

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import com.example.grpcapp.proto.GreetingServiceGrpc
import com.example.grpcapp.proto.GreetingProto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class GrpcClient {

    private var channel: ManagedChannel? = null
    private var stub: GreetingServiceGrpc.GreetingServiceBlockingStub? = null

    fun connect(host: String, port: Int) {
        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()
        stub = GreetingServiceGrpc.newBlockingStub(channel)
    }

    suspend fun sayHello(name: String): String = withContext(Dispatchers.IO) {
        try {
            val request = HelloRequest.newBuilder()
                .setName(name)
                .build()

            val response = stub?.sayHello(request)
            response?.message ?: "Keine Antwort erhalten"
        } catch (e: Exception) {
            "Fehler: ${e.message}"
        }
    }

    fun disconnect() {
        channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
    }
}