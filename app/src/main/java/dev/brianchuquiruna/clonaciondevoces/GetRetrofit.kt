package dev.brianchuquiruna.clonaciondevoces

import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object GetRetrofit {
    operator fun invoke(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl("https://api.elevenlabs.io/")
            .client(OkHttpClient.Builder().readTimeout(120,TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}