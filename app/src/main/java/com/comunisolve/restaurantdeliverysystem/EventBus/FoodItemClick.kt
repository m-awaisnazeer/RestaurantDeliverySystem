package com.comunisolve.restaurantdeliverysystem.EventBus

import com.comunisolve.restaurantdeliverysystem.Model.CategoryModel
import com.comunisolve.restaurantdeliverysystem.Model.FoodModel

class FoodItemClick(var isSuccess: Boolean, var food: FoodModel)