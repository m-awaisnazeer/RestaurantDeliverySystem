package com.comunisolve.restaurantdeliverysystem.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.comunisolve.restaurantdeliverysystem.Database.CartDataSource
import com.comunisolve.restaurantdeliverysystem.Database.CartDatabase
import com.comunisolve.restaurantdeliverysystem.Database.CartItem
import com.comunisolve.restaurantdeliverysystem.Database.LocalCartDataSource
import com.comunisolve.restaurantdeliverysystem.EventBus.UpdateItemInCart
import com.comunisolve.restaurantdeliverysystem.Model.FoodModel
import com.comunisolve.restaurantdeliverysystem.R
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyCartAdapter(
    internal var context: Context,
    internal var cartItemList: List<CartItem>
) : RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {

    internal var compositeDisposable: CompositeDisposable
    internal var cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDao())
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var img_cart: ImageView
        lateinit var txt_food_name: TextView
        lateinit var txt_food_price: TextView
        lateinit var number_button: ElegantNumberButton

        init {
            img_cart = itemView.findViewById(R.id.img_cart)
            txt_food_name = itemView.findViewById(R.id.txt_food_name)
            txt_food_price = itemView.findViewById(R.id.txt_food_price)
            number_button = itemView.findViewById(R.id.number_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_cart_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return cartItemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.txt_food_price.text = cartItemList.get(position).foodPrice.toString() + "$"
        holder.txt_food_name.text =
            StringBuilder("").append(cartItemList.get(position).foodName + cartItemList[position].foodExtraPrice)
        holder.number_button.number = cartItemList[position].foodQuantitiy.toString()

        Glide.with(context).load(cartItemList[position].foodImage).into(holder.img_cart)

        holder.number_button.setOnValueChangeListener { view, oldValue, newValue ->
            cartItemList[position].foodQuantitiy = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(cartItemList[position]))
        }

    }
}