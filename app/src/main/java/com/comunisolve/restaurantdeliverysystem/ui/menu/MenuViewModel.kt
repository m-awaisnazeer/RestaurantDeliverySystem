package com.comunisolve.restaurantdeliverysystem.ui.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comunisolve.restaurantdeliverysystem.Callback.ICategoryCallBackListner
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuViewModel : ViewModel(), ICategoryCallBackListner {

    private var catogoriesListMutable: MutableLiveData<List<CategoryModel>>? = null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val catogoryCallBaclListner: ICategoryCallBackListner

    fun getCtegoryList():MutableLiveData<List<CategoryModel>>{
        if (catogoriesListMutable ==null){
            catogoriesListMutable = MutableLiveData()
            loadCategory()
        }
        return catogoriesListMutable!!
    }

    fun getMessageError():MutableLiveData<String>{
        return messageError
    }
    private fun loadCategory() {
        val templist = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                catogoryCallBaclListner.onCategoryLoadFailed(error.message)

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot!!.children) {
                    val model = itemSnapShot.getValue(CategoryModel::class.java)
                    model!!.menu_id= itemSnapShot.key
                    templist.add(model!!)
                }
                catogoryCallBaclListner.onCategoryLoadSuccess(templist)
            }

        })
    }

    init {
        catogoryCallBaclListner = this
    }

    override fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>) {
        catogoriesListMutable!!.value = categoriesList

    }

    override fun onCategoryLoadFailed(message: String) {
        messageError.value = message
    }
}