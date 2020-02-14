package severNetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BackToByteBufHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.printf("\nОтправляем файл");
        String path = (String) msg;

        //Что за регион такой
        FileRegion region = new DefaultFileRegion(new FileInputStream(Paths.get(path).toFile()).getChannel(), 0, Files.size(Paths.get(path)));


        ByteBuf buf = null;
        String filename = Paths.get(path).getFileName().toString();
        //служеный фал
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeByte(0);
        ctx.writeAndFlush(buf);
        //байтов в имени файла
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filename.length());
        ctx.writeAndFlush(buf);
        //имя файла в байтах
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeBytes(filename.getBytes());
        ctx.writeAndFlush(buf);
        //лонг количесво байтов в файле
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeLong(new File(path).length());
        ctx.writeAndFlush(buf);
        //сам файл
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeLong(new File(path).length());
        ctx.writeAndFlush(buf);

        //С этим разабраться не смог finishListener?
//        ChannelFuture transferOperationFuture = ctx.writeAndFlush(region);
//        if (ctx.channel().finishListener != null) {
//            transferOperationFuture.addListener(finishListener);
//        }

    }
}

//        ctx.writeAndFlush(0);
//        String filename = Paths.get(path).getFileName().toString();
//        int fileNameLength = filename.length();
//        ctx.writeAndFlush(fileNameLength);
//        ctx.writeAndFlush(filename.getBytes());
//        ctx.writeAndFlush(new File(path).length());
//        byte[] buf = new byte[8192];
//        try (InputStream in = new FileInputStream(path)) {
//            int n;
//            while ((n = in.read(buf)) != -1){
//                ctx.writeAndFlush(n);
//            }
//        }
//==========================
//        System.out.print((String)msg);
//        String str = (String)msg;
//        byte[] arr = str.getBytes();
//        ByteBuf buf = ctx.alloc().buffer(arr.length);
//        buf.writeBytes(arr);
//        ctx.writeAndFlush(buf);
//        buf.release();
