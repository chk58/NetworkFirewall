package chk.android.networkfirewall.view;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class CheckableDrawable extends Drawable implements Runnable, Animatable, Drawable.Callback {

    /** An invalid duration */
    private static final int INVALID_DURATION = -1;

    /** The delay between frames */
    private static final int DEFAULT_FRAME_DELAY = 10;

    /** The blur radius of the mark shadow */
    private static final int SHADOW_BLUR_RADIUS = 3;

    /** An interpolator used for animation */
    private static final Interpolator ACC_DEC = new AccelerateDecelerateInterpolator();

    /** An interpolator used for animation */
    private static final Interpolator DEC = new DecelerateInterpolator();

    /** An interpolator used for animation */
    private static final Interpolator ACC = new AccelerateInterpolator();

    /** An interpolator used for animation */
    private static final Interpolator LINEAR = new LinearInterpolator();

    /*
     * The transitions from checked to unchecked and unchecked to checked are
     * animated by animating several variables. The following six varables are
     * animated:
     *
     * The scale of the background, crossfade of the background (between the
     * states), the scale of the mark drawable, the alpha of the mark drawable,
     * the offset of the mark shadow and the alpha for the mark shadow.
     *
     * The animation is key-frame based with various interpolation inbetween
     * values. The animation to checked is different than the animation to
     * unchecked, that is, it's not the same animaion that runs backwards.
     *
     * Below are the 12 key frame lists that hold the info of this animation and
     * the initialization of them.
     */

    /*
     * The mark starts to get visible at 0.2 in the animation and is fully
     * visible at 0.35.
     */
    private static final KeyFrame[] MARK_ALPHA_CHECKED_FRAMES = {
            new KeyFrame(0.20f, 0.00f, null),
            new KeyFrame(0.35f, 1.00f, LINEAR)
    };

    /*
     * The mark starts to fade out at 0.4 in the animation and then fades
     * away to 20% visibility.
     */
    private static final KeyFrame[] MARK_ALPHA_UN_CHECKED_FRAMES = {
            new KeyFrame(0.40f, 1.00f, null),
            new KeyFrame(0.60f, 0.20f, LINEAR)
    };

    /*
     * The mark start out at 0 scale but at 0.2 starts to scale up to 2
     * times the size. At 0.35 it starts to "fall back" to scale 1.
     */
    private KeyFrame[] mMarkScaleCheckedFrames;

    /*
     * The mark starts to scale to almost twice the size at the start of the
     * animation, and at 0.2 it starts to "fall back" and completely
     * disappear with a scale of 0.
     */
    private KeyFrame[] mMarkScaleUnCheckedFrames;

    /*
     * The shadow of the mark is direcly under the mark at the begining, but
     * animates to be 8 pixels below when the mark is as biggest and then
     * back to directly underneath.
     */
    private static final KeyFrame[] MARK_SHADOW_OFFSET_CHECKED_FRAMES = {
            new KeyFrame(0.20f, 0.00f, null),
            new KeyFrame(0.35f, 8.00f, DEC),
            new KeyFrame(0.90f, 0.00f, ACC_DEC)
    };

    /*
     * The shadow starts directly underArrayList<KeyFrame> the mark and then animates to be at
     * most 8 pixels below the mark and then animates back to directly under the mark.
     */
    private static final KeyFrame[] MARK_SHADOW_OFFSET_UN_CHECKED_FRAMES = {
            new KeyFrame(0.00f, 0.00f, null),
            new KeyFrame(0.20f, 8.00f, DEC),
            new KeyFrame(0.60f, 0.00f, ACC)
    };

    /*
     * The alpha of the shadow of the mark starts invisible and is made
     * visible before it fades to invisibility in the end.
     */
    private static final KeyFrame[] MARK_SHADOW_ALPHA_CHECKED_FRAMES = {
            new KeyFrame(0.20f, 0.00f, null),
            new KeyFrame(0.35f, 0.35f, LINEAR),
            new KeyFrame(0.75f, 0.75f, LINEAR),
            new KeyFrame(0.90f, 0.00f, LINEAR)
    };

    /*
     * The alpha of the shadow starts out at 0 and then animates to a slight
     * visibility at 0.2 in the animation and then fades away.
     */
    private static final KeyFrame[] MARK_SHADOW_ALPHA_UN_CHECKED_FRAMES = {
            new KeyFrame(0.00f, 0.00f, null),
            new KeyFrame(0.20f, 0.30f, DEC),
            new KeyFrame(0.60f, 0.00f, ACC)
    };

    /*
     * The background crossfade between the old state and the new starts at 0.3 in the
     * animation and the new state is completely visible at the end
     * of the animation.
     */
    private static final KeyFrame[] BACKGROUND_CROSSFADE_CHECKED_FRAMES = {
            new KeyFrame(0.30f, 0.00f, null),
            new KeyFrame(1.00f, 1.00f, LINEAR)
    };

    /*
     * The background crossfade between the old state and the new starts at
     * 0.3 in the animation and the new state is completely visible at the
     * end of the animation.
     */
    private static final KeyFrame[] BACKGROUND_CROSSFADE_UN_CHECKED_FRAMES = {
            new KeyFrame(0.30f, 0.00f, null),
            new KeyFrame(1.00f, 1.00f, LINEAR)
    };

    /*
     * The mark drawable for when the CheckableDrawable is checked should always be the
     * drawable for the new state.
     */
    private static final KeyFrame[] MARK_CROSSFADE_CHECKED_FRAMES = {
            new KeyFrame(0.00f, 1.00f, null),
            new KeyFrame(1.00f, 1.00f, LINEAR)
    };

    /*
     * The mark drawable for when the CheckableDrawable is unchecked should always be the
     * drawable for the old state.
     */
    private static final KeyFrame[] MARK_CROSSFADE_UN_CHECKED_FRAMES = {
            new KeyFrame(0.99f, 0.00f, null),
            new KeyFrame(1.00f, 1.00f, LINEAR)
    };

    /*
     * The background scale starts and ends at 1, but have in between been
     * animated to simulate a bouncing behaviour.
     */
    private static final KeyFrame[] BACKGROUND_SCALE_CHECKED_FRAMES = {
            new KeyFrame(0.00f, 1.00f, null),
            new KeyFrame(0.10f, 0.50f, ACC_DEC),
            new KeyFrame(0.30f, 1.10f, ACC_DEC),
            new KeyFrame(0.50f, 0.95f, ACC_DEC),
            new KeyFrame(1.00f, 1.00f, ACC_DEC)
    };

    /*
     * The background scale starts and at 1, quickly animates to a scale of
     * 0.85 and then slowly scales back up to 1.
     */
    private static final KeyFrame[] BACKGROUND_SCALE_UN_CHECKED_FRAMES = {
            new KeyFrame(0.00f, 1.00f, null),
            new KeyFrame(0.10f, 0.85f, ACC_DEC),
            new KeyFrame(0.30f, 1.00f, ACC_DEC)
    };

    /** The drawable for the background */
    private Drawable mBackground;

    /** The drawable for the mark */
    private Drawable mMark;

    /** Bitmap of the mark shadow */
    private Bitmap mMarkShadow;

    /** true if the drawable is checked */
    private boolean mIsChecked;

    /** true if the animation was started */
    private boolean mAnimationWasStarted;

    /** true if the on-mark should be drawn */
    private boolean mDrawMarkOn;

    /** The alpha of the on-mark for the disabled and checked state */
    private int mMarkDisabledOpacity;

    /** true if the drawable is pressed */
    private boolean mIsPressed;

    /** true if the drawable was recently pressed */
    private boolean mWasRecentlyPressed;

    /** true if the animation is running */
    private boolean mRunning;

    /** The time the animation started */
    private long mStartTime;

    /** the duration of the animation */
    private int mDuration;

    /** Current progress of the animation. 0 = just started, 1 = finished */
    private float mProgress;

    /** true if we have gotten bounds */
    private boolean mHasGottenBounds;

    /** The current state of the drawable */
    private int[] mCurrentDrawableState;

    /** The state the background drawable had before it was clicked */
    private int[] mPrevDrawableStateBackground;

    /** The state the mark drawable had before it was clicked */
    private int[] mPrevDrawableStateMark;

    /** Paint object used to draw the shadow */
    private Paint mPaint;

    /** The middle x-coordinate of the drawable */
    private int mCenterX;

    /** The middle y-coordinate of the drawable */
    private int mCenterY;

    /** The distance to offset the mark shadow */
    private final int[] mOffset = { 0, 0 };

    /** The current alpha used when drawing the drawable for the background */
    private int mAlphaBackground;

    /** The current alpha used when drawing the drawable for the mark */
    private int mAlphaMark;

    /** The current scale used when drawing the drawable for the background */
    private float mScaleBackground;

    /** The current scale used when drawing the drawable for the on-mark */
    private float mScaleMarkOn;

    /** The current scale used when drawing the drawable for the off-mark */
    private float mScaleMarkOff = 1.0f;

    /** The current mark bounds used as the position for the shadow for the mark */
    private Rect mPosMark;

    /** The current offset used when drawing the shadow for the mark */
    private float mOffsetMark;

    public CheckableDrawable() {
        mProgress = 1;
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
    }

    private void updateDrawables() {
        mAlphaBackground = (int)(0xFF * getInterpolatedValue(mIsChecked ?
                BACKGROUND_CROSSFADE_CHECKED_FRAMES : BACKGROUND_CROSSFADE_UN_CHECKED_FRAMES,
                mProgress));
        mAlphaMark = (int)(0xFF * getInterpolatedValue(mIsChecked ?
                MARK_CROSSFADE_CHECKED_FRAMES : MARK_CROSSFADE_UN_CHECKED_FRAMES,
                mProgress));
        mScaleBackground = getInterpolatedValue(mIsChecked ? BACKGROUND_SCALE_CHECKED_FRAMES
                : BACKGROUND_SCALE_UN_CHECKED_FRAMES, mProgress);
        mScaleMarkOn = getInterpolatedValue(mIsChecked ? mMarkScaleCheckedFrames :
                mMarkScaleUnCheckedFrames, mProgress);
        mPosMark = mMark.getBounds();
        int shadowAlphaMark = (int)(0xFF * getInterpolatedValue(mIsChecked ?
                MARK_SHADOW_ALPHA_CHECKED_FRAMES : MARK_SHADOW_ALPHA_UN_CHECKED_FRAMES, mProgress));
        mOffsetMark = getInterpolatedValue(mIsChecked ? MARK_SHADOW_OFFSET_CHECKED_FRAMES
                : MARK_SHADOW_OFFSET_UN_CHECKED_FRAMES, mProgress);
        mPaint.setAlpha(shadowAlphaMark);
        int markAlpha = (int)(0xFF * getInterpolatedValue(mIsChecked ? MARK_ALPHA_CHECKED_FRAMES :
                MARK_ALPHA_UN_CHECKED_FRAMES, mProgress));
        mMark.setState(mCurrentDrawableState);
        mMark.setAlpha(markAlpha);
    }

    @Override
    public void start() {
        if (!mRunning) {
            mDrawMarkOn = true;
            mRunning = true;
            mAnimationWasStarted = true;
            nextFrame();
        }
    }

    @Override
    public void stop() {
        if (mRunning) {
            setMarkState(mCurrentDrawableState);
            mRunning = false;
            mProgress = 1;
            unscheduleSelf(this);
            invalidateSelf();
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    private void nextFrame() {
        unscheduleSelf(this);
        scheduleSelf(this, SystemClock.uptimeMillis() + DEFAULT_FRAME_DELAY);
    }

    @Override
    public void run() {
        long delta = SystemClock.uptimeMillis() - mStartTime;
        if (delta > mDuration) {
            // it's been more than mDuration, the animation is done
            mProgress = 1;
            stop();
        } else if (delta < 0) {
            // we're not even started yet
            mProgress = 0;
            nextFrame();
        } else {
            mProgress = (float)delta / (float)mDuration;
            nextFrame();
        }

        // Update the values for drawing the drawables
        updateDrawables();

        invalidateSelf();
    }

    private boolean isChecked(int[] states) {
        return isStateSet(states, android.R.attr.state_checked);
    }

    private boolean isPressed(int[] states) {
        return isStateSet(states, android.R.attr.state_pressed);
    }

    private boolean isEnabled(int[] states) {
        return isStateSet(states, android.R.attr.state_enabled);
    }

    private boolean isStateSet(int[] states, int state) {
        for (int s : states) {
            if (s == state) {
                return true;
            }
        }
        return false;
    }

    private void drawBackground(Canvas canvas) {
        canvas.save();

        canvas.scale(mScaleBackground, mScaleBackground, mCenterX, mCenterY);
        if (mRunning) {
            drawDrawable(canvas, mBackground, mPrevDrawableStateBackground,
                    0xFF - mAlphaBackground);
        }
        drawDrawable(canvas, mBackground, mCurrentDrawableState, mAlphaBackground);

        canvas.restore();
    }

    private void drawMark(Canvas canvas, Drawable mark, float scale) {
        canvas.save();

        // scale the canvas (with the center point as pivot point)
        canvas.scale(scale, scale, mCenterX, mCenterY);

        // draw the shadow
        canvas.drawBitmap(mMarkShadow, mPosMark.left + mOffset[0], mPosMark.top +
                mOffsetMark + mOffset[1], mPaint);

        // draw the mark
        if (mRunning) {
            drawDrawable(canvas, mark, mPrevDrawableStateMark, 0xFF - mAlphaMark);
        }
        drawDrawable(canvas, mark, mCurrentDrawableState, mAlphaMark);

        canvas.restore();
    }

    private void drawDrawable(Canvas canvas, Drawable drawable, int[] state, int alpha) {
        drawable.setState(state);
        drawable.setAlpha(alpha);
        drawable.draw(canvas);
    }

    private float getInterpolatedValue(KeyFrame[] keyFrames, float progress) {
        if (keyFrames == null || keyFrames.length == 0) {
            // we have nothing to interpolate, just return 0
            return 0;
        } else if (keyFrames.length == 1) {
            // only one key frame, return the value of it
            return keyFrames[0].value;
        } else {
            // given the current progress, we need to find the start and end key
            // frames to interpolate between

            // start with the first two
            int startIndex = 0;
            KeyFrame startFrame = keyFrames[startIndex];
            KeyFrame endFrame = keyFrames[startIndex + 1];

            // then step through the list until the end key frame progress is
            // larger than the progress
            while (progress > endFrame.progress && startIndex < keyFrames.length - 2) {
                startIndex++;
                startFrame = keyFrames[startIndex];
                endFrame = keyFrames[startIndex + 1];
            }

            // then return the interpolated value
            return interpolate(progress, startFrame, endFrame);
        }
    }

    private float interpolate(float progress, KeyFrame start, KeyFrame end) {
        if (progress >= end.progress) {
            // the progress is larger than the end key frame progress
            // just return the value of the end key frame
            return end.value;
        } else if (progress <= start.progress) {
            // the progress is smaller than the start key frame progress
            // just return the value of the start key frame
            return start.value;
        } else {
            // interpolate between start and end key values using the end key
            // frame interpolator
            return start.value
                    + (end.value - start.value)
                    * end.interpolator.getInterpolation((progress - start.progress)
                            / (end.progress - start.progress));
        }
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);

        TypedArray ta = r.obtainAttributes(attrs,
                com.sonyericsson.uxp.R.styleable.CheckableDrawable);

        // get the wanted duration
        mDuration = ta
                .getInteger(
                        com.sonyericsson.uxp.R.styleable.CheckableDrawable_transitionDuration,
                        INVALID_DURATION);
        if (mDuration == INVALID_DURATION) {
            // client has to specify the duration
            throw new IllegalArgumentException("No transitionDuration specified");
        }

        // and the drawables for the background and mark
        Drawable background = ta
                .getDrawable(com.sonyericsson.uxp.R.styleable.CheckableDrawable_background);
        Drawable mark = ta
                .getDrawable(com.sonyericsson.uxp.R.styleable.CheckableDrawable_mark);
        // get the max animation size of the on mark
        float markSize = ta.getFloat(com.sonyericsson.uxp.R.styleable.CheckableDrawable_markSize,
                1.0f);

        mMarkScaleCheckedFrames = new KeyFrame[] {
                new KeyFrame(0.20f, 0.00f, null),
                new KeyFrame(0.35f, 2.00f * markSize, DEC),
                new KeyFrame(1.00f, 1.00f, ACC_DEC)
        };

        mMarkScaleUnCheckedFrames = new KeyFrame[] {
                new KeyFrame(0.00f, 1.00f, null),
                new KeyFrame(0.20f, 1.70f * markSize, DEC),
                new KeyFrame(0.60f, 0.00f, ACC)
        };

        ta.recycle();

        if (background == null) {
            // client has to specify the background drawable
            throw new IllegalArgumentException("No drawable specified for background");
        }

        if (mark == null) {
            // client has to specify the mark drawable
            throw new IllegalArgumentException("No drawable specified for mark");
        }

        Bitmap markBitmap = getMarkBitmap(mark);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setMaskFilter(new BlurMaskFilter(SHADOW_BLUR_RADIUS, Blur.NORMAL));

        mMarkShadow = markBitmap.extractAlpha(paint, mOffset);

        // finally, store the drawables
        mBackground = background;
        mMark = mark;
        mBackground.mutate();
        mMark.mutate();
        background.setCallback(this);
        mark.setCallback(this);

        // Set initial values for drawing the drawables
        updateDrawables();
    }

    /**
     * Get the bitmap for the mark from the drawable for the mark. The bitmap
     * is used to create the shadow for the mark. If the drawable is a
     * StateListDrawable the first item from the list is used. The alpha layer
     * from the drawable is the only thing that is used. This means that the
     * shape of the drawable but not the color of the drawable will affect how
     * the shadow will look. Because of this any drawable from the
     * StateListDrawable can be used because the shape of the drawables should
     * be the same.
     *
     * @param mark The drawable for the mark.
     */
    private Bitmap getMarkBitmap(Drawable mark) {
        Bitmap markBitmap;
        Drawable markBitmapDrawable;
        if (mark instanceof StateListDrawable) {
            markBitmapDrawable = ((StateListDrawable) mark).getStateDrawable(0);
        } else {
            markBitmapDrawable = mark;
        }
        if (markBitmapDrawable instanceof BitmapDrawable) {
            // create a shadow of the mark
            markBitmap = ((BitmapDrawable)markBitmapDrawable).getBitmap();
        } else {
            int width = markBitmapDrawable.getIntrinsicWidth();
            width = Math.max(width, 1);
            int height = markBitmapDrawable.getIntrinsicHeight();
            height = Math.max(height, 1);
            markBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
            Canvas canvas = new Canvas(markBitmap);
            markBitmapDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            markBitmapDrawable.draw(canvas);
        }
        return markBitmap;
    }

    @Override
    public int getOpacity() {
        // just use the background opacity
        return mBackground.getOpacity();
    }

    @Override
    public boolean isStateful() {
        // we will always be stateful
        return true;
    }

    @Override
    public int getIntrinsicWidth() {
        // the background defines the size
        return mBackground.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        // the background defines the size
        return mBackground.getIntrinsicHeight();
    }

    @Override
    public void setAlpha(int alpha) {
        mBackground.setAlpha(alpha);
        mMark.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mBackground.setColorFilter(cf);
        mMark.setColorFilter(cf);
    }

    @Override
    public void draw(Canvas canvas) {
        if(mAnimationWasStarted && mWasRecentlyPressed) {
            mWasRecentlyPressed = false;
            mAnimationWasStarted = false;
        }

        canvas.save();

        drawBackground(canvas);

        if(mDrawMarkOn) {
            drawMark(canvas, mMark, mScaleMarkOn);
        } else {
            drawMark(canvas, mMark, mScaleMarkOff);
        }

        canvas.restore();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mHasGottenBounds = true;

        mBackground.setBounds(bounds);
        mMark.setBounds(bounds);

        mCenterX = (bounds.left + bounds.right) / 2;
        mCenterY = (bounds.top + bounds.bottom) / 2;
    }

    @Override
    protected boolean onLevelChange(int level) {
        final boolean backgroundChanges = mBackground.setLevel(level);
        final boolean markChanges = mMark.setLevel(level);
        return backgroundChanges || markChanges;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (state == null) {
            return false;
        }

        boolean isPressed = isPressed(state);

        if (!isPressed && mIsPressed) {
            mWasRecentlyPressed = true;
        }
        mIsPressed = isPressed;

        boolean needsToBeRedrawn = false;
        boolean isChecked = isChecked(state);

        if (mIsChecked != isChecked) {
            // checked state has changed
            needsToBeRedrawn = true;
            mIsChecked = isChecked;

            // save the background state before the checked state was changed
            mPrevDrawableStateBackground = mBackground.getState();

            // save the mark state before the checked state was changed
            mPrevDrawableStateMark = mMark.getState();

            // only start the animation if we have got our bounds, otherwise the
            // animation would run if the drawable is checked from the
            // beginning, and it's pressed (or just recently was pressed)
            if (mHasGottenBounds && (isPressed || mWasRecentlyPressed)) {
                mStartTime = SystemClock.uptimeMillis();
                mProgress = 0;
                // If the drawable was pressed and then disabled then we have to make sure that it
                // has the right state
                if (!isEnabled(state)) {
                    int [] stateExtended = new int[state.length + 1];
                    System.arraycopy(state, 0, stateExtended, 0, state.length);
                    stateExtended[state.length] = android.R.attr.state_enabled;
                    state = stateExtended;
                }
                if (!isEnabled(mPrevDrawableStateMark)) {
                    int [] prevStateExtended = new int[mPrevDrawableStateMark.length + 1];
                    System.arraycopy(mPrevDrawableStateMark, 0, prevStateExtended, 0,
                            mPrevDrawableStateMark.length);
                    prevStateExtended[mPrevDrawableStateMark.length] =
                            android.R.attr.state_enabled;
                    mPrevDrawableStateMark = prevStateExtended;
                }
                start();
            }
        }

        // save the current state and set it to the background and mark
        mCurrentDrawableState = state;
        final boolean backgroundChanges = mBackground.setState(state);
        final boolean markChanges = mMark.setState(state);

        if (!mRunning) {
            setMarkState(state);
        }

        return needsToBeRedrawn || backgroundChanges || markChanges;
    }

    private void setMarkState(int[] state) {
        if ((isEnabled(state) && isChecked(state)) || (!isEnabled(state) && isChecked(state))) {
            mDrawMarkOn = true;
            updateDrawables();
        } else {
            mDrawMarkOn = false;
        }
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mBackground.getPadding(padding);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        mBackground.setVisible(visible, restart);
        mMark.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }

    private static class KeyFrame {

        /** The progress of the animation. 0 = just started, 1 = finished */
        public final float progress;

        /** The value at the given progress */
        public final float value;

        /** The interpolator used to interpolate values before the given progress */
        public final Interpolator interpolator;

        public KeyFrame(float progress, float value, Interpolator interpolator) {
            this.progress = progress;
            this.value = value;
            this.interpolator = interpolator;
        }
    }

}
