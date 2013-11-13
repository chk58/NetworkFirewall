package chk.android.networkfirewall;

import android.app.Application;

public class MyApplication extends Application {
    public static boolean sAsRoot = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if (getApplicationInfo().uid == 1000) {
            sAsRoot = false;
        } else {
            sAsRoot = true;
        }
    }
}
