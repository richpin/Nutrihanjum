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

    private val _chatBotList = MutableLiveData<ArrayList<ChatBotDTO>>()
    val chatBotList: LiveData<ArrayList<ChatBotDTO>> get() = _chatBotList

    fun loadAllChatBots() = viewModelScope.launch {
        ChatBotRepository.loadAllChatBots().collect {
            _chatBotList.postValue(ArrayList(it))
        }
    }


    private val REPLY_FAILED by lazy {
        val temp = BotData()
        temp.name = chatBot.profileName
        temp.profileUrl = chatBot.profileUrl
        temp.quickReplies.add(QuickReplyOption("다시 시도하기", ""))
        temp.quickReplies.add(QuickReplyOption("처음으로", "event_welcome"))
        temp
    }

    private var lastRequest = Pair("event_welcome", "event")

    lateinit var chatBot: ChatBotDTO

    private val _chatList = MutableLiveData<ArrayList<ChatData>>(arrayListOf())
    val chatList: LiveData<ArrayList<ChatData>> get() = _chatList

    private val _initialized = MutableLiveData<Boolean>()
    val initialized: LiveData<Boolean> get() = _initialized

    fun initChatBot(chatBot: ChatBotDTO) {
        this.chatBot = chatBot
        REPLY_FAILED.message = chatBot.fallback
        ChatBotRepository.initChatBot(chatBot)
        welcomeMessage()
    }


    private fun welcomeMessage() {
        if (chatList.value?.isNotEmpty() == true) {
            _initialized.value = true
            return
        }

        requestToBot("event_welcome", "event").invokeOnCompletion {
            _initialized.value = true
        }
    }


    fun sendMessage(message: String) = viewModelScope.launch {
        _chatList.value!!.add(makeUserData(message))
        _chatList.value = _chatList.value
        requestToBot(message, "text")
    }

    fun sendEvent(message: String, event: String) = viewModelScope.launch {
        if (event.isEmpty()) {
            requestToBot(lastRequest.first, lastRequest.second)
        }
        else {
            _chatList.value!!.add(makeUserData(message))
            _chatList.value = _chatList.value
            requestToBot(event, "event")
        }
    }

    private fun requestToBot(message: String, type: String) = viewModelScope.launch {
        lastRequest = Pair(message, type)

        ChatBotRepository.sendMessage(message, type, chatBot).collect {
            if (it != null) {
                _chatList.value!!.add(makeBotData(it))
                _chatList.postValue(_chatList.value)
            } else {
                _chatList.value!!.add(REPLY_FAILED)
                _chatList.postValue(_chatList.value)
            }
        }
    }


    private fun makeBotData(response: ChatBotResponseDTO): BotData {
        val data = BotData()

        data.name = chatBot.profileName
        data.profileUrl = chatBot.profileUrl
        data.message = response.text

        data.quickReplies = response.quickReplies

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