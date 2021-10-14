package com.example.nutrihanjum.model

data class UserDTO(
  var userID : String = "",
  var profileUrl : String = "",
  var saved: MutableMap<String, Boolean> = HashMap(),
  var posts: MutableMap<String, Boolean> = HashMap()
){}
