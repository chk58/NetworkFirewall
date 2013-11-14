package chk.android.networkfirewall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import chk.android.networkfirewall.AppInfo.AppInfoListUid;
import chk.android.networkfirewall.controller.Controller;

public class ApplicationListLoader extends AsyncTaskLoader<Object> {

    public static class LoaderParams {
        public boolean showSysApps = false;
        public boolean showOnlyDisabledApps = false;
        public String query;
    }

    private LoaderParams mParams;

    public void setLoaderParams(LoaderParams params) {
        mParams = params;
    }

    public ApplicationListLoader(Context context, LoaderParams params) {
        super(context);
        mParams = params;
    }

    @Override
    public Object loadInBackground() {
        try {
            return createAppList();
        } catch (NoPermissionException e) {
            return e;
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        cancelLoad();
    }

    private ArrayList<AppInfo> createAppList() throws NoPermissionException {

        final PackageManager pm = getContext().getPackageManager();
        List<PackageInfo> list = null;
        try {
            list = pm.getInstalledPackages(PackageManager.GET_GIDS);
        } catch (Exception e) {
            Log.e(Utils.TAG, e.toString());
            return null;
        }

        final ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
        final SparseArray<AppInfo> appMap = new SparseArray<AppInfo>();
        final int myUid = getContext().getApplicationInfo().uid;
        final File file = new File(getContext().getCacheDir(),
                Controller.SCRIPT_FILE);
        final ArrayList<Integer> rejectedWifi = Controller
                .getAllRejectedApps(file,
                Controller.NETWORK_MODE_WIFI);
        final ArrayList<Integer> rejected3g = Controller
                .getAllRejectedApps(file, Controller.NETWORK_MODE_3G);
        int uid;
        for (PackageInfo p : list) {
            ApplicationInfo a = p.applicationInfo;
            if (a == null) {
                continue;
            }
            uid = a.uid;
            if (uid < Process.FIRST_APPLICATION_UID || uid == myUid) {
                continue;
            }

            if (Utils.checkSystemApp(a) && !mParams.showSysApps) {
                continue;
            }

            if (!Utils.checkNetWorkPermission(p)) {
                continue;
            }

            boolean disabledWifi = rejectedWifi.contains(uid);
            boolean disabled3g = rejected3g.contains(uid);

            if (mParams.showOnlyDisabledApps && !disabledWifi
                    && !disabled3g) {
                continue;
            }

            String label = a.loadLabel(pm).toString();

            AppInfo app = new AppInfo(pm, a, label, p.lastUpdateTime);
            app.disabledWifi = disabledWifi;
            app.disabled3g = disabled3g;

            AppInfo appUid = appMap.get(uid);
            if (appUid == null) {
                appMap.put(uid, app);
            } else if (appUid instanceof AppInfoListUid) {
                ((AppInfoListUid) appUid).add(app);
            } else {
                AppInfoListUid uidList = new AppInfoListUid(uid);
                uidList.disabledWifi = disabledWifi;
                uidList.disabled3g = disabled3g;
                uidList.add(appUid);
                uidList.add(app);
                appMap.put(uid, uidList);
            }
        }

        for (int i = 0; i < appMap.size(); i++) {
            AppInfo app = appMap.valueAt(i);
            if (!TextUtils.isEmpty(mParams.query)) {
                if (app instanceof AppInfoListUid) {
                    AppInfoListUid appUidList = (AppInfoListUid) app;
                    boolean match = false;
                    for (int j = 0; j < appUidList.getCount(); j++) {
                        AppInfo appUid = appUidList.get(j);
                        if (matchQuery(appUid, mParams.query)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        continue;
                    }
                } else {
                    if (!matchQuery(app, mParams.query)) {
                        continue;
                    }
                }
            }

            appList.add(app);
        }

        Collections.sort(appList);
        return appList;
    }

    private boolean matchQuery(AppInfo app, String query) {
        if (app instanceof AppInfoListUid) {
            return false;
        }
        boolean hit = false;
        String label = app.label;
        String pacakge = app.packageName;
        if (!TextUtils.isEmpty(label)
                && label.toLowerCase(Locale.ENGLISH).contains(
                        mParams.query.toLowerCase(Locale.ENGLISH))) {
            hit = true;
        }
        if (!TextUtils.isEmpty(pacakge)
                && pacakge.toLowerCase(Locale.ENGLISH).contains(
                        mParams.query.toLowerCase(Locale.ENGLISH))) {
            hit = true;
        }
        return hit;
    }
}
