package chk.android.networkfirewall.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import chk.android.networkfirewall.Utils;

public class FirewallReceiver extends BroadcastReceiver {

    private static int BOOT_PENDING_TIME = 10000;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Some systems will auto add ACCEPT chains.
            // To avoid this, make a delay.
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, FirewallService.class);
            i.putExtra(Utils.EXTRA_KEY, intent);
            PendingIntent pendIntent = PendingIntent.getService(context, 0, i, 0);
            long triggerAtTime = SystemClock.elapsedRealtime() + BOOT_PENDING_TIME;
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pendIntent);
            return;
        }
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            Intent i = new Intent(context, FirewallService.class);
            i.putExtra(Utils.EXTRA_KEY, intent);
            context.startService(i);
        }
    }
}
