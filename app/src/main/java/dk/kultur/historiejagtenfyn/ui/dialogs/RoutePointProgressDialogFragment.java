package dk.kultur.historiejagtenfyn.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIContentHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Created by RokasTS on 2015.02.20.
 */
public class RoutePointProgressDialogFragment extends AbsFullScreenDialogFragment {

    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_TITLE = "arg_title";
    private String mMessage;
    //private String mTitle;

    private TextView mTitleView;
    private TextView mDescriptionView;


    public static RoutePointProgressDialogFragment newInstance(String message) {
        RoutePointProgressDialogFragment f = new RoutePointProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        //args.putString(ARG_TITLE, title);
        f.setArguments(args);

        return f;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mMessage = getArguments().getString(ARG_MESSAGE);
        //mTitle = getArguments().getString(ARG_TITLE);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_error_popup, null);

        mTitleView = (TextView) view.findViewById(R.id.textTitle);
        mTitleView.setVisibility(View.GONE);
        //mTitleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        //mTitleView.setText(mTitle);

        mDescriptionView = (TextView) view.findViewById(R.id.textMessage);
        mDescriptionView.setText(mMessage);

        TextView okText = (TextView) view.findViewById(R.id.textOk);
        okText.setOnClickListener(mCloseListener);
        okText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        dialog.getWindow().setContentView(view);
        SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_GET_POINT_WITH_BONUS);
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