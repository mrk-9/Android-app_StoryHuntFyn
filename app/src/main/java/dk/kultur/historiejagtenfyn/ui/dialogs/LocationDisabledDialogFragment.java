package dk.kultur.historiejagtenfyn.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.activities.HomeActivity;
import dk.kultur.historiejagtenfyn.ui.activities.POIDetailsActivity;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

public class LocationDisabledDialogFragment extends AbsFullScreenDialogFragment {

    public static LocationDisabledDialogFragment newInstance() {
        LocationDisabledDialogFragment f = new LocationDisabledDialogFragment();
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_popup, null);

        TextView cancelText = (TextView) view.findViewById(R.id.textBack);
        cancelText.setText(R.string.not_now);
        cancelText.setOnClickListener(mCloseListener);
        cancelText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        TextView okText = (TextView) view.findViewById(R.id.textMore);
        okText.setText(R.string.enable);
        okText.setOnClickListener(mOkListener);
        okText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        TextView titleView = (TextView) view.findViewById(R.id.textTitle);
        titleView.setVisibility(View.GONE);

        TextView descriptionView = (TextView) view.findViewById(R.id.textMessage);
        descriptionView.setText(R.string.please_enable_location);
        dialog.getWindow().setContentView(view);
        return dialog;
    }

    android.view.View.OnClickListener mCloseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ((HomeActivity) getActivity()).mUserOpenedSettings = false;
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BUTTON);
            dismiss();

        }
    };

    android.view.View.OnClickListener mOkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ((HomeActivity) getActivity()).mUserOpenedSettings = true;
            Intent settings = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
            startActivity(settings);
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BUTTON);
            dismiss();
        }
    };
}