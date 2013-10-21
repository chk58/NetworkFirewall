package chk.android.networkfirewall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import chk.android.networkfirewall.script.Controller;

public class Welcome extends Activity {
    public static final long TIME = 350;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            Welcome wel = (Welcome) msg.obj;
            wel.startActivity(new Intent(wel, ApplicationListActivity.class));
            wel.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time1 = System.currentTimeMillis();
                Controller.initIpTablesIfNecessary(Welcome.this);
                long time2 = System.currentTimeMillis() - time1;

                if (time2 < TIME) {
                    try {
                        Thread.sleep(TIME - time2);
                    } catch (InterruptedException e) {
                        Log.e("chk", e.toString());
                    }
                }

                Message.obtain(mHandler, 0, Welcome.this).sendToTarget();
            }
        }).start();
    }
}
