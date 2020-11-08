package com.comunisolve.restaurantdeliverysystem.ui.feedDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Model.CommentModel
import com.comunisolve.restaurantdeliverysystem.Model.FoodModel

class FoodDetailsViewModel : ViewModel() {

    private var mutableLiveDataFood: MutableLiveData<FoodModel>? = null
    private var mutableLiveDataComment: MutableLiveData<CommentModel>? = null

    init {
        mutableLiveDataComment = MutableLiveData()
    }

    fun getMutableLiveDataFood(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood == null)
            mutableLiveDataFood = MutableLiveData()
        mutableLiveDataFood!!.value = Common.foodSelected
        return mutableLiveDataFood!!
    }

    fun getMutableLiveDataComment(): MutableLiveData<CommentModel> {
        if (mutableLiveDataComment == null)
            mutableLiveDataComment = MutableLiveData()
        return mutableLiveDataComment!!
    }

    fun setCommentModel(commentModel: CommentModel) {
        if (mutableLiveDataComment != null) {
            mutableLiveDataComment!!.value = (commentModel)
        }

    }

    fun setFoodModel(foodModel: FoodModel) {
        if (mutableLiveDataFood != null)
            mutableLiveDataFood!!.value = foodModel

    }


}