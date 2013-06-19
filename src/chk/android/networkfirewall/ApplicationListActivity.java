package chk.android.networkfirewall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.Menu;
import chk.android.networkfirewall.script.Script;

public class ApplicationListActivity extends ListActivity {
    private ApplicationListAdapter mAdapter;
    private ArrayList<AppInfo> mAppList = new ArrayList<AppInfo>();

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            mAdapter = new ApplicationListAdapter(ApplicationListActivity.this,
                    mAppList);
            setListAdapter(mAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_list);

        getActionBar().setTitle(
                getApplicationInfo().loadLabel(getPackageManager()) + " ("
                        + String.valueOf(getApplicationInfo().uid) + ")");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Script.initIpTablesIfNecessary(ApplicationListActivity.this);
                initAppList();
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.application_list, menu);
        return true;
    }

    private void initAppList() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> list = pm
                .getInstalledPackages(PackageManager.GET_GIDS);
        final File file = new File(getCacheDir(), Script.SCRIPT_FILE);
        ArrayList<Integer> rejectedWifi = Script.getAllRejectedApps(file,
                Script.NETWORK_MODE_WIFI);
        ArrayList<Integer> rejected3g = Script.getAllRejectedApps(file,
                Script.NETWORK_MODE_3G);
        int uid;
        for (PackageInfo p : list) {
            ApplicationInfo a = p.applicationInfo;
            if (a == null) {
                continue;
            }
            uid = a.uid;
            if (uid < Process.FIRST_APPLICATION_UID
                    || uid == getApplicationInfo().uid) {
                continue;
            }

            if (checkSystemApp(a)) {
                continue;
            }

            if (!checkNetWorkPermission(p)) {
                continue;
            }

            AppInfo app = new AppInfo(pm, a, p.lastUpdateTime);
            app.disabledWifi = rejectedWifi.contains(uid);
            app.disabled3g = rejected3g.contains(uid);

            mAppList.add(app);
        }

        Collections.sort(mAppList);
    }

    private boolean checkSystemApp(ApplicationInfo a) {
        return (a.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private boolean checkNetWorkPermission(PackageInfo p) {
        boolean result = false;
        int[] gids = p.gids;
        if (gids != null && gids.length > 0) {
            for (int g : gids) {
                if (g == 3003) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
