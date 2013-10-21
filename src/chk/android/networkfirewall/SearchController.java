package chk.android.networkfirewall;

import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

public class SearchController implements SearchView.OnQueryTextListener {

    private static final int QUERY_PENDING_TIME = 500;
    private static final int MESSAGE_PENDING_QUERY = 1;

    private final ActionBar mActionBar;
    private final ApplicationListActivity mActivity;
    private final ViewGroup mActionBarCustomView;
    private SearchView mSearchView;
    private View mAppName;

    private final DelayedHandler mDelayedHandler;

    private final static class DelayedHandler extends Handler {
        WeakReference<ApplicationListActivity> mReference;

        public DelayedHandler(ApplicationListActivity activity) {
            mReference = new WeakReference<ApplicationListActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ApplicationListActivity activity = mReference.get();
            if (activity != null && !activity.isFinishing()
                    && !activity.isDestroyed()) {
                String text = msg.obj.toString();
                activity.startQuery(text);
            }
        }

        public void sendQueryMessage(CharSequence text) {
            Message m = obtainMessage(MESSAGE_PENDING_QUERY, text);
            sendMessageDelayed(m, QUERY_PENDING_TIME);
        }

        public void removePendingMessage() {
            removeMessages(MESSAGE_PENDING_QUERY);
        }
    }

    public SearchController(ApplicationListActivity activity) {
        mActivity = activity;
        mActionBar = activity.getActionBar();
        mDelayedHandler = new DelayedHandler(activity);
        mActionBarCustomView = (ViewGroup) mActionBar.getCustomView();
        mAppName = mActionBarCustomView.findViewById(R.id.app_name);
        mSearchView = (SearchView) mActionBarCustomView
                .findViewById(R.id.search_view);
        mSearchView.setSubmitButtonEnabled(false);
    }

    public void showSearchBar() {
        if (mSearchView.getVisibility() != View.VISIBLE) {
            mAppName.setVisibility(View.GONE);
            mSearchView.setQuery(null, false);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.onActionViewExpanded();
            mSearchView.requestFocus();
        }
    }

    public void hideSearchBar() {
        if (mSearchView.getVisibility() != View.GONE) {
            mSearchView.setVisibility(View.GONE);
            mSearchView.setOnQueryTextListener(null);
            mSearchView.setQuery(null, false);
            mAppName.setVisibility(View.VISIBLE);
            mActivity.startQuery(null);
        }
    }

    public boolean onBackPressed() {
        if (mSearchView.getVisibility() == View.VISIBLE) {
            hideSearchBar();
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText != null) {
            mDelayedHandler.removePendingMessage();
            mDelayedHandler.sendQueryMessage(newText.trim());
        }
        return true;
    }
}
