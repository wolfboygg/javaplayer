package com.ggwolf.imagedeal

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/**
 * 一个自定义印章的view
 */

class CloneStampView @kotlin.jvm.JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val INIT_MODEL: Int = 1000
    val CLONE_MODEL: Int = 1001

    var CURRENT_MODEL = INIT_MODEL

    val PAINT_WIDTH = 100

    val backPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_1)
    val initArray: IntArray = IntArray(bitmap.width * bitmap.height)

    val srcRect = Rect()
    val dstRect = Rect()

    var drawBitmap = bitmap

    var oldSourcePoint: Point = Point()
    var oldDestPoint: Point = Point()

    var newSourcePoint: Point = Point()
    var newDestPoint: Point = Point()

    var newBitmap: Bitmap? = null


    init {
        var count = 0
        backPaint.strokeWidth = PAINT_WIDTH.toFloat()
        for (i in 0 until bitmap.height) {
            for (j in 0 until bitmap.width) {
                initArray[count] = bitmap.getPixel(j, i)
                count++
            }
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        srcRect.set(0, 0, w, h)
        dstRect.set(0, 0, w, h)
    }

    /**
     * 一个绘制就应该是一个事件序列的处理 down..move..up 结束，同是内存中应该有我们对应的像素值，用来进行回退处理
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                println("action_down:x.${event.x},y.${event.y}")
                if (CURRENT_MODEL == INIT_MODEL) {
                    CURRENT_MODEL = CLONE_MODEL
                    println("oldSourcePoint.x:${event.x},oldSourcePoint.y:${event.y}")
                    oldSourcePoint.x = event.x.toInt()
                    oldSourcePoint.y = event.y.toInt()
                    initNewBitmap()
                } else if (CURRENT_MODEL == CLONE_MODEL) {
                    // 开始进行拷贝 从源到目标位置
                    println("oldDestPoint.x:${event.x},oldDestPoint.y:${event.y}")
                    oldDestPoint.x = event.x.toInt()
                    oldDestPoint.y = event.y.toInt()
                }
            }
            MotionEvent.ACTION_UP -> {
                // 进行数据拷贝
//                CURRENT_MODEL = INIT_MODEL
//                cloneSourceToDest(oldSourcePoint, oldDestPoint)
            }
            MotionEvent.ACTION_MOVE -> {
                if (CURRENT_MODEL == CLONE_MODEL) {
                    println("move.x${event.x},move.y${event.y}")
                    newDestPoint.x = event.x.toInt()
                    newDestPoint.y = event.y.toInt()
                    // 判断移动的角度用来确定源的像素值
                    var distansX = (newDestPoint.x - oldDestPoint.x).toDouble()
                    var distansY = (newDestPoint.y - oldDestPoint.y).toDouble()

                    var pi_angle = atan(abs(distansY) / abs(distansX))

                    var userDistanX = cos(pi_angle) * sqrt(distansX.pow(2.toDouble()) + distansY.pow(2.toDouble()))
                    var userDistanY = sin(pi_angle) * sqrt(distansX.pow(2.toDouble()) + distansY.pow(2.toDouble()))

                    if (distansX < 0) {
                        userDistanX = -userDistanX
                    }
                    if (distansY < 0) {
                        userDistanY = -userDistanY
                    }

                    newSourcePoint.x = oldSourcePoint.x + userDistanX.toInt()
                    newSourcePoint.y = oldSourcePoint.y + userDistanY.toInt()

                    cloneSourceToDest(newSourcePoint, newDestPoint)
                }
            }
            else -> {
            }
        }
        return true
    }

    // 解决边界问题
    fun cloneSourceToDest(src: Point, dst: Point) {
        // 得到我们的矩形
        println("cloneSourceToDest....")
        var srcRect: Rect = Rect(src.x - PAINT_WIDTH, src.y - PAINT_WIDTH, src.x + PAINT_WIDTH, src.y + PAINT_WIDTH)
        var dstRect: Rect = Rect(dst.x - PAINT_WIDTH, dst.y - PAINT_WIDTH, dst.x + PAINT_WIDTH, dst.y + PAINT_WIDTH)

        if (srcRect.left <= 0) {
            srcRect.left = 0
        } else if (srcRect.right >= bitmap.width) {
            srcRect.right = bitmap.width - 1
        }

        if (srcRect.top <= 0) {
            srcRect.top = 0
        } else if (srcRect.bottom >= bitmap.height) {
            srcRect.bottom = bitmap.height - 1
        }

        if (dstRect.left <= 0) {
            dstRect.left = 0
        } else if (dstRect.right >= bitmap.width) {
            dstRect.right = bitmap.width - 1
        }

        if (dstRect.top <= 0) {
            dstRect.top = 0
        } else if (dstRect.bottom >= bitmap.height) {
            dstRect.bottom = bitmap.height - 1
        }



        for (i in 0 until min(srcRect.bottom - srcRect.top, dstRect.bottom - dstRect.top)) {
            for (j in 0 until min(srcRect.right - srcRect.left, dstRect.right - dstRect.left)) {
                newBitmap?.setPixel(dstRect.left + j, dstRect.top + i, bitmap.getPixel(srcRect.left + j, srcRect.top + i))
            }
        }
        if (newBitmap != null) {
            drawBitmap = newBitmap as Bitmap
        }
        invalidate()
    }

    fun initNewBitmap() {
        newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        var allCount = 0
        for (i in 0 until bitmap.height) {
            for (j in 0 until bitmap.width) {
                newBitmap?.setPixel(j, i, initArray[allCount])
                allCount++
            }
        }
    }

    fun replaceContainer() {
        // 进行像素值替换
        val needReplaceArray: IntArray = IntArray((bitmap.width / 2) * (bitmap.height / 2))
        var needCount = 0
        for (i in 0 until bitmap.height / 2) {
            for (j in 0 until bitmap.width / 2) {
                needReplaceArray[needCount] = bitmap.getPixel(j, i)
                needCount++
            }
        }

        val newBitmap: Bitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        var putcount = 0
        var allCount = 0
        for (i in 0 until bitmap.height) {
            for (j in 0 until bitmap.width) {
                if (i > bitmap.height / 2 && j < bitmap.width / 2) {
                    newBitmap.setPixel(j, i, needReplaceArray[putcount])
                    putcount++
                } else {
                    newBitmap.setPixel(j, i, initArray[allCount])
                }
                allCount++
            }
        }
        drawBitmap = newBitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(drawBitmap, srcRect, dstRect, backPaint) ?: null
    }

}