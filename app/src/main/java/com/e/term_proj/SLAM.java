package com.e.term_proj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class SLAM extends Activity {

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    public Bitmap Object_map = null;
    public Bitmap SLAM_map = null;
    ImageView SLAM;
    ImageView bot;
    Vibrator mVibe;
    Button re;
    Button save;

    public float prev_x = 0;
    public float prev_y = 0;
    public float curnt_x = 0;
    public float curnt_y = 0;
    public int prev_deg = 0;
    public int curnt_deg = 0;

    public int object_value = 0;

    public int bot_move = 0;

    String root = Environment.getExternalStorageDirectory().toString() + "/SLAM";

    calc_distance Calc_Distance;
    draw_map Draw_Map;

    static {
        System.loadLibrary("native-lib");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slam);

        Object_map = MainActivity.saved_view;

        SLAM = (ImageView) findViewById(R.id.SLAM);
        bot = (ImageView) findViewById(R.id.bot);
        mVibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        int map_width = Object_map.getWidth();
        int map_height = Object_map.getHeight();

        SLAM_map = Bitmap.createBitmap(map_width, map_height, Bitmap.Config.ARGB_8888);

        SLAM.setImageBitmap(SLAM_map);

        final Bitmap source = BitmapFactory.decodeResource(this.getResources(), R.drawable.tri);

        prev_x = (float) map_width/2;
        prev_y = map_height - 60;
        System.out.println(prev_x);
        System.out.println(prev_y);


        int radious = 1;
        int deg = 0;
        float[] init_xy = new float[2];

        while(true){
            object_value = get_pixel(prev_x, prev_y);
            System.out.println(object_value);
            if(object_value == 1){
                init_xy = position_rotate(prev_x,(prev_y-radious),deg);

                prev_x = init_xy[0];
                prev_y = init_xy[1];

                if(prev_x >= map_width)prev_x = map_width-1;
                if(prev_x <= 0)prev_x = 0;
                if(prev_y >= map_height)prev_y = map_height-1;
                if(prev_y <= 0)prev_y = 0;

                deg += deg;
                if(deg == 361){
                    deg = 0;
                    radious += 1;
                }
            }
            else{
                break;
            }
        }

        bot.setX(prev_x);
        bot.setY(prev_y);

        Calc_Distance = new calc_distance();
        Calc_Distance.start();

        Draw_Map = new draw_map();
        Draw_Map.start();

        SLAM.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(bot_move == 1){

                    int radious = 1;
                    int deg = 0;
                    float[] init_xy = new float[2];
                    prev_x = event.getX();
                    prev_y = event.getY();

                    while(true){
                        object_value = get_pixel(prev_x, prev_y);
                        if(object_value == 1){

                            init_xy = position_rotate(prev_x,(prev_y-radious),deg);

                            prev_x = init_xy[0];
                            prev_y = init_xy[1];

                            int map_width = Object_map.getWidth();
                            int map_height = Object_map.getHeight();
                            if(prev_x >= map_width)prev_x = map_width-1;
                            if(prev_x <= 0)prev_x = 0;
                            if(prev_y >= map_height)prev_y = map_height-1;
                            if(prev_y <= 0)prev_y = 0;

                            deg += deg;
                            if(deg == 361){
                                deg = 0;
                                radious += 1;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    bot.setX(prev_x);
                    bot.setY(prev_y);

                    bot_move = 0;
                }
                return false;
            }
        });

        re = (Button) findViewById(R.id.re);
        re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bot_move = 1;
            }
        });

        ((Button)findViewById(R.id.up)).setOnTouchListener(new RepeatListener(400, 50, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curnt_x = prev_x;
                curnt_y = prev_y;

                float[] rot_xy = new float[2];
                rot_xy = position_rotate(0,-1,curnt_deg);
                curnt_x = curnt_x + rot_xy[0];
                curnt_y = curnt_y + rot_xy[1];

                int map_width = Object_map.getWidth();
                int map_height = Object_map.getHeight();
                if(curnt_x >= map_width)curnt_x = map_width-1;
                if(curnt_x <= 0)curnt_x = 0;
                if(curnt_y >= map_height)curnt_y = map_height-1;
                if(curnt_y <= 0)curnt_y = 0;

                object_value = get_pixel(curnt_x, curnt_y);
                if(object_value == 1){
                    curnt_x = curnt_x - rot_xy[0];
                    curnt_y = curnt_y - rot_xy[1];
                }

                bot.setX(curnt_x);
                bot.setY(curnt_y);
                prev_x = curnt_x;
                prev_y = curnt_y;
            }
        }));

        ((Button)findViewById(R.id.down)).setOnTouchListener(new RepeatListener(400, 50, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curnt_x = prev_x;
                curnt_y = prev_y;

                float[] rot_xy = new float[2];
                rot_xy = position_rotate(0,1,curnt_deg);
                curnt_x = curnt_x + rot_xy[0];
                curnt_y = curnt_y + rot_xy[1];

                int map_width = Object_map.getWidth();
                int map_height = Object_map.getHeight();
                if(curnt_x >= map_width)curnt_x = map_width-1;
                if(curnt_x <= 0)curnt_x = 0;
                if(curnt_y >= map_height)curnt_y = map_height-1;
                if(curnt_y <= 0)curnt_y = 0;

                object_value = get_pixel(curnt_x, curnt_y);
                if(object_value == 1){
                    curnt_x = curnt_x - rot_xy[0];
                    curnt_y = curnt_y - rot_xy[1];
                }

                bot.setX(curnt_x);
                bot.setY(curnt_y);
                prev_x = curnt_x;
                prev_y = curnt_y;
            }
        }));

        ((Button)findViewById(R.id.left)).setOnTouchListener(new RepeatListener(400, 50, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                curnt_x = prev_x;
                curnt_y = prev_y;

                curnt_deg = prev_deg - 1;
                RotateBitmap(source, curnt_deg);
                bot.setImageBitmap(RotateBitmap(source, curnt_deg));
                bot.setX(curnt_x);
                bot.setY(curnt_y);
                prev_deg = curnt_deg;
            }
        }));

        ((Button)findViewById(R.id.right)).setOnTouchListener(new RepeatListener(400, 50, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                curnt_x = prev_x;
                curnt_y = prev_y;

                curnt_deg = prev_deg + 1;
                RotateBitmap(source, curnt_deg);
                bot.setImageBitmap(RotateBitmap(source, curnt_deg));
                bot.setX(curnt_x);
                bot.setY(curnt_y);
                prev_deg = curnt_deg;
            }
        }));

    }

    private class calc_distance extends Thread{
        @Override
        public void run() {
            super.run();
            float[] rot_xy = new float[2];
            float calc_x;
            float calc_y;
            int calc_value;
            int distance = -1;
            curnt_x = prev_x;
            curnt_y = prev_y;

            int map_width = Object_map.getWidth();
            int map_height = Object_map.getHeight();

            while(true){
                rot_xy = position_rotate(0,distance,curnt_deg);
                calc_x = curnt_x + rot_xy[0];
                calc_y = curnt_y + rot_xy[1];


                if(calc_x >= map_width)calc_x = map_width-1;
                if(calc_x <= 0)calc_x = 0;
                if(calc_y >= map_height)calc_y = map_height-1;
                if(calc_y <= 0)calc_y = 0;

                calc_value = get_pixel(calc_x, calc_y);
                distance -= 1;
                if((calc_value == 1)||(distance == -21)){

                    if(calc_value == 1){
                        ReceiveFndValue(Integer.toString(-(distance)));
                        buzzvalue(1);

                    }

                    else{
                        ReceiveFndValue("0000");
                        buzzvalue(0);
                    }

                    distance = -1;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class draw_map extends Thread{
        @Override
        public void run() {
            super.run();
            float[] rot_xy = new float[2];
            float calc_x;
            float calc_y;
            int calc_value;
            int calc_deg = 0;
            int distance = -1;

            int map_width = Object_map.getWidth();
            int map_height = Object_map.getHeight();

            curnt_x = prev_x;
            curnt_y = prev_y;

            while(true){
                rot_xy = position_rotate(0,distance,calc_deg);
                calc_x = curnt_x + rot_xy[0];
                calc_y = curnt_y + rot_xy[1];


                if(calc_x >= map_width)calc_x = map_width-1;
                if(calc_x <= 0)calc_x = 0;
                if(calc_y >= map_height)calc_y = map_height-1;
                if(calc_y <= 0)calc_y = 0;

                calc_value = get_pixel(calc_x, calc_y);

                distance -= 1;
                if((calc_value == 1)||(distance == -61)){
                    if(calc_value == 1){
                        SLAM_map.setPixel((int) calc_x, (int) calc_y, Color.argb(255, 0, 0, 0));
                    }

                    distance = -1;
                    calc_deg += 1;
                    if(calc_deg == 361){
                        calc_deg = 0;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            finish();
        }
        else
        {
            backPressedTime = tempTime;
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public float[] position_rotate(float x, float y, int deg){
        float[] Rotate_XY = new float[2];
        float x_r = 0;
        float y_r = 0;
        float rad = (float) (deg*(Math.PI/180));

        x_r = (float) (x*Math.cos(rad) - y*Math.sin(rad));
        y_r = (float) (x*Math.sin(rad) + y*Math.cos(rad));

        Rotate_XY[0] = x_r;
        Rotate_XY[1] = y_r;
        return Rotate_XY;
    }

    public int get_pixel(float x, float y){
        int alpha_value = 0;
        int R_value = 0;
        int recog_wall = 0;

        int Pixel = Object_map.getPixel((int) x,(int) y);
        alpha_value = (Pixel>>24)&0xff;
        R_value = (Pixel>>16)&0xff;

        if(alpha_value != 0){
            if(R_value < 250){
                recog_wall = 1;
            }

            else recog_wall = 0;
        }
        else recog_wall = 0;

        return recog_wall;
    }


    public native int ReceiveFndValue(String ptr);
    public native int buzzvalue(int x);

}
