package JavaFXClient.Client;

import CloudPackage.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Helpers helpers = new Helpers("client_repository");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            //пока в этом буфере есть хоть один непрочитанный байт читаем и выводим в консоль
            while (in.isReadable()) {
                byte b = in.readByte();
                if(b == 15) {
                    helpers.Write(in);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
