package com.example.nutrihanjum.user

import android.util.Patterns
import java.util.regex.Pattern

object UserDataPattern {
    val USER_NAME = Pattern.compile("^[가-힣a-zA-Z0-9_]{2,15}$")
    val EMAIL = Patterns.EMAIL_ADDRESS
    val PASSWORD = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*;])[a-zA-Z0-9!@#$%^&*;]{8,16}$")
}