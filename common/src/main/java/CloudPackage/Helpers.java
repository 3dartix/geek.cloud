package CloudPackage;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Helpers {
    private String mainCatalog;

    public Helpers(String mainCatalog){
        this.mainCatalog = mainCatalog;
    }

    public String getMainCatalog() {
        return mainCatalog;
    }

    // мотоды в который отдаются байты, парсятся и сохранаются в каталог
    public void Write(String clientName, ByteBuf in) throws IOException {
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        long fileSize = in.readLong();
        CreateFolder(clientName);

        Path path = Paths.get(mainCatalog + "/" + clientName + "/" + fileName);

        try (OutputStream outputStreamToFile = new BufferedOutputStream(new FileOutputStream(path.toString()))) {
            for (long i = 0; i < fileSize; i++) {
                outputStreamToFile.write(in.readByte());
            }
        }
        System.out.println(String.format("Файл %s получен и размещен: %s", fileName, path.toString()));
    }
    public void Write(ByteBuf in) throws IOException {
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        long fileSize = in.readLong();

        Path path = Paths.get(mainCatalog + "/" + fileName);

        try (OutputStream outputStreamToFile = new BufferedOutputStream(new FileOutputStream(path.toString()))) {
            for (long i = 0; i < fileSize; i++) {
                outputStreamToFile.write(in.readByte());
            }
        }
        System.out.println(String.format("Файл %s получен и размещен: %s", fileName, path.toString()));
    }
    public void Write(DataInputStream in) throws IOException {
        System.out.print("Получаем файлы и записываем их на диск");
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.read(fileNameBytes);
        String fileName = new String(fileNameBytes);
        long fileSize = in.readLong();

        Path path = Paths.get(mainCatalog + "/" + fileName);

        try (OutputStream outputStreamToFile = new BufferedOutputStream(new FileOutputStream(path.toString()))) {
            for (long i = 0; i < fileSize; i++) {
                outputStreamToFile.write(in.readByte());
            }
        }
        System.out.println(String.format("Файл %s получен и размещен: %s", fileName, path.toString()));
    }

    //создать папку если она не существует
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
    //сформировать пачку байтов для передачи фала и отправить на другую сторону
    public void ReadAndSendFile (DataOutputStream out, String filename) {
        try {
            System.out.print("Начинаем передачу файлов\n");
            out.write(0);
            int fileNameLength = filename.length();
            out.writeInt(fileNameLength);
            out.write(filename.getBytes());
            //out.writeLong(new File(mainCatalog + filename).length());
            out.writeLong(new File(mainCatalog+"/" + filename).length());
            byte[] buf = new byte[8192];
            try (InputStream in = new FileInputStream(mainCatalog + "/" + filename)) {
                int n;
                while ((n = in.read(buf)) != -1){
                    out.write(buf, 0, n);
                }
            }
            System.out.print("Закончили передачу файлов\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //cформировать запрос для получения файла от сервера
    public void SendRequest (DataOutputStream out, String filename){
        try {
            System.out.print("\nНачинаем собирать байты");
            out.write(1);
            int fileNameLength = filename.length();
            out.writeInt(fileNameLength);
            out.write(filename.getBytes());
            System.out.print("\nБайты отправлены");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //распарсить запрос и получить из него имя файла
    public String ParseRequest (ByteBuf in){
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readBytes(fileNameBytes);
        System.out.print("Получили имя файла: " + new String(fileNameBytes));
        return new String(fileNameBytes);
    }
}
