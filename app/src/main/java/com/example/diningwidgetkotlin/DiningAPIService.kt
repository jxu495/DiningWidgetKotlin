package com.example.diningwidgetkotlin

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface DiningAPIService {

    data class Entree(
        //Note that @SerializedName not needed because val names are same as json names
        val name: String,
        val station: String
    )
    //By passing @Header as a parameter, Retrofit allows dynamic headers
    @Headers("accept: application/json")
    @GET("{date}/{dining_common}/{meal}/")
    fun getMenu(@Header("ucsb-api-key") key: String, @Path("date") date: String, @Path("dining_common") diningCommon: String, @Path("meal") meal: String): Call<List<Entree>>

    companion object {
        fun create(): DiningAPIService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.ucsb.edu/dining/menu/v1/")
                .build()
            return retrofit.create(DiningAPIService::class.java)
        }
    }
}