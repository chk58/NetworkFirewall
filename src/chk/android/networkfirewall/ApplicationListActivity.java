package chk.android.networkfirewall;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ExpandableListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import chk.android.networkfirewall.AppInfo.AppInfoListUid;
import chk.android.networkfirewall.ApplicationListLoader.LoaderParams;

public class ApplicationListActivity extends ExpandableListActivity implements
        LoaderCallbacks<Object>, OnClickListener {

    private static final int LOADER_ID = 0;
    private static final int CONTEXT_MENU_APP_INFO = 0;
    private LoaderParams mParams;

    private SearchController mSearchController;
    private ApplicationListLoader mLoader;
    private ApplicationListAdapter mAdatper;
    private View mProgressBar;
    private TextView mErrorText;
    private PackageChangedObserver mObserver;

    private class PackageChangedObserver extends ContentObserver {

        public PackageChangedObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            loadAppList();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_list);

        mParams = new LoaderParams();
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.action_bar_custom_view);
        mProgressBar = getActionBar().getCustomView().findViewById(
                R.id.progress_bar);
        mErrorText = (TextView) findViewById(R.id.error_text);
        mErrorText.setOnClickListener(this);
        mSearchController = new SearchController(this);

        mObserver = new PackageChangedObserver();
        getContentResolver().registerContentObserver(Utils.NOTIFY_URI_PACAKGE_CHANGED, false,
                mObserver);
        registerForContextMenu(getExpandableListView());
        mAdatper = new ApplicationListAdapter(this, null, mParams);
        mAdatper.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onInvalidated() {
                mErrorText.setVisibility(View.VISIBLE);
            }
        });
        getExpandableListView().setGroupIndicator(null);
        setListAdapter(mAdatper);
        loadAppList();
    }

    private void loadAppList() {
        if (mLoader == null) {
            LoaderManager lm = getLoaderManager();
            lm.initLoader(LOADER_ID, null, this);
        } else {
            mLoader.onContentChanged();
        }
        mErrorText.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onDestroy() {
        if (mObserver != null) {
            getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
        unregisterForContextMenu(getExpandableListView());
        mLoader = null;
        getLoaderManager().destroyLoader(LOADER_ID);
        mAdatper.setAppList(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!mSearchController.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        ExpandableListAdapter adapter = getExpandableListAdapter();
        if (adapter == null) {
            return;
        }
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        int group = ExpandableListView
                .getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView
                .getPackedPositionChild(info.packedPosition);
        if (group < 0 || group >= adapter.getGroupCount()) {
            return;
        }
        AppInfo app = (AppInfo) adapter.getGroup(group);
        if (app == null) {
            return;
        }

        if (app instanceof AppInfoListUid) {
            AppInfoListUid appUidList = (AppInfoListUid) app;
            if (child < 0 || child >= appUidList.getCount()) {
                return;
            }
            app = appUidList.get(child);
        }

        menu.setHeaderIcon(app.icon);
        menu.setHeaderTitle(app.label);
        menu.add(0, CONTEXT_MENU_APP_INFO, Menu.NONE,
                R.string.context_menu_app_info);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListAdapter adapter = getExpandableListAdapter();
        if (adapter == null) {
            return false;
        }
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
                .getMenuInfo();
        int group = ExpandableListView
                .getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView
                .getPackedPositionChild(info.packedPosition);
        if (group < 0 || group >= adapter.getGroupCount()) {
            return false;
        }
        AppInfo app = (AppInfo) adapter.getGroup(group);
        if (app == null) {
            return false;
        }

        if (app instanceof AppInfoListUid) {
            AppInfoListUid appUidList = (AppInfoListUid) app;
            if (child < 0 || child >= appUidList.getCount()) {
                return false;
            }
            app = appUidList.get(child);
        }

        switch (item.getItemId()) {
        case CONTEXT_MENU_APP_INFO:
            try {
                Uri uri = Uri.parse("package:" + app.packageName);
                Intent i = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                Log.e(Utils.TAG, "Package : " + app.packageName + " not found!");
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.application_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        case R.id.show_sys_app:
            if (item.isCheckable()) {
                boolean check;
                if (item.isChecked()) {
                    item.setChecked(false);
                    check = false;
                } else {
                    item.setChecked(true);
                    check = true;
                }
                swtichSysApps(check);
                return true;
            }
            break;
        case R.id.show_only_disabled:
            if (item.isCheckable()) {
                boolean check;
                if (item.isChecked()) {
                    item.setChecked(false);
                    check = false;
                } else {
                    item.setChecked(true);
                    check = true;
                }
                showOnlyDisalbedApps(check);
                return true;
            }
            break;
        case R.id.search:
            mSearchController.showSearchBar();
            return true;
        case R.id.action_show_app_ops:
            Intent intent = new Intent("android.settings.APP_OPS_SETTINGS");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(Utils.TAG, "APP_OPS_SETTINGS not found!");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void swtichSysApps(boolean show) {
        if (mParams.showSysApps != show) {
            mParams.showSysApps = show;
            loadAppList();
        }
    }

    public void showOnlyDisalbedApps(boolean only) {
        if (mParams.showOnlyDisabledApps != only) {
            mParams.showOnlyDisabledApps = only;
            loadAppList();
        }
    }

    public void startQuery(String text) {
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(mParams.query)) {
            return;
        }
        if (text != null && text.equalsIgnoreCase(mParams.query)) {
            return;
        }
        mParams.query = text;
        loadAppList();
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        mLoader = new ApplicationListLoader(this, mParams);
        return mLoader;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        ArrayList<AppInfo> appList = null;
        if (data instanceof NoPermissionException) {
            Log.e(Utils.TAG,
                    "Has no permission to run iptables : " + data.toString());
            mErrorText.setVisibility(View.VISIBLE);
        } else if (data instanceof ArrayList<?>) {
            appList = (ArrayList<AppInfo>) data;
            mErrorText.setVisibility(View.GONE);
        }
        mAdatper.setAppList(appList);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        mAdatper.setAppList(null);
        mLoader = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.error_text) {
            loadAppList();
        }
    }
}
