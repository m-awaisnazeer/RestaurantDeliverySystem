package com.comunisolve.restaurantdeliverysystem.Callback

import com.comunisolve.restaurantdeliverysystem.Model.BestDealModel
import com.comunisolve.restaurantdeliverysystem.Model.PopularCategoryModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>)
    fun onBestDealLoadFailed(message: String)
}