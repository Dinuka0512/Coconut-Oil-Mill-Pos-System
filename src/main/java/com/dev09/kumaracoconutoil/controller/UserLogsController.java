package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserLogBO;
import com.dev09.kumaracoconutoil.dto.UserLogDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class UserLogsController implements Initializable {

    @FXML private TableView<UserLogDto> tblLogs;
    @FXML private TableColumn<UserLogDto, String> colDate;
    @FXML private TableColumn<UserLogDto, String> colTime;
    @FXML private TableColumn<UserLogDto, String> colUserName;
    @FXML private TableColumn<UserLogDto, String> colRole;
    @FXML private TableColumn<UserLogDto, String> colAction;

    private final UserLogBO userLogBO = (UserLogBO) BOFactory.getInstance().getBo(BOFactory.BOType.USER_LOG);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        loadLogs();
    }

    @FXML
    void btnRefreshOnAction(ActionEvent event) {
        loadLogs();
    }

    private void loadLogs() {
        ArrayList<UserLogDto> logs = userLogBO != null ? userLogBO.getAllUserLogs() : new ArrayList<>();
        ObservableList<UserLogDto> items = FXCollections.observableArrayList(logs);
        tblLogs.setItems(items);
    }
}
