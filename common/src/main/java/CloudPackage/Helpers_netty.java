package CloudPackage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Helpers_netty {
    private String mainCatalog;
    public Helpers_netty(String mainCatalog) {
        this.mainCatalog = mainCatalog;
    }
    public String getMainCatalog() {
        return mainCatalog;
    }

    //распарсить поток батов и записать файл на диск
    public void Write(ByteBuf in) throws IOException {
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        long fileSize = in.readLong();

        Path path = Paths.get(mainCatalog + "/" + fileName);
        System.out.printf("Название файла >> " + fileName + "\n" + "Количество байтов в файла >> " + fileSize +"\n");

//        for (int i = 0; i < fileSize; i++) {
//            byte b = in.readByte();
//            System.out.print((char)b);
//        }

        try (OutputStream outputStreamToFile = new BufferedOutputStream(new FileOutputStream(path.toString()))) {
            for (long i = 0; i < fileSize; i++) {
                byte b = in.readByte();
                System.out.print((char) b);
                outputStreamToFile.write(b);
            }
        }
        System.out.println(String.format("Файл %s получен и размещен: %s", fileName, path.toString()));
    }

    //сформировать пачку байтов
    // [служебный файл][длина названия файла][название файла в байтах][размер файла лонг][сам файл]
    public void Send(ChannelHandlerContext ctx, Object msg) throws IOException {
        System.out.printf("\nОтправляем файл");
        String path = (String) msg;
        String filename = Paths.get(path).getFileName().toString();
        //готовим регион для оправки файла целиком
        FileRegion region = new DefaultFileRegion(new FileInputStream(Paths.get(path).toFile()).getChannel(), 0, Files.size(Paths.get(path)));
        //формируем служ инофрмацию о фале
        ByteBuf buf = null;
        //Служебны байт 0 - прочитать пачку байтов и сохранить в локальную папку
        buf = ByteBufAllocator.DEFAULT.directBuffer(5);
        buf.writeByte(0);
        ctx.writeAndFlush(buf);
        //длина названия файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeInt(filename.length());
        ctx.writeAndFlush(buf);
        //название файла в байтах
        buf = ByteBufAllocator.DEFAULT.directBuffer(15);
        buf.writeBytes(filename.getBytes());
        ctx.writeAndFlush(buf);
        //размер файла лонг
        buf = ByteBufAllocator.DEFAULT.directBuffer(50);
        buf.writeLong(new File(path).length());
        ctx.writeAndFlush(buf);
        //сам файл
        // -- полагаю, что при отправке поток не закрывается, а другая сторона продолжает ожидать байтов --
        ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
        //возможно как раз вот этот кусок кода и решает эту проблему, но я не смог разобраться откуда берется
        //вот эта штука -> Network.getInstance().stop();
        /*
        future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан");
                Network.getInstance().stop();
            }
        }
         */
    }

    //распарсить запрос и получить из него имя файла
    public String ParseRequest (ByteBuf in){
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readBytes(fileNameBytes);
        System.out.print("Получили имя файла: " + new String(fileNameBytes));
        return new String(fileNameBytes);
    }

    //сформирвать запрос для получения файла от сервера Netty
    public void SendRequest(ChannelHandlerContext ctx, String path){
        System.out.printf("\nГотовим запрос на копирование файла: " + path);
        ByteBuf buf = null;
        String filename = Paths.get(path).getFileName().toString();
        //служеный фал
        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeByte((byte)1);
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(3);
        buf.writeInt(filename.length());
        ctx.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(15);
        buf.writeBytes(filename.getBytes());
        ctx.writeAndFlush(buf);

        System.out.printf("\nЗапрос отправили!");

        //buf.writeLong(new File(path).length());
        //ctx.writeAndFlush(buf);
    }

}
