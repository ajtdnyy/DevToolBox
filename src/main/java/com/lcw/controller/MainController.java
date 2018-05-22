package com.lcw.controller;

import com.lcw.action.MainAction;
import com.lcw.util.MavenDependencyUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class MainController implements Initializable {

    @FXML
    public void openFileChooserBtn() {
        mainAction.openFileChooserBtnAction();
    }

    @FXML
    public void connectZKBtn() {
        mainAction.connectZKBtnAction();
    }

    @FXML
    public void changeDubboVersion() {
        mainAction.changeDubboVersion();
    }

    @FXML
    public void serviceFilterKeyUp() {
        mainAction.serviceFilterKeyUpAction();
    }

    @FXML
    public void serviceMethodClick() {
        mainAction.serviceMethodClickAction();
    }

    @FXML
    public void invokeAction() {
        mainAction.invokeActionAction();
    }

    @FXML
    public void serviceListClick() {
        mainAction.serviceListClickAction();
    }

    @FXML
    public void checkUpdate() {
        mainAction.checkUpdateAction();
    }

    @FXML
    public void showDialog() {
        mainAction.showDialogAction();
    }

    @FXML
    public void showQRCode() {
        mainAction.showQRCodeAction();
    }

    @FXML
    public void showHelp() {
        mainAction.showHelpAction();
    }

    @FXML
    public void wrapTextCheckBox() {
        mainAction.wrapTextCheckBoxAction();
    }

    @FXML
    public void wrapTextCheckBoxFormat() {
        mainAction.wrapTextCheckBoxFormatAction();
    }

    @FXML
    public void wrapTextCheckBoxJava() {
        mainAction.wrapTextCheckBoxJavaAction();
    }

    @FXML
    public void wrapTextCheckBoxHttp() {
        mainAction.wrapTextCheckBoxHttpAction();
    }

    @FXML
    public void clearCache() {
        MavenDependencyUtil.clearCache();
    }

    @FXML
    public void showFeedback() {
        mainAction.showFeedbackAction();
    }

    @FXML
    public void showAbout() {
        mainAction.showAboutAction();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mainAction = new MainAction(this);
        mainAction.initializeAction();
    }

    @FXML
    public void formatOrConvert() {
        mainAction.formatOrConvertAction();
    }

    @FXML
    public void sourceToTarget() {
        mainAction.sourceToTargetAction();
    }

    @FXML
    public void targetToSource() {
        mainAction.targetToSourceAction();
    }

    @FXML
    public void operSelectChange() {
        mainAction.operSelectChangeAction();
    }

    @FXML
    public void historyFilterKeyUp() {
        mainAction.historyFilterKeyUpAction();
    }

    @FXML
    public void sendAction() {
        mainAction.sendActionAction();
    }

    @FXML
    public void historyListClick(MouseEvent me) {
        mainAction.historyListClickAction(me);
    }

    @FXML
    public void generateAction() {
        mainAction.generateActionAction();
    }

    @FXML
    public void insertExample() {
        mainAction.insertExampleAction();
    }

    @FXML
    public void redisConnect() {
        mainAction.redisConnectAction();
    }

    @FXML
    public void pageSizeBoxChange() {
        mainAction.pageSizeBoxChangeAction();
    }

    @FXML
    public void pageSizeBoxChangeForValue() {
        mainAction.pageSizeBoxChangeForValueAction();
    }

    @FXML
    public void redisRefreshInfo() {
        mainAction.redisRefreshInfoAction();
    }

    @FXML
    public void searchKeyFromRedis() {//redisSearchKey cssID
        mainAction.searchKeyFromRedisAction();
    }

    @FXML
    public void deleteRedisKey() {
        mainAction.deleteRedisKeyAction();
    }

    @FXML
    public void addRedisKey() {
        mainAction.addRedisKeyAction();
    }

    @FXML
    public void redisRefreshChar() {
        mainAction.redisRefreshCharAction();
    }

    @FXML
    public void changeRefresh() {
        mainAction.changeRefreshAction();
    }

    @FXML
    public void modifyRedisKey() {
        mainAction.modifyRedisKeyAction();
    }

    @FXML
    public TabPane tabPane;
    @FXML
    public AnchorPane pane;
    @FXML
    public TextField zkClientAddress;
    @FXML
    public TextField servicePom;
    @FXML
    public Label listViewStatusLabel;
    @FXML
    public WebView adWeb;
    @FXML
    public TextField serviceFilter;
    @FXML
    public ComboBox providerIP;
    @FXML
    public ComboBox dubboVersionBox;
    @FXML
    public ComboBox fileType;
    @FXML
    public ListView serviceList;
    @FXML
    public ListView serviceMethods;
    /**
     * dubbo 请求参数
     */
    @FXML
    public TextArea requestArea;
    /**
     * dubbo 返回值
     */
    @FXML
    public TextArea responseArea;
    @FXML
    public CheckBox wrapText;
    @FXML
    public CheckBox noImplBox;
    @FXML
    public ComboBox filterType;
    @FXML
    public TextField timeout;
    //tab2
    @FXML
    public CheckBox wrapTextHttp;
    @FXML
    public TextField requestUrl;
    @FXML
    public TextField alias;
    @FXML
    public TextField historyFilter;
    @FXML
    public ComboBox requestMethod;
    @FXML
    public ListView httpHistory;
    @FXML
    public ComboBox contentType;
    @FXML
    public ComboBox useragent;
    @FXML
    public ComboBox httpTimeout;
    @FXML
    public ComboBox charset;
    @FXML
    public TextArea header;
    @FXML
    public TextArea requestBody;
    @FXML
    public CheckBox replaceVar;
    @FXML
    public TextArea responseBody;
    @FXML
    public TextArea requestHeader;
    @FXML
    public TextArea prettyBody;
    @FXML
    public TextArea showDoc;
    @FXML
    public WebView previewBody;
    @FXML
    public TextArea responseHeader;
    //tab3
    @FXML
    public VBox operBox;
    @FXML
    public ComboBox operSelect;
    @FXML
    public TextArea formatSource;
    @FXML
    public TextArea formatTarget;
    @FXML
    public SplitPane formatSplitPane;
    @FXML
    public CheckBox wrapTextFormat;
    //tab4
    @FXML
    public ComboBox generateType;
    @FXML
    public CheckBox jsonField;
    @FXML
    public CheckBox wrapTextToJava;
    @FXML
    public CheckBox xmlField;
    @FXML
    public TextField eumPrefix;
    @FXML
    public TextField fieldSplit;
    @FXML
    public TextArea inputText;
    @FXML
    public TextArea outputJava;
    //tab5
    @FXML
    public PasswordField redisPwd;
    @FXML
    public TextField redisAddr;
    @FXML
    public TextField redisPort;
    @FXML
    public TitledPane redisDatabase;
    @FXML
    public TabPane redisTabPane;
    @FXML
    public Pagination pagination;
    @FXML
    public Pagination paginationForValue;
    @FXML
    public TableView redisInfoTable;
    @FXML
    public TableView redisValueTable;
    @FXML
    public CheckBox redisAutoRefreshBox;
    @FXML
    public TextField redisSearchKey;
    @FXML
    public ComboBox pageSizeBox;
    @FXML
    public ComboBox pageSizeBoxForValue;
    @FXML
    public ListView redisDatabaseList;
    @FXML
    public Label memeryLabel;
    @FXML
    public Label totalKeyLabel;
    @FXML
    public Label clientLabel;
    @FXML
    public Label commandLabel;
    @FXML
    public Label cpuLabel;
    @FXML
    public LineChart memeryChart;
    @FXML
    public LineChart memeryChart1;
    @FXML
    public LineChart cpuChart;
    @FXML
    public LineChart cpuChart1;
    @FXML
    public LineChart qpsChart;
    @FXML
    public LineChart qpsChart1;
    @FXML
    public LineChart networkChart;
    @FXML
    public LineChart networkChart1;
    private MainAction mainAction;
    //tab6
    @FXML
    public TextArea logArea;

}
