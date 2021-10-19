package com.example.nutrihanjum.model

data class UserDTO(
  var name : String = "",
  var userID: String = "",
  var profileUrl : String = "",
  var saved: List<String> = listOf(),
  var posts: List<String> = listOf()
){}
