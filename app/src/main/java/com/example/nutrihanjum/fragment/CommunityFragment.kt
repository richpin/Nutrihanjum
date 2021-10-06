package com.example.nutrihanjum.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrihanjum.R
import com.example.nutrihanjum.viewmodel.CommunityViewModel
import com.example.nutrihanjum.databinding.CommunityFragmentBinding
import com.example.nutrihanjum.model.ContentDTO

class CommunityFragment private constructor() : Fragment() {
    private var _binding : CommunityFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        @Volatile private var instance: CommunityFragment? = null

        @JvmStatic fun getInstance(): CommunityFragment = instance ?: synchronized(this) {
            instance ?: CommunityFragment().also {
                instance = it
            }
        }
    }

    private lateinit var viewModel: CommunityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CommunityFragmentBinding.inflate(inflater, container, false)

        val temp = arrayListOf<ContentDTO>()
        temp.add(ContentDTO("오늘은 김치찌개를 해보았어요~ 너무 맛있더라구요!","https://recipe1.ezmember.co.kr/cache/recipe/2018/03/26/2534479bee6df21a0504761742af7a6d1.jpg","https://recipe1.ezmember.co.kr/cache/recipe/2016/03/16/4b2ad9b10dd6253ae2236658bab43b0b1.jpg","temp","계란말이",934))
        temp.add(ContentDTO("오늘은 유영석 때문에 다시 한 번 고구려 짬뽕에 가게 되었다. 맛도 ㅈ같이 없는데 또 엿같은 기분이 사라지지 않아서 먹다가 토를 해버리고 말았다..","https://t1.daumcdn.net/cfile/tistory/99A8FB375E2D95C21A","https://pds.joins.com/news/component/htmlphoto_mmdata/201502/08/htm_20150208195421a010a012.jpg","temp2","내이름은조현웅",45))

        binding.communityfragmentRecylerview.adapter = CommunityRecyclerViewAdapter(temp)
        binding.communityfragmentRecylerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)

    }

    inner class CommunityRecyclerViewAdapter(private val contentDTOs : ArrayList<ContentDTO>)
        : RecyclerView.Adapter<CommunityRecyclerViewAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val communityitem_profile_imageview : ImageView
            val communityitem_profile_textview : TextView
            val communityitem_content_imageview : ImageView
            val communityitem_content_textview : TextView
            val press_like_imageview : ImageView
            val press_comment_imageview : ImageView
            val press_report_imageview : ImageView

            init{
                communityitem_profile_imageview = view.findViewById(R.id.communityitem_profile_imageview)
                communityitem_profile_textview = view.findViewById(R.id.communityitem_profile_textview)
                communityitem_content_imageview = view.findViewById(R.id.communityitem_content_imageview)
                communityitem_content_textview = view.findViewById(R.id.communityitem_content_textview)
                press_like_imageview = view.findViewById(R.id.press_like_imageview)
                press_comment_imageview = view.findViewById(R.id.press_comment_imageview)
                press_report_imageview = view.findViewById(R.id.press_report_imageview)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_community, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            Glide.with(viewHolder.itemView.context).load(contentDTOs[position].profileUrl).circleCrop().into(viewHolder.communityitem_profile_imageview)
            viewHolder.communityitem_profile_textview.text = contentDTOs[position].userId
            Glide.with(viewHolder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHolder.communityitem_content_imageview)
            viewHolder.communityitem_content_textview.text = contentDTOs[position].content
        }

        override fun getItemCount() : Int {
            return contentDTOs.size
        }
    }
}