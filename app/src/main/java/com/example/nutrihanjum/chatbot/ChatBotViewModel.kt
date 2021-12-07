package com.example.nutrihanjum.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrihanjum.chatbot.model.*
import com.example.nutrihanjum.repository.ChatBotRepository
import com.example.nutrihanjum.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatBotViewModel : ViewModel() {


    val userName get() = UserRepository.userName
    val userPhoto get() = UserRepository.userPhoto

    private val _chatBotList = MutableLiveData<ArrayList<ChatBotDTO>>()
    val chatBotList: LiveData<ArrayList<ChatBotDTO>> get() = _chatBotList

    fun loadAllChatBots() = viewModelScope.launch {
        ChatBotRepository.loadAllChatBots().collect {
            _chatBotList.postValue(ArrayList(it))
        }
    }


    private val REPLY_FAILED by lazy {
        val temp = BotData()
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

        requestToBot("event_welcome", "event")
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
                if (_initialized.value == null || _initialized.value == false) {
                    _initialized.postValue(true)
                }

                makeBotData(it).forEach { data ->
                    _chatList.value!!.add(data)
                    _chatList.postValue(_chatList.value)
                    delay(1200)
                }
            } else {
                _chatList.value!!.add(REPLY_FAILED)
                _chatList.postValue(_chatList.value)
            }
        }
    }


    private fun makeBotData(response: ChatBotResponseDTO): ArrayList<BotData> {
        val data = ArrayList<BotData>()

        response.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            data.add(BotData(message = it))
        }

        if (response.imageUrl.isNotEmpty()) {
            data.add(BotData(imageUrl = response.imageUrl))
        }

        if (data.isNotEmpty()) {
            data.last().quickReplies = response.quickReplies
        }

        return data
    }


    private fun makeUserData(message: String): UserData {
        val data = UserData()

        data.message = message

        return data
    }

}