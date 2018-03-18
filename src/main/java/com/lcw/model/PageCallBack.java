package com.lcw.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 *
 * @author lancw
 */
public class PageCallBack implements Callback {

    private ObservableList allData = FXCollections.observableArrayList();
    private final ObservableList currentData = FXCollections.observableArrayList();
    private Integer pageSize = 0;
    private Integer currentPage = 0;
    private Node node;

    public void setNode(Node node, Integer pageSize) {
        this.node = node;
        this.pageSize = pageSize;
        allData.clear();
        initPageData();
    }

    public void setAllData(ObservableList allData) {
        this.allData = allData;
        initPageData();
    }

    public int setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        initPageData();
        return allData.size() / pageSize + (allData.size() % pageSize == 0 ? 0 : 1);
    }

    @Override
    public Object call(Object param) {
        currentPage = (Integer) param;
        int start = initPageData();
        if (allData.isEmpty()) {
            return null;
        }
        return new Label("显第" + (start + 1) + "到" + (start + currentData.size()) + "条 共" + allData.size() + "条");
    }

    private int initPageData() {
        if (allData.isEmpty()) {
            if (node instanceof TableView) {
                allData.addAll(((TableView) node).getItems());
            } else if (node instanceof ListView) {
                allData.addAll(((ListView) node).getItems());
            }
        }
        int start = pageSize * currentPage;
        int end = start + pageSize;
        end = end >= allData.size() ? allData.size() : end;
        currentData.clear();
        if (start < end) {
            currentData.addAll(allData.subList(start, end));
        }
        if (node instanceof TableView) {
            ((TableView) node).setItems(currentData);
        } else if (node instanceof ListView) {
            ((ListView) node).setItems(currentData);
        }
        return start;
    }

}
