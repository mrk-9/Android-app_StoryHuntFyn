package dk.kultur.historiejagtenfyn.data.entities;

import java.util.List;

/**
 * Created by RokasTS on 2015.02.23.
 */
public class Quiz {

    private String name;
    private List<QuizAnswer> answers;
    private int clicked = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAnswer> answers) {
        this.answers = answers;
    }

    public int getClicked() {
        return clicked;
    }

    public void setClicked(int clicked) {
        this.clicked = clicked;
    }
}
