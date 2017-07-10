package dk.kultur.historiejagtenfyn.ui.fragments.map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
import dk.kultur.historiejagtenfyn.data.entities.Quiz;
import dk.kultur.historiejagtenfyn.data.entities.QuizAnswer;
import dk.kultur.historiejagtenfyn.data.sql.Columns.AnswerColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.QuestionColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.QuizColumns;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.Views.QuizQuestionView;
import dk.kultur.historiejagtenfyn.ui.dialogs.ErrorDialogFragment;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Created by Lina on 2015.02.05
 */
public class POIQuizFragment extends AbsFragmentWithSideTabView {

    private static final int LOADER_QUIZ_ID = 1;
    private static final int LOADER_QUIZ_CONTENT = 2;

    private static final String TAG = POIQuizFragment.class.getSimpleName();

    private static final String ARG_POI_ID = "arg_poi_id";

    private String mPoiID;
    private String mQuizID;


    public static POIQuizFragment newInstance(String poiID, int count, int activeTab) {
        POIQuizFragment fragment = new POIQuizFragment();

        Bundle args = new Bundle();
        args.putString(ARG_POI_ID, poiID);
        initArgs(args, count, true, activeTab);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPoiID = getArguments().getString(ARG_POI_ID);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        TextView titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));

        TextView btnBack = (TextView) layout.findViewById(R.id.btnBack);
        btnBack.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                getActivity().onBackPressed();
            }
        });

        return layout;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_quiz;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLoaderManager().initLoader(LOADER_QUIZ_ID, getArguments(), mLoaderCallbacks);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_poi_quiz_fragment), true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_poi_quiz_fragment));
    }

    private class QuizAdapter extends ArrayAdapter<Quiz> {

        public QuizAdapter(Context context, List<Quiz> questions) {
            super(context, 0, questions);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new QuizQuestionView(getContext());
            }
            ((QuizQuestionView) convertView).populateQuestion(getItem(position));

            return convertView;
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_QUIZ_ID:
                    return ProviderMethods.getPoinEntry(getActivity(), new String[]{HisContract.POIEntry.COLUMN_QUIZ}, mPoiID, null);
                case LOADER_QUIZ_CONTENT:
                    String columns[] = new String[]{
                            QuizColumns.COLUMN_NAME,
                            QuizColumns.QuizContentColumns.COLUMN_HEADER,
                            QuestionColumns.COLUMN_QUESTION,
                            AnswerColumns.COLUMN_ANSWER,
                            AnswerColumns.COLUMN_CORRECT
                    };
                    Log.d(TAG, "on create loader quiz id " + mQuizID);
                    return ProviderMethods.getQuizView(getActivity(), columns, mQuizID, null);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case LOADER_QUIZ_ID:
                    if (data.getCount() > 0) {
                        data.moveToFirst();
                        mQuizID = data.getString(0);
                        if (mQuizID != null) {
                            getLoaderManager().initLoader(LOADER_QUIZ_CONTENT, getArguments(), mLoaderCallbacks);
                        }
                    }
                    break;
                case LOADER_QUIZ_CONTENT:

                    TextView header = null;
                    List<Quiz> questions = new ArrayList<>();
                    Quiz question = null;
                    int count = data.getCount();
                    Log.d(TAG, "quiz count " + count);
                    if (count == 0) {
                        ErrorDialogFragment newFragment = ErrorDialogFragment.newInstance(getString(R.string.error_unknown));
                        newFragment.setCancelable(false);
                        newFragment.show(getFragmentManager(), "dialog");
                        return;
                    }
                    while (data.moveToNext()) {
                        if (header == null) {
                            header = new TextView(getActivity());
                            header.setText(data.getString(1));
                            header.setTextAppearance(getActivity(), R.style.Text_Regular);
                            header.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
                            header.setPadding(0, 50, 0, 50);
                        }
                        if (question == null) {
                            question = new Quiz();
                            question.setName(data.getString(2));
                        }
                        if (!question.getName().equals(data.getString(2))) {
                            questions.add(question);
                            question = new Quiz();
                            question.setName(data.getString(2));
                        }

                        List<QuizAnswer> answers = question.getAnswers();
                        if (answers == null) {
                            answers = new ArrayList<QuizAnswer>();
                            question.setAnswers(answers);
                        }
                        QuizAnswer a = new QuizAnswer(data.getString(3), data.getInt(4) == 1 ? true : false);
                        answers.add(a);

                    }
                    questions.add(question);

                    ListView list = (ListView) getView().findViewById(R.id.quiz_list);
                    list.addHeaderView(header, null, false);//header view not clickable

                    QuizAdapter adapter = new QuizAdapter(getActivity(), questions);

                    list.setAdapter(adapter);

                    DatabaseUtils.dumpCursor(data);
                    break;
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };
}
