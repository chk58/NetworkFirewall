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

    private static final int DURATION = 300;
    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final PropertyValuesHolder VALUE_HOLDER =
            PropertyValuesHolder.ofKeyframe(
                    "Frame",
                    Keyframe.ofInt(0, 0),
                    Keyframe.ofInt(0.36f, 1),
                    Keyframe.ofInt(0.70f, 2),
                    Keyframe.ofInt(1f, 3));

    private static Paint sPaint = new Paint();
    private static Bitmap[] sSignal;
    private static Bitmap[] sWall;

    private int mCurFrame = -1;
    private int mWidth;
    private int mHeight;
    private ObjectAnimator mAnimator;
    private int[] mOldState;

    public WallCheckBoxDrawable(Resources res) {
        if (sSignal == null) {
            sSignal = new Bitmap[4];
            sSignal[0] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_1);
            sSignal[1] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_2);
            sSignal[2] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_3);
            sSignal[3] = BitmapFactory.decodeResource(res, R.drawable.signal_wifi_4);
        }
        if (sWall == null) {
            sWall = new Bitmap[4];
            sWall[0] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_1);
            sWall[1] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_2);
            sWall[2] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_3);
            sWall[3] = BitmapFactory.decodeResource(res, R.drawable.checkbox_wall_4);
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
        if (mCurFrame > -1 && mCurFrame < sSignal.length) {
            canvas.drawBitmap(sSignal[mCurFrame], 0, 0, sPaint);
        }
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
        } else if (StateSet.stateSetMatches(CHECKED_STATE_SET, state) && mOldState == null) {
            setFrame(0);
            result = true;
        } else if (!StateSet.stateSetMatches(CHECKED_STATE_SET, state) && mOldState == null) {
            setFrame(sSignal.length - 1);
            result = true;
        } else if (StateSet.stateSetMatches(CHECKED_STATE_SET, state)
                && !StateSet.stateSetMatches(CHECKED_STATE_SET, mOldState)) {
            start(false);
            result = true;
        } else if (!StateSet.stateSetMatches(CHECKED_STATE_SET, state)
                && StateSet.stateSetMatches(CHECKED_STATE_SET, mOldState)) {
            start(true);
            result = true;
        }
        mOldState = state;
        return (result || super.onStateChange(state));
    }

    private void start(boolean onToOff) {
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofPropertyValuesHolder(this, VALUE_HOLDER);
            mAnimator.setDuration(DURATION);
        } else if (mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        if (onToOff) {
            mAnimator.start();
        } else {
            mAnimator.reverse();
        }
    }

    public void setFrame(int frame) {
        if (frame >= sSignal.length) {
            return;
        }
        mCurFrame = frame;
        invalidateSelf();
    }
}
