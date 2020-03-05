/*
 * Copyright 2017 lancw.
 *  个人博客 https://www.vbox.top/
 */
package com.lcw.controller;

import com.lcw.action.ThankUserAction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

/**
 *
 * @author lancw
 * @since 2017-8-26 10:16:38
 */
public class ThankUserController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tua = new ThankUserAction(this);
        tua.initializeAction();
    }

    @FXML
    public WebView thanks;
    private ThankUserAction tua;

}
