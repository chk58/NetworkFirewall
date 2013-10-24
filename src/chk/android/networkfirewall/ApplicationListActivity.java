package chk.android.networkfirewall;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import chk.android.networkfirewall.ApplicationListLoader.LoaderParams;

public class ApplicationListActivity extends ListActivity implements
        LoaderCallbacks<ArrayList<AppInfo>> {

    private static final int LOADER_ID = 0;

    private LoaderParams mParams;

    private SearchController mSearchController;
    private ApplicationListAdapter mAdapter;
    private ApplicationListLoader mLoader;
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

        loadAppList();
    }

    @Override
    protected void onDestroy() {
        if (mObserver != null) {
            getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!mSearchController.onBackPressed()) {
            super.onBackPressed();
        }
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
        case R.id.search:
            mSearchController.showSearchBar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAppList() {
        if (mLoader == null) {
            LoaderManager lm = getLoaderManager();
            lm.initLoader(LOADER_ID, null, this);
        } else {
            mLoader.setLoaderParams(mParams);
            mLoader.onContentChanged();
        }

        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void swtichSysApps(boolean show) {
        if (mParams.showSysApps != show) {
            mParams.showSysApps = show;
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
        mAdapter = new ApplicationListAdapter(ApplicationListActivity.this,
                data, mParams);
        setListAdapter(mAdapter);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        setListAdapter(null);
        mLoader = null;
    }
}
