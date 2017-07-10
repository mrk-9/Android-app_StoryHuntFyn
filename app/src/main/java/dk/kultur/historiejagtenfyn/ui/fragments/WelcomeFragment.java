package dk.kultur.historiejagtenfyn.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.parse.contracts.InfoHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.InfoEntry;
import dk.kultur.historiejagtenfyn.ui.activities.HomeActivity;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Welcome screen
 * Created by JustinasK on 2/14/2015.
 */
public class WelcomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = WelcomeFragment.class.getSimpleName();
    private final static String sSelectionInfoWithLanguage = InfoHisContract.KEY_LANGUAGE + ParseContract.POINTER_ID + " = ?";

    public static String getLogTag() {
        return LOG_TAG;
    }

    private static final String LANGUAGE_KEY = "language";
    private static final String[] INFO_COLUMNS = {
            InfoEntry.TABLE_NAME + "." + InfoEntry._ID,
            InfoHisContract.KEY_LANGUAGE + ParseContract.POINTER_ID,
            InfoHisContract.KEY_TITLE,
            InfoHisContract.KEY_TEXT
    };

    private static final int INFO_LOADER = 0;

    private String activeLanguageId;
    private TextView titleView;
    private TextView infoView;

    public WelcomeFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LANGUAGE_KEY, activeLanguageId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            activeLanguageId = savedInstanceState.getString(LANGUAGE_KEY);
        }
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        titleView = (TextView) rootView.findViewById(R.id.title);
        infoView = (TextView) rootView.findViewById(R.id.info);
        TextView btnContinue = (TextView) rootView.findViewById(R.id.btnContinue);

        titleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        btnContinue.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        rootView.findViewById(R.id.btnContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), HomeActivity.class));
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, R.anim.anim_shrink_to_left);
                    SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BOOK_PAGE);
                } else {
                    Log.w(getLogTag(), "crash avoided");
                }

            }
        });
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            activeLanguageId = savedInstanceState.getString(LANGUAGE_KEY);
        }
        getLoaderManager().initLoader(INFO_LOADER, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        titleView = null;
        infoView = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        switch (id) {
            case INFO_LOADER:
                String activeLanguageId = UIUtils.getActiveLanguage(getActivity().getApplicationContext());
                cursorLoader = new CursorLoader(
                        getActivity(),
                        InfoEntry.CONTENT_URI,
                        INFO_COLUMNS,
                        sSelectionInfoWithLanguage,
                        new String[]{activeLanguageId},
                        null
                );
                break;
        }
        return cursorLoader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case INFO_LOADER:
                if (data != null && data.moveToFirst()) {
                    String infoTitle = data.getString(data.getColumnIndex(InfoHisContract.KEY_TITLE));
                    String infoText = data.getString(data.getColumnIndex(InfoHisContract.KEY_TEXT));

                    titleView.setText(infoTitle);
                    infoView.setText(Html.fromHtml(infoText));
                }
                break;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_welcome_fragment), true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_welcome_fragment));
    }
}
