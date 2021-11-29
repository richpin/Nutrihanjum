package com.example.nutrihanjum.model

import java.io.Serializable

data class FoodDTO(
    var name: String = "",
    var calorie: Float = 0f,
    var carbohydrate: Float = 0f,
    var protein: Float = 0f,
    var fat: Float = 0f,
): Serializable {
    override fun toString(): String {
        return name
    }
}
