/*
 * Copyright 2017 lancw.
 *  个人博客 https://www.vbox.top/
 */
package com.lcw.action;

import static com.lcw.action.MainAction.AD_URL;
import com.lcw.controller.*;
import com.lcw.util.ApplicationStageManager;
import insidefx.undecorator.Undecorator;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author lancw
 * @since 2017-7-29 20:46:59
 */
public class AboutAction {

    public AboutAction(AboutController ac) {
        this.ac = ac;
    }

    public void initializeAction() {
        ac.titleLabel.setText(Undecorator.LOC.getString("AppName") + " " + Undecorator.LOC.getString("Version") + "  版本号：" + Undecorator.LOC.getString("VersionCode"));
        ac.adWeb.getEngine().locationProperty().addListener((ObservableValue<? extends String> ov, final String oldLoc, final String loc) -> {
            if (!AD_URL.equals(loc)) {
                Platform.runLater(() -> {
                    ApplicationStageManager.getApplication().getHostServices().showDocument(loc);
                });
            }
            ac.adWeb.getEngine().load(AD_URL);
        });
        ac.adWeb.getEngine().load(AD_URL);
    }

    public void openWebAction() {
        ApplicationStageManager.getApplication().getHostServices().showDocument("https://www.vbox.top/?from=dvb");
    }
    private final AboutController ac;
    public static final String AD_TEXT = "虚位以待";
}
