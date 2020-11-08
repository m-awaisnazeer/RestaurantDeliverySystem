package com.comunisolve.restaurantdeliverysystem.Adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.comunisolve.restaurantdeliverysystem.Model.CategoryModel
import com.comunisolve.restaurantdeliverysystem.Model.CommentModel
import com.comunisolve.restaurantdeliverysystem.R

class MyCommentAdapter(
    internal var context: Context,
    internal var commentsLists: List<CommentModel>
) : RecyclerView.Adapter<MyCommentAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var comment_img: ImageView? = null
        var txt_comment_name: TextView? = null
        var txt_comment_date: TextView? = null
        var txt_comment: TextView? = null
        var rating_bar: RatingBar? = null

        init {
            comment_img = itemView.findViewById(R.id.comment_img)
            txt_comment_name = itemView.findViewById(R.id.txt_comment_name)
            txt_comment_date = itemView.findViewById(R.id.txt_comment_date)
            txt_comment = itemView.findViewById(R.id.txt_comment)
            rating_bar = itemView.findViewById(R.id.rating_bar)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_comment_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return commentsLists.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val timeStamp =
            commentsLists.get(position).commentTimeStamp!!["timeStamp"]!!.toString().toLong()
        holder.txt_comment_date!!.text = DateUtils.getRelativeTimeSpanString(timeStamp)
        holder.txt_comment!!.text = commentsLists.get(position).comment
        holder.txt_comment_name!!.text = commentsLists.get(position).name
        holder.rating_bar!!.rating = commentsLists.get(position).ratingValue

    }
}