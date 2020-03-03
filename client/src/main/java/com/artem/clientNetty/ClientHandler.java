package com.artem.clientNetty;

import com.artem.helpers.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import javafx.application.Platform;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Controller controller;
    private Helpers helpers;
    private long time;

    private boolean isProcessing = false;
    byte command;

    public ClientHandler(Controller controller, Helpers helpers) {
        this.controller = controller;
        this.helpers = helpers;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        try {
            //проверка авторизован ли ползователь
            if(!controller.isAuthorized()) {
                if(buf.readByte() == 1){
                    controller.setAuthrized(true);
                    controller.sendRequestForFilesList();
                } else {
                    controller.setAuthrized(false);
                }
                return;
            }

            if(!isProcessing) {
                command = buf.readByte();
                isProcessing = true;
                System.out.printf("\nНачинаем измерять скорость передачи\n");
                time = System.currentTimeMillis();
            }

            //получаем файл
            if(command == 0) {
                if(helpers.write(buf)){
                    isProcessing = false;
                    controller.updateListClient();
                    System.out.printf("Скорость приема (Клиент) - " + (System.currentTimeMillis() - time));
                }
            }

            //получаем список файлов хранящихся на сервере
            if(command == 2) {
                String fileName = helpers.getStringFromBytes(buf);
                System.out.printf("\nОтправляем список на сервер");
                if(fileName != ""){
                    isProcessing = false;
                    //обновляем список файлов на клиенте
                    String[] strings = fileName.split(";");

                    Platform.runLater(() -> {
                        controller.listViewServer.getItems().clear();
                        for (int i = 0; i < strings.length; i++) {
                            controller.listViewServer.getItems().add(strings[i]);
                        }
                    });
                }
            }
        } finally {
            buf.release();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close();
    }
}
