package chk.android.networkfirewall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.view.Menu;
import chk.android.networkfirewall.script.Script;

public class ApplicationListActivity extends ListActivity implements
        LoaderCallbacks<ArrayList<AppInfo>> {

    private static final int LOADER_ID = 0;

    private ApplicationListAdapter mAdapter;
    private int mMyUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_list);
        mMyUid = getApplicationInfo().uid;

        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.application_list, menu);
        return true;
    }

    private ArrayList<AppInfo> createAppList() {
        final ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
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
                    || uid == mMyUid) {
                continue;
            }

            // if (checkSystemApp(a)) {
            // continue;
            // }

            if (!checkNetWorkPermission(p)) {
                continue;
            }

            AppInfo app = new AppInfo(pm, a, p.lastUpdateTime);
            app.disabledWifi = rejectedWifi.contains(uid);
            app.disabled3g = rejected3g.contains(uid);

            appList.add(app);
        }

        Collections.sort(appList);
        return appList;
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

    @Override
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        return (new AsyncTaskLoader<ArrayList<AppInfo>>(this) {
            @Override
            public ArrayList<AppInfo> loadInBackground() {
                return createAppList();
            }
        });
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader,
            ArrayList<AppInfo> data) {
        mAdapter = new ApplicationListAdapter(ApplicationListActivity.this,
                data);
        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        setListAdapter(null);
    }
}
