package com.example.drawingfun

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //drawing path
    private val drawPath = Path()
    //drawing and canvas paint
    private val drawPaint = Paint()
    private val canvasPaint = Paint(Paint.DITHER_FLAG)
    //initial color
    private var paintColor = -0x9a0000
    private var paintAlpha = 255
    //canvas
    private var drawCanvas: Canvas? = null
    //canvas bitmap
    private lateinit var canvasBitmap: Bitmap
    //brush sizes
    private var brushSize: Float = resources.getInteger(R.integer.medium_size).toFloat()
    //get and set last brush size
    var lastBrushSize: Float = brushSize
    //erase flag
    private var erase = false

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        //prepare for drawing and setup paint stroke properties
        drawPaint.color = paintColor
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = brushSize
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    //size assigned to view
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
    }

    //draw the view - will be called after touch event
    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(drawPath, drawPaint)
    }

    //register user touches as drawing action
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        //respond to down, move and up events
        when (event.action) {
            MotionEvent.ACTION_DOWN -> drawPath.moveTo(touchX, touchY)
            MotionEvent.ACTION_MOVE -> drawPath.lineTo(touchX, touchY)
            MotionEvent.ACTION_UP -> {
                drawPath.lineTo(touchX, touchY)
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
            }
            else -> return false
        }
        //redraw
        invalidate()
        return true
    }

    fun setColor(newColor: String) {
        invalidate()
        //check whether color value or pattern name
        if (newColor.startsWith("#")) {
            paintColor = Color.parseColor(newColor)
            drawPaint.color = paintColor
            drawPaint.shader = null
        } else {
            //pattern
            val patternID = resources.getIdentifier(
                newColor, "drawable", "com.example.drawingfun"
            )
            //decode
            val patternBMP = BitmapFactory.decodeResource(resources, patternID)
            //create shader
            val patternBMPshader = BitmapShader(
                patternBMP,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT
            )
            //color and shader
            drawPaint.color = -0x1
            drawPaint.shader = patternBMPshader
        }
    }

    fun setBrushSize(newSize: Float) {
        val pixelAmount = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics
        )
        brushSize = pixelAmount
        drawPaint.strokeWidth = brushSize
    }

    fun setErase(isErase: Boolean) {
        erase = isErase
        if (erase)
            drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        else
            drawPaint.xfermode = null
    }

    fun startNew() {
        drawCanvas?.drawColor(0, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun getPaintAlphaPercent() = Math.round(paintAlpha.toFloat() / 255 * 100)

    fun setPaintAlphaPercent(newAlpha: Int) {
        paintAlpha = Math.round(newAlpha.toFloat() / 100 * 255)
        drawPaint.color = paintColor
        drawPaint.alpha = paintAlpha
    }
}
