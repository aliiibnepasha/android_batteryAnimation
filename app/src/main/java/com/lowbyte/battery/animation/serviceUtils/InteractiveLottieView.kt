package com.lowbyte.battery.animation.custom

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.serviceUtils.LottieItem
import com.lowbyte.battery.animation.serviceUtils.LottieItemData
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION_REMOVE
import com.lowbyte.battery.animation.utils.AppPreferences

class InteractiveLottieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val lottieItems = mutableListOf<LottieItem>()
    private val preferences = AppPreferences.getInstance(context)

    var itemInteractionListener: OnItemInteractionListener? = null
    private var selectedItem: LottieItem? = null
    private var downX = 0f
    private var downY = 0f

    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            (180 * context.resources.displayMetrics.density).toInt()
        )
        setWillNotDraw(false)
        layoutDirection = LAYOUT_DIRECTION_LTR
    }

    fun addLottieItem(animationRes: Int): Boolean {
        if (lottieItems.size >= 5) {
            Log.d("InteractiveLottieView", "Cannot add more items. Limit reached.")
            return false
        }

        val lottie = LottieAnimationView(context).apply {
            setAnimation(animationRes)
            playAnimation()
            repeatCount = LottieDrawable.INFINITE
            layoutParams = LayoutParams(200, 200)
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
            isFocusable = false
        }

        val item = LottieItem(lottie, animationRes)

//        lottie.setOnClickListener {
//            selectItem(item)
//            Log.d("Clicked", "Lottie ${item.resId}")
//        }

        lottieItems.add(item)
        addView(lottie)

        lottie.viewTreeObserver.addOnGlobalLayoutListener {
            val savedX = preferences.getFloat("${animationRes}_x", -1f)
            val savedY = preferences.getFloat("${animationRes}_y", -1f)
            val savedScale = preferences.getFloat("${animationRes}_scale", 1.0f)
            val savedRotation = preferences.getFloat("${animationRes}_rotation", 0f)

            lottie.scaleX = savedScale
            lottie.scaleY = savedScale
            lottie.rotation = savedRotation

            val defaultCenterX = (this.width - lottie.width * savedScale) / 2f
            val defaultCenterY = (this.height - lottie.height * savedScale) / 2f

            val finalX = if (savedX == -1f) defaultCenterX else savedX
            val finalY = if (savedY == -1f) defaultCenterY else savedY

            // ✅ Correct usage: use saved or default
            lottie.translationX = if (finalX== 0.0.toFloat()) defaultCenterX else finalX
            lottie.translationY = if (finalY== 0.0.toFloat()) defaultCenterY else finalY

            Log.d("LottiePlacement", "Placed Lottie ID: $animationRes at X: $finalX, Y: $finalY")

            invalidate()
        }

        itemInteractionListener?.onItemCountChanged(lottieItems)
        selectItem(item,"addLottieItem")
        saveTransform(item)
        sendBroadcast(item = item, isEditing = true)
        invalidate()

        return true
    }


    fun removeItemByResId(resId: Int) {
        val itemToRemove = lottieItems.find { it.resId == resId } ?: return
        Log.d("LottieView", "Trying to remove item with resId: $resId")
        Log.d("LottieView", "Children before: $childCount")

        try {
            itemToRemove.view.apply {
                cancelAnimation()
                clearAnimation()
                removeAllAnimatorListeners()
                setImageDrawable(null)
            }

            if (itemToRemove.view.parent != null) {
                removeView(itemToRemove.view)
            }
        } catch (e: Exception) {
            Log.e("LottieView", "Error removing view", e)
        }

        Log.d("LottieView", "Children after: $childCount")

        lottieItems.remove(itemToRemove)

        // Clear selection if the removed item was selected
        if (selectedItem == itemToRemove) {
            selectedItem = null
            itemInteractionListener?.onItemSelected(null)
        }

        itemInteractionListener?.onItemCountChanged(lottieItems)
        sendBroadcast(itemToRemove, BROADCAST_ACTION_REMOVE, true)
        requestLayout()
        invalidate()
        Log.d("LottieView", "Final Log - Item Removed")
    }

    fun removeSelectedItem() {
        selectedItem?.let {
            removeView(it.view)
            lottieItems.remove(it)
            itemInteractionListener?.onItemCountChanged(lottieItems)
            itemInteractionListener?.onItemSelected(null)
            selectedItem = null
            invalidate()
          //  saveTransform(itemToRemove)
            sendBroadcast(it, BROADCAST_ACTION_REMOVE, true)
            requestLayout()
            invalidate()
            // BROADCAST_ACTION_REMOVE
        }
    }


    fun scaleSelectedItem(scale: Float) {
        selectedItem?.let { item ->
            val view = item.view
            val oldScale = view.scaleX
            val centerX = view.translationX + view.width * oldScale / 2f
            val centerY = view.translationY + view.height * oldScale / 2f

            view.scaleX = scale
            view.scaleY = scale

            view.translationX = centerX - view.width * scale / 2f
            view.translationY = centerY - view.height * scale / 2f

            // Ensure the item stays within bounds after scaling
            val scaledWidth = view.width * scale
            val scaledHeight = view.height * scale
            
            view.translationX = view.translationX.coerceIn(0f, width - scaledWidth)
            view.translationY = view.translationY.coerceIn(0f, height - scaledHeight)

            saveTransform(item)
            sendBroadcast(item = item, isEditing = true)
            invalidate()
        }
    }

    fun rotateSelectedItem(angle: Float) {
        selectedItem?.let { item ->
            item.view.rotation = angle
            saveTransform(item)
            sendBroadcast(item = item, isEditing = true)
            invalidate()
        }
    }

    fun moveSelectedItem(dx: Int, dy: Int) {
        selectedItem?.let { item ->
            val view = item.view
            val scaledWidth = view.width * view.scaleX
            val scaledHeight = view.height * view.scaleY
            
            val newX = (view.translationX + dx).coerceIn(0f, width - scaledWidth)
            val newY = (view.translationY + dy).coerceIn(0f, height - scaledHeight)
            view.translationX = newX
            view.translationY = newY

            saveTransform(item)
            sendBroadcast(item = item, isEditing = true)
            invalidate()
        }
    }


    fun getItemByResId(resId: Int): LottieItem? {
        return lottieItems.find { it.resId == resId }
    }

    fun getSelectedItem(): LottieItem? {
        return selectedItem
    }

    fun getSelectedItemResId(): Int? {
        return selectedItem?.resId
    }
    private fun saveTransform(item: LottieItem) {
        val resId = item.resId
        val view = item.view

        preferences.putFloat("${resId}_x", view.translationX)
        preferences.putFloat("${resId}_y", view.translationY)
        preferences.putFloat("${resId}_scale", view.scaleX)
        preferences.putFloat("${resId}_rotation", view.rotation)
    }


    private fun sendBroadcast(
        item: LottieItem,
        action: String = BROADCAST_ACTION,
        isEditing: Boolean
    ) {
        val view = item.view
        val intent = Intent(action).apply {
            putExtra("isEditing", isEditing)
            putExtra("resId", item.resId)
            putExtra("x", view.translationX)
            putExtra("y", view.translationY)
            putExtra("scale", view.scaleX)
            putExtra("rotation", view.rotation)
        }
        context.sendBroadcast(intent)
    }

    fun selectItem(item: LottieItem , tag: String) {
        Log.d("InteractiveLottieView", "Item selected:$tag ${item.resId}")
        // Clear previous selection
        selectedItem?.view?.setBackgroundColor(Color.TRANSPARENT)
        
        selectedItem = item
        bringChildToFront(item.view)
        
        // Set selection border
        try {
            item.view.setBackgroundResource(R.drawable.lottie_border)
        } catch (e: Exception) {
            // Fallback to color if drawable not found
            item.view.setBackgroundColor(Color.YELLOW)
        }
        
        itemInteractionListener?.onItemSelected(item.resId)
        sendBroadcast(item = item, isEditing = true)
        invalidate()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y
                
                // Check if touch is on any Lottie item
                for (item in lottieItems.reversed()) {
                    if (hitTest(item, x, y)) {
                        selectItem(item,"onInterceptTouchEvent")
                        Log.d("onTouchEvent", "lottieItems $x  / $y")

                        return false // Let child handle it
                    }
                }
                // Touch is outside any item - consume it
                deselectItem()
                return true
            }
        }
        return false
    }


    private fun deselectItem() {
        selectedItem?.view?.setBackgroundColor(Color.TRANSPARENT)
        selectedItem = null
        itemInteractionListener?.onItemSelected(null)
        invalidate()
    }
    private var dragging = false




    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
      //  if (!isEditing) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            for (item in lottieItems.reversed()) {
                if (hitTest(item, x, y)) {
                    selectItem(item, "dispatchTouchEvent")

                    downX = x
                    downY = y
                    dragging = true

                    Log.d("TouchEvent", "Selected ${item.resId} at X:$x Y:$y → Start drag")
                    return super.dispatchTouchEvent(event) // let it go to onTouchEvent
                }
            }

            deselectItem()
            dragging = false
            return true // consumed, no dragging
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        Log.d("onTouchEvent", "onTouchEvent $x  / $y")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                downX = x
                downY = y
                Log.d("onTouchEvent", "ACTION_DOWN $downX / $downY")
                if (selectedItem?.let {


                        Log.d("onTouchEvent", "hitTest $x / $y")
                        hitTest(it, x, y)


                    } == true) {
                    dragging = true
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d("onTouchEvent", "ACTION_MOVE")

                if (dragging && selectedItem != null) {

                    val view = selectedItem!!.view
                    val dx = x - downX
                    val dy = y - downY

                    Log.d("onTouchEvent", " if ACTION_MOVE $dx / $dy")
                    val scaledWidth = view.width * view.scaleX
                    val scaledHeight = view.height * view.scaleY

                    val newX = (view.translationX + dx).coerceIn(0f, width - scaledWidth)
                    val newY = (view.translationY + dy).coerceIn(0f, height - scaledHeight)

                    Log.d("onTouchEvent", " if view translate  $newX / $newY")

                    view.translationX = newX
                    view.translationY = newY

                    downX = x
                    downY = y

                    saveTransform(selectedItem!!)
                    sendBroadcast(item = selectedItem!!, isEditing = true)
                    invalidate()
                    return true
                }else{
                    Log.d("onTouchEvent", " else  ACTION_MOVE")

                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d("onTouchEvent", " else  ACTION_CANCEL  ACTION_UP")

                dragging = false
            }
        }

        return true // Always consume the event to prevent system UI interaction
    }
    private fun hitTest(item: LottieItem, x: Float, y: Float): Boolean {
        val view = item.view
        val left = view.translationX
        val top = view.translationY
        val right = left + view.width * view.scaleX
        val bottom = top + view.height * view.scaleY
        return x in left..right && y in top..bottom
    }




    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }



    fun containsItem(resId: Int): Boolean {
        return lottieItems.any { it.resId == resId }
    }

    fun updateItemTransform(resId: Int, x: Float, y: Float, scale: Float, rotation: Float) {
        val item = lottieItems.find { it.resId == resId } ?: return
        val view = item.view

        view.translationX = x
        view.translationY = y
        view.scaleX = scale
        view.scaleY = scale
        view.rotation = rotation

        invalidate()
    }
    fun addLottieItemFromData(data: LottieItemData) {
        if (containsItem(data.resId)) return

        val lottie = LottieAnimationView(context).apply {
            setAnimation(data.resId)
            playAnimation()
            repeatCount = LottieDrawable.INFINITE
            layoutParams = LayoutParams(200, 200)
            setBackgroundColor(Color.TRANSPARENT)
            translationX = data.x
            translationY = data.y
            scaleX = data.scale
            scaleY = data.scale
            rotation = data.rotation
            isClickable = false
            isFocusable = false
        }

        val item = LottieItem(lottie, data.resId)

//        lottie.setOnClickListener {
//            selectItem(item)
//            Log.d("Clicked","Lottie ${item.resId}")
//        }

        lottieItems.add(item)
        addView(lottie)
        itemInteractionListener?.onItemCountChanged(lottieItems)
        invalidate()
    }
    fun getCurrentLottieItemData(): List<LottieItemData> {
        return lottieItems.map { item ->
            LottieItemData(
                resId = item.resId,
                x = item.view.translationX,
                y = item.view.translationY,
                scale = item.view.scaleX,
                rotation = item.view.rotation
            )
        }
    }

    fun loadLottieItemsFromPrefs() {
        val savedItems = preferences.getLottieItemList("lottie_item_list")
        savedItems.forEach { data ->
            addLottieItemFromData(data)
        }
    }


    fun saveAllLottieItemsToPrefs() {
        val dataList = lottieItems.map { item ->
            LottieItemData(
                resId = item.resId,
                x = item.view.translationX,
                y = item.view.translationY,
                scale = item.view.scaleX,
                rotation = item.view.rotation
            )
        }
        preferences.putLottieItemList("lottie_item_list", dataList)
    }
}