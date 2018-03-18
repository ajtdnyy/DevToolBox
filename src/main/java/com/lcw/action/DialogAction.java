/*
 *
 */
package com.lcw.action;

import com.lcw.controller.*;
import com.lcw.util.ApplicationStageManager;
import com.lcw.util.DialogUtil;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;

/**
 * FXML Controller class
 *
 * @author lancw
 */
public class DialogAction {

    private final String adUrl = "http://www.vbox.top/ad.php?type=dialog";
    private final DialogController dc;

    public DialogAction(DialogController dc) {
        this.dc = dc;
    }

    public void closeAdAction() {
        DialogUtil.close();
    }

    public void openAdAction() {
        ApplicationStageManager.getApplication().getHostServices().showDocument(dc.webView.getUserData().toString());
    }

    public void initializeAction() {
        dc.closeAdBtn.setVisible(false);
        dc.webView.getEngine().load(adUrl);
        dc.webView.getEngine().locationProperty().addListener((ObservableValue<? extends String> ov, final String oldLoc, final String loc) -> {
            if (!adUrl.equals(loc)) {
                Platform.runLater(() -> {
                    ApplicationStageManager.getApplication().getHostServices().showDocument(loc);
                });
            }
            dc.webView.getEngine().load(adUrl);
        });
        DialogUtil.setCloseAdBtn(dc.closeAdBtn);
        dc.progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }

}
