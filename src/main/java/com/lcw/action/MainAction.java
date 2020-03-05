package com.lcw.action;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.JSONArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lcw.controller.MainController;
import com.lcw.eum.Charset;
import com.lcw.eum.CodeFormat;
import com.lcw.eum.ContentType;
import com.lcw.eum.FilterType;
import static com.lcw.eum.FilterType.APP_NAME;
import static com.lcw.eum.FilterType.SERVER_IP;
import static com.lcw.eum.FilterType.SERVICE_NAME;
import com.lcw.eum.RedisDataType;
import com.lcw.eum.RequestMethod;
import com.lcw.eum.UserAgent;
import com.lcw.game.PuzzlePiecesApp;
import com.lcw.model.DubboServiceModel;
import com.lcw.model.HttpRequestData;
import com.lcw.model.MethodModel;
import com.lcw.model.PageCallBack;
import com.lcw.model.ParameterModel;
import com.lcw.model.SystemSetting;
import com.lcw.util.ApplicationStageManager;
import com.lcw.util.ChartUtil;
import com.lcw.util.DataAccessUtil;
import com.lcw.util.DialogUtil;
import com.lcw.util.DubboUtil;
import com.lcw.util.Formatter;
import com.lcw.util.HttpClientUtil;
import com.lcw.util.JaxbUtil;
import com.lcw.util.MavenDependencyUtil;
import com.lcw.util.RedisUtil;
import com.lcw.util.ReflectUtil;
import com.lcw.util.SerializableUtil;
import com.lcw.util.Toast;
import com.lcw.util.ZookeeperUtil;
import insidefx.undecorator.Undecorator;
import insidefx.undecorator.UndecoratorScene;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainAction {

    public void openFileChooserBtnAction() {
        String pom = mc.servicePom.getText();
        File dir = new File(pom).getParentFile();
        if (pom != null && !pom.trim().isEmpty() && dir.exists()) {
            fileChooser.setInitialDirectory(dir);
            dirChooser.setInitialDirectory(dir);
        }
        fileChooser.setTitle("打开pom或jar文件夹");
        dirChooser.setTitle("打开pom或jar文件夹");
        if (mc.fileType.getSelectionModel().getSelectedIndex() == 0) {
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                mc.servicePom.setText(file.getAbsolutePath());
            }
        } else {
            File file = dirChooser.showDialog(null);
            if (file != null) {
                mc.servicePom.setText(file.getAbsolutePath());
            }
        }
    }

    public void connectZKBtnAction() {
        String address = mc.zkClientAddress.getText();
        if (!address.contains(":")) {
            Toast.makeText("zookeeper地址错误，需要包含端口").show(mc.pane.getScene().getWindow());
            return;
        }
        try {
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            Task task = new Task() {
                @Override
                protected Object call() {
                    try {
                        ZookeeperUtil.connectZK(address);
                    } catch (Exception e) {
                        Toast.makeText("zookeeper连接异常：" + e.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                        LOGGER.error(e.getLocalizedMessage(), e);
                        DialogUtil.close();
                    }
                    List<String> services = ZookeeperUtil.getDubboService(null);
                    List<DubboServiceModel> dsms = new ArrayList<>();
                    if (services != null) {
                        String filter = mc.serviceFilter.getText();
                        for (String service : services) {
                            if (StringUtils.isNotBlank(filter)) {
                                if (service.toLowerCase().contains(filter.toLowerCase())) {
                                    initService(service, dsms);
                                }
                            } else {
                                initService(service, dsms);
                            }
                        }
                    }
                    Platform.runLater(() -> {
                        itemsCache.clear();
                        itemsCache.addAll(dsms);
                        ObservableList items = mc.serviceList.getItems();
                        items.clear();
                        items.addAll(dsms);
                        mc.serviceMethods.getItems().clear();
                        serviceFilterKeyUpAction();
                        ZookeeperUtil.close();
                        DialogUtil.close();
                    });
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    Toast.makeText("zookeeper连接异常：" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    LOGGER.error("连接zookeeper异常", t);
                    DialogUtil.close();
                }
            };
            new Thread(task).start();
            saveSetting();
        } catch (Exception ex) {
            Toast.makeText("zookeeper连接异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
            DialogUtil.close();
        }
    }

    private void initService(String service, Collection<DubboServiceModel> dsms) {
        List<String> subList = ZookeeperUtil.getDubboService(service);
        DubboServiceModel ds = new DubboServiceModel(service);
        for (String s : subList) {
            ds.init(s, mc.timeout.getText());
        }
        dsms.add(ds);
    }

    public void serviceFilterKeyUpAction() {
        ObservableList items = mc.serviceList.getItems();
        if (itemsCache.isEmpty() && items != null) {
            items.forEach((item) -> {
                itemsCache.add((DubboServiceModel) item);
            });
        }
        String filter = mc.serviceFilter.getText();
        if ((filter == null || filter.trim().isEmpty()) && items != null) {
            items.clear();
            items.addAll(itemsCache.toArray());
        } else {
            items.clear();
            FilterType ft = (FilterType) mc.filterType.getSelectionModel().getSelectedItem();
            switch (ft) {
                case SERVICE_NAME:
                    itemsCache.stream().filter((object) -> (object.getInterfaceFullName().toLowerCase().contains(filter.toLowerCase()))).forEachOrdered((object) -> {
                        items.add(object);
                    });
                    break;
                case APP_NAME:
                    itemsCache.stream().filter((dsm) -> (dsm.getApplication().toLowerCase().contains(filter.toLowerCase()))).forEachOrdered((dsm) -> {
                        items.add(dsm);
                    });
                    break;
                case SERVER_IP:
                    itemsCache.stream().filter((dsm) -> (dsm.getClientStr().contains(filter))).forEachOrdered((dsm) -> {
                        items.add(dsm);
                    });
                    break;
            }
        }
        mc.listViewStatusLabel.setText(String.format("共%d条,显示%d条", itemsCache.size(), items.size()));
        saveSetting();
    }

    public void serviceMethodClickAction() {
        try {
            DubboServiceModel item = (DubboServiceModel) mc.serviceList.getSelectionModel().getSelectedItem();
            if (item == null) {
                return;
            }
            MethodModel mm = (MethodModel) mc.serviceMethods.getSelectionModel().getSelectedItem();
            if (mm == null) {
                return;
            }
            saveSetting();
            String tmp = paramCache.get(item.getInterfaceFullName() + mm.toString());
            mc.requestArea.setText(Formatter.formatDubboParam(tmp == null ? mm.toJsonString() : tmp));
        } catch (Exception ex) {
            Toast.makeText("接口分析异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void invokeActionAction() {
        try {
            saveSetting();
            DubboServiceModel item = (DubboServiceModel) mc.serviceList.getSelectionModel().getSelectedItem();
            MethodModel mm = (MethodModel) mc.serviceMethods.getSelectionModel().getSelectedItem();
            String json = mc.requestArea.getText();
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        Method md = mm.getInterfacMethod();
                        String to = mc.timeout.getText();
                        String ip = (String) mc.providerIP.getSelectionModel().getSelectedItem();
                        List<Object> ja = com.alibaba.fastjson.JSON.parseArray(json, md.getParameterTypes());
                        List<Object> param = new ArrayList<>();
                        for (int i = 0; i < ja.size(); i++) {
                            Object o = ja.get(i);
                            if (o instanceof List) {
                                ParameterModel pm = mm.getParameters().get(i);
                                String tn = pm.getParameterizedType().getActualTypeArguments()[0].getTypeName();
                                com.alibaba.fastjson.JSONArray o1 = (com.alibaba.fastjson.JSONArray) com.alibaba.fastjson.JSON.parseArray(json).get(i);
                                List<Object> ls = new ArrayList<>();
                                for (Object obj : o1) {
                                    com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) obj;
                                    ls.add(com.alibaba.fastjson.JSON.parseObject(jobj.toJSONString(), Class.forName(tn)));
                                }
                                param.add(ls);
                            } else {
                                param.add(o);
                            }
                        }
                        Object o = DubboUtil.invoke(loader.loadClass(item.getInterfaceFullName()), md, item.getUrl(to, ip), param.toArray());
                        Platform.runLater(() -> {
                            mc.responseArea.setText(o != null ? Formatter.formatDubboParam(o.toString()) : "");
                            DialogUtil.close();
                        });
                        //保存时清除掉非引号中的空格、回车、制表符
                        paramCache.put(item.getInterfaceFullName() + mm.toString(), json.replaceAll("\\r|\\n|\\t|\\s{2,}", ""));
                        SerializableUtil.saveObject(paramCache, SerializableUtil.FileNameEnum.PARAMETER_CACHE);
                    } catch (Exception ex) {
                        Toast.makeText("接口调用异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                        Platform.runLater(() -> {
                            mc.responseArea.setText(ex.getLocalizedMessage());
                            DialogUtil.close();
                        });
                    }
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    Toast.makeText("接口调用异常：" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    LOGGER.error("调用dubbo接口异常", t);
                    DialogUtil.close();
                }
            };
            new Thread(task).start();
        } catch (Exception ex) {
            Toast.makeText("接口调用异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
            mc.responseArea.setText(ex.getLocalizedMessage());
            DialogUtil.close();
        }
    }

    public void serviceListClickAction() {
        DubboServiceModel item = (DubboServiceModel) mc.serviceList.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        try {
            saveSetting();
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            Task task = new Task() {
                @Override
                protected Object call() {
                    try {
                        Platform.runLater(() -> {
                            mc.serviceMethods.getItems().clear();
                        });
                        ObservableList items = mc.providerIP.getItems();
                        items.clear();
                        String ip = item.getClientStr();
                        if (StringUtils.isBlank(ip)) {
                            DialogUtil.close();
                            return null;
                        }
                        if (CollectionUtils.isEmpty(item.getFullMethods(ip)) && item.getMethods(ip) != null) {
                            item.putFullMethods(ip, getAllMethods(item.getMethods(ip)));
                        }
                        Platform.runLater(() -> {
                            item.getUrlMap().entrySet().stream().map((entry) -> entry.getKey()).forEachOrdered((key) -> {
                                items.add(key);
                            });
                            mc.serviceMethods.getItems().addAll(item.getFullMethods(ip));
                            mc.providerIP.getSelectionModel().select(ip);
                            DialogUtil.close();
                        });
                    } catch (Exception e) {
                        Toast.makeText("解析接口参数异常：" + e.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                        LOGGER.error(e.getLocalizedMessage(), e);
                        DialogUtil.close();
                    }
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    Toast.makeText("解析接口参数异常：" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    LOGGER.error(t.getLocalizedMessage(), t);
                    DialogUtil.close();
                }
            };
            new Thread(task).start();
        } catch (Exception ex) {
            Toast.makeText("获取接口方法异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
            DialogUtil.close();
        }
    }

    public void wrapTextCheckBoxAction() {
        mc.requestArea.setWrapText(mc.wrapText.isSelected());
        mc.responseArea.setWrapText(mc.wrapText.isSelected());
    }

    public void wrapTextCheckBoxFormatAction() {
        mc.formatSource.setWrapText(mc.wrapTextFormat.isSelected());
        mc.formatTarget.setWrapText(mc.wrapTextFormat.isSelected());
    }

    public void wrapTextCheckBoxJavaAction() {
        mc.inputText.setWrapText(mc.wrapTextToJava.isSelected());
        mc.outputJava.setWrapText(mc.wrapTextToJava.isSelected());
    }

    public void wrapTextCheckBoxHttpAction() {
        mc.header.setWrapText(mc.wrapTextHttp.isSelected());
        mc.requestBody.setWrapText(mc.wrapTextHttp.isSelected());
        mc.prettyBody.setWrapText(mc.wrapTextHttp.isSelected());
        mc.responseBody.setWrapText(mc.wrapTextHttp.isSelected());
        mc.responseBody.setWrapText(mc.wrapTextHttp.isSelected());
        mc.showDoc.setWrapText(mc.wrapTextHttp.isSelected());
        mc.requestHeader.setWrapText(mc.wrapTextHttp.isSelected());
        mc.responseHeader.setWrapText(mc.wrapTextHttp.isSelected());
    }

    public void checkUpdateAction() {
        try {
            HttpRequestData.Request req = new HttpRequestData.Request();
            req.setUrl(updateURL);
            req.setMethod(RequestMethod.GET.toString());
            req.setTimeout("10000");
            req.setCharset(Charset.UTF8.getCode());
            HttpResponse resp = HttpClientUtil.sendRequest(req);
            String respBody = EntityUtils.toString(resp.getEntity(), req.getCharset());
            JsonObject json = gson.fromJson(respBody, JsonObject.class);
            String cur = Undecorator.LOC.getString("VersionCode");
            if (Integer.parseInt(cur) < json.get("version").getAsInt()) {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("版本更新");
                a.setContentText("当前版本：" + cur + "\n最新版本：" + json.get("version") + "\n更新内容：\n" + json.get("message").getAsString());
                a.initOwner(mc.pane.getScene().getWindow());
                Optional<ButtonType> o = a.showAndWait();
                if (o.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                    String dir = System.getProperty("user.dir");
                    HttpClientUtil.downloadFile(json.get("url").getAsString(), dir + "/开发辅助.jar.update");
                    a.setContentText("下载完成，请重启程序。");
                    a.setAlertType(Alert.AlertType.INFORMATION);
                    a.showAndWait();
                    File f = new File(dir + "/update.jar");
                    StringBuilder sb = new StringBuilder();
                    System.getenv().entrySet().forEach((entry) -> {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        sb.append(key).append("=").append(value).append("#");
                    });
                    Runtime.getRuntime().exec("java -jar update.jar", sb.toString().split("#"), f.getParentFile());
                    System.exit(0);
                }
            } else {
                Toast.makeText("当前已是最新版本", Duration.seconds(1)).show(mc.pane.getScene().getWindow());
            }
        } catch (Exception ex) {
            Toast.makeText("检查更新异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void clearCacheAction() {
        MavenDependencyUtil.clearCache();
    }

    public void showDialogAction() {
        try {
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
        } catch (Exception ex) {
            Toast.makeText("打开窗口异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void showHelpAction() {
        try {
            if (helpStage == null) {
                helpStage = new Stage();
                helpStage.setTitle(Undecorator.LOC.getString("AppName") + "-使用手册");
                helpStage.setResizable(false);
                helpStage.initOwner(mc.pane.getScene().getWindow());
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/help.fxml"));
                Region root = (Region) fxmlLoader.load();
                UndecoratorScene scene = new UndecoratorScene(helpStage, root);
                scene.setFadeInTransition();
                scene.getStylesheets().add("/styles/feedback.css");
                helpStage.setScene(scene); //将场景载入舞台；
                ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.HELP, helpStage, fxmlLoader.getController());
            }
            helpStage.show();
        } catch (Exception ex) {
            Toast.makeText("显示帮助界面异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void showQRCodeAction() {
        try {
            if (qrCodeStage == null) {
                qrCodeStage = new Stage();
                qrCodeStage.setTitle(Undecorator.LOC.getString("AppName") + "-感谢捐助");
                qrCodeStage.setResizable(false);
                qrCodeStage.initOwner(mc.pane.getScene().getWindow());
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/qrCode.fxml"));
                Region root = (Region) fxmlLoader.load();
                UndecoratorScene scene = new UndecoratorScene(qrCodeStage, root);
                scene.setFadeInTransition();
                scene.getStylesheets().add("/styles/feedback.css");
                qrCodeStage.setScene(scene); //将场景载入舞台；
                ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.QRCODE, qrCodeStage, fxmlLoader.getController());
            }
            qrCodeStage.show();
        } catch (Exception ex) {
            Toast.makeText("显示二维码异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void showFeedbackAction() {
        try {
            if (feedbackStage == null) {
                feedbackStage = new Stage();
                feedbackStage.setTitle(Undecorator.LOC.getString("AppName") + "-提交反馈");
                feedbackStage.setResizable(false);
                feedbackStage.initOwner(mc.pane.getScene().getWindow());
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/feedback.fxml"));
                Region root = (Region) fxmlLoader.load();
                UndecoratorScene scene = new UndecoratorScene(feedbackStage, root);
                scene.setFadeInTransition();
                scene.getStylesheets().add("/styles/feedback.css");
                feedbackStage.setScene(scene); //将场景载入舞台；
                ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.FEEDBACK, feedbackStage, fxmlLoader.getController());
            }
            feedbackStage.show();
        } catch (Exception ex) {
            Toast.makeText("显示反馈窗口异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    public void showAboutAction() {
        try {
            if (aboutStage == null) {
                aboutStage = new Stage();
                aboutStage.setTitle("关于" + Undecorator.LOC.getString("AppName"));
                aboutStage.setResizable(false);
                aboutStage.initOwner(mc.pane.getScene().getWindow());
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/about.fxml"));
                Region root = (Region) fxmlLoader.load();
                aboutStage.setUserData(root.getChildrenUnmodifiable().get(0));
                AnchorPane ap = (AnchorPane) root.getChildrenUnmodifiable().get(4);
                if (ap.getChildren().isEmpty()) {
                    ap.getChildren().add(new PuzzlePiecesApp().createContent());
                }
                UndecoratorScene scene = new UndecoratorScene(aboutStage, root);
                scene.setFadeInTransition();
                scene.getStylesheets().add("/styles/about.css");
                aboutStage.setScene(scene); //将场景载入舞台；
                ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.ABOUT, aboutStage, fxmlLoader.getController());
            }
            aboutStage.show();
        } catch (Exception ex) {
            Toast.makeText("显示关于窗口异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * 获取所有方法 dubbo中同名方法仅展示一次，所以需要通过反射找出所有方法
     *
     * @param names
     * @return
     */
    private List<MethodModel> getAllMethods(String[] names) {
        DubboServiceModel item = (DubboServiceModel) mc.serviceList.getSelectionModel().getSelectedItem();
        if (item == null) {
            return Collections.EMPTY_LIST;
        }
        try {
            String pom = mc.servicePom.getText();
            if (pom != null && !pom.trim().isEmpty()) {
                ArrayList<URL> uls = MavenDependencyUtil.analysisPOM(pom);
                for (URL ul : uls) {
                    if (LOADED_JAR_CACHE.add(ul)) {
                        LOGGER.info("加载class:" + ul.toString());
                        ReflectUtil.addURL(ul);
                    } else {
                        LOGGER.info("已经加载过的class:" + ul.toString());
                    }
                }
                Class c = loader.loadClass(item.getInterfaceFullName());
                item.setCls(c);
                Method[] ims = c.getMethods();
                if (c.isInterface() && !mc.noImplBox.isSelected()) {//如果是接口则获取某个实现类。因为接口无法反射出参数名
                    int size = uls.size() - 1;
                    for (int i = size; i >= 0; i--) {
                        URL ul = uls.get(i);
                        Class tmp = ReflectUtil.findImplementFromJar(c, ul);
                        if (tmp != null) {
                            c = tmp;
                            break;
                        }
                    }
                }
                Method[] ms = c.getMethods();
                List<MethodModel> mms = new ArrayList<>();
                for (String name : names) {
                    for (Method m : ms) {
                        if (m.getName().equals(name)) {//仅展示对外暴露的方法
                            List<ParameterModel> pms = ReflectUtil.getParameterNames(m);
                            for (Method im : ims) {
                                if (im.getName().equals(m.getName()) && im.getParameterCount() == m.getParameterCount()) {
                                    Class[] pts1 = im.getParameterTypes();
                                    Class[] pts2 = m.getParameterTypes();
                                    int sameCount = 0;
                                    for (int i = 0; i < m.getParameterCount(); i++) {
                                        if (pts1[i].getName().equals(pts2[i].getName())) {
                                            sameCount++;
                                        }
                                    }
                                    if (sameCount == m.getParameterCount()) {
                                        mms.add(new MethodModel(m.getName(), m, im, pms));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return mms;
            }
        } catch (Exception e) {
            Toast.makeText("获取接口方法异常：" + e.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    public void changeDubboVersion() {
        int idx = mc.dubboVersionBox.getSelectionModel().getSelectedIndex();
        saveSetting();
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        if (idx > 1) {
            a.setContentText("其他dubbo请将dubbo的jar包放置在该工具lib目录下，并命名为dubbo.jar。放好之后重启工具即可！点击确认将关闭程序。");
        } else {
            a.setContentText("更换dubbo版本需要重启工具才能生效！点击确认将关闭程序。");
        }
        Optional<ButtonType> o = a.showAndWait();
        if (o.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
            System.exit(0);
        }
    }

    private void saveSetting() {
        SystemSetting ss = (SystemSetting) JaxbUtil.loadXMLToBean(SystemSetting.class, JaxbUtil.SETTING_FILE);
        ss.setServicePomLocation(mc.servicePom.getText());
        ss.setZookeeperClientString(mc.zkClientAddress.getText());
        ss.setServiceFilter(mc.serviceFilter.getText());
        ss.setDubboWrapText(mc.wrapText.isSelected());
        ss.setHttpWrapText(mc.wrapTextHttp.isSelected());
        ss.setTimeout(mc.timeout.getText());
        ss.setToJavaWrapText(mc.wrapTextToJava.isSelected());
        ss.setFormatWrapText(mc.wrapTextFormat.isSelected());
        ss.setFilterType(((FilterType) mc.filterType.getSelectionModel().getSelectedItem()).getCode());
        ss.setRedisPort(mc.redisPort.getText());
        ss.setRedisPassword(mc.redisPwd.getText());
        ss.setRedisAddress(mc.redisAddr.getText());
        ss.setFileType(mc.fileType.getSelectionModel().getSelectedIndex() + "");
        ss.setNoImplBox(mc.noImplBox.isSelected());
        ss.setDubboVersion(mc.dubboVersionBox.getSelectionModel().getSelectedIndex());
        JaxbUtil.saveToXML(ss, JaxbUtil.SETTING_FILE);
    }

    public void initializeAction() {
        if (MavenDependencyUtil.getMavenHome() == null) {//初始化mavenHome变量
            mc.responseArea.appendText("未设置系统环境变量：MAVEN_HOME将无法使用Dubbo调试功能。");
        }
        mc.fileType.getItems().addAll("pom文件", "jar文件夹");
        mc.serviceList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        SystemSetting ss = (SystemSetting) JaxbUtil.loadXMLToBean(SystemSetting.class, JaxbUtil.SETTING_FILE);
        if (ss.getFileType() != null) {
            mc.fileType.getSelectionModel().select(Integer.parseInt(ss.getFileType()));
        } else {
            mc.fileType.getSelectionModel().select(0);
        }
        if (ss.getServicePomLocation() != null) {
            mc.servicePom.setText(ss.getServicePomLocation());
        }
        if (ss.getZookeeperClientString() != null) {
            mc.zkClientAddress.setText(ss.getZookeeperClientString());
        }
        if (ss.getServiceFilter() != null) {
            mc.serviceFilter.setText(ss.getServiceFilter());
        }
        if (ss.getDubboWrapText() != null) {
            mc.wrapText.setSelected(ss.getDubboWrapText());
        }
        if (ss.getNoImplBox() != null) {
            mc.noImplBox.setSelected(ss.getNoImplBox());
        }
        if (ss.getFormatWrapText()) {
            mc.wrapTextFormat.setSelected(ss.getFormatWrapText());
        }
        if (ss.getToJavaWrapText()) {
            mc.wrapTextToJava.setSelected(ss.getToJavaWrapText());
        }
        if (ss.getHttpWrapText()) {
            mc.wrapTextHttp.setSelected(ss.getHttpWrapText());
        }
        if (ss.getTimeout() != null) {
            mc.timeout.setText(ss.getTimeout());
        }
        mc.dubboVersionBox.getItems().addAll("dubbo 2.5.3", "dubbo 2.8.4", "其他版本");
        mc.dubboVersionBox.getSelectionModel().select(ss.getDubboVersion().intValue());
        int index = mc.dubboVersionBox.getSelectionModel().getSelectedIndex();
        String path = System.getProperty("user.dir") + "/lib";
        try {
            URL dubboJar = null;
            switch (index) {
                case 0:
                    dubboJar = new File(path + "/dubbo-2.5.3.jar").toURI().toURL();
                    break;
                case 1:
                    dubboJar = new File(path + "/dubbo-2.8.4.jar").toURI().toURL();
                    break;
                case 2:
                    dubboJar = new File(path + "/dubbo.jar").toURI().toURL();
                    break;
            }
            if (LOADED_JAR_CACHE.add(dubboJar)) {
                LOGGER.info("加载class:" + dubboJar.toString());
                ReflectUtil.addURL(dubboJar);
            } else {
                LOGGER.info("已经加载过的class:" + dubboJar.toString());
            }
        } catch (Exception e) {
            LOGGER.error("加载dubbo jar包异常", e);
        }
        mc.filterType.getItems().addAll(FilterType.values());
        if (ss.getFilterType() != null) {
            try {
                FilterType ft = FilterType.valueOf(ss.getFilterType());
                mc.filterType.getSelectionModel().select(ft);
            } catch (Exception e) {
                mc.filterType.getSelectionModel().select(0);
            }
        } else {
            mc.filterType.getSelectionModel().select(0);
        }
        if (paramCache.isEmpty()) {
            try {
                paramCache.putAll((Map<String, String>) SerializableUtil.readObject(SerializableUtil.FileNameEnum.PARAMETER_CACHE));
            } catch (Exception ex) {
                LOGGER.info("未找到参数缓存");
            }
        }
        requestData = (HttpRequestData) JaxbUtil.loadXMLToBean(HttpRequestData.class, JaxbUtil.HTTP_CONFIG_FILE);
        if (requestData != null) {
            mc.httpHistory.getItems().addAll(requestData.getRequest());
        } else {
            new File(JaxbUtil.HTTP_CONFIG_FILE).renameTo(new File(JaxbUtil.HTTP_CONFIG_FILE + "_" + System.currentTimeMillis() + ".bak"));
            requestData = new HttpRequestData();
        }
        mc.formatSplitPane.getItems().remove(mc.operBox);
        wrapTextCheckBoxAction();
        wrapTextCheckBoxHttpAction();
        wrapTextCheckBoxJavaAction();
        wrapTextCheckBoxFormatAction();
        mc.httpTimeout.getItems().addAll("60000", "20000", "15000", "10000", "5000");
        mc.generateType.getItems().addAll("实体", "枚举");
        mc.contentType.getItems().addAll(ContentType.values());
        mc.requestMethod.getItems().addAll(RequestMethod.values());
        mc.charset.getItems().addAll(Charset.values());
        mc.useragent.getItems().addAll(UserAgent.values());
        mc.operSelect.getItems().addAll(CodeFormat.values());
        mc.contentType.getSelectionModel().select(0);
        mc.httpTimeout.getSelectionModel().select(0);
        mc.useragent.getSelectionModel().select(0);
        mc.charset.getSelectionModel().select(0);
        mc.requestMethod.getSelectionModel().select(0);
        mc.operSelect.getSelectionModel().select(0);
        mc.generateType.getSelectionModel().select(0);
        mc.adWeb.getEngine().locationProperty().addListener((ObservableValue<? extends String> ov, final String oldLoc, final String loc) -> {
            if (!AD_URL.equals(loc)) {
                Platform.runLater(() -> {
                    ApplicationStageManager.getApplication().getHostServices().showDocument(loc);
                });
            }
            mc.adWeb.getEngine().load(AD_URL);
        });
        mc.providerIP.setOnAction((e) -> {
            String ip = (String) mc.providerIP.getSelectionModel().getSelectedItem();
            DubboServiceModel item = (DubboServiceModel) mc.serviceList.getSelectionModel().getSelectedItem();
            if (item != null) {
                mc.serviceMethods.getItems().clear();
                if (CollectionUtils.isNotEmpty(item.getFullMethods(ip))) {
                    mc.serviceMethods.getItems().addAll(item.getFullMethods(ip));
                } else if (item.getMethods(ip) != null) {
                    item.putFullMethods(ip, getAllMethods(item.getMethods(ip)));
                    mc.serviceMethods.getItems().addAll(item.getFullMethods(ip));
                }
            }
        });
        mc.adWeb.getEngine().load(AD_URL);
        mc.tabPane.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> o, Number ov, Number nv) -> {
            if (nv.intValue() != 0) {
                statusText = statusText.isEmpty() ? mc.listViewStatusLabel.getText() : statusText;
                mc.listViewStatusLabel.setText("");
            } else {
                mc.listViewStatusLabel.setText(statusText);
            }
        });
        mc.redisAddr.setText(ss.getRedisAddress());
        mc.redisPort.setText(ss.getRedisPort());
        mc.redisPwd.setText(ss.getRedisPassword());

        mc.redisInfoTable.getColumns().addAll(getColumn("属性名称", "parameter"), getColumn("属性值", "value"), getColumn("描述", "content"));
        mc.pageSizeBox.getItems().addAll(10, 20, 30, 40, 50, 100);
        mc.pageSizeBox.getSelectionModel().select(1);
        mc.pagination.setPageFactory(new PageCallBack());

        mc.redisDatabaseList.getSelectionModel().selectedIndexProperty().addListener((idx) -> {
            redisSelectDatabase();
        });
        mc.redisValueTable.getColumns().addAll(getColumn("key", "key"), getColumn("type", "type", 60d), getColumn("value", "value", 575d), getColumn("expire", "expire", 150d));
        mc.redisValueTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mc.pageSizeBoxForValue.getItems().addAll(10, 20, 30, 40, 50, 100);
        mc.pageSizeBoxForValue.getSelectionModel().select(1);
        mc.paginationForValue.setPageFactory((Integer param) -> {
            try {
                String name = (String) mc.redisDatabaseList.getSelectionModel().getSelectedItem();
                if (name == null) {
                    return null;
                }
                Integer pageSize = (Integer) mc.pageSizeBoxForValue.getSelectionModel().getSelectedItem();
                String key = mc.redisSearchKey.getText();
                key = StringUtils.isBlank(key) ? "nokey" : key;
                int start = param * pageSize;
                Map<String, Object> ret = RedisUtil.getNoSQLDBForRedis(pageSize, start, name, key, key);
                List<Map<String, Object>> ls = (List<Map<String, Object>>) ret.get("dataList");
                if (!Platform.isFxApplicationThread()) {
                    Platform.runLater(() -> {
                        mc.redisValueTable.getItems().clear();
                    });
                } else {
                    mc.redisValueTable.getItems().clear();
                }
                if (ls == null || ls.isEmpty()) {
                    if (!Platform.isFxApplicationThread()) {
                        Platform.runLater(() -> {
                            mc.paginationForValue.setPageCount(1);
                        });
                    } else {
                        mc.paginationForValue.setPageCount(1);
                    }
                    return new Label();
                }
                int total = (int) ret.get("rowCount");
                int page = total / pageSize + (total % pageSize == 0 ? 0 : 1);
                if (!Platform.isFxApplicationThread()) {
                    Platform.runLater(() -> {
                        mc.redisValueTable.getItems().addAll(ls.toArray());
                        mc.paginationForValue.setPageCount(page);
                    });
                } else {
                    mc.redisValueTable.getItems().addAll(ls.toArray());
                    mc.paginationForValue.setPageCount(page);
                }
                return new Label("显第" + (start + 1) + "到" + (start + ls.size()) + "条 共" + total + "条");
            } catch (Exception e) {
                LOGGER.error("读取数据异常", e);
                Toast.makeText("读取数据异常" + e.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                return new Label();
            }
        });
        new Thread(() -> {
            Platform.runLater(() -> {
                checkUpdateAction();
            });
        }).start();
    }

    private TableColumn getColumn(String name, String key) {
        return getColumn(name, key, 250d);
    }

    private TableColumn getColumn(String name, String key, double width) {
        TableColumn attr = new TableColumn(name);
        attr.setCellValueFactory(new MapValueFactory<>(key));
        attr.setPrefWidth(width);
        return attr;
    }

    public void formatOrConvertAction() {
        try {
            String src = mc.formatSource.getText();
            String tag = mc.formatTarget.getText();
            CodeFormat cf = (CodeFormat) mc.operSelect.getSelectionModel().getSelectedItem();
            if ((cf != CodeFormat.JSON_XML_EXCHANGE && StringUtils.isBlank(src)) || (StringUtils.isBlank(src) && StringUtils.isBlank(tag))) {
                return;
            }
            switch (cf) {
                case FORMAT_XML:
                    mc.formatTarget.setText(Formatter.formatXML(src));
                    break;
                case FORMAT_HTML:
                    mc.formatTarget.setText(Formatter.formatHtml(src, Charset.UTF8.getCode()));
                    break;
                case FORMAT_JSON:
                    mc.formatTarget.setText(Formatter.formatJson(src));
                    break;
                case FORMAT_SQL:
                    mc.formatTarget.setText(Formatter.formatSQL(src));
                    break;
                case JSON_XML_EXCHANGE:
                    if (src != null) {
                        mc.sourceToTarget();
                    } else if (tag != null) {
                        mc.targetToSource();
                    }
                    break;
            }
        } catch (Exception ex) {
            mc.formatTarget.setText(ex.getLocalizedMessage());
            Toast.makeText("格式与转换操作异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error("格式与转换操作异常", ex);
        }
    }

    public void sourceToTargetAction() {
        String src = mc.formatSource.getText();
        if (src == null || src.trim().isEmpty()) {
            return;
        }
        if (src.contains("<") && src.contains(">")) {
            mc.formatTarget.setText(Formatter.xml2json(src));
        } else if (src.startsWith("[") || src.startsWith("{")) {
            try {
                mc.formatTarget.setText(Formatter.json2xml(src));
            } catch (Exception ex) {
                LOGGER.error("转换异常", ex);
                Toast.makeText("转换异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                mc.formatTarget.setText("转换异常" + ex.getLocalizedMessage());
            }
        }
    }

    public void targetToSourceAction() {
        String tag = mc.formatTarget.getText();
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        if (tag.contains("<") && tag.contains(">")) {
            mc.formatSource.setText(Formatter.xml2json(tag));
        } else if (tag.startsWith("[") || tag.startsWith("{")) {
            try {
                mc.formatSource.setText(Formatter.json2xml(tag));
            } catch (Exception ex) {
                LOGGER.error("转换异常", ex);
                Toast.makeText("转换异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                mc.formatSource.setText("转换异常" + ex.getLocalizedMessage());
            }
        }
    }

    public void operSelectChangeAction() {
        CodeFormat cf = (CodeFormat) mc.operSelect.getSelectionModel().getSelectedItem();
        switch (cf) {
            case FORMAT_XML:
            case FORMAT_HTML:
            case FORMAT_JSON:
            case FORMAT_SQL:
                mc.formatSplitPane.getItems().remove(mc.operBox);
                break;
            case JSON_XML_EXCHANGE:
                mc.formatSplitPane.getItems().add(1, mc.operBox);
                break;
        }
    }

    public void historyFilterKeyUpAction() {
        String filter = mc.historyFilter.getText();
        mc.httpHistory.getItems().clear();
        if (filter == null || filter.trim().isEmpty()) {
            mc.httpHistory.getItems().addAll(requestData.getRequest());
        } else {
            requestData.getRequest().stream().filter((req) -> (req.getUrl().toLowerCase().contains(filter.toLowerCase()) || req.getAlias().contains(filter))).forEachOrdered((req) -> {
                mc.httpHistory.getItems().add(req);
            });
        }
    }

    public void sendActionAction() {
        try {
            HttpRequestData.Request req = saveHttpConfig();
            if (req == null) {
                return;
            }
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            Task task = new Task() {
                @Override
                protected Object call() {
                    try {
                        HttpResponse resp = HttpClientUtil.sendRequest(req);
                        String respBody = EntityUtils.toString(resp.getEntity(), req.getCharset());
                        Platform.runLater(() -> {
                            mc.responseHeader.setText("");
                            mc.requestHeader.setText("");
                            mc.prettyBody.setText("");
                            mc.showDoc.setText("");
                            if (respBody != null && !respBody.trim().isEmpty()) {
                                if (respBody.startsWith("{") || respBody.startsWith("[")) {
                                    mc.prettyBody.setText(Formatter.formatJson(respBody));
                                    try {
                                        createShowDoc(respBody, req);
                                    } catch (Exception ex) {
                                        LOGGER.error("生成ShowDoc异常：", ex);
                                        Toast.makeText("生成ShowDoc异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                                        mc.showDoc.setText("生成ShowDoc异常：" + ex.getLocalizedMessage());
                                    }
                                } else {
                                    try {
                                        if (respBody.contains("<html")) {
                                            try {
                                                mc.prettyBody.setText(Formatter.formatHtml(respBody, req.getCharset()));
                                            } catch (Exception ex) {
                                                LOGGER.error("格式化HTML异常：", ex);
                                                Toast.makeText("格式化HTML异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                                                mc.prettyBody.setText(respBody);
                                            }
                                        } else {
                                            mc.prettyBody.setText(Formatter.formatXML(respBody));
                                        }
                                    } catch (Exception e) {
                                        LOGGER.error("格式化XML异常：", e);
                                        Toast.makeText("格式化XML异常：" + e.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                                    }
                                }
                            }
                            mc.responseBody.setText(respBody);
                            if (resp.getStatusLine().getStatusCode() == 302) {
                                String loc = resp.getLastHeader("Location").getValue();
                                mc.prettyBody.setText("Status:302\nLocation:" + loc);
                                mc.previewBody.getEngine().load(loc);
                            } else if (RequestMethod.GET.equals(mc.requestMethod.getSelectionModel().getSelectedItem())) {
                                mc.previewBody.getEngine().load(req.getUrlWithData());
                            } else if (!StringUtils.isBlank(respBody)) {
                                mc.previewBody.getEngine().loadContent(respBody);
                            }
                            Header[] hs = resp.getAllHeaders();
                            if (hs != null) {
                                for (Header h : hs) {
                                    if (h.getName().startsWith(HttpClientUtil.REQUEST_HEADER_PREFIX)) {
                                        mc.requestHeader.appendText(h.getName().replace(HttpClientUtil.REQUEST_HEADER_PREFIX, "") + ":" + h.getValue() + "\n");
                                    } else {
                                        mc.responseHeader.appendText(h.getName() + ":" + h.getValue() + "\n");
                                    }
                                }
                            }
                            mc.prettyBody.appendText("\n");
                            mc.prettyBody.appendText("请求参数：\n");
                            mc.prettyBody.appendText(req.getBody().replaceAll("&", "\n"));
                            DialogUtil.close();
                        });
                        return null;
                    } catch (Exception ex) {
                        LOGGER.error("请求异常", ex);
                        Toast.makeText("请求异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                        mc.prettyBody.setText(ex.getLocalizedMessage());
                        DialogUtil.close();
                        return null;
                    }
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    mc.prettyBody.setText(t.getLocalizedMessage());
                    Toast.makeText("请求异常：" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    LOGGER.error("调用接口异常", t);
                    DialogUtil.close();
                }
            };
            new Thread(task).start();
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            Toast.makeText("请求异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            mc.prettyBody.setText(ex.getLocalizedMessage());
            DialogUtil.close();
        }
    }

    public void historyListClickAction(MouseEvent me) {
        if (mc.httpHistory.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        HttpRequestData.Request req = (HttpRequestData.Request) mc.httpHistory.getSelectionModel().getSelectedItem();
        if (me.getClickCount() == 2) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setContentText("确认要删除选中记录吗？");
            a.setTitle("确认删除");
            a.initOwner(mc.pane.getScene().getWindow());
            Optional<ButtonType> o = a.showAndWait();
            if (o.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                requestData.getRequest().remove(req);
                JaxbUtil.saveToXML(requestData, JaxbUtil.HTTP_CONFIG_FILE);
                mc.httpHistory.getItems().remove(req);
            }
            return;
        }
        mc.requestUrl.setText(req.getUrl());
        try {
            for (ContentType value : ContentType.values()) {
                if (value.getCode().equals(req.getContentType())) {
                    mc.contentType.getSelectionModel().select(value);
                    break;
                }
            }
        } catch (Exception e) {
            mc.contentType.getSelectionModel().select(0);
        }
        try {
            RequestMethod rm = RequestMethod.valueOf(req.getMethod());
            mc.requestMethod.getSelectionModel().select(rm);
        } catch (Exception e) {
            mc.requestMethod.getSelectionModel().select(0);
        }
        try {
            for (UserAgent value : UserAgent.values()) {
                if (value.getCode().equals(req.getUseagent())) {
                    mc.useragent.getSelectionModel().select(value);
                }
            }
        } catch (Exception e) {
            mc.useragent.getSelectionModel().select(0);
        }
        try {
            for (Charset value : Charset.values()) {
                if (value.getCode().equals(req.getCharset())) {
                    mc.charset.getSelectionModel().select(value);
                }
            }
        } catch (Exception e) {
            mc.charset.getSelectionModel().select(0);
        }
        if (!StringUtils.isBlank(req.getTimeout())) {
            mc.httpTimeout.getSelectionModel().select(req.getTimeout());
        }
        mc.header.setText(req.getHeader());
        mc.alias.setText(req.getAlias());
        mc.requestBody.setText(req.getBody());
        mc.replaceVar.setSelected(req.getReplaceVar());
    }

    public void insertExampleAction() {
        mc.fieldSplit.setText(" ");
        mc.inputText.setText("元素名称 长度 必填 样例 说明\nServerName 16 是 orderStatus 固定值orderStatus\nCustID 16 是 1300000465 工厂编号\nSign 50 是 见Post发送样例 数字签名(生成方式请参见Post发送样例)\nContent 1280 是 见XML示例 内容为XML格式的单据状态信息");
    }

    public void generateActionAction() {
        String text = mc.inputText.getText();
        saveSetting();
        if (text == null || text.trim().isEmpty()) {
            Toast.makeText("未输入任何文本").show(mc.pane.getScene().getWindow());
        } else {
            String type = mc.generateType.getSelectionModel().getSelectedItem().toString();
            Boolean json = mc.jsonField.isSelected();
            Boolean xml = mc.xmlField.isSelected();
            String pre = mc.eumPrefix.getText();
            pre = pre == null || pre.trim().isEmpty() ? "CODE" : pre;
            try {
                String spe = mc.fieldSplit.getText();
                StringBuilder sb = new StringBuilder();
                String[] input = text.split("\\n");
                String common = "/**" + input[0].replaceAll(spe, ":%s ") + ":%s */\n";
                for (int i = 1; i < input.length; i++) {
                    String tmp = input[i];
                    if (tmp != null && !tmp.trim().isEmpty()) {
                        String[] arr = tmp.split(spe);
                        sb.append(String.format(common, arr));
                        if ("实体".equals(type)) {
                            sb.append("private String ").append(arr[0]).append(";\n");
                        } else {
                            if (xml) {
                                sb.append("@XmlEnumValue(\"").append(arr[0]).append("\")\n");
                            }
                            sb.append(pre.toUpperCase()).append("_").append(arr[0].toUpperCase()).append("(");
                            for (int k = 0; k < arr.length; k++) {
                                String t = arr[k];
                                sb.append("\"").append(t).append("\"");
                                if (k != arr.length - 1) {
                                    sb.append(",");
                                }
                            }
                            sb.append(")");
                            if (i != input.length - 1) {
                                sb.append(",\n");
                            } else {
                                sb.append(";\n");
                            }
                        }
                    }
                }
                if ("实体".equals(type)) {
                    common = "/**" + input[0].replaceAll(spe, ":%s ") + ":%s *@return */\n";
                    String pcom = "/**" + input[0].replaceAll(spe, ":%s ") + ":%s *@param %s*/\n";
                    for (int i = 1; i < input.length; i++) {
                        String tmp = input[i];
                        if (tmp != null && !tmp.trim().isEmpty()) {
                            String[] arr = tmp.split(spe);
                            sb.append(String.format(common, arr));
                            if (json) {
                                sb.append("@JSONField(name = \"").append(arr[0]).append("\")\n");
                            }
                            sb.append("public String get").append(arr[0]).append("(){\nreturn ").append(arr[0]).append(";\n}\n");
                            String[] param = new String[arr.length + 1];
                            System.arraycopy(arr, 0, param, 0, arr.length);
                            param[arr.length] = arr[0];
                            sb.append(String.format(pcom, param));
                            sb.append("public void set").append(arr[0]).append("(String ").append(arr[0]).append("){\n this.").append(arr[0]).append("=").append(arr[0]).append(";\n}\n");
                        }
                    }
                } else {
                    sb.append("private final String code;\nprivate final String text;\npublic String getCode(){\nreturn code;\n}\npublic String getText(){\nreturn text;\n}\n");
                }
                mc.outputJava.setText(sb.toString());
            } catch (Exception e) {
                LOGGER.error("数据异常:", e);
                Toast.makeText("数据异常：" + e.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                mc.outputJava.setText("数据异常:" + e.getClass().getName() + "_" + e.getLocalizedMessage());
            }

        }
    }

    private void cancelTimerAndRelease() {
        timer.cancel();
        timer = new Timer();
        RedisUtil.pool = null;
    }

    public void redisConnectAction() {
        try {
            String rdaddr = mc.redisAddr.getText();
            if (rdaddr == null || rdaddr.trim().isEmpty()) {
                Toast.makeText("请输入redis地址").show(mc.pane.getScene().getWindow());
                return;
            }
            String rdport = mc.redisPort.getText();
            if (rdport == null || rdport.trim().isEmpty()) {
                rdport = "6379";
            }
            String pwd = mc.redisPwd.getText();
            saveSetting();
            RedisUtil.pool = null;
            RedisUtil.initPool(rdaddr, rdport, pwd);
            redisRefreshInfoAction();
            List<String> dbs = DataAccessUtil.getAllDataBaseForReids();
            rdaddr = rdaddr.length() > 15 ? rdaddr.substring(0, 14) + "..." : rdaddr;
            mc.redisDatabase.setText(rdaddr);
            if (CollectionUtils.isNotEmpty(dbs)) {
                mc.redisDatabaseList.getItems().clear();
                mc.redisDatabaseList.getItems().addAll(dbs);
                mc.redisValueTable.getItems().clear();
                mc.redisDatabaseList.getSelectionModel().select(0);
                mc.paginationForValue.setPageCount(1);
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        mc.redisRefreshChar();
                    });
                }
            }, 0, 10000);
        } catch (Exception ex) {
            cancelTimerAndRelease();
            Toast.makeText("获取redis信息异常" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error("获取redis信息异常", ex);
        }
    }

    public void pageSizeBoxChangeAction() {
        PageCallBack pcb = (PageCallBack) mc.pagination.getPageFactory();
        int pageCount = pcb.setPageSize((Integer) mc.pageSizeBox.getSelectionModel().getSelectedItem());
        mc.pagination.setPageCount(pageCount);
    }

    public void pageSizeBoxChangeForValueAction() {
        try {
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            new Thread(new Task() {
                @Override
                protected Object call() throws Exception {
                    mc.paginationForValue.getPageFactory().call(0);
                    DialogUtil.close();
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    LOGGER.error("获取redis分页数据异常", t);
                    Toast.makeText("获取redis信息异常" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    DialogUtil.close();
                }
            }).start();
        } catch (Exception ex) {
            Toast.makeText("获取redis信息异常" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error("获取redis信息异常", ex);
            DialogUtil.close();
        }
    }

    public void redisRefreshInfoAction() {
        try {
            Map<String, Object> ret = DataAccessUtil.selectNoSQLDBStatusForRedis();
            List<Map<String, Object>> ls = (List<Map<String, Object>>) ret.get("result");
            Integer pageSize = (Integer) mc.pageSizeBox.getSelectionModel().getSelectedItem();
            Integer pageCount = ls.size() / pageSize + (ls.size() % pageSize == 0 ? 0 : 1);
            PageCallBack pcb = (PageCallBack) mc.pagination.getPageFactory();
            mc.redisInfoTable.getItems().clear();
            mc.redisInfoTable.getItems().addAll(ls.toArray());
            pcb.setNode(mc.redisInfoTable, pageSize);
            mc.pagination.setPageCount(pageCount);
            Toast.makeText("刷新成功", Duration.seconds(1)).show(mc.pane.getScene().getWindow());
        } catch (Exception ex) {
            cancelTimerAndRelease();
            Toast.makeText("获取redis信息异常" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error("获取redis信息异常", ex);
        }
    }

    private void redisSelectDatabase() {
        try {
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            new Thread(new Task() {
                @Override
                protected Object call() {
                    try {
                        mc.paginationForValue.getPageFactory().call(0);
                        mc.redisTabPane.getSelectionModel().select(2);
                        Tab tab = mc.redisTabPane.getTabs().get(2);
                        Platform.runLater(() -> {
                            tab.setText(mc.redisDatabaseList.getSelectionModel().getSelectedItem().toString());
                        });
                        DialogUtil.close();
                    } catch (Exception ex) {
                        Toast.makeText("接口调用异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                        DialogUtil.close();
                    }
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    Toast.makeText("接口调用异常：" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    LOGGER.error("获取redis数据库信息异常", t);
                    DialogUtil.close();
                }
            }).start();
        } catch (Exception ex) {
            Toast.makeText("获取redis信息异常" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error("获取redis信息异常", ex);
            DialogUtil.close();
        }
    }

    public void searchKeyFromRedisAction() {//redisSearchKey cssID
        try {
            DialogUtil.showDialog(mc.pane.getScene().getWindow());
            new Thread(new Task() {
                @Override
                protected Object call() {
                    try {
                        mc.paginationForValue.getPageFactory().call(0);
                        Toast.makeText("查询|刷新成功", Duration.seconds(1)).show(mc.pane.getScene().getWindow());
                        DialogUtil.close();
                    } catch (Exception ex) {
                        Toast.makeText("接口调用异常：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                        DialogUtil.close();
                    }
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    LOGGER.error("查询redis异常", t);
                    Toast.makeText("接口调用异常：" + t.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
                    DialogUtil.close();
                }
            }).start();
        } catch (Exception ex) {
            Toast.makeText("获取redis信息异常" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error("获取redis信息异常", ex);
        }
    }

    public void deleteRedisKeyAction() {
        ObservableList list = mc.redisValueTable.getSelectionModel().getSelectedItems();
        if (list.isEmpty()) {
            Toast.makeText("请选择要删除的记录").show(mc.pane.getScene().getWindow());
            return;
        }
        StringBuilder key = new StringBuilder();
        int i = 0;
        for (Object object : list) {
            Map mp = (Map) object;
            key.append(mp.get("key"));
            if (i != list.size() - 1) {
                key.append(",");
            }
            i++;
        }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("确认删除");
        a.setContentText("确认要删除[" + key + "]这些键值吗？");
        a.initOwner(mc.pane.getScene().getWindow());
        Optional<ButtonType> o = a.showAndWait();
        if (o.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
            String[] keys = key.toString().split(",");
            String name = (String) mc.redisDatabaseList.getSelectionModel().getSelectedItem();
            RedisUtil.deleteKeys(name, keys);
            Toast.makeText("删除成功", Duration.seconds(1)).show(mc.pane.getScene().getWindow());
            mc.paginationForValue.getPageFactory().call(0);
        }
    }

    public void addRedisKeyAction() {
        operRedis("添加记录", null);
    }

    Timer timer = new Timer();

    public void redisRefreshCharAction() {
        try {
            Map<String, Object> ret = DataAccessUtil.selectNoSQLDBStatusForRedis();
            List<Map<String, Object>> ls = (List<Map<String, Object>>) ret.get("result");
            Double memery = null;
            Double memery2 = null;
            Double cpuSys = null;
            Double cpuUser = null;
            Double qps1 = null;
            Double qps2 = null;
            Double input = null;
            Double output = null;
            for (Map<String, Object> l : ls) {
                String param = (String) l.get("parameter");
                if (null != param) {
                    Object value = l.get("value");
                    switch (param) {
                        case "used_memory":
                            memery = Double.parseDouble((String) value);
                            break;
                        case "used_memory_human":
                            mc.memeryLabel.setText("used memory\n" + value);
                            break;
                        case "mem_fragmentation_ratio":
                            memery2 = Double.parseDouble((String) value);
                            break;
                        case "used_cpu_sys":
                            cpuSys = Double.parseDouble((String) value);
                            mc.cpuLabel.setText("used cpu sys\n" + value);
                            break;
                        case "used_cpu_user":
                            cpuUser = Double.parseDouble((String) value);
                            break;
                        case "totalKeys":
                            mc.totalKeyLabel.setText("total keys\n" + value);
                            break;
                        case "connected_clients":
                            mc.clientLabel.setText("connected clients\n" + value);
                            break;
                        case "total_commands_processed":
                            mc.commandLabel.setText("total commands processed\n" + value);
                            qps1 = Double.parseDouble((String) value);
                            break;
                        case "instantaneous_ops_per_sec":
                            qps2 = Double.parseDouble((String) value);
                            break;
                        case "total_net_input_bytes":
                            input = Double.parseDouble((String) value);
                            break;
                        case "total_net_output_bytes":
                            output = Double.parseDouble((String) value);
                            break;
                        default:
                            break;
                    }
                }
            }
            Date now = new Date();
            if (commandPerSec != null) {
                ChartUtil.changeChart(mc.qpsChart, now, (qps1 - commandPerSec) / (System.currentTimeMillis() - second) / 1000, "total commands");
            }
            commandPerSec = qps1;
            ChartUtil.changeChart(mc.memeryChart, now, memery, "used memory");
            ChartUtil.changeChart(mc.cpuChart, now, cpuSys, "cpu sys");
            if (inputPerSec != null) {
                ChartUtil.changeChart(mc.networkChart, now, (input - inputPerSec) / (System.currentTimeMillis() - second) / 1000, "total net input bytes");
            }
            inputPerSec = input;

            ChartUtil.changeChart(mc.memeryChart1, now, memery2, "mem fragmentation ratio");
            ChartUtil.changeChart(mc.cpuChart1, now, cpuUser, "cpu user");
            ChartUtil.changeChart(mc.qpsChart1, now, qps2, "ops per sec");
            if (outputPerSec != null) {
                ChartUtil.changeChart(mc.networkChart1, now, (output - outputPerSec) / (System.currentTimeMillis() - second) / 1000, "total net output bytes");
            }
            outputPerSec = output;
            second = System.currentTimeMillis();
        } catch (Exception ex) {
            cancelTimerAndRelease();
            LOGGER.error("刷新数据异常", ex);
            Toast.makeText("刷新数据异常" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
        }
    }

    public void changeRefreshAction() {
        if (mc.redisAutoRefreshBox.isSelected()) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!Platform.isFxApplicationThread()) {
                        Platform.runLater(() -> {
                            redisRefreshCharAction();
                        });
                    } else {
                        redisRefreshCharAction();
                    }
                }
            }, 0, 10000);
        } else {
            timer.cancel();
            timer = new Timer();
        }
    }

    public void modifyRedisKeyAction() {
        if (mc.redisValueTable.getSelectionModel().getSelectedItems().size() > 1) {
            Toast.makeText("只能选择一条记录进行修改").show(mc.pane.getScene().getWindow());
            return;
        }
        Map item = (Map) mc.redisValueTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            Toast.makeText("至少选择一条记录进行修改").show(mc.pane.getScene().getWindow());
            return;
        }
        operRedis("修改记录", item);
    }

    private void operRedis(String title, Map<String, Object> data) {
        try {
            data = data == null ? new HashMap<>() : data;
            if (redisOperStage == null) {
                redisOperStage = new Stage();
                redisOperStage.initOwner(mc.pane.getScene().getWindow());
                redisOperStage.setResizable(false);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/redisOper.fxml"));
                Region root = (Region) fxmlLoader.load();
                UndecoratorScene scene = new UndecoratorScene(redisOperStage, root);
                scene.setFadeInTransition();
                scene.getStylesheets().add("/styles/redisOper.css");
                redisOperStage.setScene(scene); //将场景载入舞台；
                ApplicationStageManager.putStageAndController(ApplicationStageManager.StateAndController.REDIS, redisOperStage, fxmlLoader.getController());
            }
            redisOperStage.setTitle(Undecorator.LOC.getString("AppName") + "-" + title);
            UndecoratorScene s = (UndecoratorScene) redisOperStage.getScene();
            AnchorPane ap = (AnchorPane) s.getMyRoot();
            for (Node node : ap.getChildren()) {
                if (!(node instanceof HBox)) {
                    continue;
                }
                HBox nd = (HBox) node;
                if (nd.getChildren().size() < 2) {
                    Button bt = (Button) nd.getChildren().get(0);
                    bt.setDisable(false);
                    bt.setText("确认");
                    if (!data.isEmpty() && !RedisDataType.string.equals(data.get("type"))) {
                        bt.setDisable(true);
                        bt.setText("暂不支持此类型操作");
                    }
                    continue;
                }
                Node n = nd.getChildren().get(1);
                String id = n.getId();
                if (n instanceof TextField) {
                    TextField tf = (TextField) n;
                    if ("redisKey".equals(id)) {
                        tf.setText((String) data.get("key"));
                    }
                } else if (n instanceof ComboBox) {
                    ComboBox cb = (ComboBox) n;
                    if (data.get("type") != null) {
                        cb.getSelectionModel().select(data.get("type"));
                        cb.setDisable(true);
                    } else {
                        cb.setDisable(false);
                        cb.getSelectionModel().select(0);
                    }
                } else if (n instanceof TextArea) {
                    TextArea ta = (TextArea) n;
                    ta.setText(Formatter.formatDubboParam((String) data.get("value")));
                }
            }
            redisOperStage.show();
        } catch (Exception ex) {
            Toast.makeText(title + "失败：" + ex.getLocalizedMessage()).show(mc.pane.getScene().getWindow());
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void createShowDoc(String respBody, HttpRequestData.Request req) throws Exception {
        String tmp = respBody.startsWith("\"") ? respBody.substring(1, respBody.length() - 1) : respBody;
        JsonObject json = gson.fromJson(tmp, JsonObject.class);
        StringBuilder sb = new StringBuilder();
        String urlstr = req.getUrl();
        String[] adds = urlstr.split("/");
        int idx = urlstr.indexOf(adds[3]);
        sb.append("**简要描述：**\n\n- \n\n**请求URL：**\n\n- ` ").append(urlstr.substring(idx));
        sb.append(" `\n\n**请求方式：**\n\n- ").append(req.getMethod()).append(" \n\n**参数：** \n\n|参数名|必选|类型|说明|\n|:----|:---|:-----|-----|\n");
        String param = req.getBody();

        if (param != null && !param.trim().isEmpty()) {
            if ((param.startsWith("{") || param.startsWith("[")) && (param.endsWith("{") || param.endsWith("["))) {
                JsonObject jo = JSON.parse(param, JsonObject.class
                );
                jo.entrySet().forEach((entry) -> {
                    removeArrayEle(entry.getValue());
                });
                analysisJsonStr(sb, jo);
            } else {
                String[] pms = param.split("&");
                for (String pm : pms) {
                    sb.append("|").append(pm.split("=")[0]).append("|  |String|  |\n");
                }
            }
        }
        json.entrySet().forEach((entry) -> {
            removeArrayEle(entry.getValue());
        });
        sb.append("**返回示例**\n\n```\n").append(Formatter.formatJson(json.toString())).append("\n```\n\n**返回参数说明** \n\n|参数名|类型|说明|备注|\n|:-----|:-----|-----| |\n");
        analysisJsonStr(sb, json);
        sb.append(" **备注** \n\n- 更多返回错误代码请看首页的错误代码描述");
        mc.showDoc.setText(sb.toString());
    }

    private void analysisJsonStr(StringBuilder sb, Object json) {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        Object jo = json;
        if (jo instanceof JsonObject) {
            analysisJson(map, (JsonObject) jo, "");
        } else if (jo instanceof JsonArray) {
            JsonArray ja = (JsonArray) jo;
            Object subjo = ja.get(0);
            if (subjo instanceof JsonObject) {
                analysisJson(map, (JsonObject) subjo, "");
            }
        } else {
            sb.append("**result有误").append(jo);
        }
        analysisMap(map, sb, "");
    }

    /**
     * 解析TreeMap生成表格
     *
     * @param map
     * @param sheet
     */
    private static void analysisMap(LinkedHashMap<Object, Object> map, StringBuilder sb, String key1) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof LinkedHashMap) {
                sb.append("|").append(key).append("|").append(getValueType(value)).append("|  |  |\n");
            } else if (!key.equals(key1)) {
                sb.append("|").append(key).append("|").append(getValueType(value)).append("|  |  |\n");
            }
        }
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals(key1)) {
                continue;
            }
            if (value instanceof LinkedHashMap) {
                sb.append("\n\n**").append(key).append("参数说明** \n\n|参数名|类型|说明|备注|\n|:-----|:-----|-----| |\n");
                analysisMap((LinkedHashMap) value, sb, key.toString());
            }
        }
    }

    /**
     * 解析josn串到TreeMap
     *
     * @param treeMap
     * @param json
     * @param tmp
     */
    public static void analysisJson(LinkedHashMap<Object, Object> treeMap, JsonObject json, String tmp) {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        json.entrySet().forEach((t) -> {
            Object k = t.getKey();
            Object val = t.getValue();
            if (val instanceof JsonObject) {
                map.put(tmp + k, val);
            } else if (val instanceof JsonArray) {
                JsonArray arr = (JsonArray) val;
                map.put(tmp + k, arr.size() > 0 ? arr.get(0) : "");
            } else {
                treeMap.put(tmp + k, val);
            }
        });
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            LinkedHashMap<Object, Object> tmpMap = new LinkedHashMap<>();
            tmpMap.put(key, value);
            treeMap.put(key, tmpMap);
        }
        for (Map.Entry<Object, Object> entry : treeMap.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (value instanceof LinkedHashMap) {
                LinkedHashMap<Object, Object> v = (LinkedHashMap<Object, Object>) value;
                if (v.get(key) instanceof JsonObject) {
                    analysisJson(v, (JsonObject) v.get(key), tmp + "  ");
                }
            }
        }
    }

    private static String getValueType(Object value1) {
        if (value1 instanceof JsonPrimitive) {
            JsonPrimitive jp = (JsonPrimitive) value1;
            if (jp.isNumber() && jp.toString().contains(".")) {
                return "Double";
            }
            return jp.isBoolean() ? "Boolean" : jp.isNumber() ? "Integer" : jp.isString() ? "String" : "String";
        } else if (value1 instanceof JsonObject || value1 instanceof JSONArray || value1 instanceof LinkedHashMap) {
            return "Object[data]";
        }
        return "String";
    }

    /**
     * 清除集合中其他元素。只保留第一个
     *
     * @param val
     */
    private void removeArrayEle(Object val) {
        if (val instanceof JsonArray) {
            JsonArray arr = (JsonArray) val;
            int len = arr.size();
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    arr.remove(1);
                } else {
                    Object v = arr.get(i);
                    removeArrayEle(v);
                }
            }
        } else if (val instanceof JsonObject) {
            JsonObject jo = (JsonObject) val;
            jo.entrySet().forEach((next) -> {
                removeArrayEle(next.getValue());
            });
        }
    }

    private HttpRequestData.Request saveHttpConfig() throws Exception {
        String url = mc.requestUrl.getText();
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        HttpRequestData.Request req = new HttpRequestData.Request();
        req.setReplaceVar(mc.replaceVar.isSelected());
        String bd = mc.requestBody.getText();
        req.setBody(bd);
        req.setHeader(mc.header.getText());
        req.setUrl(url);
        req.setAlias(mc.alias.getText());
        req.setMethod(mc.requestMethod.getSelectionModel().getSelectedItem().toString());
        req.setContentType(mc.contentType.getSelectionModel().getSelectedItem().toString());
        req.setTimeout(mc.httpTimeout.getSelectionModel().getSelectedItem().toString());
        req.setUseagent(((UserAgent) mc.useragent.getSelectionModel().getSelectedItem()).getText());
        req.setCharset(mc.charset.getSelectionModel().getSelectedItem().toString());
        if (requestData.add(req)) {
            mc.httpHistory.getItems().add(req);
            mc.httpHistory.getSelectionModel().selectLast();
        } else {
            for (HttpRequestData.Request r : requestData.getRequest()) {
                if (r.equals(req)) {
                    BeanUtils.copyProperties(r, req);
                    break;
                }
            }
        }
        mc.httpHistory.refresh();
        JaxbUtil.saveToXML(requestData, JaxbUtil.HTTP_CONFIG_FILE);
        HttpRequestData.Request tmpReq = new HttpRequestData.Request();
        BeanUtils.copyProperties(tmpReq, req);
        URL ul = new URL(url);
        String qr = ul.getQuery();
        bd = StringUtils.isBlank(bd) ? "" : bd;

        if (qr != null) {
            bd += bd.isEmpty() ? qr : "&" + qr;
            tmpReq.setUrl(url.substring(0, url.indexOf("?")));
        }
        if (tmpReq.getReplaceVar()) {
            String[] tmp = bd.split("&");
            Date now = new Date();
            for (String str : tmp) {
                str = str.replace("==", "#$#");
                String[] param = str.split("=");
                if (param.length == 2) {
                    switch (param[1]) {
                        case "${UUID}":
                            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                            bd = bd.replace("=" + param[1], "=" + uuid);
                            break;
                        case "${random}":
                            bd = bd.replace("=" + param[1], "=" + System.currentTimeMillis());
                            break;
                        case "${date}":
                            dateFormat.applyPattern("yyyy-MM-dd");
                            bd = bd.replace("=" + param[1], "=" + URLEncoder.encode(dateFormat.format(now), req.getCharset()));
                            break;
                        case "${time}":
                            dateFormat.applyPattern("HH:mm:ss");
                            bd = bd.replace("=" + param[1], "=" + URLEncoder.encode(dateFormat.format(now), req.getCharset()));
                            break;
                        case "${datetime}":
                            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
                            bd = bd.replace("=" + param[1], "=" + URLEncoder.encode(dateFormat.format(now), req.getCharset()));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        tmpReq.setBody(bd);
        return tmpReq;
    }

    public MainAction(MainController mc) {
        this.mc = mc;
    }

    private final String updateURL = "https://www.vbox.top/app/version.php";
    public static final String AD_URL = "https://www.vbox.top/ad.php?type=text";
    private static final Logger LOGGER = LoggerFactory.getLogger(MainAction.class);
    static DirectoryChooser dirChooser = new DirectoryChooser();
    static FileChooser fileChooser = new FileChooser();
    static ClassLoader loader = ClassLoader.getSystemClassLoader();
    static HashSet<DubboServiceModel> itemsCache = new HashSet<>();
    static Gson gson = new GsonBuilder().create();
    static HashMap<String, String> paramCache = new HashMap<>();
    private String statusText = "";
    private static final LinkedHashSet<URL> LOADED_JAR_CACHE = new LinkedHashSet();
    private HttpRequestData requestData;
    private Long second;
    private Double commandPerSec;
    private Double inputPerSec;
    private Double outputPerSec;
    private Stage aboutStage;
    private Stage qrCodeStage;
    private Stage helpStage;
    private Stage feedbackStage;
    private Stage redisOperStage;
    private final MainController mc;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat();

}
