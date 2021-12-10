package com.example.nutrihanjum

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.nutrihanjum.chatbot.ChatBotFragment
import com.example.nutrihanjum.community.CommunityFragment
import com.example.nutrihanjum.community.NoticeActivity
import com.example.nutrihanjum.databinding.ActivityMainBinding
import com.example.nutrihanjum.diary.DiaryFragment
import com.example.nutrihanjum.news.NewsFragment
import com.example.nutrihanjum.repository.UserRepository
import com.example.nutrihanjum.user.SettingActivity
import com.example.nutrihanjum.user.UserFragment
import com.example.nutrihanjum.util.NHFirebaseMessagingService
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var curFragment: Fragment

    private lateinit var userViewModel: UserViewModel

    lateinit var settingLauncher: ActivityResultLauncher<Intent>

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        initFragments()
        setBottomNavItemListener()

        if (savedInstanceState?.containsKey("curFragment") == true) {
            val tag = savedInstanceState.getString("curFragment")

            curFragment = supportFragmentManager.findFragmentByTag(tag)!!
            binding.bottomNavigation.selectedItemId = fragmentTagToResId(tag!!)
        } else {
            curFragment = CommunityFragment.getInstance()
            binding.bottomNavigation.selectedItemId = R.id.action_home
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result

                Log.wtf("Token", token)

                UserRepository.updateToken(token)
            }
        }
    }

    private fun setBottomNavItemListener() {
        binding.bottomNavigation.setOnItemSelectedListener {
            val transaction = supportFragmentManager.beginTransaction()
                .hide(curFragment)

            when (it.itemId) {
                R.id.action_home -> {
                    val fragment = supportFragmentManager.findFragmentByTag("Community")!!

                    transaction.show(fragment).commit()
                    curFragment = fragment
                    binding.topBarTextview.text = getString(R.string.home_category)
                    binding.topBarActionImageview.setImageResource(R.drawable.ic_notification)
                    binding.topBarActionLayout.setOnClickListener {
                        startActivity(Intent(this, NoticeActivity::class.java))
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.action_chatbot -> {
                    val fragment = supportFragmentManager.findFragmentByTag("ChatBot")!!

                    transaction.show(fragment).commit()
                    curFragment = fragment
                    binding.topBarTextview.text = getString(R.string.chatbot_category)
                    binding.topBarActionImageview.setImageResource(R.drawable.ic_notification)
                    binding.topBarActionLayout.setOnClickListener {
                        startActivity(Intent(this, NoticeActivity::class.java))
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.action_diary -> {
                    val fragment = supportFragmentManager.findFragmentByTag("Diary")!!

                    transaction.show(fragment).commit()
                    curFragment = fragment
                    binding.topBarTextview.text = getString(R.string.diary_category)
                    binding.topBarActionImageview.setImageResource(R.drawable.ic_notification)
                    binding.topBarActionLayout.setOnClickListener {
                        startActivity(Intent(this, NoticeActivity::class.java))
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.action_news -> {
                    val fragment = supportFragmentManager.findFragmentByTag("News")!!

                    transaction.show(fragment).commit()
                    curFragment = fragment
                    binding.topBarTextview.text = getString(R.string.news_category)
                    binding.topBarActionImageview.setImageResource(R.drawable.ic_notification)
                    binding.topBarActionLayout.setOnClickListener {
                        startActivity(Intent(this, NoticeActivity::class.java))
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.action_user -> {
                    val fragment = supportFragmentManager.findFragmentByTag("User")!!

                    transaction.show(fragment).commit()
                    curFragment = fragment
                    binding.topBarTextview.text = getString(R.string.user_category)
                    binding.topBarActionImageview.setImageResource(R.drawable.ic_settings)
                    binding.topBarActionLayout.setOnClickListener {
                        settingLauncher.launch(Intent(this, SettingActivity::class.java))
                    }
                    return@setOnItemSelectedListener true
                }
                else -> { return@setOnItemSelectedListener false }
            }
        }
    }

    private fun initFragments() {

        if (supportFragmentManager.fragments.isNotEmpty()) {
            val transaction = supportFragmentManager.beginTransaction()

            supportFragmentManager.fragments.forEach {
                transaction.attach(it).hide(it)
            }

            transaction.commit()
            return
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.main_content, ChatBotFragment(), "ChatBot")
            .add(R.id.main_content, CommunityFragment(), "Community")
            .add(R.id.main_content, DiaryFragment(), "Diary")
            .add(R.id.main_content, NewsFragment(), "News")
            .add(R.id.main_content, UserFragment(), "User")
            .commitNow()

        val transaction = supportFragmentManager.beginTransaction()

        supportFragmentManager.fragments.forEach {
            transaction.hide(it)
        }

        transaction.commit()
    }


    private fun fragmentTagToResId(tag: String) = when(tag) {
        "Diary" -> R.id.action_diary
        "ChatBot" -> R.id.action_chatbot
        "News" -> R.id.action_news
        "User" -> R.id.action_user
        else -> R.id.action_home
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("curFragment", curFragment.tag)
    }
}