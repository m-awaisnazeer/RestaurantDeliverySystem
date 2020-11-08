package com.comunisolve.restaurantdeliverysystem.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.comunisolve.restaurantdeliverysystem.Callback.IRecyclerItemClickListner
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.EventBus.CategoryClick
import com.comunisolve.restaurantdeliverysystem.Model.CategoryModel
import com.comunisolve.restaurantdeliverysystem.R
import org.greenrobot.eventbus.EventBus

class MyCategoriesAdapter(
    internal var context: Context,
    internal var categoriesList: List<CategoryModel>
) : RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var txt_category_name: TextView? = null
        var category_image: ImageView? = null
        internal var listner: IRecyclerItemClickListner? = null


        fun setListner(listner: IRecyclerItemClickListner) {
            this.listner = listner
        }

        init {
            txt_category_name = itemView.findViewById(R.id.category_name)
            category_image = itemView.findViewById(R.id.category_image)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listner!!.onItemClick(view!!, adapterPosition)

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.layout_category_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(categoriesList.get(position).image)
            .into(holder.category_image!!)
        holder.txt_category_name!!.setText(categoriesList.get(position).name)
        holder.setListner(object :IRecyclerItemClickListner{
            override fun onItemClick(view: View, pos: Int) {
                Common.categorySelected = categoriesList.get(pos)
                EventBus.getDefault().postSticky(CategoryClick(true,categoriesList.get(pos)))
            }

        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (categoriesList.size == 1) {
            Common.DEFAULT_COLUMN_COUNT
        } else {
            if (categoriesList.size % 2 == 0) {
                Common.DEFAULT_COLUMN_COUNT
            } else {
                if (position > 1 && position == categoriesList.size - 1) Common.FULL_WIDTH_COLOMN else Common.DEFAULT_COLUMN_COUNT
            }
        }
    }
}