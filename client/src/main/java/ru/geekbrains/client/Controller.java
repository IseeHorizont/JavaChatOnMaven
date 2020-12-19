package ru.geekbrains.client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField, loginField;

    @FXML
    VBox mainBox;

    @FXML
    HBox authPanel, msgPanel;

    @FXML
    PasswordField passField;

    @FXML
    Button sendMsgBtn;

    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private ObservableList<String> clients;
    private boolean authorized;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authorized = false;
        Platform.runLater(() -> ((Stage) mainBox.getScene().getWindow()).setOnCloseRequest(t -> {
            sendMsg("/end");
            Platform.exit();
        }));

        textField.textProperty().addListener((observableValue, s, t1) -> sendMsgBtn.setDisable(t1.isEmpty()));

        clients = FXCollections.observableArrayList();
        clientsList.setItems(clients);
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
            nickname = "";
        }

        Platform.runLater(() -> {
            if (nickname.isEmpty()) {
                ((Stage) mainBox.getScene().getWindow()).setTitle("Java Chat Client");
            } else {
                ((Stage) mainBox.getScene().getWindow()).setTitle("Java Chat Client: " + nickname);
            }
        });

    }

    public void sendMsg() {
        try {
            if (socket != null && !socket.isClosed()) {
                String str = textField.getText();
                out.writeUTF(str);
                textField.clear();
                textField.requestFocus();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            if (socket != null && !socket.isClosed()) {
                if (!msg.isEmpty()) {
                    out.writeUTF(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAuth() {
        connect();
        // /auth login1 password1
        sendMsg("/auth " + loginField.getText() + " " + passField.getText());
        loginField.clear();
        passField.clear();
    }

    private void connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("localhost", 8189);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                new Thread(() -> {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            // /authok nick
                            if (str.startsWith("/authok")) {
                                nickname = str.split(" ")[1];
                                setAuthorized(true);
                                break;
                            }
                        }
                        while (true) {
                            String str = in.readUTF();
                            if (!str.startsWith("/")) {
                                textArea.appendText(str + System.lineSeparator());
                            } else if (str.startsWith("/clientslist")) {
                                // /clientslist nick1 nick2 nick3
                                String[] subStr = str.split(" ");
                                clients.clear();
                                for (int i = 1; i < subStr.length; i++) {
                                    clients.add(subStr[i]);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void registerBtn() {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            Parent root = fxmlLoader.load();
            stage.setTitle("Registration");
            stage.setScene(new Scene(root, 400, 240));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}