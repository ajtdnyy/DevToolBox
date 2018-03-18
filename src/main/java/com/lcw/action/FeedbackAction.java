/*
 * Copyright 2017 lancw.
 *  个人博客 http://www.vbox.top/
 */
package com.lcw.action;

import com.lcw.controller.*;
import com.lcw.eum.Charset;
import com.lcw.eum.ContentType;
import com.lcw.eum.RequestMethod;
import com.lcw.model.HttpRequestData;
import com.lcw.util.ApplicationStageManager;
import com.lcw.util.DialogUtil;
import com.lcw.util.HttpClientUtil;
import com.lcw.util.Toast;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 * @since 2017-7-29 20:47:12
 */
public class FeedbackAction {

    public void submitFeedbackAction() {
        try {
            String title = fc.feedbackTitle.getText();
            String content = fc.feedbackContent.getText();
            String email = fc.mail.getText();
            if (title == null || title.trim().isEmpty()) {
                Toast.makeText("请填写标题").show(fc.stage);
                return;
            }
            if (!StringUtils.isBlank(email) && !Pattern.matches("^[A-Za-zd]+([-_.][A-Za-zd]+)*@([A-Za-zd]+[-.])+[A-Za-zd]{2,5}$", email)) {
                Toast.makeText("请填写正确的邮箱地址").show(fc.stage);
                return;
            } else {
                email = "bug@vbox.top";
            }
            String em = email;
            if (content == null || content.trim().isEmpty()) {
                Toast.makeText("请填写内容").show(fc.stage);
                return;
            }
            ApplicationStageManager.getStage(ApplicationStageManager.StateAndController.FEEDBACK).close();
            DialogUtil.showDialog(fc.stage);
            Task task = new Task() {
                @Override
                protected Object call() {
                    try {
                        HttpRequestData.Request req = new HttpRequestData.Request();
                        req.setUrl(FEEDBACK_URL);
                        req.setContentType(ContentType.CONTENT_TYPE_TEXT_PLAIN.getCode());
                        req.setCharset(Charset.UTF8.getCode());
                        req.setMethod(RequestMethod.GET.name());

                        req.setBody("name=" + title + "&content=" + content + "&post_id=91&email=" + em);
                        req.setTimeout("5000");
                        HttpResponse resp = HttpClientUtil.sendRequest(req);
                        if (resp != null) {
                            String json = EntityUtils.toString(resp.getEntity());
                            JSONObject jo = JSONObject.fromObject(json);
                            DialogUtil.close();
                            if (!"error".equals(jo.getString("status"))) {
                                Toast.makeText("提交成功，感谢反馈").show(fc.stage);
                            } else {
                                if (jo.containsKey("error")) {
                                    LOGGER.error(jo.getString("error"));
                                }
                                Toast.makeText("提交失败，请采用其他方式反馈，谢谢。").show(fc.stage);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("提交反馈异常", e);
                        DialogUtil.close();
                        Toast.makeText("提交反馈异常，请查看dev_tool_box.log日志。").show(fc.stage);
                    }
                    return null;
                }

                @Override
                protected void setException(Throwable t) {
                    super.setException(t);
                    LOGGER.error("提交反馈异常", t);
                    Toast.makeText("提交反馈异常，请查看dev_tool_box.log日志。").show(fc.stage);
                    DialogUtil.close();
                }
            };
            new Thread(task).start();
        } catch (Exception ex) {
            LOGGER.error("提交反馈异常", ex);
        }
    }

    public void initializeAction() {
        fc.stage = ApplicationStageManager.getStage(ApplicationStageManager.StateAndController.FEEDBACK);
    }

    public FeedbackAction(FeedbackController fc) {
        this.fc = fc;
    }
    private final FeedbackController fc;
    private static final String FEEDBACK_URL = "http://www.vbox.top/api/respond/submit_comment/";
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackAction.class);
}
