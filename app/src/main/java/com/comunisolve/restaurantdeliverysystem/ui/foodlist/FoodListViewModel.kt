package com.comunisolve.restaurantdeliverysystem.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Model.FoodModel

class FoodListViewModel : ViewModel() {

    private var mutableFoodModelListData: MutableLiveData<List<FoodModel>>? = null

    fun getMutableFoodModelListData(): MutableLiveData<List<FoodModel>> {
        if (mutableFoodModelListData == null)
            mutableFoodModelListData = MutableLiveData()
        mutableFoodModelListData!!.value = Common.categorySelected!!.foods
        return mutableFoodModelListData!!

    }
}