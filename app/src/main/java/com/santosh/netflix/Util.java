package com.santosh.netflix;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

public class Util {
    public static void slidefromRightToLeft(View view) {

        TranslateAnimation animate;
        if (view.getHeight() == 0) {
//            main_layout.getHeight(); // parent layout
//            animate = new TranslateAnimation(main_layout.getWidth()/2, 0, 0, 0);
        } else {
            animate = new TranslateAnimation(view.getWidth(), 0, 0, 0); // View for animation
        }
        animate = new TranslateAnimation(view.getWidth(), 0, 0, 0); // View for animation


        animate.setDuration(5000);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE); // Change visibility VISIBLE or GONE
    }

    public static boolean isNetworkConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    public static void slideDown(View view) {
        //view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                -view.getHeight(),// fromYDelta
                0
        );                // toYDelta
        animate.setDuration(500);

        animate.setFillAfter(true);
        view.startAnimation(animate);

    }

    public static void expand(final View v) {

        v.setVisibility(View.VISIBLE);

        v.measure(0, 0);
        int targetHeight = v.getMeasuredWidth();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().width = 1;


        ValueAnimator va = ValueAnimator.ofInt(1, targetHeight);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().width = (Integer) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        });

        va.setDuration(2500);
        va.setInterpolator(new LinearInterpolator());

        va.start();
    }
}
