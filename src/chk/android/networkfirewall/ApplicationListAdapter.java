package chk.android.networkfirewall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import chk.android.networkfirewall.ApplicationListLoader.LoaderParams;
import chk.android.networkfirewall.controller.Controller;

public class ApplicationListAdapter extends BaseAdapter implements OnClickListener {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ArrayList<AppInfo> mAppList;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final LoaderParams mParams;

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
    public Object getItem(int position) {
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

        CompoundButton cb = (CompoundButton) v.findViewById(R.id.checkbox_wifi);
        cb.setChecked(!app.disabledWifi);
        cb.setTag(position);
        cb.setOnClickListener(this);

        if ("com.google.android.location".equals(app.packageName)) {
            Log.d("chk", "location : " + app.uid);
        }
        if ("com.google.android.gsf.login".equals(app.packageName)) {
            Log.d("chk", "account : " + app.uid);
        }
        cb = (CompoundButton) v.findViewById(R.id.checkbox_3g);
        cb.setChecked(!app.disabled3g);
        cb.setTag(position);
        cb.setOnClickListener(this);
        // ImageButton button = (ImageButton) v.findViewById(R.id.button);
        // TransitionDrawable drawable = (TransitionDrawable)
        // button.getDrawable();
        // drawable.startTransition(500);

        return v;
    }

    @Override
    public void onClick(View v) {
        if (mAppList == null) {
            return;
        }
        CompoundButton cb = ((CompoundButton) v);
        int position = Integer.parseInt(v.getTag().toString());
        int mode = Controller.NETWORK_MODE_WIFI;
        if (v.getId() == R.id.checkbox_wifi) {
            mode = Controller.NETWORK_MODE_WIFI;
        } else if (v.getId() == R.id.checkbox_3g) {
            mode = Controller.NETWORK_MODE_3G;
        } else {
            throw new IllegalArgumentException("Unknow click.");
        }

        AppInfo a = null;
        if (position >= 0 && position < mAppList.size()) {
            a = mAppList.get(position);
        }
        if (a == null) {
            return;
        }

        switch (mode) {
            case Controller.NETWORK_MODE_WIFI:
                a.disabledWifi = !cb.isChecked();
                break;
            case Controller.NETWORK_MODE_3G:
                a.disabled3g = !cb.isChecked();
                break;
            default:
                throw new IllegalArgumentException("Unknow network mode : " + mode);
        }

        try {
            Controller.handleApp(mContext, a, mode);
        } catch (NoPermissionException e) {
            Log.e(Utils.TAG, "Has no permission to run iptables");
        }
    }

    public AppInfo findAppInfoByUid(int uid) {
        for (AppInfo a : mAppList) {
            if (a.uid == uid) {
                return a;
            }
        }
        return null;
    }
}
