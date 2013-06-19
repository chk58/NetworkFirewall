package chk.android.networkfirewall.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import chk.android.networkfirewall.AppInfo;

public class NetworkFirewall extends BaseContent {

    public static final String CONTENT_PATH = "networkfirewall";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BaseContent.CONTENT_URI, CONTENT_PATH);

    public static final String TABLE_NAME = "NetworkFirewall";

    public static final int DISABLE_FLAG_WIFI = 1 << 0;
    public static final int DISABLE_FLAG_3G = 1 << 1;

    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_PACKAGE_NAME = "packageName";
    public static final String COLUMN_DISABLE_FLAGS = "enableFlags";
    public static final String COLUMN_TIMESTAMP = "timeStamp";

    public static final String[] PROJECTION_ALL = {
        COLUMN_ID,
        COLUMN_UID,
        COLUMN_PACKAGE_NAME,
        COLUMN_DISABLE_FLAGS,
        COLUMN_TIMESTAMP
    };

    public int mUid;
    public String mPackageName;
    public boolean mWifiDisable;
    public boolean m3gDisable;
    public long mTimeStamp;

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_UID, mUid);
        values.put(COLUMN_PACKAGE_NAME, mPackageName);

        int disableFlags = 0;
        if (mWifiDisable)
            disableFlags |= DISABLE_FLAG_WIFI;
        if (m3gDisable)
            disableFlags |= DISABLE_FLAG_3G;
        values.put(COLUMN_DISABLE_FLAGS, disableFlags);
        values.put(COLUMN_TIMESTAMP, mTimeStamp);
        return values;
    }

    @Override
    public void restore(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        mUid = cursor.getInt(cursor.getColumnIndex(COLUMN_UID));
        mPackageName = cursor.getString(cursor
                .getColumnIndex(COLUMN_PACKAGE_NAME));
        mWifiDisable = (cursor
                .getInt(cursor.getColumnIndex(COLUMN_DISABLE_FLAGS)) & DISABLE_FLAG_WIFI) != 0;
        m3gDisable = (cursor.getInt(cursor.getColumnIndex(COLUMN_DISABLE_FLAGS)) & DISABLE_FLAG_3G) != 0;
        mTimeStamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
    }

    public static Cursor queryAll(Context context) {
        return context.getContentResolver().query(CONTENT_URI, PROJECTION_ALL,
                null, null, null);
    }

    public static int findUidByPackageName(Context context, String packageName) {
        Cursor c = null;
        int result = -1;
        try {
            c = context.getContentResolver().query(CONTENT_URI, PROJECTION_ALL,
                    COLUMN_PACKAGE_NAME + " LIKE ?", new String[] { packageName }, null);
            if (c != null && c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex(COLUMN_UID));
            }
        } finally {
            if (c != null) c.close();
        }
        return result;
    }

    public static int deleteByUid(Context context, String uid) {
        int result = 0;
        try {
            result = context.getContentResolver().delete(CONTENT_URI,
                    COLUMN_UID + " = ?", new String[] { uid });
        } finally {
        }
        return result;
    }

    public static NetworkFirewall queryByUid(Context context, String uid) {
        NetworkFirewall n = null;
        Cursor c = null;
        try {
            c = context.getContentResolver().query(CONTENT_URI, PROJECTION_ALL,
                    COLUMN_UID + " = ?", new String[] { uid }, null);
            if (c != null && c.moveToFirst()) {
                n = new NetworkFirewall();
                n.restore(c);
            }
        } finally {
            if (c != null) c.close();
        }
        return n;
    }

    public static NetworkFirewall fromAppInfo(AppInfo app) {
        NetworkFirewall nf = new NetworkFirewall();
        nf.mUid = app.uid;
        nf.mPackageName = app.packageName;
        nf.m3gDisable = app.disabled3g;
        nf.mWifiDisable = app.disabledWifi;
        return nf;
    }

    public static void insertOrUpdate(Context context, AppInfo app) {
        String uid = String.valueOf(app.uid);
        NetworkFirewall origin = queryByUid(context, uid);
        if (origin == null) {
            origin = fromAppInfo(app);
            origin.mTimeStamp = System.currentTimeMillis();
            context.getContentResolver().insert(CONTENT_URI, origin.toContentValues());
        } else {
            ContentValues values = new ContentValues();

            int enableFlags = 0;
            if (app.disabledWifi)
                enableFlags |= DISABLE_FLAG_WIFI;
            if (app.disabled3g)
                enableFlags |= DISABLE_FLAG_3G;

            values.put(COLUMN_DISABLE_FLAGS, enableFlags);
            values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
            context.getContentResolver().update(CONTENT_URI, values,
                    COLUMN_UID + " = " + uid, null);
        }
    }
}
