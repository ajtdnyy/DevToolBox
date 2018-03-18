/*
 * Copyright 2017 lancw.
 *  个人博客 http://www.vbox.top/
 */
package com.lcw.util;

import com.sun.javafx.charts.Legend;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 *
 * @author lancw
 * @since 2017-7-30 21:14:35
 */
public class ChartUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Map<String, Boolean> CACHE = new HashMap<>();

    public static void changeChart(LineChart chart, Date now, Double dt, String name) {
        ObservableList<XYChart.Series> data = chart.getData();
        XYChart.Series xys = null;
        if (data.isEmpty()) {
            xys = getSeries(chart, dt, name);
            data.add(xys);
            addClickListener(chart);
        } else {
            boolean exits = false;
            for (XYChart.Series series : data) {
                if (name.equals(series.getName())) {
                    exits = true;
                    xys = series;
                    break;
                }
            }
            if (!exits) {
                xys = getSeries(chart, dt, name);
                data.add(xys);
                addClickListener(chart);
            }
            NumberAxis na = (NumberAxis) chart.getYAxis();
            na.setUpperBound(Math.max(na.getUpperBound(), dt));
        }
        XYChart.Data xdata = new XYChart.Data<>(DATE_FORMAT.format(now), dt);
        xdata.setNode(new HoveredThresholdNode(dt, dt));
        xdata.getNode().setVisible(CACHE.get(name) == null ? true : CACHE.get(name));
        xys.getData().add(xdata);
        if (xys.getData().size() > 9) {
            xys.getData().remove(0);
        }
    }

    private static XYChart.Series getSeries(LineChart chart, Double dt, String name) {
        NumberAxis na = (NumberAxis) chart.getYAxis();
        na.setAutoRanging(true);
        na.setForceZeroInRange(false);
        XYChart.Series s = new XYChart.Series();
        s.setName(name);
        return s;
    }

    private static void addClickListener(LineChart chart) {
        for (Node n : chart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (Legend.LegendItem li : l.getItems()) {
                    for (Iterator it = chart.getData().iterator(); it.hasNext();) {
                        XYChart.Series<Number, Number> s = (XYChart.Series<Number, Number>) it.next();
                        if (s.getName().equals(li.getText())) {
                            li.getSymbol().setCursor(Cursor.HAND); // Hint user that legend symbol is clickable
                            if (li.getSymbol().getOnMouseClicked() == null) {
                                li.getSymbol().setOnMouseClicked(me -> {
                                    if (me.getButton() == MouseButton.PRIMARY) {
                                        s.getNode().setVisible(!s.getNode().isVisible()); // Toggle visibility of line
                                        CACHE.put(s.getName(), s.getNode().isVisible());
                                        for (XYChart.Data<Number, Number> d : s.getData()) {
                                            if (d.getNode() != null) {
                                                d.getNode().setVisible(s.getNode().isVisible()); // Toggle visibility of every node in the series
                                            }
                                        }
                                        for (Iterator it2 = chart.getData().iterator(); it2.hasNext();) {
                                            XYChart.Series<Number, Number> s2 = (XYChart.Series<Number, Number>) it2.next();
                                            if (!s.equals(s2)) {
                                                for (XYChart.Data<Number, Number> d2 : s2.getData()) {
                                                    if (!d2.getNode().isVisible()) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    static class HoveredThresholdNode extends StackPane {

        HoveredThresholdNode(Double priorValue, Double value) {
            setPrefSize(15, 15);
            final Label label = createDataThresholdLabel(priorValue, value);
            setOnMouseEntered((MouseEvent mouseEvent) -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited((MouseEvent mouseEvent) -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        private Label createDataThresholdLabel(Double priorValue, Double value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }
            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
}
