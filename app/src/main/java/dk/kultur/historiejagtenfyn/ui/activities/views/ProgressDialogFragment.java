package dk.kultur.historiejagtenfyn.ui.activities.views;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.activities.WelcomeActivity;

/**
 * Created by JustinasK on 12/11/2014.
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String LOG_TAG = ProgressDialogFragment.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setTitle(getString(R.string.pleaseWait));
        dialog.setMessage(getString(R.string.data_being_loaded));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startActivity(new Intent(getActivity(), WelcomeActivity.class));
    }
}