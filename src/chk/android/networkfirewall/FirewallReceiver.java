package chk.android.networkfirewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import chk.android.networkfirewall.provider.NetworkFirewall;
import chk.android.networkfirewall.script.Script;

public class FirewallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.e("chk", "ACTION_BOOT_COMPLETED");
            Script.initIpTablesIfNecessary(context);
            Log.e("chk", "init finished");
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            Log.e("chk", "removed package name : " + packageName);
            int uid = NetworkFirewall.findUidByPackageName(context, packageName);
            if (uid > 0) {
                Log.e("chk", "removed package uid : " + uid);
                Script.deleteAppInfo(context, String.valueOf(uid));
                Log.e("chk", "delete completed");
            }
        }
    }
}
