package com.geekbrains.geek.cloud.client;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final byte SC_SEND_FILE = 0;
    private final byte SC_GET_FILENAME_LIST_FROM_SERVER= 16;

    private String host;
    private int port;
    private String mainCatalog = "client_repository/";

    private DataOutputStream out;
    private DataInputStream in;

    Scanner scanner = new Scanner(System.in);

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            Connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Connect() throws IOException {
        try (Socket socket = new Socket(host, port)) {
            out = new DataOutputStream(socket.getOutputStream());
            SendFile("1.txt");
            //SendCommad("", 1);


            //нерабочий код

            //in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            //GetFilesNamesFromServer();



//            while (true) {
//                int x = in.read();
//                System.out.println(x);
//                if(x == 16) {
//                    readFilesNamesList();
//                }
//                if(x == -1) {
//                    break;
//                }
//            }

        }
    }

    public void SendFile (String filename) throws IOException {
        System.out.println("Начинаем передачу файлов");
        out.write(SC_SEND_FILE);
        int fileNameLength = filename.length();
        out.writeInt(fileNameLength);
        out.write(filename.getBytes());
        out.writeLong(new File(mainCatalog + filename).length());
        byte[] buf = new byte[8192];
        try (InputStream in = new FileInputStream(mainCatalog + filename)) {
            int n;
            while ((n = in.read(buf)) != -1){
                out.write(buf, 0, n);
            }
        }
        System.out.println("Закончили передачу файлов");
    }
    public void SendCommad(String name, int b) throws IOException {
        out.write(b);
    }

    private void readFilesNamesList() throws IOException {
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.read(fileNameBytes);
        String fileName = new String(fileNameBytes);
        System.out.println(fileName);
    }


    public void GetFilesNamesFromServer() throws IOException {
        out.write(SC_GET_FILENAME_LIST_FROM_SERVER);
    }

//    public static void main(String[] args) {
//        String fileName = "1.txt";
//
//        try (Socket socket = new Socket("localhost", 8189)) {
//            int fileNameLength = fileName.length();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] dataPackage = Files.readAllBytes(Paths.get("client_repository/" + fileName));
//            baos.write(SC_SEND_FILE);
//            baos.write(fileNameLength);
//            baos.write(fileName.getBytes());
//            baos.write(dataPackage);
//            baos.write(0x00);
//
//            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
//            socket.getOutputStream().write(baos.toByteArray());
//
//            //запрос на получение списка файлов на сервере.
//            baos.reset();
//            baos.write(SC_GET_FILENAME_LIST_FROM_SERVER);
//            socket.getOutputStream().write(baos.toByteArray());
//            //читаем все что приходит от клиента
//            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
//            int n;
//            while ((n = in.read()) != -1) {
//                System.out.print((char)n);
//            }
//
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }
}
