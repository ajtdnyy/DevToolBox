/*
 * Copyright 2017 lancw.
 *  个人博客 https://www.vbox.top/
 */
package com.lcw.controller;

import com.lcw.action.RedisAction;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author lancw
 * @since 2017-7-29 17:16:45
 */
public class RedisController implements Initializable {

    @FXML
    public void submitRedis() {
        redisAction.submitRedisAction();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        redisAction = new RedisAction(this);
        redisAction.initializeAction();
    }

    public RedisAction redisAction;
    @FXML
    public TextField redisKey;
    @FXML
    public TextField expireTime;
    @FXML
    public TextArea redisValue;
    @FXML
    public ComboBox typeBox;
}
