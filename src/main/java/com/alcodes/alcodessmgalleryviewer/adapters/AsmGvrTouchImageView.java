package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.ViewPager;

import com.alcodes.alcodessmgalleryviewer.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AsmGvrTouchImageView extends androidx.appcompat.widget.AppCompatImageView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    Matrix matrix;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 3f;
    float[] m;
    boolean reachEndImage = false;

    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;


    Context context;

    public AsmGvrTouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public AsmGvrTouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    public AsmGvrTouchImageView(Context context, Uri imageUri){
        super(context);
        sharedConstructing(context);
        setZoomForImageFile(imageUri);
        loadIntoGlide(context,imageUri);
    }

    GestureDetector mGestureDetector;

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setOnDoubleTapListener(this);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);

                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        start.set(last);
                        mode = DRAG;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth,
                                    origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight,
                                    origHeight * saveScale);
                            matrix.postTranslate(fixTransX, fixTransY);

                            fixTrans();
                            last.set(curr.x, curr.y);

                            //Prevent View Pager Touch Happens
                            if(saveScale == 1.0){
                                //Slide Left/Right to Previous/Next Picture
                                getParent().requestDisallowInterceptTouchEvent(false);
                            }else{
                                if(!reachEndImage){
                                    //Panning the Image
                                    getParent().requestDisallowInterceptTouchEvent(true);
                                }else{
                                    //Reach The End of Image
                                    getParent().requestDisallowInterceptTouchEvent(false);
                                }
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        getParent().requestDisallowInterceptTouchEvent(false);
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }

                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }

        });
    }

    public void setZoomForImageFile(Uri imageUri) {
        try {
            String fileExtension = null;
            if(imageUri.getScheme().equals("http") | imageUri.getScheme().equals("https")){
                if(getImageFileExtensionURL(imageUri) != null){
                    fileExtension = getImageFileExtensionURL(imageUri);
                }
            }else{
                if(getImageFileExtensionURI(imageUri) != null){
                    fileExtension = getImageFileExtensionURI(imageUri);
                }
            }
            if(fileExtension.toLowerCase().equals("gif")){
                maxScale = 1f;
            }else if(fileExtension.toLowerCase().equals("png") ||
                    fileExtension.toLowerCase().equals("jpg") ||
                    fileExtension.toLowerCase().equals("bmp") ||
                    fileExtension.toLowerCase().equals("jpeg")){
                maxScale = 4f;
            }
        } catch (Exception e) {
            maxScale = 1f;
            e.printStackTrace();
        }
    }

    public String getImageFileExtensionURL(Uri imageUri) throws Exception {
        String fileType;
        try{
            fileType = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(imageUri)).toLowerCase();
            //fileType = fileType.substring(fileType.lastIndexOf("/")+1);
            return fileType;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getImageFileExtensionURI(Uri imageUri){
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(imageUri));
    }

    public void loadIntoGlide(Context context, Uri imageUri){
        final AsmGvrTouchImageView imageView = this;
        //PlaceHolder Drawable
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.setColorSchemeColors(Color.WHITE);
        circularProgressDrawable.start();

        //Error Drawable
        RequestBuilder<Drawable> requestBuilder =
                Glide.with(context)
                        .load(R.drawable.asm_gvr_ic_error_outline_black_128dp)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
                                imageView.setLayoutParams(layoutParams);
                                imageView.setScaleType(ScaleType.CENTER);
                                return false;
                            }
                        })
                        .centerInside();

        Glide.with(context)
                .load(imageUri)
                .placeholder(circularProgressDrawable)
                .transition(withCrossFade())
                .error(requestBuilder)
                //.apply(new RequestOptions().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
                .fitCenter()
                .into(this);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Double tap is detected
        float origScale = saveScale;
        float mScaleFactor;

        if (saveScale == minScale) {
            saveScale = maxScale;
            mScaleFactor = maxScale / origScale;
        } else {
            saveScale = minScale;
            mScaleFactor = minScale / origScale;
        }

        matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                viewHeight / 2);

        fixTrans();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        return;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            if (origWidth * saveScale <= viewWidth
                    || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                        viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight
                * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);

        if(fixTransX != 0){
            reachEndImage = true;
        }else{
            reachEndImage = false;
        }

    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {
            // Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0
                    || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight
                    - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth
                    - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }
}