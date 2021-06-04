/*
 * Copyright 2017 lancw.
 *  个人博客 https://vbox.top/
 */
package com.lcw.util;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author lancw
 * @since 2017-7-29 20:36:47
 */
public class ApplicationStageManager {

    public static enum StateAndController {
        LOADING("loading", "LoadingController"),
        MAIN("main", "mainController"),
        ABOUT("about", "aboutController"),
        FEEDBACK("feedback", "feedbackController"),
        QRCODE("qrcode", "qrcodeController"),
        HELP("help", "helpController"),
        DIALOG("dialog", "dialogController"),
        REDIS("redis", "redisController");
        private final String state;
        private final String controller;

        private StateAndController(String state, String controller) {
            this.state = state;
            this.controller = controller;
        }

        public String getState() {
            return state;
        }

        public String getController() {
            return controller;
        }

    }

    private static final Map<String, Stage> STAGE = new HashMap<String, Stage>();
    private static final Map<String, Object> CONTROLLER = new HashMap<String, Object>();
    private static Application application;

    public static void putStageAndController(StateAndController sac, Stage s, Object controller) {
        STAGE.put(sac.getState(), s);
        CONTROLLER.put(sac.getController(), controller);
    }

    public static Application getApplication() {
        return application;
    }

    public static void setApplication(Application application) {
        ApplicationStageManager.application = application;
    }

    public static Stage getStage(StateAndController sac) {
        return STAGE.get(sac.getState());
    }

    public static Object getController(StateAndController sac) {
        return CONTROLLER.get(sac.getController());
    }
}
