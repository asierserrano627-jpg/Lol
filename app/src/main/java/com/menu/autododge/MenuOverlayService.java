package com.menu.autododge;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MenuOverlayService extends Service {

    private WindowManager windowManager;
    private LinearLayout rootView;
    private boolean dodgeActive = false;

    private int initX, initY;
    private float touchStartX, touchStartY;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildOverlay();
    }

    private void buildOverlay() {
        rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setPadding(32, 24, 32, 24);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(210, 15, 15, 25));
        bg.setCornerRadius(32f);
        rootView.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("⚡ Menú");
        title.setTextColor(Color.parseColor("#CCCCFF"));
        title.setTextSize(16f);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 12);
        title.setLayoutParams(titleParams);
        rootView.addView(title);

        View sep = new View(this);
        LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        sepParams.setMargins(0, 0, 0, 12);
        sep.setLayoutParams(sepParams);
        sep.setBackgroundColor(Color.parseColor("#44AAAAFF"));
        rootView.addView(sep);

        final ToggleButton btn = new ToggleButton(this);
        btn.setTextOn("Autododge: ON");
        btn.setTextOff("Autododge: OFF");
        btn.setChecked(false);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(13f);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 4, 0, 4);
        btn.setLayoutParams(btnParams);
        setButtonBackground(btn, false);

        btn.setOnCheckedChangeListener((compound, isChecked) -> {
            dodgeActive = isChecked;
            setButtonBackground(btn, isChecked);
            if (isChecked) {
                onDodgeEnabled();
            } else {
                onDodgeDisabled();
            }
        });
        rootView.addView(btn);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 20;
        params.y = 100;

        rootView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initX = params.x;
                    initY = params.y;
                    touchStartX = event.getRawX();
                    touchStartY = event.getRawY();
                    return false;

                case MotionEvent.ACTION_MOVE:
                    int dx = Math.round(event.getRawX() - touchStartX);
                    int dy = Math.round(event.getRawY() - touchStartY);
                    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
                        params.x = initX + dx;
                        params.y = initY + dy;
                        windowManager.updateViewLayout(rootView, params);
                        return true;
                    }
                    return false;
            }
            return false;
        });

        windowManager.addView(rootView, params);
    }

    private void onDodgeEnabled() {
        android.util.Log.i("MenuOverlay", "Autododge ACTIVADO");
    }

    private void onDodgeDisabled() {
        android.util.Log.i("MenuOverlay", "Autododge DESACTIVADO");
    }

    private void setButtonBackground(ToggleButton btn, boolean on) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(on ? Color.parseColor("#44CC44") : Color.parseColor("#CC4444"));
        d.setCornerRadius(20f);
        btn.setBackground(d);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rootView != null) windowManager.removeView(rootView);
    }
}
