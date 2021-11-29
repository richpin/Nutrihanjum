package com.example.nutrihanjum.util

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

open class DelayedTextWatcher(val owner: LifecycleOwner): TextWatcher {
    var job: Job? = null
    var job2: Job? = null

    final override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    final override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        job2?.cancel()
        job2 = owner.lifecycleScope.launchWhenStarted {
            delay(300)
            delayedOnTextChanged(p0)
        }
    }

    final override fun afterTextChanged(p0: Editable?) {
        job?.cancel()

        job = owner.lifecycleScope.launchWhenStarted {
            delay(300)
            delayedAfterChanged(p0)
        }
    }

    open fun delayedAfterChanged(editable: Editable?) {}
    open fun delayedOnTextChanged(text: CharSequence?) {}
}