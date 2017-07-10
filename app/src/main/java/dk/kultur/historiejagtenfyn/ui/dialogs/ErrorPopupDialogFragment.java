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
public class ErrorPopupDialogFragment extends AbsFullScreenDialogFragment {

    private static final int POI_LOADER_ID = 1;
    private static final String ARG_MESSAGE = "arg_message";
    //private static final String ARG_TITLE = "arg_title";
    private static final String ARG_POI_ID = "arg_poi_id";
    private String mMessage;
    //private String mTitle;

    private TextView mTitleView;
    private TextView mDescriptionView;


    public static ErrorPopupDialogFragment newInstance(String poiId, String message) {
        ErrorPopupDialogFragment f = new ErrorPopupDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POI_ID, poiId);
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
        mTitleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        mDescriptionView = (TextView) view.findViewById(R.id.textMessage);


        TextView okText = (TextView) view.findViewById(R.id.textOk);
        okText.setOnClickListener(mCloseListener);
        okText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        dialog.getWindow().setContentView(view);

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getLoaderManager().initLoader(POI_LOADER_ID, getArguments(), mLoaderCallbacks);
    }


    android.view.View.OnClickListener mCloseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BUTTON);
            dismiss();

        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String poiId = args.getString(ARG_POI_ID);
            return ProviderMethods.getPointContentEntry(getActivity(), new String[]{
                            HisContract.POIContentEntry.TABLE_NAME + "." + POIContentHisContract.KEY_NAME},
                    poiId, null);

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            while (data.moveToNext()) {
                String title = data.getString(0);
                if (title == null) {
                    mTitleView.setVisibility(View.GONE);
                } else {
                    mTitleView.setText(title);
                }
                mDescriptionView.setText(Html.fromHtml(mMessage));

                return;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };


}