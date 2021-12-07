package com.example.nutrihanjum.util

import android.util.Patterns
import java.util.regex.Pattern

object NHPatternUtil {
    val USER_NAME = Pattern.compile("^[가-힣a-zA-Z0-9_]{2,15}$")
    val EMAIL = Patterns.EMAIL_ADDRESS
    val PASSWORD = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*;])[a-zA-Z0-9!@#$%^&*;]{8,16}$")

    val HASHTAG_NOT_ALLOWED = Regex("[^a-zA-Zㄱ-ㅎ가-힣ㅏ-ㅣ0-9]")
    val HASHTAG_WHITESPACE = Regex("[ #\n]")
}