package chk.android.networkfirewall.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import chk.android.networkfirewall.NoPermissionException;
import chk.android.networkfirewall.Utils;
import chk.android.networkfirewall.controller.Controller;
import chk.android.networkfirewall.provider.NetworkFirewall;

public class FirewallService extends IntentService {
    private static final String NAME = "FirewallService";

    public FirewallService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent i) {
        Intent intent = i.getParcelableExtra(Utils.EXTRA_KEY);
        if (intent == null) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Controller.initIpTablesIfNecessary(this);
            } catch (NoPermissionException e) {
                Log.e(Utils.TAG, "Has no permission to run iptables");
            }
            return;
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
                && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            int uid = NetworkFirewall.findUidByPackageName(this, packageName);
            if (uid > 0) {
                Controller.deleteAppInfo(this, String.valueOf(uid));
            }
            getContentResolver().notifyChange(Utils.NOTIFY_URI_PACAKGE_CHANGED, null);
            return;
        }

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            if (Utils.checkNetWorkPermission(this, packageName)) {
                getContentResolver().notifyChange(Utils.NOTIFY_URI_PACAKGE_CHANGED, null);
            }
            return;
        }

        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            getContentResolver().notifyChange(Utils.NOTIFY_URI_PACAKGE_CHANGED, null);
            return;
        }
    }
}
