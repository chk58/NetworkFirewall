package chk.android.networkfirewall;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppInfo extends ApplicationInfo implements Comparable<AppInfo> {

    public String label;
    public Drawable icon;
    public long lastUpdateTime;
    public boolean disabledWifi = false;
    public boolean disabled3g = false;
    public boolean processingWifi = false;
    public boolean processing3g = false;

    public AppInfo(PackageManager pm, ApplicationInfo orig, String l,
            long lastUpdateTime) {
        super(orig);
        label = l;
        icon = loadIcon(pm);
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public int compareTo(AppInfo another) {
        if (lastUpdateTime > another.lastUpdateTime) return -1;
        if (lastUpdateTime < another.lastUpdateTime) return 1;
        return 0;
    }
}
