package chk.android.networkfirewall;

import android.app.IntentService;
import android.content.Intent;

public class FirewallService extends IntentService {
    private static final String NAME = "FirewallService";

    public FirewallService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {

        }
    }
}
