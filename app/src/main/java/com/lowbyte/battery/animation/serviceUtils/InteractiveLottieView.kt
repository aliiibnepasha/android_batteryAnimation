package com.lowbyte.battery.animation.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView

class InteractiveLottieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val lottieItems = mutableListOf<LottieItem>()
    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private var selectedItem: LottieItem? = null
    private var downX = 0f
    private var downY = 0f

    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            (250 * context.resources.displayMetrics.density).toInt()
        )
        setWillNotDraw(false)
        setBackgroundColor(Color.LTGRAY)
    }

    fun addLottieItem(animationRes: Int) {
        if (lottieItems.size >= 5) return

        val lottie = LottieAnimationView(context).apply {
            setAnimation(animationRes)
            playAnimation()
            repeatCount = 200
            layoutParams = LayoutParams(200, 200)
        }

        val item = LottieItem(lottie)
        lottieItems.add(item)
        addView(lottie)

        lottie.post {
            lottie.translationX = (width - lottie.width) / 2f
            lottie.translationY = (height - lottie.height) / 2f
            invalidate()
        }

        selectItem(item)
    }

    fun removeSelectedItem() {
        selectedItem?.let {
            removeView(it.view)
            lottieItems.remove(it)
            selectedItem = null
            invalidate()
        }
    }

    fun scaleSelectedItem(scale: Float) {
        selectedItem?.view?.apply {
            scaleX = scale
            scaleY = scale
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
        selectedItem = item
        bringChildToFront(item.view)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                for (item in lottieItems.reversed()) {
                    if (hitTest(item, event.x, event.y)) {
                        selectItem(item)
                        break
                    }
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
        selectedItem?.view?.let {
            val left = it.translationX
            val top = it.translationY
            val right = left + it.width * it.scaleX
            val bottom = top + it.height * it.scaleY
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    private data class LottieItem(val view: LottieAnimationView)
}