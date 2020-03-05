/*
 * Copyright 2017 lancw.
 *  个人博客 https://www.vbox.top/
 */
package com.lcw.action;

import com.lcw.controller.ThankUserController;

/**
 *
 * @author lancw
 * @since 2017-8-26 10:18:42
 */
public class ThankUserAction {

    public void initializeAction() {
        tuc.thanks.getEngine().load("https://www.vbox.top/app/thanks.html");
    }

    public ThankUserAction(ThankUserController rc) {
        this.tuc = rc;
    }

    private final ThankUserController tuc;
}
