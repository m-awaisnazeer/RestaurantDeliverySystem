package com.comunisolve.restaurantdeliverysystem.ui.foodlist

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comunisolve.restaurantdeliverysystem.Adapters.MyFoodListAdapter
import com.comunisolve.restaurantdeliverysystem.Common.Common
import com.comunisolve.restaurantdeliverysystem.R
import com.comunisolve.restaurantdeliverysystem.ui.home.HomeViewModel

class FoodListFragment : Fragment() {


    var recycler_food_list: RecyclerView? = null
    var layoutAnimationController: LayoutAnimationController? = null
    var adapter: MyFoodListAdapter? = null
    private lateinit var viewModel: FoodListViewModel

    override fun onStop() {
        if (adapter != null)
            adapter!!.onStop()
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root: View = inflater.inflate(R.layout.food_list_fragment, container, false)
        viewModel =
            ViewModelProviders.of(this).get(FoodListViewModel::class.java)

        initViews(root)

        viewModel.getMutableFoodModelListData().observe(viewLifecycleOwner, Observer {
            adapter = MyFoodListAdapter(requireContext(), it)
            recycler_food_list!!.adapter = adapter
            recycler_food_list!!.layoutAnimation = layoutAnimationController
        })

        return root
    }

    private fun initViews(root: View) {
        recycler_food_list = root.findViewById(R.id.recycler_food_list)
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager = LinearLayoutManager(context)

        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FoodListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}