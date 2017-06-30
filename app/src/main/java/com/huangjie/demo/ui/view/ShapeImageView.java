package com.huangjie.demo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.huangjie.demo.ui.R;
import com.huangjie.demo.util.BitmapUtils;
import com.huangjie.demo.util.DimenUtils;

/**
 * Created by huangjie on 2017/6/28.
 */

public class ShapeImageView extends ImageView {


    public enum ImageType {
        TYPE_RECTANGLE,
        TYPE_CIRCLE,
        TYPE_ROUNDED_RECTANGLE
    }


    private Bitmap mImageBitmap;
    private int mWidth;
    private static final int DEFAULT_ROUND_CORNER_SIZE = DimenUtils.dp2Px(2);
    private int mRoundCornerSize = DEFAULT_ROUND_CORNER_SIZE;
    private ImageType mImageShape = ImageType.TYPE_RECTANGLE;

    public ShapeImageView(Context context) {
        super(context);
    }

    public ShapeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mWidth = getResources().getDimensionPixelSize(R.dimen.grid_item_img_width);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ImageViewShape);
        if (typedArray != null) {
            String shape = typedArray.getString(R.styleable.ImageViewShape_image_shape);
            if (shape.equalsIgnoreCase("circle")) {
                mImageShape = ImageType.TYPE_CIRCLE;
            } else if (shape.equalsIgnoreCase("round")){
                mImageShape = ImageType.TYPE_ROUNDED_RECTANGLE;
            } else {
                mImageShape = ImageType.TYPE_RECTANGLE;
            }
            typedArray.recycle();
        }
        setBackgroundColor(getResources().getColor(R.color.transparent));
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        if (mWidth > 0) {
            mImageBitmap = BitmapUtils.resizeBitmap(bm, mWidth, mWidth);
            if (mImageBitmap != null) {
                Bitmap outBitmap = Bitmap.createBitmap(mWidth, mWidth, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(outBitmap);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true);
                if (mImageShape == ImageType.TYPE_ROUNDED_RECTANGLE) {
                    canvas.drawRoundRect(new RectF(0, 0, mWidth, mWidth), mRoundCornerSize, mRoundCornerSize, paint);
                } else if (mImageShape == ImageType.TYPE_CIRCLE) {
                    canvas.drawCircle((float) mWidth / 2, (float) mWidth / 2, (float) mWidth / 2, paint);
                } else {
                    canvas.drawRect(new Rect(0, 0, mWidth, mWidth), paint);
                }
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(mImageBitmap, 0, 0, paint);
                super.setImageBitmap(outBitmap);
            }
        }
    }

    public void setRoundCornerSize(int size) {
        mRoundCornerSize = size;
        invalidate();
    }
}
