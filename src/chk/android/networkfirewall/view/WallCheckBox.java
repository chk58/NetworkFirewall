package chk.android.networkfirewall.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

public class WallCheckBox extends CompoundButton {

    public WallCheckBox(Context context) {
        super(context);
        init();
    }

    public WallCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WallCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setClickable(true);
        Drawable d = new WallCheckBoxDrawable(getContext().getResources());
        setButtonDrawable(d);
    }
}
