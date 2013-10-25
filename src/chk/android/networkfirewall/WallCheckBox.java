package chk.android.networkfirewall;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

public class WallCheckBox extends CompoundButton {

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
    }

    @Override
    public Drawable getBackground() {
        return super.getBackground();
    }

    public WallCheckBox(Context context) {
        super(context);
        setButtonDrawable(R.drawable.checkbox_on);
    }

    public WallCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        setButtonDrawable(R.drawable.checkbox_on);
    }

    public WallCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setButtonDrawable(R.drawable.checkbox_on);
    }
}
