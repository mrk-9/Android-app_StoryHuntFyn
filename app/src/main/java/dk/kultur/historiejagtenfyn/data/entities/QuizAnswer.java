package dk.kultur.historiejagtenfyn.data.entities;

/**
 * Created by RokasTS on 2015.02.23.
 */
public class QuizAnswer {

    private String name;
    private boolean correct;

    public QuizAnswer() {
    }

    public QuizAnswer(String name, boolean correct) {
        this.name = name;
        this.correct = correct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
