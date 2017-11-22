package com.example.xyzreader.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


import com.example.xyzreader.R;

/**
 * TODO: document your custom view class.
 */
public class MyFullBleedImageView extends AppCompatImageView {

    public MyFullBleedImageView(Context context) {
        super(context);
    }

    public MyFullBleedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFullBleedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int twothirdHeight = MeasureSpec.getSize(widthMeasureSpec) * 2 / 3;
        int twothirdHeightMeasureSpec = MeasureSpec.makeMeasureSpec(twothirdHeight, MeasureSpec.EXACTLY);
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // we set the calculated height measure spec
        super.onMeasure(widthMeasureSpec, twothirdHeightMeasureSpec);
    }


}
