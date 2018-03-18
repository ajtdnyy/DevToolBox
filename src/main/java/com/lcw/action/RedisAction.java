/*
 * Copyright 2017 lancw.
 *  个人博客 http://www.vbox.top/
 */
package com.lcw.action;

import com.lcw.controller.*;
import com.lcw.eum.RedisDataType;
import com.lcw.model.NotSqlEntity;
import com.lcw.util.ApplicationStageManager;
import com.lcw.util.RedisUtil;
import com.lcw.util.Toast;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author lancw
 * @since 2017-7-29 17:16:45
 */
public class RedisAction {

    public void submitRedisAction() {
        String key = rc.redisKey.getText();
        if (StringUtils.isBlank(key)) {
            Toast.makeText("请输入key").show(ApplicationStageManager.getStage(ApplicationStageManager.StateAndController.MAIN));
            return;
        }
        String val = rc.redisValue.getText();
        if (StringUtils.isBlank(val)) {
            Toast.makeText("请输入value").show(ApplicationStageManager.getStage(ApplicationStageManager.StateAndController.MAIN));
            return;
        }
        val = val.replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
        String time = rc.expireTime.getText();
        time = StringUtils.isBlank(time) ? "-1" : time;
        RedisDataType type = (RedisDataType) rc.typeBox.getSelectionModel().getSelectedItem();
        NotSqlEntity nse = new NotSqlEntity();
        nse.setKey(key);
        nse.setValue(val);
        nse.setExTime(time);
        nse.setType(type.toString());
        RedisUtil.set(nse);
        Toast.makeText("处理成功").show(ApplicationStageManager.getStage(ApplicationStageManager.StateAndController.MAIN));
    }

    public void initializeAction() {
        rc.typeBox.getItems().addAll(RedisDataType.string);
        rc.typeBox.getSelectionModel().select(0);
    }

    public RedisAction(RedisController rc) {
        this.rc = rc;
    }

    private final RedisController rc;

}
