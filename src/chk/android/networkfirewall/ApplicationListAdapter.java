package chk.android.networkfirewall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import chk.android.networkfirewall.script.Script;

public class ApplicationListAdapter extends BaseAdapter implements
        OnClickListener {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ArrayList<AppInfo> mAppList;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public ApplicationListAdapter(Context context, ArrayList<AppInfo> list) {
        mInflater = LayoutInflater.from(context);
        mAppList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        tv.setText("(" + String.valueOf(app.uid) + ") " + app.label);

        tv = (TextView) v.findViewById(R.id.last_update_time);
        String time = "-";
        try {
            time = DATE_FORMAT.format(new Date(app.lastUpdateTime));
        } catch (Exception e) {
        }
        tv.setText(time);

        tv = (TextView) v.findViewById(R.id.app_package_name);
        tv.setText(app.packageName);

        Switch s = (Switch) v.findViewById(R.id.app_wifi);
        s.setChecked(!app.disabledWifi);
        s.setTag(app.uid);
        s.setOnClickListener(this);

        s = (Switch) v.findViewById(R.id.app_3g);
        s.setChecked(!app.disabled3g);
        s.setTag(app.uid);
        s.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        Switch s = ((Switch) v);
        String uid = v.getTag().toString();
        int mode = Script.NETWORK_MODE_WIFI;
        if (v.getId() == R.id.app_wifi) {
            mode = Script.NETWORK_MODE_WIFI;
        } else if (v.getId() == R.id.app_3g) {
            mode = Script.NETWORK_MODE_3G;
        } else {
            throw new IllegalArgumentException("Unknow click.");
        }

        AppInfo a = findAppInfoByUid(Integer.parseInt(uid));

        switch (mode) {
        case Script.NETWORK_MODE_WIFI:
            a.disabledWifi = !s.isChecked();
            break;
        case Script.NETWORK_MODE_3G:
            a.disabled3g = !s.isChecked();
            break;
        default:
            throw new IllegalArgumentException("Unknow network mode : " + mode);
        }

        Script.handleApp(mContext, a, mode);
    }

    private AppInfo findAppInfoByUid(int uid) {
        for (AppInfo a : mAppList) {
            if (a.uid == uid) {
                return a;
            }
        }
        return null;
    }
}