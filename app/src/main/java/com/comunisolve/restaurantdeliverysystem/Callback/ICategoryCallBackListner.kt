package com.comunisolve.restaurantdeliverysystem.Callback

import com.comunisolve.restaurantdeliverysystem.Model.BestDealModel
import com.comunisolve.restaurantdeliverysystem.Model.CategoryModel

interface ICategoryCallBackListner {
    fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>)
    fun onCategoryLoadFailed(message: String)
}