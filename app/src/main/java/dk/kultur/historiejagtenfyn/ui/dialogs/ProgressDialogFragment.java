package dk.kultur.historiejagtenfyn.ui.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import dk.kultur.historiejagtenfyn.R;

/**
 * Progress dialog
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
        dialog.setMessage(getString(R.string.fetching_data));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setText(String message) {
        ((ProgressDialog) getDialog()).setMessage(message);
    }

}