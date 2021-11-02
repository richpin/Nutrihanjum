package com.example.nutrihanjum.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.chatbot.model.*
import com.example.nutrihanjum.repository.ChatBotRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatBotViewModel : ViewModel() {

    private val _chatBotList = MutableLiveData<ArrayList<ChatBotProfileDTO>>()
    val chatBotList: LiveData<ArrayList<ChatBotProfileDTO>> get() = _chatBotList

    fun loadAllChatBots() = viewModelScope.launch {
        ChatBotRepository.loadAllChatBots().collect {
            _chatBotList.postValue(ArrayList(it))
        }
    }


    private val REPLY_FAILED by lazy {
        val temp = BotData()
        temp.message = "응답에 실패했습니다. 잠시 후 다시 시도해주세요."
        temp.name = chatBot.profileName
        temp.profileUrl = chatBot.profileUrl
        temp
    }

    var chatBot = ChatBotProfileDTO()
    private var info: ChatBotRequestInfo? = null

    private val _chatList = MutableLiveData<ArrayList<ChatData>>(arrayListOf())
    val chatList: LiveData<ArrayList<ChatData>> get() = _chatList

    private val _initialized = MutableLiveData<Boolean>()
    val initialized: LiveData<Boolean> get() = _initialized

    fun initChatBot(chatBot: ChatBotProfileDTO) {
        this.chatBot = chatBot

        viewModelScope.launch {
            ChatBotRepository.initChatBot(chatBot.id, viewModelScope).collect {
                if (it != null) {
                    info = it
                } else {
                    _chatList.value!!.add(REPLY_FAILED)
                    _chatList.postValue(_chatList.value)
                }

                _initialized.postValue(true)
            }
        }
    }


    fun welcomeMessage() {
        info?.welcome?.let {
            _chatList.value!!.add(makeBotData(it))
            _chatList.value = _chatList.value
        }
    }


    fun sendMessage(message: String) = viewModelScope.launch {
        _chatList.value!!.add(makeUserData(message))
        _chatList.value = _chatList.value
        requestToBot(message)
    }


    private fun requestToBot(message: String) = viewModelScope.launch {
        if (info != null) {
            ChatBotRepository.sendMessage(message, info!!).collect {
                if (it != null) {
                    _chatList.value!!.add(makeBotData(it))
                    _chatList.postValue(_chatList.value)
                } else {
                    _chatList.value!!.add(REPLY_FAILED)
                    _chatList.postValue(_chatList.value)
                }
            }
        }
        else {
            initChatBot(chatBot)
        }
    }


    private fun makeBotData(response: ChatBotResponseDTO): BotData {
        val data = BotData()

        data.name = chatBot.profileName
        data.profileUrl = chatBot.profileUrl
        data.message = response.getMessage()
        data.quickReplies = ArrayList(response.getQuickReplies().map {
            QuickReplyOption(text = it.text, action = it.action)
        })

        return data
    }


    private fun makeUserData(message: String): UserData {
        val data = UserData()

        data.name = ChatBotRepository.userName ?: "익명"
        data.profileUrl = ChatBotRepository.userPhoto.toString()
        data.message = message

        return data
    }

}