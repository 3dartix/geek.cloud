package com.artem.serverNetty;

import com.artem.helpers.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class AuthServiceHandler extends ChannelInboundHandlerAdapter {
    private boolean authOk = false;
    private int command;
    private boolean isProcessing = false;
    private Helpers helpers = new Helpers("server_repository");
    private DB authService;

    public AuthServiceHandler(DB authService) {
        this.authService = authService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(authOk) {
            ctx.fireChannelRead(msg);
        } else {
            ByteBuf buf = ((ByteBuf) msg);

            if(!isProcessing) {
                command = buf.readByte();
                isProcessing = true;
            }

            //авторизация, парсим байты на логин/пароль и сравниваем с БД
            if(command == 3){
                String text = helpers.getStringFromBytes(buf);

                if(text != "") {
                    String[] strings = text.split(";");

                    if(authService.authCheck(strings[0], strings[1])){
                        System.out.printf("\nКлиент авторизован");
                        authOk = true;
                        ctx.pipeline().addLast(new ServerHandler(helpers, strings[0]));
                        //отправляем ответ клиенту, что авторизация прошла успешно
                        helpers.sendAuthRequest(ctx, (byte)1);
                        //задаем новый главный каталог и создаем папку для хранения файлов клиента
                        helpers.setMainCatalog(helpers.getMainCatalog()+"/"+strings[0]);

                    } else {
                        System.out.printf("\nКлиент не авторизован");
                        helpers.sendAuthRequest(ctx, (byte)0);
                    }

                    isProcessing = false;
                    buf.clear();
                    buf.release();
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("\nКлиент отвалился");
        ctx.close();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //cause.printStackTrace();
        ctx.close();
    }
}
