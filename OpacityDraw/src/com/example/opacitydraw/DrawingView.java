package com.example.opacitydraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * This is demo code to accompany the Mobiletuts+ tutorial series:
 * - Android SDK: Using Opacity in Drawing Apps
 * 
 * Sue Smith
 * September 2013
 *
 */
public class DrawingView extends View {

	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFFFF0000, paintAlpha = 255;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;

	//constructor
	public DrawingView(Context context, AttributeSet attrs){
		super(context, attrs);
		setupDrawing();
	}

	//prepare drawing
	private void setupDrawing(){
		drawPath = new Path();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(50);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	//view assigned size
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}

	//draw view
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}

	//respond to touch interaction
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();
		//respond to down, move and up events
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			drawPath.moveTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			drawPath.lineTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_UP:
			drawPath.lineTo(touchX, touchY);
			drawCanvas.drawPath(drawPath, drawPaint);
			drawPath.reset();
			break;
		default:
			return false;
		}
		//redraw
		invalidate();
		return true;
	}

	//return current alpha
	public int getPaintAlpha(){
		return Math.round((float)paintAlpha/255*100);
	}

	//set alpha
	public void setPaintAlpha(int newAlpha){
		paintAlpha=Math.round((float)newAlpha/100*255);
		drawPaint.setColor(paintColor);
		drawPaint.setAlpha(paintAlpha);
	}
}
