package com.example.nutrihanjum.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ActivityPostBinding
import com.example.nutrihanjum.databinding.ActivityPostDetailBinding
import com.example.nutrihanjum.model.PostDTO

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)

        val postDTO = intent.getSerializableExtra("postDTO") as PostDTO

        setLayout(postDTO)
        addViewListener()

        setContentView(binding.root)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setLayout(postDTO: PostDTO){
        binding.postDetailActivityTitleTextview.text = postDTO.title
        binding.postDetailActivityContentTextview.text = postDTO.content.replace("\\n","\n")
    }

    private fun addViewListener() {
        binding.PostDetailActivityBackButton.setOnClickListener { onBackPressed() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}