package com.bls.autobooking.network

import com.bls.autobooking.network.api.BlsApiService
import com.bls.autobooking.network.api.EmailJsService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BLS_BASE_URL = "https://algeria.blsspainglobal.com"
    private const val EMAILJS_BASE_URL = "https://api.emailjs.com/api/v1.0/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val blsApiService: BlsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BLS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BlsApiService::class.java)
    }
    
    val emailJsService: EmailJsService by lazy {
        Retrofit.Builder()
            .baseUrl(EMAILJS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailJsService::class.java)
    }
}