package io.moselo.SampleApps;

import android.app.Activity;
import android.app.Application;

import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Listener.TapListener;
import io.taptalk.TapTalk.Manager.TapUI;
import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.TapTalk.View.Activity.TapUIRoomListActivity;
import io.taptalk.TaptalkSample.BuildConfig;
import io.taptalk.TaptalkSample.R;

public class SampleApplication extends Application {

    private static final String TAG = "SampleApplication";

    TapListener tapListener = new TapListener() {
        @Override
        public void onTapTalkRefreshTokenExpired() {

        }

        @Override
        public void onTapTalkUnreadChatRoomBadgeCountUpdated(int unreadCount) {

        }

        @Override
        public void onNotificationReceived(TAPMessageModel message) {
            TapTalk.showTapTalkNotification(message);
        }

        @Override
        public void onUserLogout() {
            
        }

        @Override
        public void onTaskRootChatRoomClosed(Activity activity) {
            Intent intent = new Intent(activity, TapUIRoomListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        TapTalk.init(this, "YOUR_APP_KEY_ID",
                "YOUR_APP_KEY_SECRET",
                R.drawable.ic_taptalk_logo, "TapTalk Dev Sample App", "YOUR_APP_BASE_URL",
                TapTalk.TapTalkImplementationType.TapTalkImplementationTypeUI,
                tapListener);
        TapTalk.initializeGooglePlacesApiKey("YOUR_GOOGLE_PLACES_API_KEY");
        TapUI.getInstance().setLogoutButtonVisible(true);
    }
}
