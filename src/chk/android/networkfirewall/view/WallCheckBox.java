package chk.android.networkfirewall.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import chk.android.networkfirewall.R;

public class WallCheckBox extends CompoundButton {

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
}
