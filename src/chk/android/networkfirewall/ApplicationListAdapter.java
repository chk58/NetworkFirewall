package chk.android.networkfirewall;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import chk.android.networkfirewall.AppInfo.AppInfoListUid;
import chk.android.networkfirewall.ApplicationListLoader.LoaderParams;
import chk.android.networkfirewall.controller.Controller;
import chk.android.networkfirewall.view.WallCheckBox;
import chk.android.networkfirewall.view.WallCheckBox.OnStartProcessListener;

public class ApplicationListAdapter extends BaseExpandableListAdapter implements
        OnStartProcessListener {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int MAIN_MSG_PROCESS_SUCCESSED = 0;
    private static final int MAIN_MSG_PROCESS_FAILED = 1;
    private ArrayList<AppInfo> mAppList;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final LoaderParams mParams;

    private MainHanlder mMainHanlder;
    private ProcessHanlder mProcessHanlder;
    private HandlerThread mProcessThread;

    private static class MainHanlder extends Handler {
        private SoftReference<ApplicationListAdapter> mReference;
        private MainHanlder(ApplicationListAdapter adatper) {
            mReference = new SoftReference<ApplicationListAdapter>(adatper);
        }

        @Override
        public void handleMessage(Message msg) {
            ApplicationListAdapter adapter = mReference.get();
            if (adapter == null)
                return;

            switch (msg.what) {
            case MAIN_MSG_PROCESS_FAILED:
                adapter.setAppList(null);
                break;
            case MAIN_MSG_PROCESS_SUCCESSED:
                int mode = msg.arg1;
                AppInfo app = (AppInfo) msg.obj;
                if (adapter.mAppList == null || !adapter.mAppList.contains(app)) {
                    return;
                }
                if (mode == Controller.NETWORK_MODE_WIFI) {
                    app.processingWifi = false;
                    app.disabledWifi = !app.disabledWifi;
                } else if (mode == Controller.NETWORK_MODE_3G) {
                    app.processing3g = false;
                    app.disabled3g = !app.disabled3g;
                }
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private static class ProcessHanlder extends Handler {
        private SoftReference<ApplicationListAdapter> mReference;

        private ProcessHanlder(ApplicationListAdapter adatper, Looper looper) {
            super(looper);
            mReference = new SoftReference<ApplicationListAdapter>(adatper);
        }

        @Override
        public void handleMessage(Message msg) {
            ApplicationListAdapter adatper = mReference.get();
            if (adatper == null)
                return;

            int mode = msg.arg1;
            // we need a new AppInfo, since the process may delay
            AppInfo app = new AppInfo((AppInfo) msg.obj);
            if (mode == Controller.NETWORK_MODE_WIFI) {
                app.disabledWifi = !app.disabledWifi;
            } else if (mode == Controller.NETWORK_MODE_3G) {
                app.disabled3g = !app.disabled3g;
            }
            Message main = new Message();
            try {
                Controller.handleApp(adatper.mContext, app, mode);
                main.copyFrom(msg);
                main.what = MAIN_MSG_PROCESS_SUCCESSED;
            } catch (NoPermissionException e) {
                main.what = MAIN_MSG_PROCESS_FAILED;
                Log.e(Utils.TAG,
                        "Has no permission to run iptables : " + e.toString());
            }
            // try {
            // Thread.sleep(1000);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            if (adatper.mMainHanlder != null) {
                adatper.mMainHanlder.sendMessage(main);
            }
        }
    }

    public ApplicationListAdapter(Context context, ArrayList<AppInfo> list,
            LoaderParams params) {
        mInflater = LayoutInflater.from(context);
        mAppList = list;
        mContext = context;
        mParams = params;
    }

    public void setAppList(ArrayList<AppInfo> appList) {
        if (mProcessThread != null) {
            mProcessThread.quit();
        }
        mProcessThread = null;
        mMainHanlder = null;
        mProcessHanlder = null;

        mAppList = appList;
        if (appList != null) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }

    @Override
    public void OnStartProcess(WallCheckBox view) {
        if (mMainHanlder == null) {
            mMainHanlder = new MainHanlder(this);
        }

        if (mProcessThread == null) {
            mProcessThread = new HandlerThread("Process");
            mProcessThread.start();
            mProcessHanlder = new ProcessHanlder(this,
                    mProcessThread.getLooper());
        }

        int position = Integer.parseInt(view.getTag().toString());
        if (mAppList == null) {
            mMainHanlder.obtainMessage(MAIN_MSG_PROCESS_FAILED).sendToTarget();
            return;
        }
        AppInfo a = null;
        if (position >= 0 && position < mAppList.size()) {
            a = mAppList.get(position);
        }
        if (a == null) {
            mMainHanlder.obtainMessage(MAIN_MSG_PROCESS_FAILED).sendToTarget();
            return;
        }

        int mode;
        if (view.getId() == R.id.checkbox_wifi) {
            mode = Controller.NETWORK_MODE_WIFI;
            a.processingWifi = true;
        } else if (view.getId() == R.id.checkbox_3g) {
            mode = Controller.NETWORK_MODE_3G;
            a.processing3g = true;
        } else {
            throw new IllegalArgumentException("Unknow click.");
        }

        Message m = new Message();
        m.obj = a;
        m.arg1 = mode;
        mProcessHanlder.sendMessage(m);
    }

    @Override
    public int getGroupCount() {
        return mAppList == null ? 0 : mAppList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int result = 0;
        if (mAppList != null && groupPosition >= 0
                && groupPosition < mAppList.size()) {
            AppInfo app = mAppList.get(groupPosition);
            if (app instanceof AppInfoListUid) {
                result = ((AppInfoListUid) app).getCount();
            }
        }
        return result;
    }

    @Override
    public AppInfo getGroup(int groupPosition) {
        if (mAppList != null && groupPosition >= 0
                && groupPosition < mAppList.size()) {
            return mAppList.get(groupPosition);
        }
        return null;
    }

    @Override
    public AppInfo getChild(int groupPosition, int childPosition) {
        if (mAppList != null && groupPosition >= 0
                && groupPosition < mAppList.size()) {
            AppInfo app = mAppList.get(groupPosition);
            if (app instanceof AppInfoListUid) {
                AppInfoListUid appUidList = (AppInfoListUid) app;
                if (childPosition >= 0 && childPosition < appUidList.getCount()) {
                    return appUidList.get(childPosition);
                }
            }
        }
        return null;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        if (mAppList == null || groupPosition < 0
                || groupPosition >= mAppList.size()) {
            return null;
        }

        View v;
        if (convertView != null) {
            v = convertView;
        } else {
            v = mInflater.inflate(R.layout.application_list_item, null);
        }
        int childrenCount = getChildrenCount(groupPosition);
        boolean isGroup = childrenCount > 0;
        AppInfo app = mAppList.get(groupPosition);
        ImageView iv = (ImageView) v.findViewById(R.id.app_icon);
        if (isGroup) {
            if (isExpanded) {
                iv.setImageResource(android.R.drawable.arrow_up_float);
            } else {
                iv.setImageResource(android.R.drawable.arrow_down_float);
            }
        } else {
            iv.setImageDrawable(app.icon);
        }

        TextView tv = (TextView) v.findViewById(R.id.app_label);
        // SpannableStringBuilder ssb = new SpannableStringBuilder("(" +
        // String.valueOf(app.uid) + ") ");
        // ssb.append(Utils.highlightQuery(mParams.query, app.label));
        // tv.setText(ssb);
        if (isGroup) {
            tv.setText(mContext.getResources().getString(
                    R.string.item_title_pacakges_same_uid, childrenCount));
        } else {
            tv.setText(Utils.highlightQuery(mParams.query, app.label));
        }

        tv = (TextView) v.findViewById(R.id.last_update_time);
        String time = "-";
        try {
            time = DATE_FORMAT.format(new Date(app.lastUpdateTime));
        } catch (Exception e) {
        }
        tv.setText(time);

        tv = (TextView) v.findViewById(R.id.app_package_name);
        if (isGroup) {
            tv.setText(R.string.item_pacakges_same_uid);
        } else {
            tv.setText(Utils.highlightQuery(mParams.query, app.packageName));
        }

        WallCheckBox checkBox = (WallCheckBox) v
                .findViewById(R.id.checkbox_wifi);
        checkBox.setStatus(!app.disabledWifi, app.processingWifi);
        checkBox.setTag(groupPosition);
        checkBox.setOnStartProcessListener(this);

        checkBox = (WallCheckBox) v.findViewById(R.id.checkbox_3g);
        checkBox.setStatus(!app.disabled3g, app.processing3g);
        checkBox.setTag(groupPosition);
        checkBox.setOnStartProcessListener(this);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        if (mAppList == null || groupPosition < 0
                || groupPosition >= mAppList.size()) {
            return null;
        }

        AppInfo a = mAppList.get(groupPosition);
        if (!(a instanceof AppInfoListUid)) {
            return null;
        }
        AppInfoListUid appUidList = (AppInfoListUid) a;

        if (childPosition < 0 || childPosition >= appUidList.getCount()) {
            return null;
        }

        AppInfo appUid = appUidList.get(childPosition);

        View v;
        if (convertView != null) {
            v = convertView;
        } else {
            v = mInflater.inflate(R.layout.same_uid_list_item, null);
        }

        ImageView iv = (ImageView) v.findViewById(R.id.app_icon);
        iv.setImageDrawable(appUid.icon);

        TextView tv = (TextView) v.findViewById(R.id.app_label);
        // SpannableStringBuilder ssb = new SpannableStringBuilder("(" +
        // String.valueOf(app.uid) + ") ");
        // ssb.append(Utils.highlightQuery(mParams.query, app.label));
        // tv.setText(ssb);

        tv.setText(Utils.highlightQuery(mParams.query, appUid.label));


        tv = (TextView) v.findViewById(R.id.last_update_time);
        String time = "-";
        try {
            time = DATE_FORMAT.format(new Date(appUid.lastUpdateTime));
        } catch (Exception e) {
        }
        tv.setText(time);

        tv = (TextView) v.findViewById(R.id.app_package_name);
        tv.setText(Utils.highlightQuery(mParams.query, appUid.packageName));

        return v;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
