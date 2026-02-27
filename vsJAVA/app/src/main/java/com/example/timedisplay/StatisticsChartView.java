package com.example.timedisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class StatisticsChartView extends View {
    private Paint linePaint;
    private Paint barPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint pointPaint;
    
    private List<Float> dataPoints = new ArrayList<>();
    private String chartType = "bar"; // bar, line
    private int themeColor = Color.parseColor("#FF8C42");
    
    public StatisticsChartView(Context context) {
        super(context);
        init();
    }
    
    public StatisticsChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StatisticsChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4);
        linePaint.setColor(themeColor);
        
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(themeColor);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(24);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        gridPaint.setColor(Color.LTGRAY);
        
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(themeColor);
        
        // 默认数据
        setSampleData();
    }
    
    public void setThemeColor(int color) {
        this.themeColor = color;
        linePaint.setColor(themeColor);
        barPaint.setColor(themeColor);
        pointPaint.setColor(themeColor);
        invalidate();
    }
    
    public void setChartType(String type) {
        this.chartType = type;
        invalidate();
    }
    
    public void setData(List<Float> data) {
        this.dataPoints = data;
        invalidate();
    }
    
    private void setSampleData() {
        dataPoints.clear();
        dataPoints.add(30f);
        dataPoints.add(45f);
        dataPoints.add(25f);
        dataPoints.add(60f);
        dataPoints.add(40f);
        dataPoints.add(55f);
        dataPoints.add(35f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dataPoints.isEmpty()) return;
        
        int width = getWidth();
        int height = getHeight();
        int padding = 60;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;
        
        // 绘制网格线
        for (int i = 0; i <= 5; i++) {
            float y = padding + (chartHeight * i / 5f);
            canvas.drawLine(padding, y, width - padding, y, gridPaint);
        }
        
        float maxValue = getMaxValue();
        float barWidth = chartWidth / (dataPoints.size() * 1.5f);
        float spacing = barWidth * 0.5f;
        
        if ("bar".equals(chartType)) {
            drawBarChart(canvas, padding, chartHeight, maxValue, barWidth, spacing);
        } else {
            drawLineChart(canvas, padding, chartHeight, maxValue, barWidth, spacing);
        }
        
        // 绘制X轴标签
        String[] labels = {"一", "二", "三", "四", "五", "六", "日"};
        for (int i = 0; i < dataPoints.size() && i < labels.length; i++) {
            float x = padding + spacing + i * (barWidth + spacing) + barWidth / 2;
            canvas.drawText(labels[i], x, height - 20, textPaint);
        }
    }
    
    private void drawBarChart(Canvas canvas, int padding, int chartHeight, float maxValue, float barWidth, float spacing) {
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            float barHeight = (value / maxValue) * chartHeight * 0.8f;
            float left = padding + spacing + i * (barWidth + spacing);
            float top = padding + chartHeight - barHeight;
            float right = left + barWidth;
            float bottom = padding + chartHeight;
            
            canvas.drawRect(left, top, right, bottom, barPaint);
        }
    }
    
    private void drawLineChart(Canvas canvas, int padding, int chartHeight, float maxValue, float barWidth, float spacing) {
        Path path = new Path();
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            float x = padding + spacing + i * (barWidth + spacing) + barWidth / 2;
            float y = padding + chartHeight - (value / maxValue) * chartHeight * 0.8f;
            
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            
            // 绘制数据点
            canvas.drawCircle(x, y, 8, pointPaint);
        }
        
        canvas.drawPath(path, linePaint);
    }
    
    private float getMaxValue() {
        float max = 0;
        for (float value : dataPoints) {
            if (value > max) max = value;
        }
        return max > 0 ? max : 100;
    }
}
