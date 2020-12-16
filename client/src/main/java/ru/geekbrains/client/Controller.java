package ru.geekbrains.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authorized;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authorized = false;
        Platform.runLater(() -> ((Stage) mainBox.getScene().getWindow()).setOnCloseRequest(t -> {
            sendMsg("/end");
            Platform.exit();
        }));
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

    public void sendMsg() {
        try {
            String str = textField.getText();
            out.writeUTF(str);
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
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
                            if (str.equals("/authok")) {
                                setAuthorized(true);
                                break;
                            }
                        }
                        while (true) {
                            try {
                                String str = in.readUTF();
                                textArea.appendText(str + System.lineSeparator());
                            } catch (IOException e) {
                                e.printStackTrace();
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
}
