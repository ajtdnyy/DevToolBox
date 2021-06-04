/*
 * Copyright 2017 lancw.
 *  个人博客 https://vbox.top/
 */
package com.lcw.controller;

import com.lcw.action.HelpAction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

/**
 *
 * @author lancw
 * @since 2017-8-20 12:16:23
 */
public class HelpController implements Initializable {

    @FXML
    public WebView helpView;
    private HelpAction helpAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        helpAction = new HelpAction(this);
        helpAction.initializeAction();
    }
}
