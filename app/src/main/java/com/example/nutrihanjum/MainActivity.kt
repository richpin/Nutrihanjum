package com.example.nutrihanjum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.nutrihanjum.databinding.ActivityMainBinding
import com.example.nutrihanjum.fragment.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var curFragment : Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Nutrihanjum)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        curFragment = CommunityFragment.getInstance()
        addFragments()
        binding.bottomNavigation.selectedItemId = R.id.action_home
    }

    private fun addFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.main_content, ChatbotFragment.getInstance()).hide(ChatbotFragment.getInstance())
            .add(R.id.main_content, CommunityFragment.getInstance()).hide(CommunityFragment.getInstance())
            .add(R.id.main_content, DiaryFragment.getInstance()).hide(DiaryFragment.getInstance())
            .add(R.id.main_content, NewsFragment.getInstance()).hide(NewsFragment.getInstance())
            .add(R.id.main_content, UserFragment.getInstance()).hide(UserFragment.getInstance())
            .commitNow()

        binding.bottomNavigation.setOnItemSelectedListener {
            val transaction = supportFragmentManager.beginTransaction()
                .hide(curFragment)

            when (it.itemId) {
                R.id.action_home -> {
                    transaction.show(CommunityFragment.getInstance()).commit()
                    curFragment = CommunityFragment.getInstance()
                    binding.topAppBar.title= getString(R.string.home_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_chatbot -> {
                    transaction.show(ChatbotFragment.getInstance()).commit()
                    curFragment = ChatbotFragment.getInstance()
                    binding.topAppBar.title= getString(R.string.chatbot_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_diary -> {
                    transaction.show(DiaryFragment.getInstance()).commit()
                    curFragment = DiaryFragment.getInstance()
                    binding.topAppBar.title= getString(R.string.diary_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_news -> {
                    transaction.show(NewsFragment.getInstance()).commit()
                    curFragment = NewsFragment.getInstance()
                    binding.topAppBar.title= getString(R.string.news_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_user -> {
                    transaction.show(UserFragment.getInstance()).commit()
                    curFragment = UserFragment.getInstance()
                    binding.topAppBar.title= getString(R.string.user_category)
                    return@setOnItemSelectedListener true
                }
            }

            false
        }
    }
}