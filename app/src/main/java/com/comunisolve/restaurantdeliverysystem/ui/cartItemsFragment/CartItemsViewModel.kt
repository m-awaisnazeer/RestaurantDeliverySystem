package com.comunisolve.restaurantdeliverysystem.ui.cartItemsFragment

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comunisolve.restaurantdeliverysystem.Common.Common.currentUser
import com.comunisolve.restaurantdeliverysystem.Database.CartDataSource
import com.comunisolve.restaurantdeliverysystem.Database.CartDatabase
import com.comunisolve.restaurantdeliverysystem.Database.CartItem
import com.comunisolve.restaurantdeliverysystem.Database.LocalCartDataSource
import com.google.android.gms.common.internal.service.Common
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartItemsViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable
    private var cartDataSource: CartDataSource? = null
    private var mutableLiveDataCartItem: MutableLiveData<List<CartItem>>? = null

    init {
        compositeDisposable = CompositeDisposable()
    }

    fun initCartDataSource(context: Context) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = MutableLiveData()
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    private fun getCartItems() {
        compositeDisposable.addAll(com.comunisolve.restaurantdeliverysystem.Common.Common.currentUser!!.uid?.let {
            cartDataSource!!.getAllCart(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ cartItems ->

                    mutableLiveDataCartItem!!.value = cartItems
                }, { t: Throwable? -> mutableLiveDataCartItem!!.value = null })
        })
    }

    fun onStop(){
        compositeDisposable.clear()
    }


}