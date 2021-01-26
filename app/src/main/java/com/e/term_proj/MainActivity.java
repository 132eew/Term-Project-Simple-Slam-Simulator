package com.e.term_proj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{

    public DrawingView dv ;
    public Paint mPaint;
    public int circle_size = 10;
    public static Bitmap  mBitmap;
    public static Bitmap saved_view = null;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dv = new DrawingView(this);
        setContentView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(circle_size);

        TimerTask task = new TimerTask() {
            Handler hd = new Handler();
            public void run(){
                hd.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int value;
                        value = DeviceOpen();

                        if(value != -1){
                            value = ReceivePushSwitchValue();

                            if(value == 1){
                                circle_size -= 2;
                                if(circle_size <0){
                                    circle_size = 2;
                                }
                                mPaint.setStrokeWidth(circle_size);
                            }

                            if(value == 2){
                                circle_size += 2;
                                if(circle_size > 20){
                                    circle_size = 20;
                                }
                                mPaint.setStrokeWidth(circle_size);
                            }

                            if(value == 8){
                                mPaint.setColor(Color.BLACK);
                            }

                            if(value == 16){
                                mPaint.setColor(Color.WHITE);
                            }

                            if(value == 32){
                                save_view();
                            }
                        }

                        if(value != -1){
                            DeviceClose();
                        }

                        String str = Integer.toString(value,16);
                    }
                },100);
            }
        };

        Timer t = new Timer();
        t.schedule(task,100,100);
    }

    public class DrawingView extends View {

        public int width;
        public  int height;

        private Canvas  mCanvas;
        private Path    mPath;
        private Paint   mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
            setDrawingCacheEnabled(true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;

            System.out.println(mX);
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            mCanvas.drawPath(mPath,  mPaint);
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    public void save_view(){
        saved_view = mBitmap;

        Intent screen_change = new Intent(getApplicationContext(), SLAM.class);
        startActivity(screen_change);
    }

    public native int DeviceOpen();
    public native int DeviceClose();
    public native int ReceivePushSwitchValue();

}