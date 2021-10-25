package com.example.nutrihanjum.util

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

abstract class DelayedTextWatcher(val owner: LifecycleOwner): TextWatcher {
    var job: Job? = null

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(p0: Editable?) = synchronized(this) {
        job?.cancel()

        job = owner.lifecycleScope.launchWhenStarted {
            delay(300)
            delayedAfterChanged(p0)
        }
    }

    abstract fun delayedAfterChanged(editable: Editable?)
}