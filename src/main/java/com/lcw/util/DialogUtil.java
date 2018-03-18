package com.lcw.util;

import com.lcw.controller.DialogController;
import com.sun.javafx.tk.Toolkit;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 文件名称：DialogUtil.java </p>
 * <p>
 * 文件描述：</p>
 * <p>
 * 版权所有： 版权所有(C)2017-2099 </p>
 * <p>
 * 内容摘要： </p>
 * <p>
 * 其他说明： </p>
 *
 * @version 1.0
 * @author lancw
 * @since 2017-6-30 13:28:53
 */
public class DialogUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DialogUtil.class.getName());
    private static final Stage STAGE = new Stage();
    private static Button closeAdBtn;
    private static Timer timer;
    private static long delay = 0;//避免打开窗口闪一下

    static {
        new DialogUtil();
    }

    private DialogUtil() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog.fxml"));
            Region root = (Region) fxmlLoader.load();
            Scene scene = new Scene(root); //创建场景；
            STAGE.setScene(scene); //将场景载入舞台；
            STAGE.initModality(Modality.APPLICATION_MODAL);
            STAGE.initStyle(StageStyle.UNDECORATED);
            DialogController dc = fxmlLoader.getController();
            STAGE.setOnHidden((event) -> {
                dc.webView.getEngine().reload();
            });
            ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.DIALOG, STAGE, dc);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public static Stage showDialog(Window owner) throws Exception {
        if (STAGE.getOwner() == null) {
            STAGE.initOwner(owner);
        }
        delay = System.currentTimeMillis();
        STAGE.show();
        closeAdBtn.setVisible(false);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("超时显示关闭按钮");
                closeAdBtn.setVisible(true);
            }
        }, 5000);
        return STAGE;
    }

    public static void close() {
        if (STAGE != null && STAGE.isShowing()) {
            long dl = System.currentTimeMillis() - delay;
            if (dl <= 1000) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        close();
                    }
                }, dl);
            } else if (!Toolkit.getToolkit().isFxUserThread()) {
                Platform.runLater(() -> {
                    STAGE.close();
                });
            } else {
                STAGE.close();
            }
            timer.cancel();
        }
    }

    public static void setCloseAdBtn(Button closeAdBtn) {
        DialogUtil.closeAdBtn = closeAdBtn;
    }

}
