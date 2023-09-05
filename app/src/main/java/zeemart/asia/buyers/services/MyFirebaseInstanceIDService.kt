package zeemart.asia.buyers.services

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import io.intercom.android.sdk.push.IntercomPushClient
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.helper.MixPanelHelper
import zeemart.asia.buyers.helper.SharedPref

/**
 * Created by saiful on 22/1/18.
 */
class MyFirebaseInstanceIDService : FirebaseMessagingService() {
    private val intercomPushClient = IntercomPushClient()

    //    @Override
    //    public void onTokenRefresh() {
    //        //Get hold of the registration token
    ////        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
    //        String refreshedToken = "FirebaseInstanceId.getInstance().getToken()";
    //        //Log the token
    //        Log.d(TAG, "Refreshed token: " + refreshedToken);
    //        //save the device token in SharedPreference
    //        SharedPref.write(SharedPref.NOTIFICATION_DEVICE_TOKEN,refreshedToken);
    //        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(NotificationConstants.REFRESH_TOKEN_RECEIVED));
    //        intercomPushClient.sendTokenToIntercom(getApplication(), SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, ""));
    //        //send token to clevertap
    //        CleverTapHelper.registerFCM(getApplicationContext(),refreshedToken);
    //    }
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d("NEW_TOKEN", s)
        //Get hold of the registration token
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "Refreshed token: $s")
        //save the device token in SharedPreference
        SharedPref.write(SharedPref.NOTIFICATION_DEVICE_TOKEN, s)
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(NotificationConstants.REFRESH_TOKEN_RECEIVED))
        SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")?.let {
            intercomPushClient.sendTokenToIntercom(
                application,
                it
            )
        }
        //send token to clevertap
        MixPanelHelper.registerFCM(applicationContext, s)
    }

    private fun sendRegistrationToServer(token: String) {
        //Implement this method if you want to store the token on your server
    }

    companion object {
        private const val TAG = "MyAndroidFCMIIDService"
    }
}