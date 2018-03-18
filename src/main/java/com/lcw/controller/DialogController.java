/*
 *
 */
package com.lcw.controller;

import com.lcw.action.DialogAction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author lancw
 */
public class DialogController implements Initializable {

    @FXML
    public WebView webView;
    @FXML
    public Button closeAdBtn;
    @FXML
    public ProgressBar progressBar;
    private DialogAction dialogAction;

    @FXML
    public void closeAd(ActionEvent event) {
        dialogAction.closeAdAction();
    }

    @FXML
    public void openAd() {
        dialogAction.openAdAction();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dialogAction = new DialogAction(this);
        dialogAction.initializeAction();
    }

}
