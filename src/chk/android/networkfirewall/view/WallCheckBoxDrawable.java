package chk.android.networkfirewall.view;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.StateSet;
import android.view.animation.Animation;
import chk.android.networkfirewall.R;

public class WallCheckBoxDrawable extends Drawable {
    public static final int SIGNAL_WIFI = 0;
    public static final int SIGNAL_3G = 1;
    public static final int[] PROCESSING_STATE_SET = { Integer.MAX_VALUE };

    private static final int DURATION = 200;
    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final PropertyValuesHolder VALUE_HOLDER_OFF_TO_ON =
            PropertyValuesHolder.ofKeyframe(
                    "Frame",
                    Keyframe.ofInt(0f, 0),
                    Keyframe.ofInt(0.125f, 0),
                    Keyframe.ofInt(0.25f, 1),
                    Keyframe.ofInt(0.375f, 2),
                    Keyframe.ofInt(0.5f, 3),
                    Keyframe.ofInt(0.625f, 4),
                    Keyframe.ofInt(0.75f, 5),
                    Keyframe.ofInt(0.875f, 6),
                    Keyframe.ofInt(1f, 7));
    private static final PropertyValuesHolder VALUE_HOLDER_ON_TO_OFF =
            PropertyValuesHolder.ofKeyframe(
                    "Frame",
                    Keyframe.ofInt(0f, 7),
                    Keyframe.ofInt(0.125f, 7),
                    Keyframe.ofInt(0.25f, 6),
                    Keyframe.ofInt(0.375f, 5),
                    Keyframe.ofInt(0.5f, 4),
                    Keyframe.ofInt(0.625f, 3),
                    Keyframe.ofInt(0.75f, 2),
                    Keyframe.ofInt(0.875f, 1),
                    Keyframe.ofInt(1f, 0));

    private static Paint sPaint = new Paint();
    private static Bitmap[] sSignalWifi;
    private static Bitmap[] sSignal3g;
    private static Bitmap[] sWall;

    private final Bitmap[] mSignal;
    private int mCurFrame = -1;
    private int mWidth;
    private int mHeight;
    private int mSignalLeft;
    private int mSignalTop;
    private ObjectAnimator mAnimator;
    private boolean mIsProcessing;

    public WallCheckBoxDrawable(Resources res, int signal) {
        if (sSignalWifi == null) {
            sSignalWifi = new Bitmap[8];
            sSignalWifi[0] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_1);
            sSignalWifi[1] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_2);
            sSignalWifi[2] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_3);
            sSignalWifi[3] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_4);
            sSignalWifi[4] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_5);
            sSignalWifi[5] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_6);
            sSignalWifi[6] = sSignalWifi[5];
            sSignalWifi[7] = sSignalWifi[5];
        }
        if (sSignal3g == null) {
            sSignal3g = new Bitmap[8];
            sSignal3g[0] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_1);
            sSignal3g[1] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_2);
            sSignal3g[2] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_3);
            sSignal3g[3] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_4);
            sSignal3g[4] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_5);
            sSignal3g[5] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_6);
            sSignal3g[6] = sSignal3g[5];
            sSignal3g[7] = sSignal3g[5];
        }
        if (sWall == null) {
            sWall = new Bitmap[8];
            sWall[0] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_1);
            sWall[1] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_2);
            sWall[2] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_3);
            sWall[3] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_4);
            sWall[4] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_5);
            sWall[5] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_6);
            sWall[6] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_7);
            sWall[7] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_8);
        }
        switch (signal) {
            case SIGNAL_WIFI:
                mSignal = sSignalWifi;
                break;
            case SIGNAL_3G:
                mSignal = sSignal3g;
                break;
            default:
                throw new IllegalArgumentException("Unknown signal type : " + signal);
        }

        mWidth = sWall[0].getWidth();
        mHeight = sWall[0].getHeight();
        mSignalLeft = (mWidth - mSignal[0].getWidth()) / 2;
        mSignalTop = (mHeight - mSignal[0].getHeight()) / 2;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCurFrame > -1 && mCurFrame < sWall.length) {
            canvas.drawBitmap(sWall[mCurFrame], 0, 0, sPaint);
        }
        if (mCurFrame > -1 && mCurFrame < mSignal.length) {
            canvas.drawBitmap(mSignal[mCurFrame], mSignalLeft, mSignalTop, sPaint);
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public void setAlpha(int alpha) {
        throw new IllegalArgumentException("Unsupported : setAlpha");
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new IllegalArgumentException("Unsupported : setColorFilter");
    }

    @Override
    public int getOpacity() {
        throw new IllegalArgumentException("Unsupported : getOpacity");
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean result = false;
        if (state == null) {
            result = false;
        } else {
            if (isChecked(state) && isProcessing(state) && !mIsProcessing) {
                mIsProcessing = true;
                start(false);
                result = true;
            } else if (!isChecked(state) && isProcessing(state) && !mIsProcessing) {
                mIsProcessing = true;
                start(true);
                result = true;
            } else if (isChecked(state) && !isProcessing(state)) {
                mIsProcessing = false;
                showOn();
                result = true;
            } else if (!isChecked(state) && !isProcessing(state)) {
                mIsProcessing = false;
                showOff();
                result = true;
            }
        }
        return result;
    }

    private boolean isChecked(int[] state) {
        return StateSet.stateSetMatches(CHECKED_STATE_SET, state);
    }

    private boolean isProcessing(int[] state) {
        return StateSet.stateSetMatches(PROCESSING_STATE_SET, state);
    }

    private void showOff() {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        setFrame(0);
    }

    private void showOn() {
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        setFrame(mSignal.length - 1);
    }

    private void start(boolean offToOn) {
        PropertyValuesHolder values = offToOn ? VALUE_HOLDER_OFF_TO_ON : VALUE_HOLDER_ON_TO_OFF;
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofPropertyValuesHolder(this, values);
            mAnimator.setDuration(DURATION);
            mAnimator.setRepeatCount(Animation.INFINITE);
        } else {
            mAnimator.cancel();
            mAnimator.setValues(values);
        }
        mAnimator.start();
    }

    public void setFrame(int frame) {
        if (mCurFrame == frame || frame >= mSignal.length) {
            return;
        }
        mCurFrame = frame;
        invalidateSelf();
    }
}
