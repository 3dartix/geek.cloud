package com.geekbrains.geek.cloud.server;

import jdk.nashorn.internal.ir.ContinueNode;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Connection {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nameUser;
    private String mainDirectory = "server_repository/";


    //private ByteArrayOutputStream baos;

    public Connection(Socket socket, String nameUser) {
        this.socket = socket;
        this.nameUser = nameUser + "/";
        try {
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        StartThreadIn();
    }

    public void StartThreadIn(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("поток запущен");
                    while (true){
                        int x = in.read();
                        if(x == 15) {
                            System.out.println("получен 15 бит");
                            ReadFile();
                        } else if (x == 16) {
                            System.out.println("получен 16 бит");
                            SendListFilesNamesToClient();
                        }
                        if(x == -1) {
                            break;
                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
                System.out.println("Клиент отвалился...");
            }
        }).start();
    }


    private void CreateFolder(String nameUserFolder){
        //проверяем существует ли каталог
        Path path = Paths.get("server_repository/" + nameUserFolder);
        try {
            if (!Files.exists(path)) {
                //создаем каталог
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void ReadFile() throws IOException{
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.read(fileNameBytes);
        String fileName = new String(fileNameBytes);
        long fileSize = in.readLong();
        CreateFolder(nameUser);
        try (OutputStream outputStreamToFile = new BufferedOutputStream(new FileOutputStream(mainDirectory + nameUser + fileName))) {
            for (long i = 0; i < fileSize; i++) {
                outputStreamToFile.write(in.read());
            }
        }
        System.out.println(String.format("\n%s файл получен и сохранен", fileName));
    }

    private void SendListFilesNamesToClient() throws IOException {
        out.write(16);
        String list = GetListFilesNames();
        int fileNameLength = list.length();
        out.writeInt(fileNameLength);
        out.write(list.getBytes());
    }

    public String GetListFilesNames () throws IOException {
        StringBuilder list = new StringBuilder();
        //System.out.println(Paths.get("server_repository"));
        Files.walkFileTree(Paths.get("server_repository/User01"), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                //System.out.println("обход файлов" + dir.getFileName().toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                //System.out.println("обход файлов" + file.getFileName().toString());
                list.append(file.getFileName().toString() + ",");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return list.toString();
    }

/*
    public void start (){
        try {
            while (true) {
                //читаем все что приходит от клиента
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                baos = new ByteArrayOutputStream();

                //получаем байты
                int n;
                while ((n = in.read()) != -1) {
                    if(n == 15) {
                        baos.write(n);
                        while ((n = in.read()) != -1) {
                            if(n == 0x00) {
                                CreateFile file = new CreateFile(
                                        "User1",
                                        GetFilenameFromByte(),
                                        Arrays.copyOfRange(baos.toByteArray(), 2 + baos.toByteArray()[1], baos.toByteArray().length)
                                );
                                System.out.println("Закончили");
                                break;
                            } else {
                                baos.write(n);
                            }
                        }
                    }
                    System.out.println("Ждем следующую команду...");

                    if (n == 16) {
                        baos.reset();
                        File[] files = new File("server_repository/User1").listFiles();
                        for (File f : files){
                            baos.write(f.getName().getBytes());
                        }
                        socket.getOutputStream().write(baos.toByteArray());
                        System.out.println("запрос списка файлов");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String GetFilenameFromByte (){
        // создаем байтовый массив для получения имени файла
        byte[] datafileName = new byte[baos.toByteArray()[1]];
        for (int i = 0; i < baos.toByteArray()[1]; i++) {
            datafileName[i] = baos.toByteArray()[i + 2];
        }
        return new String(datafileName);
    }

 */
}
