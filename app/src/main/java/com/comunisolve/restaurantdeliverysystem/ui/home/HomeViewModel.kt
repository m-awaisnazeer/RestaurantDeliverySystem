package com.comunisolve.restaurantdeliverysystem.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comunisolve.restaurantdeliverysystem.Callback.IBestDealLoadCallback
import com.comunisolve.restaurantdeliverysystem.Callback.IPopularLoadCallback
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Model.BestDealModel
import com.comunisolve.restaurantdeliverysystem.Model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealLoadCallback {

    var popularListMutableLiveData: MutableLiveData<List<PopularCategoryModel>>? = null
    var bestDealMutableLiveData: MutableLiveData<List<BestDealModel>>? = null
    lateinit var messageError: MutableLiveData<String>
    lateinit var popularLoadCallbackLister: IPopularLoadCallback
    var bestDealCallBackListner: IBestDealLoadCallback


    val bestDealList: LiveData<List<BestDealModel>>
        get() {
            if (bestDealMutableLiveData == null) {
                bestDealMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealList()
            }
            return bestDealMutableLiveData!!
        }

    private fun loadBestDealList() {
        val templist = ArrayList<BestDealModel>()
        val bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REF)
        bestDealRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                bestDealCallBackListner.onBestDealLoadFailed(error.message)

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot!!.children) {
                    val model = itemSnapShot.getValue(BestDealModel::class.java)
                    templist.add(model!!)
                }
                bestDealCallBackListner.onBestDealLoadSuccess(templist)
            }

        })
    }

    val popularList: LiveData<List<PopularCategoryModel>>
        get() {
            if (popularListMutableLiveData == null) {
                popularListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadPopularList()
            }
            return popularListMutableLiveData!!
        }

    private fun loadPopularList() {

        val templist = ArrayList<PopularCategoryModel>()
        val popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                popularLoadCallbackLister.onPopularLoadFailed(error.message)

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot!!.children) {
                    val model = itemSnapShot.getValue(PopularCategoryModel::class.java)
                    templist.add(model!!)
                }
                popularLoadCallbackLister.onPopularLoadSuccess(templist)
            }

        })
    }

    init {
        popularLoadCallbackLister = this
        bestDealCallBackListner = this
    }

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>) {
        popularListMutableLiveData!!.value = popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
        messageError.value = message

    }

    override fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>) {
        bestDealMutableLiveData!!.value = bestDealList

    }

    override fun onBestDealLoadFailed(message: String) {
        messageError.value = message
    }
}