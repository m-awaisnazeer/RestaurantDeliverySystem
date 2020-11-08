package com.comunisolve.restaurantdeliverysystem.Callback

import com.comunisolve.restaurantdeliverysystem.Model.CategoryModel
import com.comunisolve.restaurantdeliverysystem.Model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentLists: List<CommentModel>)
    fun onCommentLoadFailed(message: String)
}