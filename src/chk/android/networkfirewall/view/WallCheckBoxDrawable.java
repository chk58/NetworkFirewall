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
import chk.android.networkfirewall.R;

public class WallCheckBoxDrawable extends Drawable {
    public static final int SIGNAL_WIFI = 0;
    public static final int SIGNAL_3G = 1;
    
    private static final int DURATION = 100;
    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final int[] PRESSED_STATE_SET = { android.R.attr.state_pressed };
    private static final PropertyValuesHolder VALUE_HOLDER =
            PropertyValuesHolder.ofKeyframe(
                    "Frame",
                    Keyframe.ofInt(0f, 0),
                    Keyframe.ofInt(0.14f, 1),
                    Keyframe.ofInt(0.30f, 2),
                    Keyframe.ofInt(0.5f, 3),
                    Keyframe.ofInt(0.72f, 4),
                    Keyframe.ofInt(1f, 5)
                    );

    private static Paint sPaint = new Paint();
    private static Bitmap[] sSignalWifi;
    private static Bitmap[] sSignal3g;
    private static Bitmap[] sWall;

    private final Bitmap[] mSignal;
    private int mCurFrame = -1;
    private int mWidth;
    private int mHeight;
    private ObjectAnimator mAnimator;
    private int[] mOldState;
    /** true if the drawable was recently pressed */
    private boolean mWasRecentlyPressed;

    public WallCheckBoxDrawable(Resources res, int signal) {
        if (sSignalWifi == null) {
            sSignalWifi = new Bitmap[6];
            sSignalWifi[0] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_1);
            sSignalWifi[1] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_2);
            sSignalWifi[2] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_3);
            sSignalWifi[3] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_4);
            sSignalWifi[4] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_1);
            sSignalWifi[5] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_2);
        }
        if (sSignal3g == null) {
            sSignal3g = new Bitmap[6];
            sSignal3g[0] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_1);
            sSignal3g[1] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_2);
            sSignal3g[2] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_3);
            sSignal3g[3] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_4);
            sSignal3g[4] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_1);
            sSignal3g[5] = BitmapFactory.decodeResource(res, R.drawable.signal_3g_2);
        }
        if (sWall == null) {
            sWall = new Bitmap[6];
            sWall[0] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_1);
            sWall[1] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_2);
            sWall[2] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_3);
            sWall[3] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_4);
            sWall[4] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_5);
            sWall[5] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_6);
        }
        switch (signal) {
            case SIGNAL_WIFI:
                mSignal = sSignalWifi;
                break;
            case SIGNAL_3G:
                mSignal = sSignal3g;
                break;
            default:
                throw new RuntimeException("Unknown signal type : " + signal);
        }

        mWidth = sWall[0].getWidth();
        mHeight = sWall[0].getHeight();
        mOldState = getState();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCurFrame > -1 && mCurFrame < sWall.length) {
            canvas.drawBitmap(sWall[mCurFrame], 0, 0, sPaint);
        }
        // if (mCurFrame > -1 && mCurFrame < mSignal.length) {
        // canvas.drawBitmap(mSignal[mCurFrame], 0, 0, sPaint);
        // }
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
        throw new RuntimeException("Unsupported : setAlpha");
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new RuntimeException("Unsupported : setColorFilter");
    }

    @Override
    public int getOpacity() {
        throw new RuntimeException("Unsupported : getOpacity");
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean result = false;
        if (state == null) {
            result = false;
        } else {
            mWasRecentlyPressed = isPressed(state);
            if (isChecked(state) && mOldState == null) {
                setFrame(mSignal.length - 1);
                result = true;
            } else if (!isChecked(state) && mOldState == null) {
                setFrame(0);
                result = true;
            } else if (isChecked(state) && !isChecked(mOldState)
                    && !mWasRecentlyPressed) {
                setFrame(mSignal.length - 1);
                result = true;
            } else if (!isChecked(state) && isChecked(mOldState)
                    && !mWasRecentlyPressed) {
                setFrame(0);
                result = true;
            } else if (isChecked(state) && !isChecked(mOldState)) {
                start(true);
                result = true;
            } else if (!isChecked(state) && isChecked(mOldState)) {
                start(false);
                result = true;
            }
        }
        mOldState = state;
        return result;
    }

    private boolean isChecked(int[] state) {
        return StateSet.stateSetMatches(CHECKED_STATE_SET, state);
    }

    private boolean isPressed(int[] state) {
        return StateSet.stateSetMatches(PRESSED_STATE_SET, state);
    }

    private void start(boolean offToOn) {
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofPropertyValuesHolder(this, VALUE_HOLDER);
            mAnimator.setDuration(DURATION);
        } else if (mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        if (offToOn) {
            mAnimator.start();
        } else {
            mAnimator.reverse();
        }
    }

    public void setFrame(int frame) {
        mWasRecentlyPressed = false;
        if (mCurFrame == frame || frame >= mSignal.length) {
            return;
        }
        mCurFrame = frame;
        invalidateSelf();
    }
}
