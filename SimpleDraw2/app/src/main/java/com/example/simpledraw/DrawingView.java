package com.example.simpledraw;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

import androidx.constraintlayout.solver.widgets.Rectangle;

import static com.example.simpledraw.DrawingView.Tool.PATH;

public class DrawingView extends View {

    private final int RECURSIVE_DEPTH = 5;

    //drawing path
    private Path drawPath, trianglePath;

    float x1, y1, x2, y2, x3, y3, cx, cy, radius, height, width;

    RectF rect;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize, lastBrushSize;

    private boolean erase=false;

    private Tool tool = PATH;


    public enum Tool {
        PATH,
        LINE,
        CIRCLE,
        RECTANGLE,
        FRACTAL
    }

    // Constructor
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        drawPath = new Path();

        trianglePath = new Path();

        x1 = x2 = y1 = y2 = cx = cy = radius = height = width = 0;

        rect = new RectF();

        drawPaint = new Paint();

        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setBrushSize(float newSize){
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }

    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public void setErase(boolean isErase){
        //set erase true or false
        erase=isErase;
        if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        switch (tool){
            case PATH:
                canvas.drawPath(drawPath, drawPaint);
                break;
            case LINE:
                canvas.drawLine(x1, y1, x2, y2, drawPaint);
                break;
            case CIRCLE:
                canvas.drawCircle(cx,cy,radius,drawPaint);
                break;
            case RECTANGLE:
                canvas.drawRect(x1, y1, x2, y2, drawPaint);
                break;
            case FRACTAL:
                setBrushSize((float) 2);
                sierpinski( x1, y1, x1+220, y1+260, x1-220, y1+260, RECURSIVE_DEPTH, drawCanvas);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                x1 = touchX;
                y1 = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                x2 = touchX;
                y2 = touchY;
                cx = (x1 + x2 )/2;
                cy = (y1 + y2 )/2;
                radius = (float) Math.sqrt(((x1 - cx)*(y1 - cy)) + ((y1 - cy)*(y1 - cy)));
                break;
            case MotionEvent.ACTION_UP:
                switch (tool){
                    case PATH:
                        drawCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                        break;
                    case LINE:
                        drawCanvas.drawLine(x1, y1, x2, y2, drawPaint);
                        break;
                    case CIRCLE:
                        drawCanvas.drawCircle(cx,cy,radius,drawPaint);
                        break;
                    case RECTANGLE:
                        drawCanvas.drawRect(x1, y1, x2, y2, drawPaint);
                        break;
                    case FRACTAL:
                        setBrushSize((float) 2);
                        sierpinski( x1, y1, x1+220, y1+260, x1-220, y1+260, RECURSIVE_DEPTH, drawCanvas);
                        break;
                }

                break;
            default:
                return false;
        }

        //Calling invalidate will cause the onDraw method to execute

        invalidate();
        return true;
    }

    public void sierpinski(float x1, float y1, float x2, float y2, float x3, float y3, int depth, Canvas c) {
        // draw the triangle specified by (x1, y1), (x2, y2), and (x3, y3)
        if (depth > 0) // stops recursion
        {
            depth--;
            Path triangle = new Path();

            triangle.moveTo(x1, y1);
            triangle.lineTo(x2, y2);
            triangle.moveTo(x2, y2);
            triangle.lineTo(x3, y3);
            triangle.moveTo(x3, y3);
            triangle.lineTo(x1, y1);
            triangle.close();
            c.drawPath(triangle, drawPaint);

            sierpinski(x1, y1, (x1 + x2) / 2, (y1 + y2) / 2, (x1 + x3) / 2, (y1 + y3) / 2, depth, c);

            sierpinski((x1 + x2) / 2, (y1 + y2) / 2, x2, y2, (x3 + x2) / 2, (y3 + y2) / 2, depth, c);

            sierpinski((x1 + x3) / 2, (y1 + y3) / 2, (x3 + x2) / 2, (y3 + y2) / 2, x3, y3,depth, c);
        }
    }

    public void setColor(String newColor){

        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }



}
