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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import chk.android.networkfirewall.ApplicationListLoader.LoaderParams;
import chk.android.networkfirewall.controller.Controller;
import chk.android.networkfirewall.view.WallCheckBox;
import chk.android.networkfirewall.view.WallCheckBox.OnStartProcessListener;

public class ApplicationListAdapter extends BaseAdapter implements
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
                } else if (mode == Controller.NETWORK_MODE_3G) {
                    app.processing3g = false;
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
            AppInfo app = (AppInfo) msg.obj;
            Message main = new Message();
            try {
                Controller.handleApp(adatper.mContext, app, mode);
                main.copyFrom(msg);
                main.what = MAIN_MSG_PROCESS_SUCCESSED;
            } catch (NoPermissionException e) {
                main.what = MAIN_MSG_PROCESS_FAILED;
                Log.e(Utils.TAG, "Has no permission to run iptables");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    @Override
    public int getCount() {
        if (mAppList == null) {
            return 0;
        }
        return mAppList.size();
    }

    @Override
    public AppInfo getItem(int position) {
        if (mAppList == null) {
            return null;
        }
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    public void setAppList(ArrayList<AppInfo> appList) {
        mAppList = appList;
        if (appList != null) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
            if (mProcessThread != null) {
                mProcessThread.quit();
            }
            mProcessThread = null;
            mMainHanlder = null;
            mProcessHanlder = null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mAppList == null) {
            return null;
        }
        View v;
        if (convertView != null) {
            v = convertView;
        } else {
            v = mInflater.inflate(R.layout.application_list_item, null);
        }

        AppInfo app = mAppList.get(position);
        ImageView iv = (ImageView) v.findViewById(R.id.app_icon);
        iv.setImageDrawable(app.icon);

        TextView tv = (TextView) v.findViewById(R.id.app_label);
        // SpannableStringBuilder ssb = new SpannableStringBuilder("(" +
        // String.valueOf(app.uid) + ") ");
        // ssb.append(Utils.highlightQuery(mParams.query, app.label));
        // tv.setText(ssb);
        tv.setText(Utils.highlightQuery(mParams.query, app.label));

        tv = (TextView) v.findViewById(R.id.last_update_time);
        String time = "-";
        try {
            time = DATE_FORMAT.format(new Date(app.lastUpdateTime));
        } catch (Exception e) {
        }
        tv.setText(time);

        tv = (TextView) v.findViewById(R.id.app_package_name);
        tv.setText(Utils.highlightQuery(mParams.query, app.packageName));

        // Switch s = (Switch) v.findViewById(R.id.app_wifi);
        // s.setChecked(!app.disabledWifi);
        // s.setTag(app.uid);
        // // s.setEnabled(false);
        // s.setOnClickListener(this);
        //
        // s = (Switch) v.findViewById(R.id.app_3g);
        // s.setChecked(!app.disabled3g);
        // s.setTag(app.uid);
        // // s.setEnabled(false);
        // s.setOnClickListener(this);

        WallCheckBox checkBox = (WallCheckBox) v.findViewById(R.id.checkbox_wifi);
        checkBox.setStatus(!app.disabledWifi, app.processingWifi);
        checkBox.setTag(position);
        checkBox.setOnStartProcessListener(this);

        checkBox = (WallCheckBox) v.findViewById(R.id.checkbox_3g);
        checkBox.setStatus(!app.disabled3g, app.processing3g);
        checkBox.setTag(position);
        checkBox.setOnStartProcessListener(this);
        // ImageButton button = (ImageButton) v.findViewById(R.id.button);
        // TransitionDrawable drawable = (TransitionDrawable)
        // button.getDrawable();
        // drawable.startTransition(500);

        return v;
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
            a.disabledWifi = !a.disabledWifi;
        } else if (view.getId() == R.id.checkbox_3g) {
            mode = Controller.NETWORK_MODE_3G;
            a.processing3g = true;
            a.disabled3g = !a.disabled3g;
        } else {
            throw new IllegalArgumentException("Unknow click.");
        }

        Message m = new Message();
        m.obj = a;
        m.arg1 = mode;
        mProcessHanlder.sendMessage(m);
    }
}
