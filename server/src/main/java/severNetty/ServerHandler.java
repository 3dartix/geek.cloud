package severNetty;

import CloudPackage.Helpers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private String userName;
    private Helpers helpers;
    private enum SERVICE_COMMANDS { GET_FILE, SEND_FILE, SEND_LIST_FILES, RENAME_FILE }

    private boolean isProcessing = false;

    public ServerHandler(Helpers helpers, String userName) {
        this.helpers = helpers;
        this.userName = userName;
    }

    private byte command;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        if(!isProcessing) {
            command = buf.readByte();
            isProcessing = true;
        }

        //получаем файл и сохраняем его в папке сервера
        if(command == 0) {
            if(helpers.Write(buf)){
                isProcessing = false;
                buf.release();
                //отправляем обновленный список файлов клиенту
                helpers.SendListFilesToClient(ctx);
            }
        }

        // отправляем запрашиваемый файл клиенту
        if(command == 1) {
            String fileName = helpers.GetStringFromBytes(buf);
            if(fileName != ""){
                //ctx.writeAndFlush(fileName);
                helpers.SendBytesFromFile(ctx, fileName);
                isProcessing = false;
                buf.clear();
                ((ByteBuf) msg).release();
            }
        }

        //формируем пачку байтов из списка файлов на сервере и отправляем клиенту
        if(command == 2) {
            helpers.SendListFilesToClient(ctx);
            isProcessing = false;
            buf.release();
        }

        //переименовываем файл в папке на сервере
        if(command == 3){
            String filesFormClient = helpers.GetStringFromBytes(buf);
            if(filesFormClient != "") {
                System.out.printf("\nСтрока с файлами ::" + filesFormClient);
                String[] strings = filesFormClient.split(";");
                helpers.RenameFile(strings[0], strings[1]);
                helpers.SendListFilesToClient(ctx);
                isProcessing = false;
                buf.release();
            }
        }

        //удаляем файл на сервере
        if(command == 4){
            String filesFormClient = helpers.GetStringFromBytes(buf);
            if(filesFormClient != "") {
                helpers.DeleteFile(filesFormClient);
                helpers.SendListFilesToClient(ctx);
                isProcessing = false;
                buf.release();
            }
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
