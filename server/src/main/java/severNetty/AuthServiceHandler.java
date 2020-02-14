package severNetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthServiceHandler extends ChannelInboundHandlerAdapter {
    private boolean authOk = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(authOk) {
            ctx.fireChannelRead(msg);
        } else {
            //авторизация
            authOk = true;
            ctx.pipeline().addLast(new ParseServiceHandler("user01"));
            ctx.fireChannelRead(msg);
        }

//        ByteBuf in = (ByteBuf) msg;
//        try {
//            //пока в этом буфере есть хоть один непрочитанный байт читаем и выводим в консоль
//            while (in.isReadable()) {
//                if(in.readByte() == 15) {
//                    //ReadFile(in);
//                }
//                System.out.print((char) in.readByte());
//            }
//        } finally {
//            ReferenceCountUtil.release(msg);
//        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент отвалился");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
