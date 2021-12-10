package com.example.nutrihanjum.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import com.example.nutrihanjum.databinding.ActivityNewsDetailBinding

class NewsDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewsDetailBinding.inflate(layoutInflater)

        binding.newsDetailActivityBackButton.setOnClickListener { onBackPressed() }

        with(binding.webview){
            settings.javaScriptEnabled = true
            webChromeClient = WebChromeClient()
            webViewClient = object: WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.layoutLoading.visibility = View.GONE
                }
            }
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        intent?.let {
            val url = intent.getStringExtra("webUrl")
            if(!url!!.isEmpty()) binding.webview.loadUrl(url)
        }

        setContentView(binding.root)
    }
}