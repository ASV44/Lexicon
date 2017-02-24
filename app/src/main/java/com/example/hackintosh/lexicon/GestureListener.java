package com.example.hackintosh.lexicon;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by hackintosh on 2/23/17.
 */

public class GestureListener implements GestureDetector.OnGestureListener {

    private swipeMotion swipeMotion;

    public GestureListener(swipeMotion swipeMotion) {this.swipeMotion = swipeMotion;}

    public interface swipeMotion {
        void onLeft();

        void onRight();
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        if(Math.abs(v)>Math.abs(v1)){
            if(v>0){
                Log.d("SwipeDetection","right");
                swipeMotion.onRight();
            }else{
                Log.d("SwipeDetection","left");
                swipeMotion.onLeft();
            }
        }else{
            if(v1>0){
                Log.d("SwipeDetection","down");
            }else{
                Log.d("SwipeDetection","up");
            }
        }
        return true;
    }
}
