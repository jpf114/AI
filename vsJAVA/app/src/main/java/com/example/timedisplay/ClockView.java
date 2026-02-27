package com.example.timedisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

public class ClockView extends View {
    private Paint circlePaint;
    private Paint hourPaint;
    private Paint minutePaint;
    private Paint secondPaint;
    private Paint textPaint;
    private Paint tickPaint;
    private int themeColor = Color.parseColor("#FF8C42"); // 默认饮食主题色
    
    private Calendar calendar;
    private Runnable tickRunnable;
    
    public ClockView(Context context) {
        super(context);
        init();
    }
    
    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        calendar = Calendar.getInstance();
        
        // 外圆画笔
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(8);
        circlePaint.setColor(themeColor);
        
        // 时针画笔
        hourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourPaint.setStyle(Paint.Style.STROKE);
        hourPaint.setStrokeWidth(12);
        hourPaint.setColor(Color.BLACK);
        hourPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // 分针画笔
        minutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minutePaint.setStyle(Paint.Style.STROKE);
        minutePaint.setStrokeWidth(8);
        minutePaint.setColor(Color.BLACK);
        minutePaint.setStrokeCap(Paint.Cap.ROUND);
        
        // 秒针画笔
        secondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondPaint.setStyle(Paint.Style.STROKE);
        secondPaint.setStrokeWidth(4);
        secondPaint.setColor(themeColor);
        secondPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // 刻度画笔
        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(3);
        tickPaint.setColor(Color.GRAY);
        
        // 数字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(32);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        startTick();
    }
    
    public void setThemeColor(int color) {
        this.themeColor = color;
        circlePaint.setColor(themeColor);
        secondPaint.setColor(themeColor);
        invalidate();
    }
    
    private void startTick() {
        tickRunnable = new Runnable() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                invalidate();
                postDelayed(this, 1000);
            }
        };
        post(tickRunnable);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 40;
        
        // 绘制外圆
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        
        // 绘制刻度和数字
        for (int i = 0; i < 12; i++) {
            float angle = (float) (i * 30 * Math.PI / 180);
            float tickStartX = centerX + (float) Math.sin(angle) * (radius - 20);
            float tickStartY = centerY - (float) Math.cos(angle) * (radius - 20);
            float tickEndX = centerX + (float) Math.sin(angle) * (radius - 10);
            float tickEndY = centerY - (float) Math.cos(angle) * (radius - 10);
            
            if (i % 3 == 0) {
                tickPaint.setStrokeWidth(5);
                tickPaint.setColor(Color.BLACK);
            } else {
                tickPaint.setStrokeWidth(3);
                tickPaint.setColor(Color.GRAY);
            }
            canvas.drawLine(tickStartX, tickStartY, tickEndX, tickEndY, tickPaint);
            
            // 绘制数字
            int number = i == 0 ? 12 : i;
            float textX = centerX + (float) Math.sin(angle) * (radius - 45);
            float textY = centerY - (float) Math.cos(angle) * (radius - 45) + 10;
            canvas.drawText(String.valueOf(number), textX, textY, textPaint);
        }
        
        // 获取时间
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        
        // 计算角度
        float hourAngle = (hour + minute / 60f) * 30;
        float minuteAngle = (minute + second / 60f) * 6;
        float secondAngle = second * 6;
        
        // 绘制时针
        float hourRad = (float) ((hourAngle - 90) * Math.PI / 180);
        float hourEndX = centerX + (float) Math.cos(hourRad) * (radius * 0.5f);
        float hourEndY = centerY + (float) Math.sin(hourRad) * (radius * 0.5f);
        canvas.drawLine(centerX, centerY, hourEndX, hourEndY, hourPaint);

        // 绘制分针
        float minuteRad = (float) ((minuteAngle - 90) * Math.PI / 180);
        float minuteEndX = centerX + (float) Math.cos(minuteRad) * (radius * 0.7f);
        float minuteEndY = centerY + (float) Math.sin(minuteRad) * (radius * 0.7f);
        canvas.drawLine(centerX, centerY, minuteEndX, minuteEndY, minutePaint);

        // 绘制秒针
        float secondRad = (float) ((secondAngle - 90) * Math.PI / 180);
        float secondEndX = centerX + (float) Math.cos(secondRad) * (radius * 0.85f);
        float secondEndY = centerY + (float) Math.sin(secondRad) * (radius * 0.85f);
        canvas.drawLine(centerX, centerY, secondEndX, secondEndY, secondPaint);
        
        // 绘制中心点
        canvas.drawCircle(centerX, centerY, 12, secondPaint);
        canvas.drawCircle(centerX, centerY, 6, new Paint() {{ setColor(Color.WHITE); setStyle(Paint.Style.FILL); }});
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (tickRunnable != null) {
            removeCallbacks(tickRunnable);
        }
    }
}
