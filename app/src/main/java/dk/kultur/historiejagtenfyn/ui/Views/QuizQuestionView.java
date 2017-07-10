package dk.kultur.historiejagtenfyn.ui.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.Quiz;
import dk.kultur.historiejagtenfyn.data.entities.QuizAnswer;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Created by RokasTS on 2015.02.23.
 */
public class QuizQuestionView extends LinearLayout {

    private List<View> mAnswerViews = new ArrayList<View>();
    private Quiz mQuestion;

    public QuizQuestionView(Context context) {
        super(context);
    }

    public QuizQuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuizQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public QuizQuestionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void populateQuestion(final Quiz question) {
        mQuestion = question;
        mAnswerViews.clear();
        removeAllViews();

        setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(getContext());
        title.setTextAppearance(getContext(), R.style.Text_Regular);
        title.setText(question.getName());
        title.setTypeface(UIUtils.getTypeFaceMarkerFelt(getContext()));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 30;

        addView(title, params);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (QuizAnswer a : question.getAnswers()) {
            final View answer = inflater.inflate(R.layout.view_quiz_answer, null, false);
            mAnswerViews.add(answer);
            answer.setOnClickListener(mOnAnswerClickListener);

            TextView answerText = (TextView) answer.findViewById(R.id.answer_text);
            answerText.setTextAppearance(getContext(), R.style.Text_Small);
            answerText.setText(a.getName());
            answerText.setTypeface(UIUtils.getTypeFaceMarkerFelt(getContext()));

            LinearLayout.LayoutParams paramsAnswer = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(answer, paramsAnswer);
        }

        initViews();
    }

    private void initViews() {
        if(mQuestion.getClicked() != -1) {
            for(int i = 0; i < mAnswerViews.size(); i++) {
                if(mQuestion.getClicked() == i) {
                    ((ImageView) mAnswerViews.get(i).findViewById(R.id.radio_button)).setImageResource(R.drawable.quiz_radiobutton_active);
                    if(!mQuestion.getAnswers().get(i).isCorrect()) {
                        ((ImageView) mAnswerViews.get(i).findViewById(R.id.answer_correct)).setImageResource(R.drawable.quiz_wrong);
                    }
                }
                if(mQuestion.getAnswers().get(i).isCorrect()) {
                    ((ImageView) mAnswerViews.get(i).findViewById(R.id.answer_correct)).setImageResource(R.drawable.quiz_correct);
                }

            }
        }
    }
    private OnClickListener mOnAnswerClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mQuestion.getClicked() == -1) {
                for (int i = 0; i < mAnswerViews.size(); i++) {
                    if (v.equals(mAnswerViews.get(i))) {
                        mQuestion.setClicked(i);
                        break;
                    }
                }
                initViews();
            }
        }
    };

}
