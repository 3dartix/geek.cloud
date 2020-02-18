package JavaFXClient.Client;

import CloudPackage.Helpers;
import CloudPackage.Helpers_netty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Controller controller;
    private Helpers_netty helpers;// = new Helpers_netty("client_repository");

    public ClientHandler(Controller controller, Helpers_netty helpers) {
        this.controller = controller;
        this.helpers = helpers;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            //пока в этом буфере есть хоть один непрочитанный байт читаем и выводим в консоль
            while (in.isReadable()) {
                int b = in.readByte();
                if(b == 0) {
                    helpers.Write(in);
                }
                controller.UpdateListClient();
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
