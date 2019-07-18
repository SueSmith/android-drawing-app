package com.example.drawingfun

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brush_chooser.*
import kotlinx.android.synthetic.main.opacity_chooser.*
import java.util.*

class MainActivity : Activity() {

    companion object {
        const val DEFAULT_100_PERCENT = 100
    }

    private var currentPaint: ImageButton? = null

    private val smallBrushSize by lazy { resources.getInteger(R.integer.small_size).toFloat() }
    private val mediumBrushSize by lazy { resources.getInteger(R.integer.medium_size).toFloat() }
    private val largeBrushSize by lazy { resources.getInteger(R.integer.large_size).toFloat() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get the palette and first color button
        currentPaint = paint_colors?.children?.first() as ImageButton
        currentPaint?.setImageResource(R.drawable.paint_pressed)

        draw_button?.setOnClickListener { showDrawSizeDialog() }
        erase_button?.setOnClickListener { showEraseSizeDialog() }
        new_button?.setOnClickListener { showNewDialog() }
        save_button?.setOnClickListener { showSaveDialog() }
        opacity_button?.setOnClickListener { showOpacityDialog() }

        setBrushSize(mediumBrushSize)
    }

    private fun showDrawSizeDialog() {
        //draw button clicked
        Dialog(this).apply {
            setTitle("Brush size:")
            setContentView(R.layout.brush_chooser)
            //listen for clicks on size buttons
            small_brush_button?.setOnClickListener {
                setBrushSize(smallBrushSize)
                dismiss()
            }
            medium_brush_button?.setOnClickListener {
                setBrushSize(mediumBrushSize)
                dismiss()
            }
            large_brush_button?.setOnClickListener {
                setBrushSize(largeBrushSize)
                dismiss()
            }
            //show and wait for user interaction
            show()
        }
    }

    private fun setBrushSize(brushSize: Float) {
        drawing_view?.setErase(false)
        drawing_view?.setBrushSize(brushSize)
        drawing_view?.lastBrushSize = brushSize
    }

    private fun showEraseSizeDialog() {
        //switch to erase - choose size
        Dialog(this).apply {
            setTitle("Eraser size:")
            setContentView(R.layout.brush_chooser)
            //size buttons
            small_brush_button?.setOnClickListener {
                setEraseSize(smallBrushSize)
                dismiss()
            }
            medium_brush_button?.setOnClickListener {
                setEraseSize(mediumBrushSize)
                dismiss()
            }
            large_brush_button?.setOnClickListener {
                setEraseSize(largeBrushSize)
                dismiss()
            }
            show()
        }
    }

    private fun setEraseSize(brushSize: Float) {
        drawing_view?.setErase(true)
        drawing_view?.setBrushSize(brushSize)
    }

    private fun showNewDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("New drawing")
            setMessage("Start new drawing (you will lose the current drawing)?")
            setPositiveButton("Yes") { dialog, _ ->
                drawing_view?.startNew()
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun showSaveDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Save drawing")
            setMessage("Save drawing to device Gallery?")
            setPositiveButton("Yes") { _, _ -> saveDrawing() }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun saveDrawing() {
        //save drawing
        drawing_view?.isDrawingCacheEnabled = true
        //attempt to save
        val imgSaved = if (isStoragePermissionGranted()) {
            MediaStore.Images.Media.insertImage(
                contentResolver, drawing_view?.drawingCache,
                UUID.randomUUID().toString() + ".png", "drawing"
            )
        } else {
            null
        }
        //feedback
        if (imgSaved != null) {
            Toast.makeText(
                this,
                "Drawing saved to Gallery!", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Oops! Image could not be saved.", Toast.LENGTH_SHORT
            ).show()
        }
        drawing_view?.destroyDrawingCache()
    }

    private fun isStoragePermissionGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else {
            true
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            saveDrawing()
        }
    }

    private fun showOpacityDialog() {
        //launch opacity chooser
        Dialog(this).apply {
            setTitle("Opacity level:")
            setContentView(R.layout.opacity_chooser)
            //set max
            opacity_seek?.max = DEFAULT_100_PERCENT
            //show current level
            val currLevel = getPaintAlphaPercent()
            opacity_percentage_text?.text = String.format("%d%%", currLevel)
            opacity_seek?.progress = currLevel
            //update as user interacts
            opacity_seek?.setOnSeekBarChangeListener(onProgressChanged = { _, progress, _ ->
                this@apply.opacity_percentage_text?.text = String.format("%d%%", progress)
            })
            //listen for clicks on ok
            opacity_ok_button?.setOnClickListener {
                setPaintAlphaPercent(this)
                dismiss()
            }
            //show dialog
            show()
        }
    }

    private fun getPaintAlphaPercent() = drawing_view?.getPaintAlphaPercent() ?: DEFAULT_100_PERCENT

    private fun setPaintAlphaPercent(dialog: Dialog) {
        drawing_view?.setPaintAlphaPercent(dialog.opacity_seek?.progress ?: DEFAULT_100_PERCENT)
    }

    fun paintClicked(view: View) {
        drawing_view?.setErase(false)
        drawing_view?.setPaintAlphaPercent(DEFAULT_100_PERCENT)
        drawing_view?.setBrushSize(drawing_view?.lastBrushSize ?: mediumBrushSize)

        //use chosen color
        if (view !== currentPaint) {
            val imgView = view as ImageButton
            val color = view.getTag().toString()
            drawing_view?.setColor(color)
            //update ui
            imgView.setImageResource(R.drawable.paint_pressed)
            currentPaint?.setImageResource(R.drawable.paint)
            currentPaint = view
        }
    }

    private fun SeekBar.setOnSeekBarChangeListener(
        onProgressChanged: (seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit = { _, _, _ -> },
        onStartTrackingTouch: (seekBar: SeekBar) -> Unit = {},
        onStopTrackingTouch: (seekBar: SeekBar) -> Unit = {}
    ) {
        this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onProgressChanged(seekBar, progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onStartTrackingTouch(seekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onStopTrackingTouch(seekBar)
            }
        })
    }
}
