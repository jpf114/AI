package com.example.timedisplay.health.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.SleepRecord;
import com.example.timedisplay.health.stats.HealthStatistics;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface PdfGenerationCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }

    public static void generateHealthReport(
            Context context,
            List<DietRecord> dietRecords,
            List<ExerciseRecord> exerciseRecords,
            List<SleepRecord> sleepRecords,
            Date startDate,
            Date endDate,
            String periodType,
            String password,
            PdfGenerationCallback callback) {

        new Thread(() -> {
            try {
                String fileName = generateFileName(periodType);
                File pdfDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "HealthReports");
                if (!pdfDir.exists()) {
                    pdfDir.mkdirs();
                }
                File pdfFile = new File(pdfDir, fileName);

                WriterProperties writerProperties = new WriterProperties();
                if (password != null && !password.isEmpty()) {
                    writerProperties.setStandardEncryption(
                            password.getBytes(),
                            password.getBytes(),
                            EncryptionConstants.ALLOW_PRINTING,
                            EncryptionConstants.ENCRYPTION_AES_256
                    );
                }

                PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath(), writerProperties);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument, PageSize.A4);

                PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                addTitle(document, boldFont, periodType);
                addDateRange(document, font, startDate, endDate);
                addSummarySection(document, boldFont, font, dietRecords, exerciseRecords, sleepRecords, startDate, endDate);
                addDietSection(document, boldFont, font, dietRecords);
                addExerciseSection(document, boldFont, font, exerciseRecords);
                addSleepSection(document, boldFont, font, sleepRecords);
                addChartsSection(document, boldFont, font, dietRecords, exerciseRecords, sleepRecords);

                document.close();

                callback.onSuccess(pdfFile.getAbsolutePath());
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private static String generateFileName(String periodType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String dateStr = sdf.format(new Date());
        String periodStr = "周".equals(periodType) ? "周" : "月";
        return dateStr + "_健康作息表_" + periodStr + ".pdf";
    }

    private static void addTitle(Document document, PdfFont boldFont, String periodType) {
        Paragraph title = new Paragraph("健康数据报告")
                .setFont(boldFont)
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        String subtitle = "(" + periodType + "报)";
        Paragraph subTitle = new Paragraph(subtitle)
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(subTitle);
    }

    private static void addDateRange(Document document, PdfFont font, Date startDate, Date endDate) {
        String dateRange = "报告周期: " + DATE_FORMAT.format(startDate) + " 至 " + DATE_FORMAT.format(endDate);
        Paragraph datePara = new Paragraph(dateRange)
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(datePara);
    }

    private static void addSummarySection(Document document, PdfFont boldFont, PdfFont font,
                                          List<DietRecord> dietRecords, List<ExerciseRecord> exerciseRecords,
                                          List<SleepRecord> sleepRecords, Date startDate, Date endDate) {
        Paragraph sectionTitle = new Paragraph("数据概览")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginBottom(10);
        document.add(sectionTitle);

        HealthStatistics.ComprehensiveStats stats = HealthStatistics.generateComprehensiveStats(
                dietRecords, exerciseRecords, sleepRecords, startDate, endDate, "周");

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        summaryTable.addCell(createSummaryCell("饮食记录数", String.valueOf(stats.dietStats.recordCount), boldFont, font));
        summaryTable.addCell(createSummaryCell("运动记录数", String.valueOf(stats.exerciseStats.recordCount), boldFont, font));
        summaryTable.addCell(createSummaryCell("睡眠记录数", String.valueOf(stats.sleepStats.recordCount), boldFont, font));
        summaryTable.addCell(createSummaryCell("总摄入热量", String.format("%.0f kcal", stats.dietStats.totalCalories), boldFont, font));
        summaryTable.addCell(createSummaryCell("总消耗热量", String.format("%.0f kcal", stats.exerciseStats.totalCaloriesBurned), boldFont, font));
        summaryTable.addCell(createSummaryCell("平均睡眠时长", String.format("%.1f 小时", stats.sleepStats.avgDurationHours), boldFont, font));

        document.add(summaryTable);
    }

    private static Cell createSummaryCell(String label, String value, PdfFont boldFont, PdfFont font) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setPadding(10);
        cell.add(new Paragraph(label).setFont(boldFont).setFontSize(12));
        cell.add(new Paragraph(value).setFont(font).setFontSize(14));
        return cell;
    }

    private static void addDietSection(Document document, PdfFont boldFont, PdfFont font, List<DietRecord> records) {
        Paragraph sectionTitle = new Paragraph("饮食记录详情")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (records.isEmpty()) {
            document.add(new Paragraph("暂无饮食记录").setFont(font).setFontSize(12));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("食物名称", boldFont));
        table.addHeaderCell(createHeaderCell("餐次", boldFont));
        table.addHeaderCell(createHeaderCell("摄入时间", boldFont));
        table.addHeaderCell(createHeaderCell("热量(kcal)", boldFont));

        for (DietRecord record : records) {
            table.addCell(createDataCell(record.getFoodName(), font));
            table.addCell(createDataCell(record.getMealTypeDisplay(), font));
            table.addCell(createDataCell(DATETIME_FORMAT.format(record.getIntakeTime()), font));
            table.addCell(createDataCell(String.format("%.0f", record.getCalories()), font));
        }

        document.add(table);
    }

    private static void addExerciseSection(Document document, PdfFont boldFont, PdfFont font, List<ExerciseRecord> records) {
        Paragraph sectionTitle = new Paragraph("运动记录详情")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (records.isEmpty()) {
            document.add(new Paragraph("暂无运动记录").setFont(font).setFontSize(12));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("运动类型", boldFont));
        table.addHeaderCell(createHeaderCell("强度", boldFont));
        table.addHeaderCell(createHeaderCell("开始时间", boldFont));
        table.addHeaderCell(createHeaderCell("时长(分钟)", boldFont));
        table.addHeaderCell(createHeaderCell("消耗(kcal)", boldFont));

        for (ExerciseRecord record : records) {
            table.addCell(createDataCell(record.getExerciseTypeDisplay(), font));
            table.addCell(createDataCell(record.getIntensityDisplay(), font));
            table.addCell(createDataCell(DATETIME_FORMAT.format(record.getStartTime()), font));
            table.addCell(createDataCell(String.valueOf(record.getDurationMinutes()), font));
            table.addCell(createDataCell(String.format("%.0f", record.getCaloriesBurned()), font));
        }

        document.add(table);
    }

    private static void addSleepSection(Document document, PdfFont boldFont, PdfFont font, List<SleepRecord> records) {
        Paragraph sectionTitle = new Paragraph("睡眠记录详情")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        if (records.isEmpty()) {
            document.add(new Paragraph("暂无睡眠记录").setFont(font).setFontSize(12));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("入睡时间", boldFont));
        table.addHeaderCell(createHeaderCell("起床时间", boldFont));
        table.addHeaderCell(createHeaderCell("时长", boldFont));
        table.addHeaderCell(createHeaderCell("质量", boldFont));
        table.addHeaderCell(createHeaderCell("夜醒次数", boldFont));

        for (SleepRecord record : records) {
            table.addCell(createDataCell(DATETIME_FORMAT.format(record.getSleepTime()), font));
            table.addCell(createDataCell(DATETIME_FORMAT.format(record.getWakeTime()), font));
            table.addCell(createDataCell(record.getFormattedDuration(), font));
            table.addCell(createDataCell(record.getSleepQualityDisplay(), font));
            table.addCell(createDataCell(String.valueOf(record.getWakeUpCount()), font));
        }

        document.add(table);
    }

    private static void addChartsSection(Document document, PdfFont boldFont, PdfFont font,
                                         List<DietRecord> dietRecords, List<ExerciseRecord> exerciseRecords,
                                         List<SleepRecord> sleepRecords) {
        Paragraph sectionTitle = new Paragraph("数据可视化")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        try {
            Bitmap chartBitmap = createSimpleBarChart(dietRecords, exerciseRecords);
            if (chartBitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ImageData imageData = ImageDataFactory.create(byteArray);
                Image image = new Image(imageData);
                image.setWidth(UnitValue.createPercentValue(100));
                image.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(image);
                chartBitmap.recycle();
            }
        } catch (Exception e) {
            document.add(new Paragraph("图表生成失败: " + e.getMessage()).setFont(font).setFontSize(10));
        }
    }

    private static Bitmap createSimpleBarChart(List<DietRecord> dietRecords, List<ExerciseRecord> exerciseRecords) {
        int width = 600;
        int height = 300;
        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paint);
        canvas.drawLine(padding, padding, padding, height - padding, paint);

        double maxCalories = 0;
        for (DietRecord record : dietRecords) {
            maxCalories = Math.max(maxCalories, record.getCalories());
        }
        for (ExerciseRecord record : exerciseRecords) {
            maxCalories = Math.max(maxCalories, record.getCaloriesBurned());
        }
        maxCalories = Math.max(maxCalories, 500);

        paint.setTextSize(20);
        paint.setColor(Color.BLACK);
        canvas.drawText("饮食摄入 vs 运动消耗", width / 2 - 80, 30, paint);

        if (!dietRecords.isEmpty()) {
            paint.setColor(Color.parseColor("#4CAF50"));
            float barWidth = 40;
            float x = padding + 50;
            float barHeight = (float) ((dietRecords.get(0).getCalories() / maxCalories) * chartHeight);
            canvas.drawRect(x, height - padding - barHeight, x + barWidth, height - padding, paint);
            paint.setColor(Color.BLACK);
            canvas.drawText("摄入", x, height - padding + 25, paint);
        }

        if (!exerciseRecords.isEmpty()) {
            paint.setColor(Color.parseColor("#FF5722"));
            float barWidth = 40;
            float x = padding + 150;
            float barHeight = (float) ((exerciseRecords.get(0).getCaloriesBurned() / maxCalories) * chartHeight);
            canvas.drawRect(x, height - padding - barHeight, x + barWidth, height - padding, paint);
            paint.setColor(Color.BLACK);
            canvas.drawText("消耗", x, height - padding + 25, paint);
        }

        return bitmap;
    }

    private static Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private static Cell createDataCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);
    }
}
