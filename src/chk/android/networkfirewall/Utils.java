package chk.android.networkfirewall;

import java.util.Locale;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;

public class Utils {
    public static final int HIGHLIGHT_COLOR_INT = 0xFF09AFED;
    public static final Uri NOTIFY_URI = Uri
            .parse("networkfirewall://package_changed");

    public static boolean checkSystemApp(ApplicationInfo a) {
        return (a.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static boolean checkNetWorkPermission(PackageInfo p) {
        boolean result = false;
        int[] gids = p.gids;
        if (gids != null && gids.length > 0) {
            for (int g : gids) {
                if (g == 3003) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static CharSequence highlightQuery(String query, String text) {
        if (!TextUtils.isEmpty(text) && query != null && query.trim().length() > 0) {
            String temp = query.trim();
            if (text.toLowerCase(Locale.ENGLISH).contains(
                    temp.toLowerCase(Locale.ENGLISH))) {
                final SpannableStringBuilder sb = new SpannableStringBuilder();
                int length = temp.length();
                int i = 0;
                int pre = 0;
                while (i < text.length()) {
                    if (text.regionMatches(true, i, temp, 0, length)) {
                        sb.append(text.substring(pre, i));

                        SpannableString highlightSpan = new SpannableString(
                                text.substring(i, i + length));
                        highlightSpan.setSpan(new BackgroundColorSpan(
                                HIGHLIGHT_COLOR_INT), 0,
                                highlightSpan.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.append(highlightSpan);

                        i = i + length;
                        pre = i;
                    } else {
                        i++;
                    }
                }

                sb.append(text.substring(pre, i));

                return sb;
            }
        }
        return text;
    }
}
