package JavaFXClient.Client;

import CloudPackage.Helpers;
import CloudPackage.Helpers_netty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Helpers_netty helpers = new Helpers_netty("client_repository");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            //пока в этом буфере есть хоть один непрочитанный байт читаем и выводим в консоль
            while (in.isReadable()) {
                byte b = in.readByte();
                //весь файл без проблем выводится в консоль
                System.out.print((char)b);
                //но когда мы пытаемся его записать происходит эксепшн
                if(b == 0) {
                    helpers.Write(in);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
