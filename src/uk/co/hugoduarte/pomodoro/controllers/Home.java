package uk.co.hugoduarte.pomodoro.controllers;

import uk.co.hugoduarte.pomodoro.model.Attempt;
import uk.co.hugoduarte.pomodoro.model.AttemptKind;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;


public class Home {
    @FXML
    private VBox container;

    @FXML
    private Label title;

    @FXML
    private TextArea message;

    private Attempt currentAttempt;
    private StringProperty timerText;
    private Timeline timeline;
    private AudioClip applause;

    public Home() {
        this.timerText = new SimpleStringProperty();
        setTimerText(0);
        applause = new AudioClip(getClass().getResource("/sounds/applause.mp3").toExternalForm());
    }

    public String getTimerText() {
        return this.timerText.get();
    }

    public void setTimerText(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        setTimerText(String.format("%02d:%02d", minutes, seconds));
    }

    public StringProperty timerTextProperty() {
        return this.timerText;
    }

    public void setTimerText(String timerText) {
        this.timerText.set(timerText);
    }

    private void prepareAttempt(AttemptKind kind) {
        reset();
        this.currentAttempt = new Attempt(kind, "");
        addAttemptStyle(kind);
        title.setText(kind.getDisplayName());
        setTimerText(this.currentAttempt.getRemainingSeconds());
        this.timeline = new Timeline();
        this.timeline.setCycleCount(kind.getTotalSeconds());
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            currentAttempt.tick();
            setTimerText(currentAttempt.getRemainingSeconds());
        }));
        timeline.setOnFinished(e -> {
            saveCurrentAttempt();
            applause.play();
            prepareAttempt(currentAttempt.getKind() == AttemptKind.FOCUS ?
                    AttemptKind.BREAK : AttemptKind.FOCUS);
        });
    }

    private void saveCurrentAttempt() {
        currentAttempt.setMessage(message.getText());
        currentAttempt.save();
    }

    private void reset() {
        clearAttemptStyles();
        if (this.timeline != null && this.timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.stop();
        }
    }

    public void playTimer() {
        container.getStyleClass().add("playing");
        timeline.play();
    }

    public void pauseTimer() {
        container.getStyleClass().remove("playing");
        timeline.pause();
    }

    private void addAttemptStyle(AttemptKind kind) {
        container.getStyleClass().add(kind.toString().toLowerCase());
    }

    private void clearAttemptStyles() {
        container.getStyleClass().remove("playing");
        for (AttemptKind kind : AttemptKind.values()) {
            container.getStyleClass().remove(kind.toString().toLowerCase());
        }
    }

    public void handleRestart(ActionEvent actionEvent) {
        prepareAttempt(AttemptKind.FOCUS);
        playTimer();
    }

    public void handlePlay(ActionEvent actionEvent) {
        if (currentAttempt == null) {
            handleRestart(actionEvent);
        } else {
            playTimer();
        }
    }

    public void handlePause(ActionEvent actionEvent) {
        pauseTimer();
    }
}
