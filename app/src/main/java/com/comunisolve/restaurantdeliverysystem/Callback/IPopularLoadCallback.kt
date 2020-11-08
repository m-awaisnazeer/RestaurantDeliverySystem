package com.comunisolve.restaurantdeliverysystem.Callback

import com.comunisolve.restaurantdeliverysystem.Model.PopularCategoryModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>)
    fun onPopularLoadFailed(message: String)
}