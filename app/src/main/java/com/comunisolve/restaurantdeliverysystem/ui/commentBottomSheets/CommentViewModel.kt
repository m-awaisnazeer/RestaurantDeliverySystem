package com.comunisolve.restaurantdeliverysystem.ui.commentBottomSheets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comunisolve.restaurantdeliverysystem.Model.CommentModel

class CommentViewModel : ViewModel() {


    val mutableLiveDataCommentList: MutableLiveData<List<CommentModel>>

    init {
        mutableLiveDataCommentList = MutableLiveData()
    }

    fun setCommentList(commentList: List<CommentModel>) {
        mutableLiveDataCommentList.value = commentList
    }
}