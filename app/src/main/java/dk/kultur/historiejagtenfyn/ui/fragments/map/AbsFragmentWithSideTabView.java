package dk.kultur.historiejagtenfyn.ui.fragments.map;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;
import dk.kultur.historiejagtenfyn.ui.Views.SideTabView;

/**
 * Created by Lina on 2015.02.06
 */
public abstract class AbsFragmentWithSideTabView extends Fragment {

    private static final String HTML_PLACEHOLDER_TITLE = "%title%";
    private static final String HTML_PLACEHOLDER_BODY = "%body%";
    private static final String HTML_PLACEHOLDER_BODY_START = "%bodystart%";

    private static final String EXTRA_COUNT = "EXTRA_COUNT";
    private static final String EXTRA_QUIZ = "EXTRA_QUIZ";
    private static final String EXTRA_ACTIVE_TAB = "EXTRA_ACTIVE_TAB";

    public static final String IMG_SCHEME = "file";
    public static final String HTML_PLACEHOLDER_IMAGE = ":::IMAGE_HERE:::";

    protected int mTabCount;
    protected boolean mHasQuiz;
    private int mActiveTab;

    protected static void initArgs(Bundle args, int count, boolean quiz, int activeTab) {
        args.putInt(EXTRA_COUNT, count);
        args.putBoolean(EXTRA_QUIZ, quiz);
        args.putInt(EXTRA_ACTIVE_TAB, activeTab);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTabCount = getArguments().getInt(EXTRA_COUNT);
        mHasQuiz = getArguments().getBoolean(EXTRA_QUIZ);
        mActiveTab = getArguments().getInt(EXTRA_ACTIVE_TAB);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(getLayoutId(), container, false);
        SideTabView sideTabView = (SideTabView) layout.findViewById(R.id.sideTabView);
        sideTabView.init(mTabCount, mHasQuiz, mActiveTab);
        // sideTabView.setActiveTabId(getFragmentId());
        sideTabView.setOnTabClickListener((SideTabView.OnTabClickedListener) getActivity());
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
    protected void refreshSideBar() {
        SideTabView sideTabView = (SideTabView) getView().findViewById(R.id.sideTabView);
        sideTabView.init(mTabCount, mHasQuiz, mActiveTab);
        Bundle args = getArguments();
        args.putInt(EXTRA_COUNT, mTabCount);
        args.putBoolean(EXTRA_QUIZ, mHasQuiz);
        args.putInt(EXTRA_ACTIVE_TAB, mActiveTab);
    }

    protected String getContentFromAssets(String fileName, String title, String body, boolean startOnload) {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuilder total = new StringBuilder();

        try {
            try {
                is = getActivity().getAssets().open(fileName);
                isr = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(isr);

                String line;
                while ((line = br.readLine()) != null) {
                    total.append(line);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int index = total.indexOf(HTML_PLACEHOLDER_BODY_START);
        if (startOnload) {
            total.replace(index, index + HTML_PLACEHOLDER_BODY_START.length(), "<body style=\"margin-left:0px; \" onload=\"start()\">");
        } else {
            total.replace(index, index + HTML_PLACEHOLDER_BODY_START.length(), "<body style=\"margin-left:0px;\">");
        }

        index = total.indexOf(HTML_PLACEHOLDER_TITLE);
        total.replace(index, index + HTML_PLACEHOLDER_TITLE.length(), title);
        index = total.indexOf(HTML_PLACEHOLDER_BODY);
        total.replace(index, index + HTML_PLACEHOLDER_BODY.length(), body);
        return total.toString();
    }

    protected abstract int getLayoutId();


}
