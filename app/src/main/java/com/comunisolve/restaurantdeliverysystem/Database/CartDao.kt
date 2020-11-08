package com.comunisolve.restaurantdeliverysystem.Database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CartDao {
    @Query("SELECT * FROM Cart WHERE uid=:uid")
    fun getAllCart(uid: String): Flowable<List<CartItem>>

    @Query("SELECT SUM(foodQuantitiy) FROM Cart WHERE uid=:uid")
    fun countItemInCart(uid: String): Single<Int>

    @Query("SELECT SUM(foodPrice+foodExtraPrice)*foodQuantitiy FROM Cart WHERE uid=:uid")
    fun sumPrice(uid: String): Single<Double>

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid")
    fun getItemInCart(foodId: String, uid: String): Single<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(vararg cartItem: CartItem): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCart(cartItem: CartItem): Single<Int>

    @Delete
    fun DeleteCart(cartItem: CartItem): Single<Int>

    @Query("DELETE FROM Cart WHERE uid=:uid")
    fun cleanCart(uid: String): Single<Int>

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid AND foodSize=:foodSize AND foodAddon=:foodAddon")
    fun getItemWihAllOptionsInCart(
        uid: String,
        foodId: String,
        foodSize: String,
        foodAddon: String
    ): Single<CartItem>
}