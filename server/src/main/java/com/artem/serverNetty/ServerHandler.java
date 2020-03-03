package com.artem.serverNetty;

import com.artem.helpers.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private String userName;
    private Helpers helpers;
    private long time;

    private boolean isProcessing = false;

    public ServerHandler(Helpers helpers, String userName) {
        this.helpers = helpers;
        this.userName = userName;
    }

    private byte command;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {

            if (!isProcessing) {
                command = buf.readByte();
                isProcessing = true;
                System.out.printf("\nНачинаем измерять скорость передачи\n");
                time = System.currentTimeMillis();
            }

            //получаем файл и сохраняем его в папке сервера
            if (command == 0) {
                if (helpers.write(buf)) {
                    isProcessing = false;
                    //отправляем обновленный список файлов клиенту
                    helpers.sendListFilesToClient(ctx);
                    System.out.printf("Скорость приема (Сервер) - " + (System.currentTimeMillis() - time));
                }
            }

            // отправляем запрашиваемый файл клиенту
            if (command == 1) {
                String fileName = helpers.getStringFromBytes(buf);
                if (fileName != "") {
                    helpers.sendBytesFromFile(ctx, fileName);
                    isProcessing = false;
                }
            }

            //формируем пачку байтов из списка файлов на сервере и отправляем клиенту
            if (command == 2) {
                helpers.sendListFilesToClient(ctx);
                isProcessing = false;
            }

            //переименовываем файл в папке на сервере
            if (command == 3) {
                String filesFormClient = helpers.getStringFromBytes(buf);
                if (filesFormClient != "") {
                    System.out.printf("\nСтрока с файлами ::" + filesFormClient);
                    String[] strings = filesFormClient.split(";");
                    helpers.renameFile(strings[0], strings[1]);
                    helpers.sendListFilesToClient(ctx);
                    isProcessing = false;
                }
            }

            //удаляем файл на сервере
            if (command == 4) {
                String filesFormClient = helpers.getStringFromBytes(buf);
                if (filesFormClient != "") {
                    helpers.deleteFile(filesFormClient);
                    helpers.sendListFilesToClient(ctx);
                    isProcessing = false;
                }
            }
        } finally {
            buf.release();
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
