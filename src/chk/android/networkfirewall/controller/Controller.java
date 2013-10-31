package chk.android.networkfirewall.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import chk.android.networkfirewall.AppInfo;
import chk.android.networkfirewall.NoPermissionException;
import chk.android.networkfirewall.provider.NetworkFirewall;

public class Controller {

    public static final String SCRIPT_FILE = "script.sh";
    public static final String PREFS_NAME = "Shared_Preference";
    public static final int NETWORK_MODE_WIFI = 1;
    public static final int NETWORK_MODE_3G = 2;

    private static final String CHAIN_NAME_OUTPUT = "OUTPUT";

    private static final String MAIN_CHAIN_NAME = "chk_firewall";
    private static final String REJECT_CHAIN_NAME_WIFI = "chk_firewall_reject_wifi";
    private static final String REJECT_CHAIN_NAME_3G = "chk_firewall_reject_3g";

    private static final String ITFS_WIFI[] = { "tiwlan+", "wlan+", "eth+", "ra+" };

    private static final String ITFS_3G[] = { "rmnet+", "pdp+", "ppp+", "usb+", "ccmni+",
            "uwbr+", "wimax+", "vsnet+" };

    public static boolean initIpTablesIfNecessary(Context context)
            throws NoPermissionException {
        boolean result = false;
        final File file = new File(context.getCacheDir(), SCRIPT_FILE);
        StringBuilder sb = new StringBuilder();
        String[] r = new String[2];

        ScriptRunner.runOnSameThread(file, "iptables -S " + CHAIN_NAME_OUTPUT, r);

        if (TextUtils.isEmpty(r[0]) || !TextUtils.isEmpty(r[1])) {
            throw new NoPermissionException();
        }

        if (!r[0].contains("-j " + MAIN_CHAIN_NAME)) {
            // TODO other cases, like incomplete chains.
            result = true;
        }
        if (result) {
            sb.append("iptables -D " + CHAIN_NAME_OUTPUT + " -j "
                    + MAIN_CHAIN_NAME + "\n");

            sb.append("iptables -F " + MAIN_CHAIN_NAME + "\n");
            sb.append("iptables -F " + REJECT_CHAIN_NAME_WIFI + "\n");
            sb.append("iptables -F " + REJECT_CHAIN_NAME_3G + "\n");

            sb.append("iptables -X " + MAIN_CHAIN_NAME + "\n");
            sb.append("iptables -X " + REJECT_CHAIN_NAME_WIFI + "\n");
            sb.append("iptables -X " + REJECT_CHAIN_NAME_3G + "\n");

            sb.append("iptables -N " + MAIN_CHAIN_NAME + "\n");
            sb.append("iptables -N " + REJECT_CHAIN_NAME_WIFI + "\n");
            sb.append("iptables -N " + REJECT_CHAIN_NAME_3G + "\n");

            sb.append("iptables -I " + CHAIN_NAME_OUTPUT + " 1 -j "
                    + MAIN_CHAIN_NAME + "\n");
            for (final String itf : ITFS_WIFI) {
                sb.append("iptables -A " + MAIN_CHAIN_NAME + " -o " + itf
                        + " -j " + REJECT_CHAIN_NAME_WIFI + "\n");
            }
            for (final String itf : ITFS_3G) {
                sb.append("iptables -A " + MAIN_CHAIN_NAME + " -o " + itf
                        + " -j " + REJECT_CHAIN_NAME_3G + "\n");
            }

            ScriptRunner.runOnSameThread(file, sb.toString());

            Cursor c = null;
            try {
                c = NetworkFirewall.queryAll(context);
                HashSet<String> rejectedWifi = new HashSet<String>();
                HashSet<String> rejected3g = new HashSet<String>();
                int flags;
                while (c.moveToNext()) {
                    flags = c.getInt(c.getColumnIndex(NetworkFirewall.COLUMN_DISABLE_FLAGS));
                    if ((flags & NetworkFirewall.DISABLE_FLAG_WIFI) != 0) {
                        rejectedWifi.add(String.valueOf(c.getInt(c.getColumnIndex(NetworkFirewall.COLUMN_UID))));
                    }
                    if ((flags & NetworkFirewall.DISABLE_FLAG_3G) != 0) {
                        rejected3g.add(String.valueOf(c.getInt(c.getColumnIndex(NetworkFirewall.COLUMN_UID))));
                    }
                }
                iptablesByUids(file, rejectedWifi.toArray(new String[rejectedWifi.size()]), true, NETWORK_MODE_WIFI);
                iptablesByUids(file, rejected3g.toArray(new String[rejected3g.size()]), true, NETWORK_MODE_3G);
            } finally {
                if (c != null) c.close();
            }
        }

        return result;
    }

    public static ArrayList<Integer> getAllRejectedApps(File file, int netMode)
            throws NoPermissionException {
        ArrayList<Integer> list = new ArrayList<Integer>();
        String[] r = new String[2];
        StringBuilder sb = new StringBuilder();
        sb.append("iptables -S ");
        switch (netMode) {
        case NETWORK_MODE_WIFI:
            sb.append(REJECT_CHAIN_NAME_WIFI);
            break;
        case NETWORK_MODE_3G:
            sb.append(REJECT_CHAIN_NAME_3G);
            break;
        default:
            throw new IllegalArgumentException("Unknow network mode : " + netMode);
        }

        ScriptRunner.runOnSameThread(file, sb.toString(), r);

        if (TextUtils.isEmpty(r[0]) || !TextUtils.isEmpty(r[1])) {
            throw new NoPermissionException();
        }

        if (!TextUtils.isEmpty(r[0])) {
            String[] array = r[0].split("\n");
            if (array.length > 1) {
                for (String s : array) {
                    if (s != null) {
                        int index = s.indexOf("--uid-owner");
                        if (index > -1) {
                            int uidIndex = index + "--uid-owner".length() + 1;
                            // TODO uid maxlength == 5 ???
                            list.add(Integer.valueOf(s.substring(uidIndex,
                                    uidIndex + 5)));
                        }
                    }
                }
            }
        }

        return list;
    }

    public static boolean checkRejected(File file, String uid, int netMode)
            throws NoPermissionException {
        boolean result = false;
        String[] r = new String[2];
        StringBuilder sb = new StringBuilder();
        sb.append("iptables -S ");
        switch (netMode) {
        case NETWORK_MODE_WIFI:
            sb.append(REJECT_CHAIN_NAME_WIFI);
            break;
        case NETWORK_MODE_3G:
            sb.append(REJECT_CHAIN_NAME_3G);
            break;
        default:
            throw new IllegalArgumentException("Unknow network mode : " + netMode);
        }
        ScriptRunner.runOnSameThread(file, sb.toString(), r);

        if (TextUtils.isEmpty(r[0]) || !TextUtils.isEmpty(r[1])) {
            throw new NoPermissionException();
        }

        if (!TextUtils.isEmpty(r[0])) {
            String[] array = r[0].split("\n");
            if (array.length > 1) {
                for (String s : array) {
                    if (s != null && s.contains("-m owner --uid-owner " + uid)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static void handleApp(Context context, AppInfo app, int netMode)
            throws NoPermissionException {
        final File file = new File(context.getCacheDir(), SCRIPT_FILE);
        boolean disable = false;
        switch (netMode) {
        case NETWORK_MODE_WIFI:
            disable = app.disabledWifi;
            break;
        case NETWORK_MODE_3G:
            disable = app.disabled3g;
            break;
        default:
            throw new IllegalArgumentException("Unknow network mode : " + netMode);
        }
        iptablesByUids(file, new String[] { String.valueOf(app.uid) }, disable, netMode);
        NetworkFirewall.insertOrUpdate(context, app);
    }

    public static void iptablesByUids(File file, String[] uids, boolean disable,
            int netMode) throws NoPermissionException {
        StringBuilder sb = new StringBuilder();
        String arg = disable ? " -A " : " -D ";
        String chain = null;
        switch (netMode) {
        case NETWORK_MODE_WIFI:
            chain = REJECT_CHAIN_NAME_WIFI;
            break;
        case NETWORK_MODE_3G:
            chain = REJECT_CHAIN_NAME_3G;
            break;
        default:
            throw new IllegalArgumentException("Unknow network mode : " + netMode);
        }
        for (String uid : uids) {
            if (disable && checkRejected(file, uid, netMode))
                continue;
            sb.append("iptables");
            sb.append(arg);
            sb.append(chain);
            sb.append(" -m owner --uid-owner ").append(uid)
                    .append(" -j REJECT \n");
        }
        if (sb.length() > 0)
            ScriptRunner.runOnSameThread(file, sb.toString());
    }

    public static void deleteAppInfo(Context context, String uid) {
        NetworkFirewall.deleteByUid(context, uid);
        final File file = new File(context.getCacheDir(), SCRIPT_FILE);

        StringBuilder sb = new StringBuilder();
        sb.append("iptables -D " + REJECT_CHAIN_NAME_WIFI);
        sb.append(" -m owner --uid-owner ").append(uid).append(" -j REJECT \n");
        sb.append("iptables -D " + REJECT_CHAIN_NAME_3G);
        sb.append(" -m owner --uid-owner ").append(uid).append(" -j REJECT \n");

        ScriptRunner.runOnSameThread(file, sb.toString());
    }
}