package com.lcw.loading;

import com.lcw.action.LoadingAction;
import com.lcw.util.HttpClientUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import org.slf4j.LoggerFactory;

public class LoadingController implements Initializable {

    @FXML
    public ImageView imageView;
    public LoadingAction action;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoadingController.class);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        action = new LoadingAction(this);
        action.initialize();
        downloadJar("httpmime-4.5.3.jar");
        downloadJar("dubbo-2.5.3.jar");
        downloadJar("fastjson-1.2.47.jar");
    }

    private void downloadJar(String name) {
        File file = new File(System.getProperty("user.dir") + "/lib/" + name);
        if (!file.exists()) {
            try {
                HttpClientUtil.downloadFile("http://www.vbox.top/app/" + name, file.getAbsolutePath());
            } catch (IOException ex) {
                LOGGER.error("下载依赖包失败，请重启再试。", ex);
            }
        }
    }
}
