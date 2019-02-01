package io.togoto.imagezoomcrop.cropoverlay;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import io.togoto.imagezoomcrop.R;
import io.togoto.imagezoomcrop.cropoverlay.edge.Edge;
import io.togoto.imagezoomcrop.cropoverlay.utils.PaintUtil;
import io.togoto.imagezoomcrop.photoview.IGetImageBounds;


/**
 * @author GT Modified/stripped down Code from cropper library : https://github.com/edmodo/cropper
 */
public class RectangularCropOverlayView extends View implements IGetImageBounds {

    //Defaults
    private boolean DEFAULT_GUIDELINES = true;
    private int DEFAULT_MARGINTOP = 100;
    private int DEFAULT_MARGINSIDE = 50;

    // we are cropping square image so width and height will always be equal
    private int DEFAULT_CROPWIDTH = 500;
    private static final int DEFAULT_CORNER_RADIUS = 250;
    private static final int DEFAULT_OVERLAY_COLOR = Color.argb(150, 0, 0, 0);

    // The Paint used to darken the surrounding areas outside the crop area.
    private Paint mBackgroundPaint;

    // The Paint used to draw the white rectangle around the crop area.
    private Paint mBorderPaint;

    // The Paint used to draw the guidelines within the crop area.
    private Paint mGuidelinePaint;

    private Path mClipPath;

    // The bounding box around the Bitmap that we are cropping.
    private RectF mBitmapRect;

    private int cropHeight = DEFAULT_CROPWIDTH;
    private int cropWidth = DEFAULT_CROPWIDTH;
    private float width_to_height_ratio = 1;


    private boolean mGuidelines;
    private int mMarginTop;
    private int mMarginSide;
    private int mOverlayColor;
    private Context mContext;

    public RectangularCropOverlayView(Context context) {
        super(context);
        init(context);
        mContext = context;
    }

    public RectangularCropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircularCropOverlayView, 0, 0);
        try {
            mGuidelines = ta.getBoolean(R.styleable.CircularCropOverlayView_guideLines, DEFAULT_GUIDELINES);
            mMarginTop = ta.getDimensionPixelSize(R.styleable.CircularCropOverlayView_marginTop, DEFAULT_MARGINTOP);
            mMarginSide = ta.getDimensionPixelSize(R.styleable.CircularCropOverlayView_marginSide, DEFAULT_MARGINSIDE);
            final float defaultRadius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_RADIUS, mContext.getResources().getDisplayMetrics());
            mOverlayColor = ta.getColor(R.styleable.CircularCropOverlayView_overlayColor, DEFAULT_OVERLAY_COLOR);
        } finally {
            ta.recycle();
        }

        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //BUG FIX : Turn of hardware acceleration. Clip path doesn't work with hardware acceleration
        //BUG FIX : Will have to do it here @ View level. Activity level not working on HTC ONE X
        //http://stackoverflow.com/questions/8895677/work-around-canvas-clippath-that-is-not-supported-in-android-any-more/8895894#8895894
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        canvas.save();
        mBitmapRect.left = Edge.LEFT.getCoordinate();
        mBitmapRect.top = Edge.TOP.getCoordinate();
        mBitmapRect.right = Edge.RIGHT.getCoordinate();
        mBitmapRect.bottom = Edge.BOTTOM.getCoordinate();

        mClipPath.addRect(mBitmapRect, Path.Direction.CW);
        canvas.clipPath(mClipPath, Region.Op.DIFFERENCE);
        canvas.drawColor(mOverlayColor);
        mClipPath.reset();
        canvas.restore();
        canvas.drawRect(mBitmapRect, mBorderPaint);

        if (mGuidelines) {
            drawRuleOfThirdsGuidelines(canvas);
        }
    }

    @Override
    public Rect getImageBounds() {
        return new Rect(
                (int) Edge.LEFT.getCoordinate(), (int) Edge.TOP.getCoordinate(),
                (int) Edge.RIGHT.getCoordinate(), (int) Edge.BOTTOM.getCoordinate());
    }

    // Private Methods /////////////////////////////////////////////////////////
    private void init(Context context) {

        int deviceWidth = context.getResources().getDisplayMetrics().widthPixels;
        int leftMargin = (deviceWidth) / 4;

        cropWidth = deviceWidth/2;
        cropHeight = (int) (cropWidth/width_to_height_ratio);

        int edgeT = mMarginTop + 50 - 5;
        int edgeB = mMarginTop + cropHeight + 50  + 5;
        int edgeL = leftMargin  - 5;
        int edgeR = leftMargin + cropWidth + 5;

        mBackgroundPaint = PaintUtil.newBackgroundPaint(context);
        mBorderPaint = PaintUtil.newBorderPaint(context);
        mGuidelinePaint = PaintUtil.newGuidelinePaint();
        Edge.TOP.setCoordinate(edgeT);
        Edge.BOTTOM.setCoordinate(edgeB);
        Edge.LEFT.setCoordinate(edgeL);
        Edge.RIGHT.setCoordinate(edgeR);
        mBitmapRect = new RectF(edgeL, edgeT, edgeR, edgeB);
        mClipPath = new Path();
    }


    private void drawRuleOfThirdsGuidelines(Canvas canvas) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        // Draw vertical guidelines.
        final float oneThirdCropWidth = Edge.getWidth() / 3;

        final float x1 = left + oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, mGuidelinePaint);
        final float x2 = right - oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = Edge.getHeight() / 3;

        final float y1 = top + oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, mGuidelinePaint);
        final float y2 = bottom - oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
    }

    public void setWidth_to_height_ratio(Context context, float ratio){
        width_to_height_ratio = ratio;
        init(context);
        this.invalidate();
    }
}