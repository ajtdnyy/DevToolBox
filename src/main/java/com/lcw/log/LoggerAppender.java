/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lcw.log;

import com.lcw.controller.MainController;
import com.lcw.util.ApplicationStageManager;
import java.util.Objects;
import javafx.application.Platform;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author ajtdnyy
 */
public class LoggerAppender extends AppenderSkeleton {

    private Long length = 0L;

    @Override
    protected void append(LoggingEvent event) {
        MainController mc = (MainController) ApplicationStageManager.getController(ApplicationStageManager.StateAndController.MAIN);
        if (mc != null) {
            Platform.runLater(() -> {
                String tmp = this.layout.format(event);
                length++;
                mc.logArea.appendText(tmp);
                if (layout.ignoresThrowable()) {
                    String[] throwableStrRep = event.getThrowableStrRep();
                    if (Objects.nonNull(throwableStrRep)) {
                        for (String throwStr : throwableStrRep) {
                            length += 2;
                            mc.logArea.appendText(throwStr);
                            mc.logArea.appendText("\n");
                        }
                    }
                }
                if (length > 1000) {
                    mc.logArea.deleteText(0, 100);
                    length -= 100;
                }
            });
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

}
