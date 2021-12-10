package com.example.nutrihanjum.util

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.example.nutrihanjum.R

class HashTagEditTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null) : AppCompatEditText(context, attrs) {

    private val TAG = javaClass.simpleName
    private var hashTagMaxLength = 0
    private var hashTagMaxCount = 0
    private var hashTagPrefix = "#"
    private var hashTagMaxLengthWarning: String? = null
    private var hashTagMaxCountWarning : String? = null
    private var hashTagNotAllowedCharWarning: String? = null


    private fun initView() {
        if (hashTagMaxLengthWarning.isNullOrEmpty()) {
            hashTagMaxLengthWarning = "태그 최대길이 초과"
        }
        if (hashTagMaxCountWarning.isNullOrEmpty()) {
            hashTagMaxCountWarning = "태그 최대개수 초과"
        }
        if (hashTagNotAllowedCharWarning.isNullOrEmpty()) {
            hashTagNotAllowedCharWarning = "허용되지 않은 입력"
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {

        if (focused && text.isNullOrEmpty()) {
            isUserInput = false
            text?.insert(0, hashTagPrefix)
            isUserInput = true
        }
        else if (!focused && text.contentEquals(hashTagPrefix)) {
            isUserInput = false
            text?.clear()
            isUserInput = true
        }

        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }


    private var isUserInput = true
    private var isInsideCall = false

    var hashTagList: List<String>
        get() {
            val result = text?.split(" ", hashTagPrefix, "\n")
                ?.map {
                    var sanitized = it.trim()

                    if (sanitized.length > hashTagMaxLength && isInsideCall) {
                        Toast.makeText(context, hashTagMaxLengthWarning, Toast.LENGTH_SHORT).show()
                    }

                    sanitized = sanitized.take(hashTagMaxLength)

                    if (sanitized.contains(NHPatternUtil.HASHTAG_NOT_ALLOWED) && isInsideCall) {
                        Toast.makeText(context, hashTagNotAllowedCharWarning, Toast.LENGTH_SHORT).show()
                    }

                    sanitized.replace(NHPatternUtil.HASHTAG_NOT_ALLOWED, "")
                }
                ?.filter { it.isNotEmpty() } ?: listOf()

            if (result.size > hashTagMaxCount && isInsideCall) {
                Toast.makeText(context, hashTagMaxCountWarning, Toast.LENGTH_SHORT).show()

            }

            return result.take(hashTagMaxCount)
        }
        set(hashTag) {
            var hashTagText = ""

            hashTag.forEach {
                hashTagText += "$hashTagPrefix$it "
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
                hashTagPrefix = getString(R.styleable.HashTagEditTextView_autoPoundSign) ?: "#"
                hashTagMaxLengthWarning = getString(R.styleable.HashTagEditTextView_hashTagMaxLengthWarning)
                hashTagMaxCountWarning = getString(R.styleable.HashTagEditTextView_hasTagMaxCountWarning)
                hashTagNotAllowedCharWarning = getString(R.styleable.HashTagEditTextView_hashTagNotAllowedCharWarning)
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

                            isInsideCall = true
                            hashTagList.forEach {
                                hashTagText += "$hashTagPrefix$it "
                            }
                            isInsideCall = false

                            isUserInput = false

                            editable?.replace(0, editable.trimEnd().length, hashTagText.trim())
                            isUserInput = true
                        }
                        else if (editable.isNullOrEmpty() && isUserInput && hasFocus()) {
                            isUserInput = false
                            append(hashTagPrefix)
                            isUserInput = true
                        }
                    }
                })
            }
        }
    }

}