/*
 * Copyright 2017 lancw.
 *  个人博客 http://www.vbox.top/
 */
package com.lcw.controller;

import com.lcw.action.AboutAction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

/**
 *
 * @author lancw
 * @since 2017-7-29 20:46:59
 */
public class AboutController implements Initializable {

    private AboutAction aboutAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        aboutAction = new AboutAction(this);
        aboutAction.initializeAction();
    }

    @FXML
    public void openWeb() {
        aboutAction.openWebAction();
    }

    @FXML
    public WebView adWeb;
    @FXML
    public Label titleLabel;

}
