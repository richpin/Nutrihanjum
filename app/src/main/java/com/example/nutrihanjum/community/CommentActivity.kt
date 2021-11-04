package com.example.nutrihanjum.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.databinding.ActivityCommentBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.UserRepository.isSigned
import com.example.nutrihanjum.repository.UserRepository.uid
import com.example.nutrihanjum.repository.UserRepository.userName
import com.example.nutrihanjum.repository.UserRepository.userPhoto

class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding

    private lateinit var communityViewModel: CommunityViewModel

    private val recyclerViewAdapter = CommentRecyclerViewAdapter()

    private lateinit var contentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        communityViewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)

        contentId = intent.getStringExtra("contentId")!!
        communityViewModel.loadComments(contentId!!)

        binding.commentActivityRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.commentActivityRecyclerview.setHasFixedSize(true)

        recyclerViewAdapter.deleteCommentEvent =
            { it -> communityViewModel.deleteComment(contentId, it) }
        binding.commentActivityRecyclerview.adapter = recyclerViewAdapter

        addLiveDataObserver()
        addViewListener()

        Glide.with(this).load(userPhoto).circleCrop()
            .into(binding.commentActivityProfileImageview)


        setContentView(binding.root)
    }

    private fun addLiveDataObserver() {
        communityViewModel.comments.observe(this, {
            recyclerViewAdapter.updateComments(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })
    }

    private fun addViewListener() {
        binding.commentActivityBackButton.setOnClickListener { onBackPressed() }

        binding.commentActivityCommentButton.setOnClickListener {
            val content = binding.commentActivityCommentEdittext.text.toString()

            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, R.string.comment_empty, Toast.LENGTH_SHORT).show()
            } else if (!isSigned()) {

            } else {
                val newComment = ContentDTO.CommentDTO()

                newComment.timeStamp = System.currentTimeMillis()
                newComment.id = uid + newComment.timeStamp
                newComment.uid = uid!!
                newComment.profileUrl = userPhoto.toString()
                newComment.profileName = userName.toString()
                newComment.comment = content

                communityViewModel.addComment(contentId, newComment)
                binding.commentActivityCommentEdittext.setText("")
                hideKeyboard()
                recyclerViewAdapter.commentDTOs.add(newComment)
                recyclerViewAdapter.notifyItemInserted(recyclerViewAdapter.commentDTOs.size - 1)
                recyclerViewAdapter.countChange += 1
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("countChange", recyclerViewAdapter.countChange)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }
    }
}