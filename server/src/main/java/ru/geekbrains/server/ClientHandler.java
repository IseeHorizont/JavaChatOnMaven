package ru.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getNickname() {
        return nickname;
    }

    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        // /auth login1 password1
                        String[] subStrings = str.split(" ");
                        if (subStrings.length >= 3 && subStrings[0].equals("/auth")) {
                            String nickFromDB = SQLHandler.getNickByLoginAndPassword(subStrings[1], subStrings[2]);
                            if (nickFromDB != null) {
                                sendMsg("/authok");
                                server.subscribe(this);
                                nickname = nickFromDB;
                                break;
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        System.out.println("Сообщение от клиента: " + str);
                        if (str.equals("/end")) {
                            break;
                        }
                        if (str.startsWith("/w")) {
                            String[] forPrivateMsg = str.split("\\s", 3);
                            server.sendPrivateMsg(this, forPrivateMsg[1], forPrivateMsg[2]);
                        }else {
                            server.broadcastMsg(nickname + ": " + str);
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
                    server.unsubscribe(this);
                }
            }).start();
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
}
