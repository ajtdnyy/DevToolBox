/*
 * Copyright 2017 lancw.
 *  个人博客 http://www.vbox.top/
 */
package com.lcw.action;

import com.lcw.controller.HelpController;
import com.lcw.util.ApplicationStageManager;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author lancw
 * @since 2017-8-20 12:17:07
 */
public class HelpAction {

    public HelpAction(HelpController hc) {
        this.hc = hc;
    }

    public void initializeAction() {
        hc.helpView.getEngine().load(helpURL);
        hc.helpView.getEngine().locationProperty().addListener((ObservableValue<? extends String> ov, final String oldLoc, final String loc) -> {
            if (!helpURL.equals(loc)) {
                Platform.runLater(() -> {
                    ApplicationStageManager.getApplication().getHostServices().showDocument(loc);
                });
            }
            hc.helpView.getEngine().load(helpURL);
        });
    }

    private final HelpController hc;
    private final String helpURL = "http://www.vbox.top/app/help.html";
}
