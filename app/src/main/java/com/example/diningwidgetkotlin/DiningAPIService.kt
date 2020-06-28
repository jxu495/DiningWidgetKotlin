package com.example.diningwidgetkotlin

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface DiningAPIService {

    data class Entree(
        //Note that @SerializedName not needed because val names are same as json names
        val name: String,
        val station: String
    )

    @GET("{date}/{dining_common}/{meal}/")
    fun getMenu(): Call<List<Entree>>

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