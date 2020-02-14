package com.geekbrains.geek.cloud.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateFile {
    private String userName;
    private String nameFile;
    private byte[] bytes;


    public CreateFile(String userName, String nameFile, byte[] bytes) {
        this.userName = userName;
        this.nameFile = nameFile;
        this.bytes = bytes;
        WriteFile();
    }

    private void WriteFile() {
        Path path;
        //проверяем существует ли каталог
        path = Paths.get("server_repository/" + userName);

        try {
            if (!Files.exists(path)) {
                //создаем каталог
                Files.createDirectories(path);
            }
            //записываем в файлы
            path = Paths.get("server_repository/" + userName + "/" + nameFile);
            Files.write(path, bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
