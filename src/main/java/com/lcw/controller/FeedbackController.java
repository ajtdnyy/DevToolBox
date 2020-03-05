/*
 * Copyright 2017 lancw.
 *  个人博客 https://www.vbox.top/
 */
package com.lcw.controller;

import com.lcw.action.FeedbackAction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author lancw
 * @since 2017-7-29 20:47:12
 */
public class FeedbackController implements Initializable {

    @FXML
    public void submitFeedback() {
        feedbackAction.submitFeedbackAction();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        feedbackAction = new FeedbackAction(this);
        feedbackAction.initializeAction();
    }
    private FeedbackAction feedbackAction;
    public Stage stage;
    @FXML
    public TextField mail;
    @FXML
    public TextField feedbackTitle;
    @FXML
    public TextArea feedbackContent;
}
