package com.comunisolve.restaurantdeliverysystem.Database

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalCartDataSource(private val cartDao: CartDao) : CartDataSource {
    override fun getAllCart(uid: String): Flowable<List<CartItem>> {
        return cartDao.getAllCart(uid)
    }

    override fun countItemInCart(uid: String): Single<Int> {
        return cartDao.countItemInCart(uid)
    }

    override fun sumPrice(uid: String): Single<Double> {
        return cartDao.sumPrice(uid)
    }

    override fun getItemInCart(foodId: String, uid: String): Single<CartItem> {
        return cartDao.getItemInCart(foodId, uid)
    }

    override fun insertOrReplaceAll(vararg cartItem: CartItem): Completable {
        return cartDao.insertOrReplaceAll(*cartItem)
    }

    override fun updateCart(cartItem: CartItem): Single<Int> {
        return cartDao.updateCart(cartItem)
    }

    override fun DeleteCart(cartItem: CartItem): Single<Int> {
        return cartDao.DeleteCart(cartItem)
    }

    override fun cleanCart(uid: String): Single<Int> {
        return cartDao.cleanCart(uid)
    }

    override fun getItemWihAllOptionsInCart(
        uid: String,
        foodId: String,
        foodSize: String,
        foodAddon: String
    ): Single<CartItem> {
        return cartDao.getItemWihAllOptionsInCart(uid, foodId, foodSize, foodAddon)
    }
}