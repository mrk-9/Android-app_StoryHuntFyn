package dk.kultur.historiejagtenfyn.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.expansion.HisDownloaderService;

/**
 *
 * Created by juskrt on 3/30/15.
 */
public class ObbDownloadDialogFragment extends DialogFragment implements IDownloaderClient {

    private static final String LOG_TAG = ProgressDialogFragment.class.getSimpleName();

    private boolean shown = false;

    private IStub mDownloaderClientStub;
    private IDownloaderService mRemoteService;
    private AlertDialog showErrorDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (shown) return;

        super.show(manager, tag);
        shown = true;
    }

    /**
     * Connect the stub to our service on start.
     */
    @Override
    public void onStart() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.connect(getActivity());
        }

        super.onStart();
    }


    /**
     * Disconnect the stub from our service on stop
     */
    @Override
    public void onStop() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.disconnect(getActivity());
        }

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (showErrorDialog != null) {
            showErrorDialog.dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(getResources().getString(R.string.downloading_assets));

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        shown = false;
        final Activity activity = getActivity();
        if (activity != null && activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startDownload();

    }

    private void startDownload() {
        try {
            Intent launchIntent = getActivity().getIntent();

            // Build an Intent to start this activity from the Notification
            Intent notifierIntent = new Intent(getActivity(), getActivity().getClass());
            notifierIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifierIntent.setAction(launchIntent.getAction());

            if (launchIntent.getCategories() != null) {
                for (String category : launchIntent.getCategories()) {
                    notifierIntent.addCategory(category);
                }
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Start the download service (if required)
            Log.v(LOG_TAG, "Start the download service");
            int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(getActivity(), pendingIntent, HisDownloaderService.class);

            if (startResult == DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                dismiss();
            } else {
                // Instantiate a member instance of IStub
                mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, HisDownloaderService.class);
                mDownloaderClientStub.disconnect(getActivity());
                mDownloaderClientStub.connect(getActivity());
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Cannot find own package! MAYDAY!");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void setText(String message) {
        ((ProgressDialog) getDialog()).setMessage(message);
    }

    @Override
    public void onServiceConnected(Messenger messenger) {
        mRemoteService = DownloaderServiceMarshaller.CreateProxy(messenger);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        Log.v(LOG_TAG, "DownloadStateChanged : " + getString(Helpers.getDownloaderStringResourceIDFromState(newState)));

        switch (newState) {
            case STATE_DOWNLOADING:
                Log.v(LOG_TAG, "Downloading...");
                break;
            case STATE_COMPLETED: // The download was finished
//                validateXAPKZipFiles();
                ((ProgressDialog)getDialog()).setMessage(getResources().getString(R.string.preparing_assets));
                // dismiss progress dialog
                dismiss();
                break;
            case STATE_FAILED_UNLICENSED:
            case STATE_FAILED_FETCHING_URL:
            case STATE_FAILED_SDCARD_FULL:
            case STATE_FAILED_CANCELED:
            case STATE_FAILED:
                if (showErrorDialog == null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(getResources().getString(R.string.error));
                    alert.setMessage(getResources().getString(R.string.download_failed));
                    alert.setNeutralButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ProgressDialog progressDialog = (ProgressDialog) getDialog();
                            progressDialog.setProgress(0);
                            progressDialog.setMessage(getResources().getString(R.string.downloading_assets));
                            startDownload();
//                        dismiss();
                        }
                    });
                    showErrorDialog = alert.create();
                }

                if (!showErrorDialog.isShowing()){
                    showErrorDialog.show();
                }
                break;
        }
    }

    @Override
    public void onDownloadProgress(DownloadProgressInfo downloadProgressInfo) {
        long percents = downloadProgressInfo.mOverallProgress * 100 / downloadProgressInfo.mOverallTotal;
        Log.v(LOG_TAG, "DownloadProgress:" + Long.toString(percents) + "%");
        ((ProgressDialog) getDialog()).setProgress((int) percents);
    }
}
