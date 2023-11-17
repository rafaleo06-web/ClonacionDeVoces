package dev.brianchuquiruna.clonaciondevoces

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CreateRetrofit {
    operator fun invoke(): ApiService {
        return GetRetrofit().create(ApiService::class.java)
    }
}