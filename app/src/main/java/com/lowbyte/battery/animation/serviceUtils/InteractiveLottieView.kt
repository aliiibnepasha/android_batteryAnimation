package com.lowbyte.battery.animation.custom

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.serviceUtils.LottieItem
import com.lowbyte.battery.animation.serviceUtils.LottieItemData
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
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
            (250 * context.resources.displayMetrics.density).toInt()
        )
        setWillNotDraw(false)
    }

    fun addLottieItem(animationRes: Int) {
        if (lottieItems.size >= 5) return

        val lottie = LottieAnimationView(context).apply {
            setAnimation(animationRes)
            playAnimation()
            repeatCount = 200
            layoutParams = LayoutParams(200, 200)
            setBackgroundColor(Color.TRANSPARENT)
        }

        val item = LottieItem(lottie, animationRes)
        lottieItems.add(item)
        addView(lottie)

        lottie.post {
            // Restore previous state if available
            val savedX = preferences.getFloat("${animationRes}_x", (width - lottie.width) / 2f)
            val savedY = preferences.getFloat("${animationRes}_y", (height - lottie.height) / 2f)
            val savedScale = preferences.getFloat("${animationRes}_scale", 1.0f)
            val savedRotation = preferences.getFloat("${animationRes}_rotation", 0f)

            lottie.scaleX = savedScale
            lottie.scaleY = savedScale
            lottie.rotation = savedRotation
            lottie.translationX = savedX
            lottie.translationY = savedY
            invalidate()
        }

        itemInteractionListener?.onItemCountChanged(lottieItems)
        selectItem(item)
      //  saveTransform(lottieItem)
        sendBroadcast(item)
        invalidate()

    }


    fun removeItemByResId(lottieItem: LottieItem) {
        removeView(lottieItem.view)
        lottieItems.remove(lottieItem)
        if (selectedItem == lottieItem) selectedItem = null
        itemInteractionListener?.onItemCountChanged(lottieItems)
        itemInteractionListener?.onItemSelected(null)
        saveTransform(lottieItem)
        sendBroadcast(lottieItem)
        invalidate()
    }


    fun removeSelectedItem() {
        selectedItem?.let {
            removeView(it.view)
            lottieItems.remove(it)
            itemInteractionListener?.onItemCountChanged(lottieItems)
            itemInteractionListener?.onItemSelected(null)
            selectedItem = null
            invalidate()
        }
    }


    fun scaleSelectedItem(scale: Float) {
        selectedItem?.let { item ->
            val view = item.view
            val centerX = view.translationX + view.width * view.scaleX / 2f
            val centerY = view.translationY + view.height * view.scaleY / 2f

            view.scaleX = scale
            view.scaleY = scale

            view.translationX = centerX - view.width * scale / 2f
            view.translationY = centerY - view.height * scale / 2f

            saveTransform(item)
            sendBroadcast(item)
            invalidate()
        }
    }

    fun rotateSelectedItem(angle: Float) {
        selectedItem?.let { item ->
            item.view.rotation = angle
            saveTransform(item)
            sendBroadcast(item)
            invalidate()
        }
    }

    fun moveSelectedItem(dx: Int, dy: Int) {
        selectedItem?.let { item ->
            val view = item.view
            val newX = (view.translationX + dx).coerceIn(0f, width - view.width * view.scaleX)
            val newY = (view.translationY + dy).coerceIn(0f, height - view.height * view.scaleY)
            view.translationX = newX
            view.translationY = newY

            saveTransform(item)
            sendBroadcast(item)
            invalidate()
        }
    }

    private fun saveTransform(item: LottieItem) {
        val resId = item.resId
        val view = item.view

        preferences.putFloat("${resId}_x", view.translationX)
        preferences.putFloat("${resId}_y", view.translationY)
        preferences.putFloat("${resId}_scale", view.scaleX)
        preferences.putFloat("${resId}_rotation", view.rotation)
    }

    private fun sendBroadcast(item: LottieItem) {
        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("resId", item.resId)
            putExtra("x", item.view.translationX)
            putExtra("y", item.view.translationY)
            putExtra("scale", item.view.scaleX)
            putExtra("rotation", item.view.rotation)
        }
        context.sendBroadcast(intent)
    }

    private fun selectItem(item: LottieItem) {
        selectedItem?.view?.setBackgroundColor(Color.TRANSPARENT)
        selectedItem = item
        bringChildToFront(item.view)
        item.view.setBackgroundResource(R.drawable.lottie_border)
        itemInteractionListener?.onItemSelected(item.resId)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y

                var itemHit = false
                for (item in lottieItems.reversed()) {
                    if (hitTest(item, event.x, event.y)) {
                        selectItem(item)
                        itemHit = true
                        break
                    }
                }

                if (!itemHit) {
                    selectedItem?.view?.setBackgroundColor(Color.TRANSPARENT)
                    selectedItem = null
                    itemInteractionListener?.onItemSelected(null)
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                selectedItem?.view?.let { view ->
                    val newX = (view.translationX + (event.x - downX)).coerceIn(
                        0f, width - view.width * view.scaleX
                    )
                    val newY = (view.translationY + (event.y - downY)).coerceIn(
                        0f, height - view.height * view.scaleY
                    )
                    view.translationX = newX
                    view.translationY = newY
                    downX = event.x
                    downY = event.y

                    saveTransform(selectedItem!!)
                    sendBroadcast(selectedItem!!)
                    invalidate()
                }
            }
        }
        return true
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
            repeatCount = 200
            layoutParams = LayoutParams(200, 200)
            setBackgroundColor(Color.TRANSPARENT)
            translationX = data.x
            translationY = data.y
            scaleX = data.scale
            scaleY = data.scale
            rotation = data.rotation
        }

        val item = LottieItem(lottie, data.resId)
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