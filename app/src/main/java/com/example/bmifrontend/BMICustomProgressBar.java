package com.example.bmifrontend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BMICustomProgressBar extends View {
    private final Paint paint = new Paint();
    private int bmiValue = 0;

    public BMICustomProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBmiValue(int value) {
        this.bmiValue = value;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float maxRange = 40f;
        float progress = (bmiValue / maxRange) * width;

        // Colors
        int lightUnderweight = getContext().getColor(R.color.lightUnderweight);
        int darkUnderweight = getContext().getColor(R.color.darkUnderweight);
        int lightNormal = getContext().getColor(R.color.lightNormal);
        int darkNormal = getContext().getColor(R.color.darkNormal);
        int lightOverweight = getContext().getColor(R.color.lightOverweight);
        int darkOverweight = getContext().getColor(R.color.darkOverweight);
        int lightObese = getContext().getColor(R.color.lightObese);
        int darkObese = getContext().getColor(R.color.darkObese);

        // Ranges
        float underWeightEnd = (18.5F / maxRange) * width;
        float normalEnd = (24.9F / maxRange) * width;
        float overweightEnd = (29.9F / maxRange) * width;

        // Draw ranges with with light colors
        paint.setColor(lightUnderweight);
        canvas.drawRect(0, 0, underWeightEnd, height, paint);

        paint.setColor(lightNormal);
        canvas.drawRect(underWeightEnd, 0, normalEnd, height, paint);

        paint.setColor(lightOverweight);
        canvas.drawRect(normalEnd, 0, overweightEnd, height, paint);

        paint.setColor(lightObese);
        canvas.drawRect(overweightEnd, 0, width, height, paint);

        // Draw progress with dark colors
        if (progress <= underWeightEnd) {
            paint.setColor(darkUnderweight);
            canvas.drawRect(0, 0, progress, height, paint);
        } else if(progress <= normalEnd) {
            paint.setColor(darkUnderweight);
            canvas.drawRect(0, 0, underWeightEnd, height, paint);
            paint.setColor(darkNormal);
            canvas.drawRect(underWeightEnd, 0, progress, height, paint);
        } else if(progress <= overweightEnd) {
            paint.setColor(darkUnderweight);
            canvas.drawRect(0, 0, underWeightEnd, height, paint);
            paint.setColor(darkNormal);
            canvas.drawRect(underWeightEnd, 0, normalEnd, height, paint);
            paint.setColor(darkOverweight);
            canvas.drawRect(normalEnd, 0, progress, height, paint);
        } else {
            paint.setColor(darkUnderweight);
            canvas.drawRect(0, 0, underWeightEnd, height, paint);
            paint.setColor(darkNormal);
            canvas.drawRect(underWeightEnd, 0, normalEnd, height, paint);
            paint.setColor(darkOverweight);
            canvas.drawRect(normalEnd, 0, overweightEnd, height, paint);
            paint.setColor(darkObese);
            canvas.drawRect(overweightEnd, 0, progress, height, paint);
        }
    }
}
