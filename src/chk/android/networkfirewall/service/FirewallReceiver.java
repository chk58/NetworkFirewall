package chk.android.networkfirewall.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import chk.android.networkfirewall.Utils;

public class FirewallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            Intent i = new Intent(context, FirewallService.class);
            i.putExtra(Utils.EXTRA_KEY, intent);
            context.startService(i);
        }
    }
}
