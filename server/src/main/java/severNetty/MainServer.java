package severNetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MainServer {
    public static void main(String[] args) throws Exception {
        new MainServer().run();
    }

    public void run() throws Exception {
        //создаем пулы потоков аналог executorservice (многопоточность)
        //первый отвечает за подключения новых клиентов
        //клиенты подключаются в отдельном пуле потоков
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //поток отвечает за обработку всех сообщений получаемых серваком
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //настройка сервака
            ServerBootstrap b = new ServerBootstrap();
            //сервер использует 2 ранее созданных пула потоков
            System.out.printf("Сервер запущен\n");
            b.group(bossGroup, workerGroup)
                    //для того чтобы клиенты подключались мы должны использовать NioServerSocketChannel
                    //аналог обычного сокета из java io
                    .channel(NioServerSocketChannel.class)
                    //после каждого подключения клиента открывается SocketChannel и его нужно настроить
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        // создаем сокет канал new ChannelInitializer<SocketChannel>() и получаем ссылку (SocketChannel ch)
                        //можно складывать на сервере список из этих клиентов ch
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.printf("Клиент подключился\n");
                            //настраиваем для каждого кликента конвейер
                            //new DiscardServerHandler() добавили 1 кубик
                            ch.pipeline().addLast(new BackToByteBufHandler(), new AuthServiceHandler());
                        }
                    })
                    //можно настраивать оптции для каждого клиента
                    //вроде постоянное соединение для клиента
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            //далее мы говорим, что хотим запустить сервак на данном порту и старт .sync();
            ChannelFuture f = b.bind(8189).sync();
            //Ожидаем события закрытия сервака. Аналог await
            //Как только мы с этой строчки сдиваемся, срабатывает блок finallyд
            f.channel().closeFuture().sync();
        } finally {
            //если сервак остановился отключаем пулы потоков
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
