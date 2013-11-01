package chk.android.networkfirewall.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import chk.android.networkfirewall.R;

public class WallCheckBox extends CompoundButton {

    private boolean mIsProcessing = false;
    private OnStartProcessListener mOnStartProcessListener;

    public WallCheckBox(Context context) {
        this(context, null);
    }

    public WallCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WallCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setClickable(true);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.WallCheckBox);
        int signal = a.getInt(R.styleable.WallCheckBox_signal, -1);
        a.recycle();

        Drawable d = new WallCheckBoxDrawable(getContext().getResources(),
                signal);
        setButtonDrawable(d);
    }

    @Override
    public boolean performClick() {
        if (!mIsProcessing) {
            setStatus(isChecked(), true);
            if (mOnStartProcessListener != null) {
                mOnStartProcessListener.OnStartProcess(this);
            }
        }
        return true;
    }

    public void setStatus(boolean checked, boolean processing) {
        boolean refresh = false;
        if (processing != mIsProcessing) {
            mIsProcessing = processing;
            refresh = true;
        }
        if (isChecked() != checked) {
            setChecked(checked);
            refresh = false;
        }
        if (refresh) {
            refreshDrawableState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mIsProcessing) {
            mergeDrawableStates(drawableState,
                    WallCheckBoxDrawable.PROCESSING_STATE_SET);
        }
        return drawableState;
    }

    public void setOnStartProcessListener(OnStartProcessListener listener) {
        mOnStartProcessListener = listener;
    }

    public static interface OnStartProcessListener {
        public void OnStartProcess(WallCheckBox view);
    }
}
