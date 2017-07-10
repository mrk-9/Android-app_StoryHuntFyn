package dk.kultur.historiejagtenfyn.expansion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

/**
 * dk.kultur.historiejagtenfyn
 * Created by juskrt on 3/3/2015.
 */
public class HisAlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = HisAlarmReceiver.class.getSimpleName();



    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, HisDownloaderService.class);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
