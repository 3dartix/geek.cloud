package CloudPackage;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Vector;

public class Helpers {
    public enum State {
        NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private String mainCatalog;
    private OutputStream out;
    private long receivedFileLength;
    private State currentState = State.NAME_LENGTH;
    private int nameLength;
    private long fileLength;
    private int progress; //прогресс передачи в процентах 0 - 100%

    public Helpers (String mainCatalog) {
        this.mainCatalog = mainCatalog;
    }

    public void setMainCatalog(String mainCatalog) {
        this.mainCatalog = mainCatalog;
        CreateFolder();
    }

    public String getMainCatalog() {
        return mainCatalog;
    }

    // методы для прогресса -->
    public int getProgress() {
        return progress;
    }
    public boolean isProgressFinish(){
        if(progress == 100){
            return true;
        } else {
            return false;
        }
    }
    //обнуляем прогресс по завершению
    public void ProgressReset(){
        progress = 0;
    }
    //считаем прогресс, когда получаем файл от сервера
    public void ProgressStatus(long totalSize, long transfered){
        progress = (int) (transfered * 100 / totalSize);
    }
    //считаем прогресс, когда отправляем файл на сервер
    public void ProgressStatus(FileRegion file){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (file.count() != file.transfered()) {
                    progress = (int) (file.transfered() * 100 / file.count());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //System.out.print("\nProgress: " + progress + "%");
                }
                progress = 100;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    // <-- методы для прогресса

    //распарсить поток батов и записать файл на диск в нужный каталог
    public boolean Write (ByteBuf buf) throws IOException {
        boolean complete = false;
        if (currentState == State.NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                System.out.println("\nSTATE: Get filename length");
                nameLength = buf.readInt();
                currentState = State.NAME;
            }
        }

        if (currentState == State.NAME) {
            if (buf.readableBytes() >= nameLength) {
                byte[] fileName = new byte[nameLength];
                buf.readBytes(fileName);
                System.out.println("\nSTATE: Filename received");
                String str = new String(fileName);
                Path path = Paths.get(mainCatalog + "/" + str);
                out = new BufferedOutputStream(new FileOutputStream(path.toString()));
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println("\nSTATE: File length received - " + fileLength);
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                // считаем прогресс
                ProgressStatus(fileLength, receivedFileLength);
                if (fileLength == receivedFileLength) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0;
                    System.out.println("\nFile received");
                    out.close();
                    complete = true;
                    progress = 100;
                }
            }
        }
        return complete;
    }
    //сформировать пачку байтов
    // [служебный файл][длина названия файла][название файла в байтах][размер файла лонг][сам файл]
    public void SendBytesFromFile (ChannelHandlerContext ctx, String filename) {
        try {
            System.out.printf("\nОтправляем файл");
            String path = mainCatalog + "/" + filename;
            //String filename = Paths.get(path).getFileName().toString();
            //готовим регион для оправки файла целиком
            FileRegion region = null;

            region = new DefaultFileRegion(new FileInputStream(Paths.get(path).toFile()).getChannel(), 0, Files.size(Paths.get(path)));

            //формируем служ инофрмацию о фале
            ByteBuf buf = null;
            //Служебны байт 0 - прочитать пачку байтов и сохранить в локальную папку
            buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte(0);
            ctx.writeAndFlush(buf);
            //длина названия файла
            buf = ByteBufAllocator.DEFAULT.directBuffer(3);
            buf.writeInt(filename.getBytes().length);
            ctx.writeAndFlush(buf);
            //название файла в байтах
            buf = ByteBufAllocator.DEFAULT.directBuffer(15);
            buf.writeBytes(filename.getBytes());
            ctx.writeAndFlush(buf);
            //размер файла лонг
            buf = ByteBufAllocator.DEFAULT.directBuffer(50);
            long l = new File(path).length();
            buf.writeLong(l);
            System.out.printf("\nРазмер фала: " + l);
            ctx.writeAndFlush(buf);

            ProgressStatus(region);
            ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //формируем пачку байтов (списко файлов на сервере) и отправляем клиенту
    public void SendListFilesToClient(ChannelHandlerContext ctx) throws IOException {
        System.out.printf("\nОтправляем список файлов клиенту");
        ByteBuf buf = null;

        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte)2);
        ctx.writeAndFlush(buf);

        String filesName = "";
        String[] strings = GetListFilesNames();
        for (int i = 0; i < strings.length; i++) {
            filesName += strings[i] + ";";
        }

        buf = ByteBufAllocator.DEFAULT.directBuffer(20);
        //System.out.printf("Размер отправляемой записи - " + filesName.length());
        buf.writeInt(filesName.getBytes().length);
        ctx.writeAndFlush(buf);

        //System.out.printf("Размер отправляемой записи в байтах - " + filesName.getBytes() + "\n");
        buf = ByteBufAllocator.DEFAULT.directBuffer(20);
        buf.writeBytes(filesName.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(buf);
    }
    //формируем пачку байтов (переименование) и отправляем на сервер
    public void SendBytesForRename (ChannelHandlerContext ctx, String oldName, String newName){
        System.out.printf("\nОтправляем список файлов клиенту");
        ByteBuf buf = null;

        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(3);
        ctx.writeAndFlush(buf);

        String filesNames = oldName + ";" + newName;

        buf = ByteBufAllocator.DEFAULT.directBuffer(20);
        buf.writeInt(filesNames.getBytes().length);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(20);
        buf.writeBytes(filesNames.getBytes());
        ctx.writeAndFlush(buf);
    }

    //распарсить запрос (байты) и получить из него string
    public String GetStringFromBytes(ByteBuf in){
        String str = "";
        if (currentState == State.NAME_LENGTH) {
            if (in.readableBytes() >= 4) {
                System.out.println("\nSTATE: State.NAME_LENGTH");
                nameLength = in.readInt();
                currentState = State.NAME;
            }
        }
        if (currentState == State.NAME) {
            if (in.readableBytes() >= nameLength) {
                byte[] fileName = new byte[nameLength];
                in.readBytes(fileName);
                System.out.println("\nSTATE: State.NAME");

                    str = new String(fileName);

                currentState = State.NAME_LENGTH;
            }
        }
        return str;
    }

    //сформирвать запрос для получения файла от сервера Netty
    public void GetFileFromServerRequest(ChannelHandlerContext ctx, String path){
        System.out.printf("\nГотовим запрос на копирование файла: " + path);
        ByteBuf buf = null;
        String filename = Paths.get(path).getFileName().toString();
        //служеный файл
        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeByte((byte)1);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeInt(filename.getBytes().length);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(15);
        buf.writeBytes(filename.getBytes());
        ctx.writeAndFlush(buf);

        System.out.printf("\nЗапрос отправили!");
    }

    //сформирвать запрос для удаления файла на сервере
    public void DeleteFileFromServerRequest (ChannelHandlerContext ctx, String path){
        System.out.printf("\nГотовим запрос на удаление фала: " + path);
        ByteBuf buf = null;
        String filename = Paths.get(path).getFileName().toString();
        //служеный файл
        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeByte((byte)4);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeInt(filename.getBytes().length);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(15);
        buf.writeBytes(filename.getBytes());
        ctx.writeAndFlush(buf);

        System.out.printf("\nЗапрос отправили!");
    }

    //сформировать запрос для получения списка файлов от сервера Netty
    public void FilesListRequestFromServer (ChannelHandlerContext ctx){
        System.out.printf("\nГотовим запрос на получение списка файлов: ");
        ByteBuf buf = null;
        //служеный файл
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte)2);
        ctx.writeAndFlush(buf);
        System.out.printf("\nЗапрос отправлен!");
    }

    public void RenameFile(String old, String cur){
        if(old == cur || cur == "") {
            System.out.printf("Ошибка");
            return;
        }
        File file = new File(mainCatalog + "/" + old);
        File newFile = new File(mainCatalog + "/" + cur);
        if(Files.exists(file.toPath())){
            if(file.renameTo(newFile)){
                System.out.println("Файл переименован успешно");
            }else{
                System.out.println("Файл не был переименован");
            }
        } else {
            System.out.println("Файл не найден");
        }
    }
    public void DeleteFile(String name){
        File file = new File(mainCatalog + "/" + name);
        if(Files.exists(file.toPath())){
            file.delete();
        }
    }

    // получаем список файлов из главного каталога
    public String[] GetListFilesNames() {
        ArrayList<String> arrList = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(mainCatalog), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    //System.out.println("обход файлов" + dir.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    //System.out.println("обход файлов" + file.getFileName().toString());
                    arrList.add(file.getFileName().toString());
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrList.toArray(new String[0]);
    }
    // Отправляем ответ клиенту пройдена\не пройдена авторизация
    public void SendAuthRequest(ChannelHandlerContext ctx, byte b){
        // если 0 авторизация не прошла, 1 авторизация успешна
        ByteBuf buf = null;
        //служеный файл
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(b);
        ctx.writeAndFlush(buf);
    }

    private void CreateFolder(){
        //проверяем существует ли каталог
        Path path = Paths.get(mainCatalog);
        try {
            if (!Files.exists(path)) {
                //создаем каталог
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
