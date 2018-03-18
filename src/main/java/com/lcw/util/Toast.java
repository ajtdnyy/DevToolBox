package com.lcw.util;

import com.sun.javafx.util.Utils;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class Toast extends Popup {

    private static final Logger LOGGER = LoggerFactory.getLogger(Toast.class.getName());
    private static double defaultContentOpacity = 0.9;
    private static Duration defaultFadeInDuration = new Duration(600);
    private static Duration defaultFadeOutDuration = new Duration(300);

    public static double getDefaultContentOpacity() {
        return defaultContentOpacity;
    }

    public static void setDefaultContentOpacity(double opacity) {
        Toast.defaultContentOpacity = opacity;
    }

    public static Duration getDefaultFadeInDuration() {
        return defaultFadeInDuration;
    }

    public static void setDefaultFadeInDuration(Duration duration) {
        if (duration == null) {
            throw new NullPointerException();
        }
        Toast.defaultFadeInDuration = duration;
    }

    public static Duration getDefaultFadeOutDuration() {
        return defaultFadeOutDuration;
    }

    public static void setDefaultFadeOutDuration(Duration duration) {
        if (duration == null) {
            throw new NullPointerException();
        }
        Toast.defaultFadeOutDuration = duration;
    }

    private static volatile boolean alreadyShowing = false;
    private static final Queue<Toast> toastQueue = new LinkedBlockingQueue<>(50);

    public static int clearWaitingToasts() {
        int count = toastQueue.size();
        toastQueue.clear();
        return count;
    }

    private Node content;
    private Duration duration;
    private boolean autoCenter = true;

    private Window window;
    private Node anchor;
    private double screenX;
    private double screenY;

    private double contentOpacity = defaultContentOpacity;
    private Duration fadeInDuration = defaultFadeInDuration;
    private Duration fadeOutDuration = defaultFadeOutDuration;

    private Timeline hideTimer;

    public Toast() {
        setAutoHide(true);
    }

    public void setContent(Node content) {
        if (content == null) {
            throw new NullPointerException();
        }
        this.content = content;
        content.getStyleClass().add("toast");
        super.getContent().setAll(content);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        if (duration == null) {
            throw new NullPointerException();
        }
        this.duration = duration;
    }

    public boolean isAutoCenter() {
        return autoCenter;
    }

    public void setAutoCenter(boolean autoCenter) {
        this.autoCenter = autoCenter;
    }

    public double getContentOpacity() {
        return contentOpacity;
    }

    public void setContentOpacity(double opacity) {
        this.contentOpacity = opacity;
    }

    public Duration getFadeInDuration() {
        return fadeInDuration;
    }

    public void setFadeInDuration(Duration duration) {
        if (duration == null) {
            throw new NullPointerException();
        }
        this.fadeInDuration = duration;
    }

    public Duration getFadeOutDuration() {
        return fadeOutDuration;
    }

    public void setFadeOutDuration(Duration duration) {
        if (duration == null) {
            throw new NullPointerException();
        }
        this.fadeOutDuration = duration;
    }

    @Override
    public void show(Window window) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                show(window, Double.NaN, Double.NaN);
            });
        } else {
            this.show(window, Double.NaN, Double.NaN);
        }
    }

    @Override
    public void show(Window window, double screenX, double screenY) {
        if (window == null) {
            return;
        }
        this.window = window;
        this.anchor = null;
        this.screenX = screenX;
        this.screenY = screenY;
        tryShow();
    }

    public void show(Node anchor) {
        autoCenter = true;
        this.show(anchor, Double.NaN, Double.NaN);
    }

    @Override
    public void show(Node anchor, double screenX, double screenY) {
        if (anchor == null) {
            return;
        }
        this.window = null;
        this.anchor = anchor;
        this.screenX = screenX;
        this.screenY = screenY;
        tryShow();
    }

    public void show(Node anchor, Side side, double offsetX, double offsetY) {
        if (anchor == null) {
            return;
        }
        autoCenter = false;

        HPos hpos = side == Side.LEFT ? HPos.LEFT : side == Side.RIGHT ? HPos.RIGHT : HPos.CENTER;
        VPos vpos = side == Side.TOP ? VPos.TOP : side == Side.BOTTOM ? VPos.BOTTOM : VPos.CENTER;
        // translate from anchor/hpos/vpos/offsetX/offsetY into screenX/screenY
        Point2D point = Utils.pointRelativeTo(anchor, content.prefWidth(-1), content.prefHeight(-1), hpos, vpos, offsetX, offsetY, true);
        this.show(anchor, point.getX(), point.getY());
    }

    public void show(Node anchor, Side side) {
        this.show(anchor, side, 0, 0);
    }

    private void tryShow() {
        if (window == null && anchor == null) {
            throw new IllegalStateException("window and anchor node are both null");
        }
        if (alreadyShowing) {
            // if another toast is already showing add this one to the waiting queue
            if (toastQueue.offer(this)) {
                LOGGER.info("toast enqueued: {}", this);
            } else {
                LOGGER.error("toast queue exceeded it's capacity");
            }
        } else {
            alreadyShowing = true;
            doShow();
        }
    }

    private void doShow() {
        LOGGER.info("show toast: {}", this);
        if (window != null) {
            if (autoCenter) {
                connectAutoCenterHandler();
            }
            if (Double.isNaN(screenX) || Double.isNaN(screenY)) {
                super.show(window);
            } else {
                super.show(window, screenX, screenY);
            }
        } else { // anchor
            if (autoCenter) {
                Scene scene = anchor.getScene();
                if (scene != null) {
                    window = scene.getWindow();
                }
                if (window == null) {
                    throw new IllegalStateException("anchor node is not attached to a window");
                }
                connectAutoCenterHandler();
            }
            super.show(anchor, Double.isNaN(screenX) ? 0.0 : screenX, Double.isNaN(screenY) ? 0.0 : screenY);
        }
        if (isAutoHide() && !duration.isIndefinite()) {
            hideTimer = new Timeline(new KeyFrame(duration));
            hideTimer.setOnFinished((ActionEvent event) -> {
                hideTimer = null;
                Toast.this.hide();
            });
            hideTimer.playFromStart();
        }
        FadeTransition transition = new FadeTransition(fadeInDuration, content);
        transition.setFromValue(0.0);
        transition.setToValue(contentOpacity);
        transition.play();
    }

    private void connectAutoCenterHandler() {
        XListener xListener = new XListener();
        super.widthProperty().addListener(xListener);
        YListener yListener = new YListener();
        super.heightProperty().addListener(yListener);
    }

    @Override
    public void hide() {
        LOGGER.info("hide toast: {}", this);
        if (hideTimer != null) {
            // cancel the timer if the toast is hidden before the timer fires
            hideTimer.stop();
            hideTimer = null;
        }
        if (!isShowing()) {
            return;
        }
        FadeTransition transition = new FadeTransition(fadeOutDuration, content);
        transition.setToValue(0.0);
        transition.setOnFinished((ActionEvent event) -> {
            doHide();
        });
        transition.play();
    }

    private void doHide() {
        super.hide();
        if (toastQueue.isEmpty()) {
            alreadyShowing = false;
        } else {
            // get the next toast and show it (may flicker if not enqueued in the FX thread!?)
            final Toast toast = toastQueue.poll();
            LOGGER.info("toast unqueued: {}", toast);
            Platform.runLater(() -> {
                toast.doShow();
            });
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Toast");
        sb.append("{content=").append(content);
        sb.append('}');
        return sb.toString();
    }

    // helper methods
    public static final Duration DURATION_SHORT = Duration.seconds(2);
    public static final Duration DURATION_LONG = Duration.seconds(4);

    private static final Color DEFAULT_BACKGROUND_COLOR = Color.gray(0.2);
    private static final double DEFAULT_BACKGROUND_ARC = 8.0;
    private static final Insets DEFAULT_CONTENT_MARGIN = new Insets(8, 16, 8, 16);

    public static Toast makeToast(Node content, Duration duration) {
        if (content == null) {
            throw new NullPointerException("content");
        }
        if (duration == null) {
            throw new NullPointerException("duration");
        }

        Rectangle rect = new Rectangle();
        rect.getStyleClass().add("background");
        rect.setFill(DEFAULT_BACKGROUND_COLOR);
        rect.setArcWidth(DEFAULT_BACKGROUND_ARC);
        rect.setArcHeight(DEFAULT_BACKGROUND_ARC);

        StackPane pane = new StackPane();
        rect.widthProperty().bind(pane.widthProperty());
        rect.heightProperty().bind(pane.heightProperty());
        StackPane.setMargin(content, DEFAULT_CONTENT_MARGIN);
        pane.getChildren().addAll(rect, content);

        Toast toast = new Toast();
        toast.setContent(pane);
        toast.setDuration(duration);
        return toast;
    }

    private static final Color DEFAULT_FILL_COLOR = Color.WHITE;

    public static Toast makeText(String text, boolean wrapText, double maxWidth, double maxHeight, Duration duration) {
        Label label = new Label(text);
        label.getStyleClass().add("text");
        label.setWrapText(wrapText);
        label.setMaxWidth(maxWidth);
        label.setMaxHeight(maxHeight);
        label.setTextFill(DEFAULT_FILL_COLOR);
        return makeToast(label, duration);
    }

    public static Toast makeText(String text) {
        return makeText(text, true, 500, 250, Duration.seconds(3));
    }

    public static Toast makeText(String text, Duration duration) {
        return makeText(text, true, 500, 250, duration);
    }

    private class XListener implements ChangeListener<Number> {

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
            double x = window.getX() + window.getWidth() / 2 - Toast.super.getWidth() / 2;
            if (!Double.isNaN(screenX)) {
                x += screenX; // use as offset
            }
            Toast.super.setX(x);
        }
    }

    private class YListener implements ChangeListener<Number> {

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
            double y = window.getY() + window.getHeight() / 2 - Toast.super.getHeight() / 2;
            if (!Double.isNaN(screenY)) {
                y += screenY; // use as offset
            }
            Toast.super.setY(y);
        }
    }
}
