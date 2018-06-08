package info.nightscout.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.nightscout.android.R;

/**
 * Created by Pogman on 19.04.18.
 */

public class FormatKit {
    private static final String TAG = FormatKit.class.getSimpleName();

    public static final float MMOLXLFACTOR = 18.016f;

    private static FormatKit instance;

    private Context mContext;

    private FormatKit(Context context) {
        Log.d(TAG, "initialise instance");
        mContext = context;
    }

    public static FormatKit getInstance(Context context) {
        if (instance == null) instance = new FormatKit(context);
        return instance;
    }

    public static FormatKit getInstance() {
        if (instance == null) new NullPointerException(TAG + " instance not initialised");
        return instance;
    }

    public static void close() {
        if (FormatKit.instance != null) instance = null;
    }

    public String formatAsGrams(Double value) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(1);
        return df.format(value) + mContext.getString(R.string.text_gram);
    }

    public String formatAsExchanges(Double value) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(1);
        return df.format(value) + mContext.getString(R.string.text_gram_exchange);
    }

    public String formatAsInsulin(Double value) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(2);
        return df.format(value) + mContext.getString(R.string.text_insulin_unit);
    }

    public String formatAsGlucose(int value) {
        return formatAsGlucose(value, false);
    }

    public String formatAsGlucose(int value, boolean units) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPreferences.getBoolean("mmolxl", false)) return formatAsGlucoseMMOL(value, units);
        return formatAsGlucoseMGDL(value, units);
    }

    public String formatAsGlucoseMGDL(int value, boolean units) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return df.format(value) + (units ? " " + mContext.getString(R.string.text_unit_mgdl) : "");
    }

    public String formatAsGlucoseMMOL(int value, boolean units) {
        DecimalFormat df = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return df.format(value / MMOLXLFACTOR) + (units ? " " + mContext.getString(R.string.text_unit_mmol) : "");
    }

    public String formatAsFraction(double value, boolean isFraction) {
        if (isFraction) {
            DecimalFormat df = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
            return df.format(value);
        }
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return df.format(value);
    }

    public String formatSecondsAsDHMS(int seconds) {
        int d = seconds / 86400;
        int h = (seconds % 86400) / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return (d > 0 ? d + mContext.getString(R.string.time_d) : "") +
                ((h | d) > 0 ? h + mContext.getString(R.string.time_h) : "") +
                ((h | d | m) > 0 ? m + mContext.getString(R.string.time_m) : "") +
                s + mContext.getString(R.string.time_s);
    }

    public String formatMinutesAsDHM(int minutes) {
        int d = minutes / 1440;
        int h = (minutes % 1440) / 60;
        int m = minutes % 60;
        return (d > 0 ? d + mContext.getString(R.string.time_d) : "") +
                ((h | d) > 0 ? h + mContext.getString(R.string.time_h) : "") +
                m + mContext.getString(R.string.time_m);
    }

    /*
    public String formatMinutesAsHM(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return (h > 0 ? h + mContext.getString(R.string.time_h) : "") +
                m + mContext.getString(R.string.time_m);
    }
    */
    public String formatMinutesAsHM(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return (h > 0 ? h + mContext.getString(R.string.time_h) : "") +
                (m > 0 ? m + mContext.getString(R.string.time_m) : (h > 0 ? "" : "0" + mContext.getString(R.string.time_m)));
    }

    public String formatHoursAsDH(int hours) {
        int d = hours / 24;
        int h = hours % 24;
        return d + mContext.getString(R.string.time_d) +
                h + mContext.getString(R.string.time_h);
    }

    public String formatAsPercent(int value) {
        return Integer.toString(value) + "%";
    }

    public String formatAsClock(Date date) {
        return formatAsClock(date.getTime());
    }

    public String formatAsClock(long time) {
        if (DateFormat.is24HourFormat(mContext)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(time);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mma", Locale.getDefault());
            return sdf.format(time).toLowerCase().replace(".", "").replace(",", "");
        }
    }

    public String formatAsClock(int hours, int minutes) {
        DecimalFormat df = new DecimalFormat("00", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        if (DateFormat.is24HourFormat(mContext)) {
            return df.format(hours) + ":" + df.format(minutes);
        } else {
            return (hours > 12 ? hours - 12 : hours) + ":" + df.format(minutes)
                    + DateFormatSymbols.getInstance().getAmPmStrings()[hours < 12 ? 0 : 1]
                    .toLowerCase().replace(".", "").replace(",", "");
        }
    }

    public String formatAsHours(int hours, int minutes) {
        DecimalFormat df = new DecimalFormat("00", DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return hours + ":" + df.format(minutes);
    }

    public String formatAsWeekday(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(time);
    }

    public String formatAsWeekday(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(date);
    }

    public String formatAsWeekdayMonthDay(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMM dd", Locale.getDefault());
        return sdf.format(time);
    }

    public String formatAsWeekdayMonthDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMMM dd", Locale.getDefault());
        return sdf.format(date);
    }

    public String getString(int ref) {
        return mContext.getString(ref);
    }

    public String[] getAlertString(int code) {
        String[] pumpAlerts = mContext.getResources().getStringArray(R.array.pump_alerts);
        String stringCode = Integer.toString(code) + "|";
        int i;

        for (i = pumpAlerts.length - 1; i > 0; i--) {
            if (pumpAlerts[i].startsWith(stringCode)) break;
        }

        String alert[] = pumpAlerts[i].split("\\|");
        if (alert.length != 5) alert = pumpAlerts[0].split("\\|");

        return alert;
    }

    // MongoDB Index Key Limit
    // The total size of an index entry, which can include structural overhead depending on the BSON type,
    // must be less than 1024 bytes.
    public String asMongoDBIndexKeySafe(String string) {
        int size = string.length();

        // json will escape "</div>" to "<\/div"

        int jsonsize = size + (size - string
                .replace("/", "")
                .replace("\"", "")
                .replace("\\", "")
                .length());

        Log.d(TAG, String.format("MongoDBIndexKeySafe: size: %d used: %d", size, jsonsize));

        if (jsonsize < 1024) return string;
        return "";
    }

}