package com.comunisolve.restaurantdeliverysystem.ui.feedDetails

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Database.CartDataSource
import com.comunisolve.restaurantdeliverysystem.Database.CartDatabase
import com.comunisolve.restaurantdeliverysystem.Database.CartItem
import com.comunisolve.restaurantdeliverysystem.Database.LocalCartDataSource
import com.comunisolve.restaurantdeliverysystem.EventBus.CountCartEvent
import com.comunisolve.restaurantdeliverysystem.HomeActivity
import com.comunisolve.restaurantdeliverysystem.MainActivity
import com.comunisolve.restaurantdeliverysystem.Model.CommentModel
import com.comunisolve.restaurantdeliverysystem.Model.FoodModel
import com.comunisolve.restaurantdeliverysystem.R
import com.comunisolve.restaurantdeliverysystem.ui.commentBottomSheets.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_food_details.*
import org.greenrobot.eventbus.EventBus


class FoodDetailsFragment : Fragment(), TextWatcher {

    private lateinit var adOnBottomSheetDialog: BottomSheetDialog


    private val compositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource:CartDataSource


    private var img_food: ImageView? = null
    private var btnCart: CounterFab? = null
    private var btnRating: FloatingActionButton? = null
    private var food_name: TextView? = null
    private var food_description: TextView? = null
    private var food_price: TextView? = null
    private var number_btn: ElegantNumberButton? = null
    private var ratingBar: RatingBar? = null
    private var btnShowComment: Button? = null
    private var rdi_group_size: RadioGroup? = null
    private var waitingDialog: android.app.AlertDialog? = null
    private var chip_group_user_selected_addon: ChipGroup? = null
    private var img_add_on: ImageView? = null

    //Addon layout
    private var chip_group_addon: ChipGroup? = null
    private var edt_search_addon: EditText? = null


    private lateinit var foodDetailsViewModel: FoodDetailsViewModel


    var mactivity: HomeActivity? = null

    override fun onStart() {
        super.onStart()
        mactivity = getActivity() as HomeActivity?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailsViewModel =
            ViewModelProvider(requireActivity()).get(FoodDetailsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_food_details, container, false)


        initView(root)
        foodDetailsViewModel.getMutableLiveDataFood().observe(viewLifecycleOwner, Observer {
            displayFoodInfo(it)

            foodDetailsViewModel.getMutableLiveDataComment().observe(viewLifecycleOwner, Observer {
                submitRatingToFIrebase(it)
            })

        })
        return root
    }

    private fun submitRatingToFIrebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        //First, we will submit to Comment Ref
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToFood(commentModel!!.ratingValue.toDouble())
                }
                waitingDialog!!.dismiss()

            }
    }

    private fun addRatingToFood(ratingValue: Double) {

        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF) // select Category
            .child(Common.categorySelected!!.menu_id!!) // select menu in categry
            .child("foods") // select foods aray
            .child(Common.foodSelected!!.key!!) // select key we initialize it in MyFoodAdaptet onClicklistner Interface
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val foodModel = snapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key
                        //Apply rating
                        val sumRating = foodModel.ratingValue.toDouble() + ratingValue.toDouble()
                        val ratingCount = foodModel.ratingCount + 1
                        val result = sumRating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        //Update Data in Variable
                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = result

                        snapshot.ref.updateChildren(updateData)
                            .addOnCompleteListener { task ->
                                waitingDialog!!.dismiss()
                                if (task.isSuccessful) {
                                    Common.foodSelected = foodModel
                                    //foodDetailsViewModel!!.setFoodModel(foodModel)
                                    //Toast.makeText(mactivity, "Thank You", Toast.LENGTH_SHORT).show()
                                }
                            }

                    } else {
                        waitingDialog!!.dismiss()
                    }
                }

            })
    }


    private fun displayFoodInfo(foodModel: FoodModel) {
        Glide.with(requireActivity()).load(foodModel.image).into(img_food!!)
        food_description!!.text = StringBuilder(foodModel.description)
        food_name!!.text = StringBuilder(foodModel.name)
        food_price!!.text = StringBuilder(foodModel.price.toString())

        ratingBar!!.rating = foodModel!!.ratingValue!!.toFloat()

        //set size
        for (sizeModel in foodModel.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
            )
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }
        //Default first radio button select
        if (rdi_group_size!!.childCount > 0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }

    }

    private fun calculateTotalPrice() {

        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        //Addon
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addOnModel in Common.foodSelected!!.userSelectedAddon!!)
                totalPrice += addOnModel.price.toDouble()
        }

        //size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price.toDouble()

        displayPrice = totalPrice * number_btn!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        food_price!!.text =
            java.lang.StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initView(root: View?) {

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDao())
        adOnBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layout_user_selected_addon = layoutInflater.inflate(R.layout.layout_addon_display, null)
        chip_group_addon = layout_user_selected_addon!!.findViewById(R.id.layout_adon_display)
        edt_search_addon = layout_user_selected_addon!!.findViewById(R.id.edt_search)
        adOnBottomSheetDialog.setContentView(layout_user_selected_addon)

        adOnBottomSheetDialog.setOnDismissListener { dialogInterface ->
            displayUserSelectedAddOn()
            calculateTotalPrice()
        }




        (activity as AppCompatActivity).supportActionBar!!.title = Common.foodSelected!!.name

        waitingDialog =
            SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        img_food = root!!.findViewById(R.id.foodImage)
        btnCart = root!!.findViewById(R.id.btnCart)
        btnRating = root!!.findViewById(R.id.btn_rating)
        food_name = root!!.findViewById(R.id.food_name)
        food_description = root!!.findViewById(R.id.food_description)
        food_price = root!!.findViewById(R.id.food_price)
        number_btn = root!!.findViewById(R.id.number_button)
        ratingBar = root!!.findViewById(R.id.food_rating)
        btnShowComment = root!!.findViewById(R.id.btnShowComment)
        rdi_group_size = root.findViewById(R.id.rdi_group_size)
        img_add_on = root.findViewById(R.id.img_add_on)
        chip_group_user_selected_addon = root.findViewById(R.id.chip_group_user_selected_addon)

        number_btn!!.setOnValueChangeListener { view, oldValue, newValue ->
            calculateTotalPrice()
        }
        img_add_on!!.setOnClickListener {
            if (Common.foodSelected!!.addon != null) {

                displayAllAddon()
                adOnBottomSheetDialog.show()
            }

        }

        btnRating!!.setOnClickListener {
            showDialogRation()
        }

        btnShowComment!!.setOnClickListener {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager, "CommentFragment")


        }

        btnCart!!.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone!!

            cartItem.foodId = Common.foodSelected!!.id!!
            cartItem.foodName = Common.foodSelected!!.name!!
            cartItem.foodImage = Common.foodSelected!!.image!!
            cartItem.foodPrice = Common.foodSelected!!.price.toDouble()!!
            cartItem.foodQuantitiy = number_button.number.toInt()
            cartItem.foodExtraPrice = Common.calculateExtraPrice(
                Common.foodSelected!!.userSelectedSize,
                Common.foodSelected!!.userSelectedAddon
            )
            if (Common.foodSelected!!.userSelectedAddon != null)
                cartItem.foodAddon = Gson().toJson(Common.foodSelected!!.userSelectedAddon)
            else
                cartItem.foodAddon = "Default"

            if (Common.foodSelected!!.userSelectedSize != null)
                cartItem.foodSize = Gson().toJson(Common.foodSelected!!.userSelectedSize)
            else
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

    private fun displayAllAddon() {
        if (Common.foodSelected!!.addon.size > 0) {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()

            edt_search_addon!!.addTextChangedListener(this)

            for (addOnModel in Common.foodSelected!!.addon) {

                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text =
                    java.lang.StringBuilder(addOnModel.name).append("(+$").append(addOnModel.price)
                        .append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addOnModel)
                    }
                }
                chip_group_addon!!.addView(chip)


            }

        }
    }

    private fun displayUserSelectedAddOn() {

        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {

            chip_group_user_selected_addon!!.removeAllViews()
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                val chip =
                    layoutInflater.inflate(R.layout.layout_chip_with_delete, null, false) as Chip
                chip.text =
                    java.lang.StringBuilder(addonModel.name).append("+$").append(addonModel.price)
                        .append(")").toString()
                chip.isClickable = false
                chip.setOnCloseIconClickListener { view ->
                    chip_group_user_selected_addon!!.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addon!!.addView(chip)

            }
        } else if (Common.foodSelected!!.userSelectedAddon!!.size == 0)
            chip_group_user_selected_addon!!.removeAllViews()
    }

    private fun showDialogRation() {

        var builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rating Food")
        builder.setMessage("Please fill information")

        var itemView =
            LayoutInflater.from(requireContext()).inflate(R.layout.layout_rating_comment, null)

        val rating = itemView.findViewById<RatingBar>(R.id.rating_bar)
        val edit_comment = itemView.findViewById<EditText>(R.id.edit_comment)

        builder.setView(itemView)

        builder.setNegativeButton("Cancel") { dialogInterface, i ->
            dialogInterface.dismiss()
        }

        builder.setPositiveButton("Ok") { dialogInterface, i ->
            val commentModel = CommentModel()

            commentModel.name =
                com.comunisolve.restaurantdeliverysystem.Common.Common.currentUser!!.name
            commentModel.uid =
                com.comunisolve.restaurantdeliverysystem.Common.Common.currentUser!!.uid
            commentModel.comment = edit_comment.text.toString()
            commentModel.ratingValue = ratingBar!!.rating

            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = (serverTimeStamp)

            foodDetailsViewModel!!.setCommentModel(commentModel)

        }
        val dialog = builder.create()
        dialog.show()

    }

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()

        for (addOnModel in Common.foodSelected!!.addon) {
            if (addOnModel.name!!.toLowerCase().contains(charSeq.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text =
                    java.lang.StringBuilder(addOnModel.name).append("(+$").append(addOnModel.price)
                        .append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addOnModel)
                    }
                }
                chip_group_addon!!.addView(chip)

            }
        }

    }
}