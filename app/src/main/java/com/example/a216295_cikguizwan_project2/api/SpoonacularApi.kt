package com.example.a216295_cikguizwan_project2.api

import com.example.a216295_cikguizwan_project2.model.RecipeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("number") number: Int = 10,
        @Query("apiKey") apiKey: String = "YOUR_API_KEY_HERE"
    ): RecipeSearchResponse
}