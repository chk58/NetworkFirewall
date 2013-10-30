package chk.android.networkfirewall;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
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
import android.widget.Adapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import chk.android.networkfirewall.ApplicationListLoader.LoaderParams;

public class ApplicationListActivity extends ListActivity implements
        LoaderCallbacks<ArrayList<AppInfo>> {

    private static final int LOADER_ID = 0;
    private static final int CONTEXT_MENU_APP_INFO = 0;
    private LoaderParams mParams;

    private SearchController mSearchController;
    private ApplicationListLoader mLoader;
    private ApplicationListAdapter mAdatper;
    private View mProgressBar;
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

        mSearchController = new SearchController(this);

        mObserver = new PackageChangedObserver();
        getContentResolver().registerContentObserver(Utils.NOTIFY_URI_PACAKGE_CHANGED, false,
                mObserver);
        registerForContextMenu(getListView());
        mAdatper = new ApplicationListAdapter(this, null, mParams);
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

        mProgressBar.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onDestroy() {
        if (mObserver != null) {
            getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
        unregisterForContextMenu(getListView());
        mLoader = null;
        getLoaderManager().destroyLoader(LOADER_ID);
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
        Adapter adapter = getListAdapter();
        if (adapter == null) {
            return;
        }
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        if (info.position < 0 || info.position >= adapter.getCount()) {
            return;
        }
        AppInfo app = (AppInfo) getListAdapter().getItem(info.position);
        if (app == null) {
            return;
        }
        menu.setHeaderIcon(app.icon);
        menu.setHeaderTitle(app.label);
        menu.add(0, CONTEXT_MENU_APP_INFO, Menu.NONE,
                R.string.context_menu_app_info);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Adapter adapter = getListAdapter();
        if (adapter == null) {
            return false;
        }
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        if (menuInfo.position < 0 || menuInfo.position >= adapter.getCount()) {
            return false;
        }
        AppInfo app = (AppInfo) getListAdapter().getItem(menuInfo.position);
        if (app == null) {
            return false;
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
    public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
        mLoader = new ApplicationListLoader(this, mParams);
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppInfo>> loader,
            ArrayList<AppInfo> data) {
        mAdatper.setAppList(data);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        mAdatper.setAppList(null);
        mLoader = null;
    }
}
