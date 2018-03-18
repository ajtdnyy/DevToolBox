package com.lcw.loading;

import com.lcw.util.ApplicationStageManager;
import insidefx.undecorator.Undecorator;
import insidefx.undecorator.UndecoratorScene;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loading extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        long time = System.currentTimeMillis();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/loading.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setTitle("启动中");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/skin/icon.png")));
        stage.show();
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        LoadingController lc = fxmlLoader.getController();
        ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.LOADING, stage, lc);
        ApplicationStageManager.setApplication(this);
        LOGGER.info("启动页加载耗时:" + (System.currentTimeMillis() - time) + "毫秒");
        new Thread(() -> {
            Platform.runLater(() -> {
                startMainApp(stage);
            });
        }).start();
    }

    public void startMainApp(Stage ms) {
        try {
            long time = System.currentTimeMillis();
            Stage stage = new Stage();
            stage.setTitle(Undecorator.LOC.getString("AppName") + " " + Undecorator.LOC.getString("Version") + " 版本号：" + Undecorator.LOC.getString("VersionCode"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Region root = (Region) fxmlLoader.load();
            final UndecoratorScene undecoratorScene = new UndecoratorScene(stage, root);
            stage.setOnCloseRequest((WindowEvent we) -> {
                we.consume();
                undecoratorScene.setFadeOutTransition();
            });
            undecoratorScene.getStylesheets().add("/styles/main.css");
            stage.setScene(undecoratorScene);
            stage.toFront();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/skin/icon.png")));
            stage.setOnShown((event) -> {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            ms.close();
                        });
                    }
                }, 1000);
            });
            stage.show();
            ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.MAIN, stage, fxmlLoader.getController());
            LOGGER.info("主页面加载耗时:" + (System.currentTimeMillis() - time) + "毫秒");
        } catch (IOException ex) {
            LOGGER.error("启动主程序异常", ex);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Loading.class);
}
