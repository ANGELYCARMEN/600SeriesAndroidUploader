package info.nightscout.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugfender.sdk.Bugfender;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.squareup.leakcanary.LeakCanary;

import info.nightscout.android.model.medtronicNg.PumpHistoryMarker;
import info.nightscout.android.model.medtronicNg.PumpHistorySystem;
import info.nightscout.android.model.medtronicNg.ContourNextLinkInfo;
import info.nightscout.android.model.medtronicNg.PumpHistoryAlarm;
import info.nightscout.android.model.medtronicNg.PumpHistoryBG;
import info.nightscout.android.model.medtronicNg.PumpHistoryBasal;
import info.nightscout.android.model.medtronicNg.PumpHistoryBolus;
import info.nightscout.android.model.medtronicNg.PumpHistoryCGM;
import info.nightscout.android.model.medtronicNg.PumpHistoryDaily;
import info.nightscout.android.model.medtronicNg.PumpHistoryLoop;
import info.nightscout.android.model.medtronicNg.PumpHistoryMisc;
import info.nightscout.android.model.medtronicNg.PumpHistoryPattern;
import info.nightscout.android.model.medtronicNg.PumpHistoryProfile;
import info.nightscout.android.model.medtronicNg.HistorySegment;
import info.nightscout.android.model.medtronicNg.PumpHistorySettings;
import info.nightscout.android.model.medtronicNg.PumpInfo;
import info.nightscout.android.model.medtronicNg.PumpStatusEvent;
import info.nightscout.android.model.store.DataStore;
import info.nightscout.android.model.store.UserLog;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.annotations.RealmModule;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static info.nightscout.android.utils.ToolKit.getWakeLock;

/**
 * Created by lgoedhart on 9/06/2016.
 */
public class UploaderApplication extends Application {
    private static final String TAG = UploaderApplication.class.getSimpleName();

    private static RealmConfiguration storeConfiguration;
    private static RealmConfiguration userLogConfiguration;
    private static RealmConfiguration historyConfiguration;

    private static ConnectivityManager connectivityManager;

    private static long startupRealtime;

    public static Context context;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate Called");

        startupRealtime = SystemClock.elapsedRealtime();

        context = this.getBaseContext();

        super.onCreate();

        // LeakCanary is only active for debug build variant

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        // Normal app init code...

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            if (prefs.getBoolean(getString(R.string.preferences_enable_crashlytics), true)) {
                Fabric.with(this, new Crashlytics());
            }
            if (prefs.getBoolean(getString(R.string.preferences_enable_answers), true)) {
                Fabric.with(this, new Answers());
            }

            if (prefs.getBoolean(getString(R.string.preferences_enable_remote_logcat), false)) {
                Bugfender.init(this, BuildConfig.BUGFENDER_API_KEY, BuildConfig.DEBUG);
                Bugfender.enableLogcatLogging();
                Bugfender.setDeviceString("NightscoutURL", prefs.getString(getString(R.string.preference_nightscout_url), "Not set"));
            }
        } catch (Exception ignored) {}

        Realm.init(this);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .modules(new MainModule())
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        storeConfiguration = new RealmConfiguration.Builder()
                .name("store.realm")
                .modules(new StoreModule())
                .deleteRealmIfMigrationNeeded()
                .build();

        userLogConfiguration = new RealmConfiguration.Builder()
                .name("userlog.realm")
                .modules(new UserLogModule())
                .deleteRealmIfMigrationNeeded()
                .build();

        historyConfiguration = new RealmConfiguration.Builder()
                .name("history.realm")
                .modules(new HistoryModule())
                .deleteRealmIfMigrationNeeded()
                .build();

        // Some Android versions will leak if ConnectivityManager not attached to app context here
        connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(CONNECTIVITY_SERVICE);
    }

    public static boolean isOnline() {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static long getStartupRealtime() {
        return UploaderApplication.startupRealtime;
    }

    public static long getUptime() {
        return SystemClock.elapsedRealtime() - UploaderApplication.startupRealtime;
    }

    public static RealmConfiguration getStoreConfiguration() {
        return storeConfiguration;
    }

    public static RealmConfiguration getUserLogConfiguration() {
        return userLogConfiguration;
    }

    public static RealmConfiguration getHistoryConfiguration() {
        return historyConfiguration;
    }

    @RealmModule(classes = {
            ContourNextLinkInfo.class,
            PumpInfo.class,
            PumpStatusEvent.class,
    })
    private class MainModule {}

    @RealmModule(classes = {
            DataStore.class,
    })
    private class StoreModule {}

    @RealmModule(classes = {
            UserLog.class
    })
    private class UserLogModule {}

    @RealmModule(classes = {
            HistorySegment.class,
            PumpHistoryCGM.class,
            PumpHistoryBolus.class,
            PumpHistoryBasal.class,
            PumpHistoryBG.class,
            PumpHistoryMisc.class,
            PumpHistoryMarker.class,
            PumpHistoryProfile.class,
            PumpHistoryPattern.class,
            PumpHistorySettings.class,
            PumpHistoryLoop.class,
            PumpHistoryDaily.class,
            PumpHistoryAlarm.class,
            PumpHistorySystem.class,
    })
    private class HistoryModule {}

}
