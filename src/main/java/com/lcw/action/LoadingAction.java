/*
 * Copyright 2017 lancw.
 *  个人博客 https://www.vbox.top/
 */
package com.lcw.action;

import com.lcw.loading.LoadingController;
import javafx.scene.image.Image;

/**
 *
 * @author lancw
 * @since 2017-8-15 22:12:18
 */
public class LoadingAction {

    private final LoadingController lc;

    public LoadingAction(LoadingController lc) {
        this.lc = lc;
    }

    public void initialize() {
        lc.imageView.setImage(new Image("/resources/siteLogo.png"));
    }

}
