package com.comunisolve.restaurantdeliverysystem.ui.commentBottomSheets

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comunisolve.restaurantdeliverysystem.Adapters.MyCommentAdapter
import com.comunisolve.restaurantdeliverysystem.Callback.ICommentCallBack
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.Model.CommentModel
import com.comunisolve.restaurantdeliverysystem.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog

class CommentFragment : BottomSheetDialogFragment(), ICommentCallBack {

    private var commentViewModel: CommentViewModel? = null

    private var listner: ICommentCallBack
    var dialog: AlertDialog? = null
    lateinit var comment_recycler: RecyclerView

    init {
        listner = this
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.comment_fragment, container, false)
        initViews(itemView)
        loadCommentFromFirebase()

        commentViewModel!!.mutableLiveDataCommentList.observe(viewLifecycleOwner, Observer {
            val adapter = MyCommentAdapter(requireContext(), it)
            comment_recycler.adapter = adapter
        })
        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()
        val commentModels = ArrayList<CommentModel>()

        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    listner.onCommentLoadFailed(error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (commentSnapShot in snapshot.children) {
                        val commentModel = commentSnapShot.getValue(CommentModel::class.java)
                        commentModels.add(commentModel!!)
                    }

                    listner.onCommentLoadSuccess(commentModels)
                }

            })

    }

    private fun initViews(itemView: View?) {
        commentViewModel = ViewModelProvider(this).get(CommentViewModel::class.java)
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        comment_recycler = itemView!!.findViewById(R.id.comment_recycler)
        comment_recycler.setHasFixedSize(true)
        val layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true)
        comment_recycler.layoutManager = layoutManager
        comment_recycler.addItemDecoration(
            DividerItemDecoration(
                context,
                layoutManager.orientation
            )
        )


    }

    override fun onCommentLoadSuccess(commentLists: List<CommentModel>) {
        dialog!!.dismiss()
        commentViewModel!!.setCommentList(commentLists)
    }

    override fun onCommentLoadFailed(message: String) {
        Toast.makeText(requireContext(), "" + message, Toast.LENGTH_SHORT).show()
        dialog!!.dismiss()
    }

    companion object {
        private var instance: CommentFragment? = null

        fun getInstance(): CommentFragment {
            if (instance == null)
                instance = CommentFragment()
            return instance!!
        }
    }

}