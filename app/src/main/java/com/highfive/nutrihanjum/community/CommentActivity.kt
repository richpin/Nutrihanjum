package com.example.nutrihanjum.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.databinding.ActivityCommentBinding
import com.example.nutrihanjum.model.ContentDTO
import com.example.nutrihanjum.repository.UserRepository.isSigned
import com.example.nutrihanjum.repository.UserRepository.uid
import com.example.nutrihanjum.repository.UserRepository.userName
import com.example.nutrihanjum.repository.UserRepository.userPhoto
import com.example.nutrihanjum.util.SwipeController
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.nutrihanjum.MainActivity
import com.example.nutrihanjum.UserViewModel
import com.example.nutrihanjum.util.MyItemDecoration


class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding

    private lateinit var communityViewModel: CommunityViewModel
    private lateinit var userViewModel: UserViewModel

    private val recyclerViewAdapter = CommentRecyclerViewAdapter()
    private var swipeController = SwipeController()

    private lateinit var contentDTO: ContentDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        communityViewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        contentDTO = intent.getSerializableExtra("contentDTO") as ContentDTO

        addLiveDataObserver()
        addViewListener()
        addRecyclerView()

        Glide.with(this).load(userPhoto).circleCrop()
            .into(binding.commentActivityProfileImageview)

        communityViewModel.loadComments(contentDTO.id)

        setContentView(binding.root)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun addRecyclerView() {
        swipeController = SwipeController().apply { setClamp(150f) }
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.commentActivityRecyclerview)
        recyclerViewAdapter.initDialog(this)
        recyclerViewAdapter.contentId = contentDTO.id

        if (isSigned()) {
            recyclerViewAdapter.deleteCommentEvent = { first, second ->
                communityViewModel.deleteComment(first, second)
            }
        } else {
            recyclerViewAdapter.deleteCommentEvent = null
        }

        binding.commentActivityRecyclerview.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            setHasFixedSize(true)
            adapter = recyclerViewAdapter
            addItemDecoration(MyItemDecoration())

            setOnTouchListener { _, _ ->
                swipeController.removePreviousClamp(this)
                false
            }
        }
    }

    private fun addLiveDataObserver() {
        communityViewModel.comments.observe(this, {
            recyclerViewAdapter.updateComments(it)

            val usersUid = arrayListOf<String>()
            it.forEach { comment ->
                if (recyclerViewAdapter.isUserEmpty(comment.uid)) usersUid.add(comment.uid)
            }

            communityViewModel.loadUserInfo(usersUid)
        })
        communityViewModel.users.observe(this, {
            it.forEach { user ->
                recyclerViewAdapter.users[user.first] = user.second
            }

            recyclerViewAdapter.notifyDataSetChanged()
        })
    }

    private fun addViewListener() {
        binding.commentActivityBackButton.setOnClickListener { onBackPressed() }

        binding.commentActivityCommentButton.setOnClickListener {
            val content = binding.commentActivityCommentEdittext.text.toString()

            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, R.string.comment_empty, Toast.LENGTH_SHORT).show()}
            else {
                recyclerViewAdapter.users[uid!!] = Pair(userName!!, userPhoto.toString())

                val newComment = ContentDTO.CommentDTO()

                newComment.timeStamp = System.currentTimeMillis()
                newComment.id = uid + newComment.timeStamp
                newComment.uid = uid!!
                newComment.comment = content

                communityViewModel.addComment(contentDTO, newComment)
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


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}