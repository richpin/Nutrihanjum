package com.example.nutrihanjum.model

import java.io.Serializable

data class FoodDTO(
    var name: String = "",
    var calorie: String = "",
    var carbohydrate: String = "",
    var protein: String = "",
    var fat: String = "",
): Serializable {
    override fun toString(): String {
        return name
    }
}
