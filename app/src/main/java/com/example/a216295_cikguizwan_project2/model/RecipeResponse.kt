package com.example.a216295_cikguizwan_project2.model

data class RecipeResult(
    val id: Int,
    val title: String,
    val image: String,
    val readyInMinutes: Int,
    val servings: Int,
    val sourceUrl: String
)

data class RecipeSearchResponse(
    val results: List<RecipeResult>,
    val totalResults: Int
)