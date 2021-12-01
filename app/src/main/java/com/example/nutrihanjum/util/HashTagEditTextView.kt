package com.example.nutrihanjum.util

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.example.nutrihanjum.R
import java.lang.Exception
import java.util.ArrayList
import java.util.regex.Pattern

class HashTagEditTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null) : AppCompatEditText(context, attrs) {

    private val TAG = javaClass.simpleName
    private var hashTagMaxLength = 0
    private var hashTagMaxCount = 0
    private var mAutoPoundSign = false
    private var hashTagMaxLengthAlert: String? = null
    private var hashTagMaxCountAlert : String? = null
    private var hashTagNotAllowedCharAlert: String? = null


    private fun initView() {
        if (hashTagMaxLengthAlert.isNullOrEmpty()) {
            hashTagMaxLengthAlert = "태그 최대길이 초과"
        }
        if (hashTagMaxCountAlert.isNullOrEmpty()) {
            hashTagMaxCountAlert = "태그 최대개수 초과"
        }
        if (hashTagNotAllowedCharAlert.isNullOrEmpty()) {
            hashTagNotAllowedCharAlert = "허용되지 않은 입력"
        }
    }


    private var isUserInput = true

    var hashTagList: List<String>
        get() {
            return text?.split(" ", "#", "\n")
                ?.map { it.trim().take(10).replace(NHPatternUtil.HASHTAG, "") }
                ?.filter { it.isNotEmpty() }
                ?.take(10)
                ?: listOf()
        }
        set(hashTag) {
            var hashTagText = ""

            hashTag.forEach {
                hashTagText += "#$it "
            }

            isUserInput = false
            setText(hashTagText)
            isUserInput = true
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.HashTagEditTextView,0,0).apply {
            try {
                hashTagMaxLength = getInt(R.styleable.HashTagEditTextView_itemMaxLength, 5)
                hashTagMaxCount = getInt(R.styleable.HashTagEditTextView_itemMaxCount, 5)
                mAutoPoundSign = getBoolean(R.styleable.HashTagEditTextView_autoPoundSign, true)
                hashTagMaxLengthAlert = getString(R.styleable.HashTagEditTextView_hashTagMaxLengthAlert)
                hashTagMaxCountAlert = getString(R.styleable.HashTagEditTextView_hasTagMaxCountAlert)
                hashTagNotAllowedCharAlert = getString(R.styleable.HashTagEditTextView_hashTagNotAllowedCharAlert)
            }
            finally {
                recycle()
                initView()

                addTextChangedListener(object: TextWatcher {
                    var formatFlag = false

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                        formatFlag = count != 0 && isUserInput
                                && p0?.slice(start until start+count)?.contains(NHPatternUtil.HASHTAG_WHITESPACE) == true
                    }

                    override fun afterTextChanged(editable: Editable?) {
                        if (formatFlag && hasFocus()) {
                            var hashTagText = ""

                            hashTagList.forEach {
                                hashTagText += "#$it "
                            }

                            isUserInput = false
                            editable?.replace(0, editable.trimEnd().length, hashTagText.trim())
                            isUserInput = true
                        }
                    }

                })
            }
        }
    }

}