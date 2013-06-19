package chk.android.networkfirewall.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class FirewallProvider extends ContentProvider {

    private final static String DB_NAME = "FirewallProvider.db";
    private final static int DB_VERSION = 1;

    private final static int NETWORKFIREWALL_BASE = 0;
    private final static int NETWORKFIREWALL = NETWORKFIREWALL_BASE;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    {
        sURIMatcher.addURI(BaseContent.AUTHORITY, NetworkFirewall.CONTENT_PATH, NETWORKFIREWALL);
    }

    private static int findMatch(Uri uri) {
        int match = sURIMatcher.match(uri);
        if (match < 0) {
            throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        return match;
    }

    private SQLiteDatabase mDatabase;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        int match = findMatch(uri);
        SQLiteDatabase db = getDatabase();
        Cursor c = null;
        //String id;

        try {
            switch (match) {
            case NETWORKFIREWALL:
                c = db.query(NetworkFirewall.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } finally {

        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = findMatch(uri);
        SQLiteDatabase db = getDatabase();
        Uri resultUri = null;
        long id;

        try {
            switch (match) {
            case NETWORKFIREWALL:
                id = db.insert(NetworkFirewall.TABLE_NAME, null, values);
                resultUri = ContentUris.withAppendedId(uri, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } finally {

        }
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = findMatch(uri);
        SQLiteDatabase db = getDatabase();
        int result = 0;

        try {
            switch (match) {
            case NETWORKFIREWALL:
                result = db.delete(NetworkFirewall.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } finally {

        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        int match = findMatch(uri);
        SQLiteDatabase db = getDatabase();
        int count = 0;

        try {
            switch (match) {
            case NETWORKFIREWALL:
                count = db.update(NetworkFirewall.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } finally {

        }
        return count;
    }

    private synchronized SQLiteDatabase getDatabase() {
        if (mDatabase != null) {
            return mDatabase;
        }

        DBHelper helper = new DBHelper(getContext(), DB_NAME, null, DB_VERSION);
        mDatabase = helper.getWritableDatabase();
        return mDatabase;
    }

    private static void createNetworkFirewallTable(SQLiteDatabase db) {
        String columns = NetworkFirewall.COLUMN_UID + " integer, "
                + NetworkFirewall.COLUMN_PACKAGE_NAME + " text, "
                + NetworkFirewall.COLUMN_DISABLE_FLAGS + " integer, "
                + NetworkFirewall.COLUMN_TIMESTAMP + " integer "
                + ");";

        String createString = " (" + BaseContent.COLUMN_ID
                + " integer primary key autoincrement, " + columns;

        db.execSQL("create table " + NetworkFirewall.TABLE_NAME + createString);

        String indexColumns[] = {
                NetworkFirewall.COLUMN_UID
            };
        for (String columnName : indexColumns) {
            db.execSQL("create index " + NetworkFirewall.TABLE_NAME + '_' + columnName
                    + " on " + NetworkFirewall.TABLE_NAME + " (" + columnName + ");");
        }
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createNetworkFirewallTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
