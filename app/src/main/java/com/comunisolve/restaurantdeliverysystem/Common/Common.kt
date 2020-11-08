package com.comunisolve.restaurantdeliverysystem.Common

import com.comunisolve.restaurantdeliverysystem.Model.*
import java.lang.StringBuilder
import java.math.RoundingMode
import java.text.DecimalFormat

object Common {
    fun formatPrice(displayPrice: Double): String {
        if (displayPrice != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(displayPrice)).toString()
            return finalPrice.replace(".", ",")
        } else
            return "0,00"
    }

    fun calculateExtraPrice(
        userSelectedSize: SizeModel?,
        userSelectedAddon: MutableList<AddonModel>?
    ): Double {
        var result: Double = 0.0
        if (userSelectedSize == null && userSelectedAddon == null)
            return 0.0
        else if (userSelectedSize == null) {
            for (addOnModel in userSelectedAddon!!)
                result += addOnModel!!.price.toDouble()
            return result
        } else if (userSelectedAddon == null) {
            result = userSelectedSize!!.price.toDouble()
            return result
        } else {
            result = userSelectedSize!!.price.toDouble()
            for (addOnModel in userSelectedAddon!!)
                result += addOnModel!!.price.toDouble()
            return result
        }
    }

    val COMMENT_REF: String = "Comments"
    var foodSelected: FoodModel? = null
    var categorySelected: CategoryModel? = null
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLOMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser: UserModel? = null
}