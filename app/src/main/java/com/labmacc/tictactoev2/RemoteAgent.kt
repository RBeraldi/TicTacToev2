package com.labmacc.tictactoev2


import org.json.JSONArray
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

interface PostChess {

    @GET("/")
    suspend fun doGet(@Query("chessboard") chess: JSONArray) : Response<Int>
}

class RemoteAgent {

    lateinit var retrofit : Retrofit

    init {
        val baseUrl = "https://robertoberaldi.pythonanywhere.com/"
        //val baseUrl = "http://192.168.1.248:8080/"
        //val baseUrl = "http://172.20.10.5:8080/"

        try {
            retrofit =
                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON converter to Kotlin object
                    .build()
        }
        catch (e: IOException){

        }
    }
}
