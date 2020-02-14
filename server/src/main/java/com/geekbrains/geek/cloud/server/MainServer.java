package com.geekbrains.geek.cloud.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MainServer {
    static private Vector<Connection> clients = new Vector<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)){
            System.out.println("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.printf("Клиент подключился \n");
                Connection client = new Connection(socket, "User01");
                clients.add(client);
            }
/*
            //читаем все что приходит от клиента

            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //получаем байты
            int n;
            while ((n = in.read()) != -1){
                baos.write(n);
            }

            if(baos.toByteArray()[0] == 15) {
                CreateFile file = new CreateFile();
                // создаем байтовый массив для получения имени файла
                byte[] datafileName = new byte[baos.toByteArray()[1]];
                for (int i = 0; i < baos.toByteArray()[1]; i++) {
                    datafileName[i] = baos.toByteArray()[i+2];
                }

                file.CloudPackage.WriteFile("User1",
                        new String(datafileName),
                        Arrays.copyOfRange(baos.toByteArray(), 2 + baos.toByteArray()[1], baos.toByteArray().length));
            }
*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
