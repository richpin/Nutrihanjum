package com.example.nutrihanjum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.chatbot.ChatBotFragment
import com.example.nutrihanjum.community.CommunityFragment
import com.example.nutrihanjum.databinding.ActivityMainBinding
import com.example.nutrihanjum.diary.DiaryFragment
import com.example.nutrihanjum.news.NewsFragment
import com.example.nutrihanjum.user.UserFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var curFragment : Fragment

    private lateinit var userViewModel : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Nutrihanjum)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        initFragments()
        setBottomNavItemListener()

        if (savedInstanceState?.containsKey("curFragment") == true) {
            val id = savedInstanceState.getInt("curFragment")

            curFragment = getFragmentFromResId(id)
            binding.bottomNavigation.selectedItemId = id
        } else {
            curFragment = CommunityFragment.getInstance()
            binding.bottomNavigation.selectedItemId = R.id.action_home
        }

        if (userViewModel.isSigned()) {
            userViewModel.notifyUserSigned()
        } else {
            userViewModel.notifyUserSignedOut()
        }
    }


    private fun setBottomNavItemListener() {
        binding.bottomNavigation.setOnItemSelectedListener {
            val transaction = supportFragmentManager.beginTransaction()
                .hide(curFragment)

            when (it.itemId) {
                R.id.action_home -> {
                    transaction.show(CommunityFragment.getInstance()).commit()
                    curFragment = CommunityFragment.getInstance()
                    binding.topBarTextview.text= getString(R.string.home_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_chatbot -> {
                    transaction.show(ChatBotFragment.getInstance()).commit()
                    curFragment = ChatBotFragment.getInstance()
                    binding.topBarTextview.text= getString(R.string.chatbot_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_diary -> {
                    transaction.show(DiaryFragment.getInstance()).commit()
                    curFragment = DiaryFragment.getInstance()
                    binding.topBarTextview.text= getString(R.string.diary_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_news -> {
                    transaction.show(NewsFragment.getInstance()).commit()
                    curFragment = NewsFragment.getInstance()
                    binding.topBarTextview.text= getString(R.string.news_category)
                    return@setOnItemSelectedListener true
                }
                R.id.action_user -> {
                    transaction.show(UserFragment.getInstance()).commit()
                    curFragment = UserFragment.getInstance()
                    binding.topBarTextview.text= getString(R.string.user_category)
                    return@setOnItemSelectedListener true
                }
            }

            false
        }
    }


    private fun initFragments() {
        val transaction = supportFragmentManager.beginTransaction()

        supportFragmentManager.fragments.forEach {
            transaction.remove(it)
        }

        transaction.commitNow()

        supportFragmentManager.beginTransaction()
            .add(R.id.main_content, ChatBotFragment.getInstance()).hide(ChatBotFragment.getInstance())
            .add(R.id.main_content, CommunityFragment.getInstance()).hide(CommunityFragment.getInstance())
            .add(R.id.main_content, DiaryFragment.getInstance()).hide(DiaryFragment.getInstance())
            .add(R.id.main_content, NewsFragment.getInstance()).hide(NewsFragment.getInstance())
            .add(R.id.main_content, UserFragment.getInstance()).hide(UserFragment.getInstance())
            .commitNow()
    }

    private fun getFragmentFromResId(id: Int) = when(id) {
        R.id.action_home -> CommunityFragment.getInstance()
        R.id.action_diary -> DiaryFragment.getInstance()
        R.id.action_chatbot -> ChatBotFragment.getInstance()
        R.id.action_news -> NewsFragment.getInstance()
        R.id.action_user -> UserFragment.getInstance()
        else -> CommunityFragment.getInstance()
    }

    private fun getResIdFromFragment(fragment: Fragment) = when (fragment) {
        is CommunityFragment -> R.id.action_home
        is DiaryFragment -> R.id.action_diary
        is ChatBotFragment -> R.id.action_chatbot
        is NewsFragment -> R.id.action_news
        is UserFragment -> R.id.action_user
        else -> R.id.action_home
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("curFragment", getResIdFromFragment(curFragment))
    }
}