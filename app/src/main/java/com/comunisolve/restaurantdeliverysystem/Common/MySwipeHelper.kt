package com.comunisolve.restaurantdeliverysystem.Common

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.widget.Button
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.comunisolve.restaurantdeliverysystem.Callback.IMyButtonCallBack
import com.firebase.ui.auth.data.model.Resource

abstract class MySwipeHelper(
    context: Context,
    val recyclerView: RecyclerView,
    internal var buttonWidth: Int
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private var buttonList: MutableList<MyButton>? = null

    inner class MyButton(
        private val context: Context,
        private val text: String,
        private val textSize: Int,
        private val imageResId: Int,
        private val color:Int,
        private val listner:IMyButtonCallBack
    ){
        private var pos:Int = 0
        private var clickRegion: RectF?=null
        private val resources:Resources

        init {
            resources = context.resources
        }

        fun onclick(x:Float,y:Float):Boolean{
            if (clickRegion != null && clickRegion!!.contains(x,y))
            {
                listner.onClick(pos)
                return true
            }
            return false
        }

        fun onDraw(c:Canvas,rectF: RectF,pos:Int){
            val  p=Paint()
            p.color = color
            p.textSize=textSize.toFloat()

            val  r = Rect()
            val cHeight = rectF.height()
            val cWidth = rectF.width()
            p.textAlign = Paint.Align.LEFT
            p.getTextBounds(text,0,text.length,r)

            var x=0f
            var y=0f
            if (imageResId ==0)
            {
                x = cWidth/2f - r.width()/2f - r.left.toFloat()
                x = cWidth/2f + r.height()/2f - r.bottom.toFloat()

                c.drawText(text,rectF.left+x,rectF.top+y,p)
                
            }
        }

    }


}