package com.example.nutrihanjum.model

import java.io.Serializable

data class ContentDTO(
    var id: String = "",
    var content: String = "",
    var imageUrl: String = "",
    var foods: MutableList<FoodDTO> = mutableListOf(),
    var nutritionInfo: NutritionInfo = NutritionInfo(),
    var uid: String = "",
    var profileName: String = "",
    var profileUrl: String = "",
    var timestamp: Long = 0,
    var date: String = "",
    var likes: MutableList<String> = mutableListOf(),
    var commentCount: Int = 0,
    var saved: MutableList<String> = mutableListOf(),
    var mealTime: String = "",
    var isPublic: Boolean = true,
) : Serializable {
    data class CommentDTO(
        var id: String = "",
        var uid: String = "",
        var comment: String = "",
        var timeStamp: Long = 0
    )
}


data class NutritionInfo(
    var calorie: Float = 0f,
    var carbohydrate: Float = 0f,
    var protein: Float = 0f,
    var fat: Float = 0f,
): Serializable