package com.alcodes.alcodessmgalleryviewer.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.AsmGvrImageCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
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
    float maxScale = 4f;
    float[] m;

    //Flag
    boolean reachEndImage = false;
    boolean disallowZoom = false;
    boolean isErrorImage = false;
    boolean internetAvailable = false;

    private AsmGvrImageCallback mImageCallback;

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

    public void initImageView(Context context, Uri imageUri, boolean internetAvailable, AsmGvrImageCallback imageCallback){
        this.internetAvailable = internetAvailable;
        this.mImageCallback = imageCallback;

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

                setZoomForLandscapeMode();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //Image is Pressing
                        last.set(curr);
                        start.set(last);
                        mode = DRAG;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //Image is moving
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


                            if(isErrorImage){
                                //Error image only able to slide.
                                getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                            }

                            if(saveScale == 1.0){
                                //Not in Zooming
                                //Slide Left/Right to Previous/Next Picture
                                getParent().requestDisallowInterceptTouchEvent(false);
                            }else{
                                //In Zooming
                                if((origWidth * saveScale) <= viewWidth){
                                    //Side still have empty Space (Margin Does Show)
                                    //Slide Left/Right to Previous/Next Picture
                                    getParent().requestDisallowInterceptTouchEvent(false);
                                }else{
                                    //Side do not have empty Space (Margin Does Not Show)
                                    if(!reachEndImage){
                                        //Panning the Image
                                        getParent().requestDisallowInterceptTouchEvent(true);
                                    }else{
                                        //Reach The End of Image
                                        getParent().requestDisallowInterceptTouchEvent(false);
                                    }
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

    public void setZoomForLandscapeMode(){
        int phoneCurrentOrientation = getResources().getConfiguration().orientation;

        if(disallowZoom | isErrorImage){
            //No Scale Set to Error File and Gif File
            return;
        }

        if(phoneCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            //Landscape
            maxScale = 8f;
        }else{
            //Portrait
            maxScale = 4f;
        }
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
            disallowZoom = false;
            if(fileExtension.toLowerCase().equals("gif")){
                maxScale = 1f;
                disallowZoom = true;
            }
        } catch (Exception e) {
            maxScale = 1f;
            disallowZoom = true;
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
        //PlaceHolder Drawable (Progress Bar)
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(context, R.color.design_default_color_surface));
        circularProgressDrawable.start();

        int imgNoInternetAccessDrawable;
        RequestOptions requestOptions;

        if(internetAvailable){
            //General Error Icon
            imgNoInternetAccessDrawable = R.drawable.asm_gvr_ic_error_outline_black_128dp;

            //As the image is in dp unit
            requestOptions = new RequestOptions().override(128, 128);
        }else{
            //No Internet Access Icon
            imgNoInternetAccessDrawable = R.drawable.asm_gvr_no_internet_access;

            //As the image is in px unit
            requestOptions = new RequestOptions().override(256, 256);
        }

        //Error Drawable (Error Image)
        RequestBuilder<Drawable> requestBuilder =
                Glide.with(context)
                        .load(imgNoInternetAccessDrawable)
                        .apply(requestOptions)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                isErrorImage = true;
                                //Set to Center Without Fit to the Frame
                                setScaleType(ScaleType.CENTER_INSIDE);
                                return false;
                            }
                        })
                        .centerInside();

        Glide.with(context)
                .load(imageUri)
                .placeholder(circularProgressDrawable)
                .transition(withCrossFade())
                .error(requestBuilder)
                .fitCenter()
                .into(this);
    }

    public void resetIamgeToCenter(){
        mode = NONE;
        saveScale = 1f;
        setScaleType(ScaleType.CENTER);
        fixTrans();
        if(!isErrorImage){
            setScaleType(ScaleType.MATRIX);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        mImageCallback.onTouchShowHideActionBar();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Double tap is detected
        float origScale = saveScale;
        float mScaleFactor;


        if (saveScale == minScale) {
            //Current Scale is Equal Minimum Scale
            //Enlarge It
            saveScale = maxScale;
            mScaleFactor = maxScale / origScale;
        } else {
            //Current Scale is at any point of Scale (Except Minimum Scale)
            //Dwindle It
            setScaleType(ScaleType.FIT_CENTER);
            saveScale = minScale;
            mScaleFactor = minScale / origScale;
        }

        matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                viewHeight / 2);

        //Check and Fix its Transition when end image is reached
        fixTrans();

        if(!isErrorImage){
            setScaleType(ScaleType.MATRIX);
        }else{
            setScaleType(ScaleType.CENTER);
        }

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
        if(!isErrorImage){
            mImageCallback.onLongPressDialog();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //Sense Gesture and set it to Zoom
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