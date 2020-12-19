package ru.geekbrains.server;

public class MainServer {

    public static void main(String[] args) {
        new Server();
    }

    //     1. Разобраться с кодом
//     2. Смена ника в БД по запросу /changenick newNick
//
//    Запрос для проверки ника:
//    ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE nickname = '" + nickname + "'");
//    if (rs.next()) {
//        ЕСТЬ!!!
//    }
//
//    Запрос для обновления:
//    statement.executeUpdate("UPDATE users SET nickname = '"+newNick+"' WHERE nickname = '"+oldNick+"'");

}
