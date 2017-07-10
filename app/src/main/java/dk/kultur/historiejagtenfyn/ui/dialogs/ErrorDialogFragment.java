package dk.kultur.historiejagtenfyn.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

public class ErrorDialogFragment extends AbsFullScreenDialogFragment {

    private static final String ARG_MESSAGE = "arg_message";
    private String mMessage;

    private TextView mDescriptionView;


    public static ErrorDialogFragment newInstance(String message) {
        ErrorDialogFragment f = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        f.setArguments(args);

        return f;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        mMessage = getArguments().getString(ARG_MESSAGE);
        //mTitle = getArguments().getString(ARG_TITLE);

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_error_popup, null);

        View mTitleView = view.findViewById(R.id.textTitle);
        mTitleView.setVisibility(View.GONE);
        //mTitleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        //mTitleView.setText(mTitle);

        mDescriptionView = (TextView) view.findViewById(R.id.textMessage);
        mDescriptionView.setText(mMessage);

        TextView okText = (TextView) view.findViewById(R.id.textOk);
        okText.setOnClickListener(mCloseListener);
        okText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        dialog.getWindow().setContentView(view);
        return dialog;
    }



    View.OnClickListener mCloseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BUTTON);
            dismiss();

        }
    };



}