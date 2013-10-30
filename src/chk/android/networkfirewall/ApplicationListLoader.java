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
import chk.android.networkfirewall.controller.Controller;

public class ApplicationListLoader extends AsyncTaskLoader<ArrayList<AppInfo>> {

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
    public ArrayList<AppInfo> loadInBackground() {
        return createAppList();
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

    private ArrayList<AppInfo> createAppList() {
        final ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
        final PackageManager pm = getContext().getPackageManager();
        final List<PackageInfo> list = pm
                .getInstalledPackages(PackageManager.GET_GIDS);
        final int myUid = getContext().getApplicationInfo().uid;
        final File file = new File(getContext().getCacheDir(),
                Controller.SCRIPT_FILE);
        final ArrayList<Integer> rejectedWifi = Controller.getAllRejectedApps(file,
                Controller.NETWORK_MODE_WIFI);
        final ArrayList<Integer> rejected3g = Controller.getAllRejectedApps(file,
                Controller.NETWORK_MODE_3G);
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

            if (mParams.showOnlyDisabledApps && !disabledWifi && !disabled3g) {
                continue;
            }

            String label = a.loadLabel(pm).toString();
            if (!TextUtils.isEmpty(mParams.query)) {
                boolean hit = false;
                if (!TextUtils.isEmpty(label)
                        && label.toLowerCase(Locale.ENGLISH).contains(
                                mParams.query.toLowerCase(Locale.ENGLISH))) {
                    hit = true;
                }
                if (!TextUtils.isEmpty(a.packageName)
                        && a.packageName.toLowerCase(Locale.ENGLISH).contains(
                                mParams.query.toLowerCase(Locale.ENGLISH))) {
                    hit = true;
                }
                if (!hit) {
                    continue;
                }
            }

            AppInfo app = new AppInfo(pm, a, label, p.lastUpdateTime);
            app.disabledWifi = disabledWifi;
            app.disabled3g = disabled3g;

            appList.add(app);
        }

        Collections.sort(appList);
        return appList;
    }
}
