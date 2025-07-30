package com.lowbyte.battery.animation.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener

class InteractiveLottieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val lottieItems = mutableListOf<LottieItem>()
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
            setBackgroundColor(Color.TRANSPARENT) // Change to Color.TRANSPARENT if needed
            pivotX = 100f  // half of width
            pivotY = 100f  // half of height
        }

        val item = LottieItem(lottie, animationRes)
        lottieItems.add(item)
        addView(lottie)

        lottie.post {
            lottie.pivotX = lottie.width / 2f
            lottie.pivotY = lottie.height / 2f

            lottie.translationX = (width - lottie.width) / 2f
            lottie.translationY = (height - lottie.height) / 2f
            invalidate()
        }
        itemInteractionListener?.onItemCountChanged(lottieItems.size)

        selectItem(item)
    }

    fun removeSelectedItem() {
        selectedItem?.let {
            removeView(it.view)
            lottieItems.remove(it)
            itemInteractionListener?.onItemCountChanged(lottieItems.size)
            itemInteractionListener?.onItemSelected(null)
            selectedItem = null
            invalidate()
        }
    }

    fun scaleSelectedItem(scale: Float) {
        selectedItem?.view?.let { view ->
            // Get current visual center
            val centerX = view.translationX + view.width * view.scaleX / 2f
            val centerY = view.translationY + view.height * view.scaleY / 2f

            // Set scale
            view.scaleX = scale
            view.scaleY = scale

            // Recompute translation so center remains fixed
            view.translationX = centerX - view.width * scale / 2f
            view.translationY = centerY - view.height * scale / 2f
        }
        invalidate()
    }


    fun rotateSelectedItem(angle: Float) {
        selectedItem?.view?.rotation = angle
        invalidate()
    }

    fun moveSelectedItem(dx: Int, dy: Int) {
        selectedItem?.view?.let { view ->
            val newX = (view.translationX + dx).coerceIn(0f, (width - view.width * view.scaleX))
            val newY = (view.translationY + dy).coerceIn(0f, (height - view.height * view.scaleY))
            view.translationX = newX
            view.translationY = newY
            invalidate()
        }
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

                // If touch was outside all items, clear selection

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
                        0f, (width - view.width * view.scaleX)
                    )
                    val newY = (view.translationY + (event.y - downY)).coerceIn(
                        0f, (height - view.height * view.scaleY)
                    )
                    view.translationX = newX
                    view.translationY = newY
                    downX = event.x
                    downY = event.y
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

    fun removeItemByResId(resId: Int) {
        val item = lottieItems.find { it.resId == resId } ?: return
        removeView(item.view)
        lottieItems.remove(item)
        if (selectedItem == item) selectedItem = null
        itemInteractionListener?.onItemCountChanged(lottieItems.size)
        itemInteractionListener?.onItemSelected(null)
        invalidate()
    }
    private data class LottieItem(val view: LottieAnimationView, val resId: Int)}