package JavaFXClient.Client;

import CloudPackage.Helpers;
import CloudPackage.Helpers_netty;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {
    Helpers_netty helpers = new Helpers_netty("client_repository");
    DataInputStream in;
    DataOutputStream out;
    SocketChannel socketChannel;

    @FXML
    TextArea textArea;
    @FXML
    TextField textField;

    public void connect() throws Exception{
        try {
            Socket socket = new Socket("localhost", 8189);
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {

                            System.out.printf("Ожидаем сообщения\n");
                            //String str = in.readUTF();
                            int x = in.read();
                            if(x == 0) {
                                //helpers.Write(in);
                            }
                            if(x == -1) {
                                break;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void connectNetty() throws Exception{
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String host = "localhost";
                int port = 8189;
                EventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    //Bootstrap похож на ServerBootstrap, но предназначен для клиентских каналов.
                    Bootstrap b = new Bootstrap();                    // (1)
                    //При указании одного объекта EventLoopGroup он будет использоваться и в качестве boss group,
                    // и как worker group. С другой стороны, boss worker не используется на клиенте.
                    b.group(workerGroup);                             // (2)
                    //Вместо NioServerSocketChannel используется клиентский NioSocketChannel.
                    b.channel(NioSocketChannel.class);                // (3)
                    b.option(ChannelOption.SO_KEEPALIVE, true);
                    b.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientHandler());
                            socketChannel = ch;
                        }
                    });

                    // Start the client.
                    //Вместо метода bind() применяется connect().
                    ChannelFuture f = b.connect(host, port).sync();   // (4)

                    // Wait until the connection is closed.
                    f.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    workerGroup.shutdownGracefully();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    public void SendFile(){
        //helpers.ReadAndSendFile(out, "1.txt");
        System.out.printf("\nжмем на кнопку отправить на сервер файл");
        helpers.SendRequest(socketChannel.pipeline().context(ClientHandler.class), "1.txt");
    }

    public void SendRequestForGetFile(){
        System.out.printf("\nжмем на кнопку отправить на сервер запрос запрос");
        helpers.SendRequest(socketChannel.pipeline().context(ClientHandler.class), "2.txt");
    }


    public void sendMsg() {
        //textArea.appendText(formatForDateNow.format(dateNow) + "\n" + textField.getText() + "\n");
        textField.clear();
        textField.requestFocus();
    }



}
