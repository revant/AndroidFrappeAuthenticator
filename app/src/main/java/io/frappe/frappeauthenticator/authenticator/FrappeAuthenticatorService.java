package io.frappe.frappeauthenticator.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Frappe
 * Date: 19/03/13
 * Time: 19:10
 */
public class FrappeAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        FrappeAuthenticator authenticator = new FrappeAuthenticator(this);
        return authenticator.getIBinder();
    }
}
