package chk.android.networkfirewall.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public abstract class BaseContent {
    public static final String AUTHORITY = "chk.android.networkfirewall.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String COLUMN_ID = "_id";

    public long mId = -1;

    // Write the Content into a ContentValues container
    public abstract ContentValues toContentValues();

    // Read the Content from a ContentCursor
    public abstract void restore(Cursor cursor);
}
