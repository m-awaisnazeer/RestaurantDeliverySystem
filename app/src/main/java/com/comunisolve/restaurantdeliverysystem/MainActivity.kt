package com.comunisolve.restaurantdeliverysystem

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Model.UserModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var listner: FirebaseAuth.AuthStateListener
    lateinit var dialog: AlertDialog
    val compositeDisposable = CompositeDisposable()
    lateinit var userRef: DatabaseReference
    private var providers: List<AuthUI.IdpConfig>? = null


    companion object {
        private val APP_REQUEST_CODE = 7171
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listner)
    }

    override fun onStop() {
        if (listner != null)
            firebaseAuth.removeAuthStateListener(listner)
        compositeDisposable.clear()
        super.onStop()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {

        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        listner = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                //Already Login
                checkUserFromFirebase(user)
                Toast.makeText(this, "Welcome Back", Toast.LENGTH_SHORT).show()
            } else {
                //Not Login
                phoneLogin()

            }
        }
    }

    private fun phoneLogin() {


        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers!!).build(),
            APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE)
        {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser

            }else{
                Toast.makeText(this,"Failed to sign in",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun checkUserFromFirebase(user: FirebaseUser) {

        dialog!!.show()
        userRef!!.child(user!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "" + error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        goToHomeActivity(userModel)
                    } else {
                        showRegisterDialog(user!!)
                    }
                    dialog!!.dismiss()
                }

            })

    }

    private fun showRegisterDialog(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.layout_register, null)

        val edit_name = itemView.findViewById<EditText>(R.id.edit_name)
        val edit_address = itemView.findViewById<EditText>(R.id.edit_address)
        val edit_phone = itemView.findViewById<EditText>(R.id.edit_phone)

        edit_phone.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton("REGISTER") { dialogInterface, i ->
            if (TextUtils.isDigitsOnly(edit_name.text.toString())) {
                Toast.makeText(this@MainActivity, "Please enter your name", Toast.LENGTH_SHORT)
                    .show()
                return@setPositiveButton
            } else if (TextUtils.isDigitsOnly(edit_address.text.toString())) {
                Toast.makeText(this@MainActivity, "Please enter your Address", Toast.LENGTH_SHORT)
                    .show()
                return@setPositiveButton
            }
            val userModel = UserModel()
            userModel.uid = user!!.uid
            userModel.name = edit_name.text.toString()
            userModel.address = edit_address.text.toString()
            userModel.phone = edit_phone.text.toString()

            userRef!!.child(user!!.uid).setValue(userModel)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialogInterface.dismiss()
                        Toast.makeText(
                            this@MainActivity,
                            "Register Successfully",
                            Toast.LENGTH_SHORT
                        )
                        goToHomeActivity(userModel)

                    }
                }
        }

        //Important ! please show dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser = userModel!!
        startActivity(Intent(this,HomeActivity::class.java))
        finish()

    }
}


/*
           ###What is CompositeDisposable in RxJava###



Composite disposable makes disposing (think cancelling early easier). Say you have an activity that has multiple api calls happening at once:

var disposable = api.call1(arg1, arg2).subscribe(...)
var disposable2 = api.call2(arg1).subscribe(...)
var disposable3 = api.call3().subscribe()

If you need to prematurely dispose (e.g. the user navigating away from the activity) then you'd need to do this:

disposable.dispose()
disposable2.dispose()
disposable3.dispose()

If you instead use a CompositeDisposable you can store all of the disposables in it. Like so:

val composite = CompositeDisposable()
composite.add(api.call1(arg1, arg2).subscribe(...))
composite.add(api.call2(arg1).subscribe(...))
composite.add(api.call3().subscribe())

And then you can make one dispose call instead:

composite.dispose()

If you are using kotlin you can use operator overloading to make this look nicer:

 operator fun CompositeDisposable.plusAssign(disposable: Disposable){
       this.add(disposable)
   }

Which enables you to express it as:

val composite = CompositeDisposable()
composite += api.call1(arg1, arg2).subscribe(...)
composite += api.call2(arg1).subscribe(...)
composite += api.call3().subscribe()

Disposable signifies a request (think work being done) and has a method called dispose for disposing of the request.

    */