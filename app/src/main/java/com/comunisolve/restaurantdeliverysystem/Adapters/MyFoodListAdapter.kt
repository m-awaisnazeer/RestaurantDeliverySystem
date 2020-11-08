package com.comunisolve.restaurantdeliverysystem.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.comunisolve.restaurantdeliverysystem.Callback.IRecyclerItemClickListner
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Database.CartDataSource
import com.comunisolve.restaurantdeliverysystem.Database.CartDatabase
import com.comunisolve.restaurantdeliverysystem.Database.CartItem
import com.comunisolve.restaurantdeliverysystem.Database.LocalCartDataSource
import com.comunisolve.restaurantdeliverysystem.EventBus.CategoryClick
import com.comunisolve.restaurantdeliverysystem.EventBus.CountCartEvent
import com.comunisolve.restaurantdeliverysystem.EventBus.FoodItemClick
import com.comunisolve.restaurantdeliverysystem.Model.FoodModel
import com.comunisolve.restaurantdeliverysystem.R
import io.reactivex.Scheduler
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    internal var context: Context,
    internal var foodList: List<FoodModel>
) : RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {

    private val compositeDisposable: CompositeDisposable
    private val cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())


    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var txt_food_price: TextView? = null
        var txt_food_name: TextView? = null

        var food_image: ImageView? = null
        var img_fav: ImageView? = null
        var img_cart: ImageView? = null

        internal var listner: IRecyclerItemClickListner? = null


        fun setListner(listner: IRecyclerItemClickListner) {
            this.listner = listner
        }


        init {
            txt_food_name = itemView.findViewById(R.id.txt_food_name)
            txt_food_price = itemView.findViewById(R.id.txt_food_price)
            food_image = itemView.findViewById(R.id.img_food_image)
            img_cart = itemView.findViewById(R.id.img_quick_cart)
            img_fav = itemView.findViewById(R.id.img_fav)
            itemView.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            listner!!.onItemClick(view!!, adapterPosition)

        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.layout_food_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodList.get(position).image)
            .into(holder.food_image!!)
        holder.txt_food_name!!.setText(foodList.get(position).name)
        holder.txt_food_price!!.setText(foodList.get(position).price.toString())

        holder.setListner(object : IRecyclerItemClickListner {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = foodList.get(pos)
                Common.foodSelected!!.key = pos.toString()
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList.get(pos)))
            }

        })

        holder.img_cart!!.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone!!

            cartItem.foodId = foodList.get(position).id!!
            cartItem.foodName = foodList.get(position).name!!
            cartItem.foodImage = foodList.get(position).image!!
            cartItem.foodPrice = foodList.get(position).price.toDouble()!!
            cartItem.foodQuantitiy = 1
            cartItem.foodExtraPrice = 0.0
            cartItem.foodAddon = "Default"
            cartItem.foodSize = "Default"

            cartDataSource.getItemWihAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId,
                cartItem.foodSize!!,
                cartItem.foodAddon!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB.equals(cartItem)) {
                            //if item already in database , just update
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantitiy = cartItem.foodQuantitiy

                            cartDataSource.updateCart(cartItemFromDB)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            "Update Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "[Update Cart]" + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })

                        } else {
                            //if item not available in database, just insert
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context!!,
                                            "Add to Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //Here we will send a notify to HomeActivity to update Counter
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "[INSERT CART]" + t!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )

                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context!!,
                                            "Add to Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //Here we will send a notify to HomeActivity to update Counter
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "[INSERT CART]" + t!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        } else
                            Toast.makeText(context, "[CART ERROR]" + e.message, Toast.LENGTH_SHORT)
                                .show()
                    }

                })

        }

    }

    fun onStop() {
        if (compositeDisposable != null)
            compositeDisposable.clear()
    }

}