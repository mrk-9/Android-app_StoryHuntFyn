package dk.kultur.historiejagtenfyn.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIContentHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.activities.POIDetailsActivity;
import dk.kultur.historiejagtenfyn.ui.fragments.ScanFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.map.osm.MapFragment;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Created by RokasTS on 2015.02.20.
 */
public class CustomPopupDialogFragment extends AbsFullScreenDialogFragment {

    private static final int POI_LOADER_ID = 1;
    private static final String ARG_ID = "arg_id";
    private static final String ARG_IN_CLICK_RANGE = "arg_in_click_range";
    private String mID;
    private boolean inClickRange = true;

    private ScanFragment.OnDialogDismissListener mListener;

    private TextView mTitleView;
    private TextView mDescriptionView;

    public static CustomPopupDialogFragment newInstance(String id, boolean inClickRange) {
        CustomPopupDialogFragment f = new CustomPopupDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putBoolean(ARG_IN_CLICK_RANGE, inClickRange);
        f.setArguments(args);

        return f;
    }


    public void setOnDismissListener(ScanFragment.OnDialogDismissListener listener) {
        mListener = listener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_popup, null);

        TextView cancelText = (TextView) view.findViewById(R.id.textBack);
        cancelText.setOnClickListener(mCloseListener);
        cancelText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        TextView okText = (TextView) view.findViewById(R.id.textMore);
        okText.setOnClickListener(mOkListener);
        okText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        mTitleView = (TextView) view.findViewById(R.id.textTitle);
        mTitleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        mDescriptionView = (TextView) view.findViewById(R.id.textMessage);
        dialog.getWindow().setContentView(view);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mID = getArguments().getString(ARG_ID);
        inClickRange = getArguments().getBoolean(ARG_IN_CLICK_RANGE);

        getLoaderManager().initLoader(POI_LOADER_ID, getArguments(), mLoaderCallbacks);
    }

    android.view.View.OnClickListener mCloseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BUTTON);
            dismiss();

        }
    };

    android.view.View.OnClickListener mOkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BUTTON);
            dismiss();
            if (inClickRange) {
                Intent intent = new Intent(getActivity(), POIDetailsActivity.class);
                intent.putExtra(POIDetailsActivity.EXTRA_POI_ID, mID);
                getActivity().startActivityForResult(intent, MapFragment.REQUEST_POI_CONTENT);
                getActivity().overridePendingTransition(0, R.anim.anim_shrink_to_left);
                SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BOOK_PAGE);
            } else {
                ErrorPopupDialogFragment newFragment = ErrorPopupDialogFragment.newInstance(mID, getString(R.string.message_too_far_from_poi));
                newFragment.setCancelable(false);
                newFragment.show(getFragmentManager(), "dialog");
            }
        }
    };

    @Override
    public void dismiss() {
        if (mListener != null) {
            mListener.onDialogDismissed();
        }
        super.dismiss();
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String poiId = args.getString(ARG_ID);
            return ProviderMethods.getPointContentEntry(getActivity(), new String[]{
                            HisContract.POIContentEntry.TABLE_NAME + "." + POIContentHisContract.KEY_NAME,
                            HisContract.POIContentEntry.TABLE_NAME + "." + POIContentHisContract.KEY_INFO},
                    poiId, null);

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            while (data.moveToNext()) {
                String title = data.getString(0);
                String info = data.getString(1);
                if (title == null) {
                    mTitleView.setVisibility(View.GONE);
                } else {
                    mTitleView.setText(title);
                }
                if (info!= null) {
                    mDescriptionView.setText(Html.fromHtml(info));
                }

                return;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

}
