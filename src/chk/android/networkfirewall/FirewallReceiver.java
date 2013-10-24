package chk.android.networkfirewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import chk.android.networkfirewall.controller.Controller;
import chk.android.networkfirewall.provider.NetworkFirewall;

public class FirewallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Controller.initIpTablesIfNecessary(context);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
                && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            int uid = NetworkFirewall.findUidByPackageName(context, packageName);
            if (uid > 0) {
                Controller.deleteAppInfo(context, String.valueOf(uid));
            }
        }
        context.getContentResolver().notifyChange(Utils.NOTIFY_URI, null);
        Log.d("chk", intent.getAction());
    }
}
