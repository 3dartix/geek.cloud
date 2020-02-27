package JavaFXClient.Client;

import CloudPackage.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import javafx.application.Platform;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Controller controller;
    private Helpers helpers;

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
        //проверка авторизован ли ползователь
        if(!controller.isAuthorized()) {
            if(buf.readByte() == 1){
                controller.setAuthrized(true);
                controller.SendRequestForFilesList();
            } else {
                controller.setAuthrized(false);
            }
            return;
        }

        if(!isProcessing) {
            command = buf.readByte();
            isProcessing = true;
        }

        //получаем файл
        if(command == 0) {
            if(helpers.Write(buf)){
                isProcessing = false;
                buf.release();
                controller.UpdateListClient();
            }
        }
        //получаем список файлов хранящихся на сервере
        if(command == 2) {
            String fileName = helpers.GetStringFromBytes(buf);
            System.out.printf("\nОтправляем список на сервер");
            if(fileName != ""){
                isProcessing = false;
                buf.clear();
                ((ByteBuf) msg).release();

                //обновляем список файлов на клиенте
                String[] strings = fileName.split(";");

                Platform.runLater(() -> {
                    controller.listViewServer.getItems().clear();
                    for (int i = 0; i < strings.length; i++) {
                        //System.out.printf("\n файл: " + strings[i]);
                        controller.listViewServer.getItems().add(strings[i]);
                    }
                });

            }
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
