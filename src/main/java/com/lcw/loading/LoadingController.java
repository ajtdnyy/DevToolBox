package com.lcw.loading;

import com.lcw.action.LoadingAction;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
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
        new Thread(() -> {
            // 20210604
            downloadJar("httpclient-4.5.13.jar");
            downloadJar("httpcore-4.4.13.jar");
            downloadJar("httpmime-4.5.6.jar");

            downloadJar("httpmime-4.5.3.jar");
            downloadJar("dubbo-2.5.3.jar");
            downloadJar("fastjson-1.2.47.jar");
            downloadJar("javassist-3.25.0-GA.jar");
        }).start();
    }

    private void downloadJar(String name) {
        File file = new File(System.getProperty("user.dir") + "/lib/" + name);
        if (!file.exists()) {
            try {
                downloadFile("https://vbox.top/app/" + name, file.getAbsolutePath());
            } catch (Exception ex) {
                LOGGER.error("下载依赖包失败，请重启再试。", ex);
            }
        }
    }

    /**
     * 下载文件
     *
     * @param destUrl
     * @param fileName
     * @throws IOException
     */
    public static void downloadFile(String destUrl, String fileName) throws IOException {
        byte[] buf = new byte[1024];
        int size = 0;
        URL url = new URL(destUrl);
        HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
        httpUrl.connect();
        try (BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream()); FileOutputStream fos = new FileOutputStream(fileName)) {
            while ((size = bis.read(buf)) != -1) {
                fos.write(buf, 0, size);
            }
        }
        httpUrl.disconnect();
    }
}
