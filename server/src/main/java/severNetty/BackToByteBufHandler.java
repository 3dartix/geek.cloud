package severNetty;

import CloudPackage.Helpers_netty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BackToByteBufHandler extends ChannelOutboundHandlerAdapter {
    Helpers_netty helpers = new Helpers_netty("server_repository");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        helpers.Send(ctx,msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}