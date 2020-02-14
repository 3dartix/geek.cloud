package severNetty;

import CloudPackage.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ParseServiceHandler extends ChannelInboundHandlerAdapter {
    private String userName;
    private Helpers helpers = new Helpers("server_repository");
    private enum SERVICE_COMMANDS { GET_FILE, SEND_FILE, SEND_FILESNAME }

    public ParseServiceHandler(String userName) {
        this.userName = userName;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            //пока в этом буфере есть хоть один непрочитанный байт читаем и выводим в консоль
            while (in.isReadable()) {
                byte b = in.readByte();
                if(b == SERVICE_COMMANDS.GET_FILE.ordinal()) {
                    helpers.Write(userName, in);
                }

                if(b == SERVICE_COMMANDS.SEND_FILE.ordinal()) {
                    String fileName = helpers.ParseRequest(in);
                    System.out.printf("\nГотовимся отправить файл");
                    ctx.writeAndFlush(helpers.getMainCatalog() +"/"+  userName +"/"+ fileName);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
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
