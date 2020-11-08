package com.comunisolve.restaurantdeliverysystem.ui.cartItemsFragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comunisolve.restaurantdeliverysystem.Adapters.MyCartAdapter
import com.comunisolve.restaurantdeliverysystem.Database.CartDataSource
import com.comunisolve.restaurantdeliverysystem.Database.CartDatabase
import com.comunisolve.restaurantdeliverysystem.Database.LocalCartDataSource
import com.comunisolve.restaurantdeliverysystem.EventBus.HideFABCart
import com.comunisolve.restaurantdeliverysystem.EventBus.UpdateItemInCart
import com.comunisolve.restaurantdeliverysystem.R
import com.google.android.gms.common.internal.service.Common
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder

class CartItemsFragment : Fragment() {

    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable? = null


    //UIs
    var recycler_cart: RecyclerView? = null
    var txt_empty_cart: TextView? = null
    var group_place_order: CardView? = null
    var txt_total_price: TextView? = null
    var btn_place_order: Button? = null

    companion object {
        fun newInstance() =
            CartItemsFragment()
    }

    override fun onResume() {
        super.onResume()
     calculateTotalPrice()
    }
    private lateinit var viewModel: CartItemsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().postSticky(HideFABCart(true))
        val root: View = inflater.inflate(R.layout.cart_items_fragment, container, false)
        viewModel = ViewModelProvider(this).get(CartItemsViewModel::class.java)

        //After create VuewModel , init data source
        viewModel.initCartDataSource(requireContext())
        initView(root)

        viewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recycler_cart!!.visibility = View.GONE
                group_place_order!!.visibility = View.GONE
                txt_empty_cart!!.visibility = View.VISIBLE
            } else {
                recycler_cart!!.visibility = View.VISIBLE
                group_place_order!!.visibility = View.VISIBLE
                txt_empty_cart!!.visibility = View.GONE

                val adapter = MyCartAdapter(requireContext(), it)
                recycler_cart!!.adapter = adapter
            }

        })

        return root
    }

    private fun initView(root: View) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())
        recycler_cart = root.findViewById(R.id.recycler_cart)
        recycler_cart!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))


        txt_empty_cart = root.findViewById(R.id.txt_empty_cart)
        group_place_order = root.findViewById(R.id.group_place_order)
        txt_total_price = root.findViewById(R.id.txt_total_price)
        btn_place_order = root.findViewById(R.id.btn_place_order)

    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFABCart(false))
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUodateItemInCart(event: UpdateItemInCart) {
        if (event.cartItem != null) {
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context!!, "[UPDATE CART]" + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {

        cartDataSource!!.sumPrice(com.comunisolve.restaurantdeliverysystem.Common.Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :SingleObserver<Double>{
                override fun onSuccess(price: Double) {
                    txt_total_price!!.text=StringBuilder("Total: ")
                        .append(com.comunisolve.restaurantdeliverysystem.Common.Common.formatPrice(price))
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context!!, "[SUM CART]" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

}