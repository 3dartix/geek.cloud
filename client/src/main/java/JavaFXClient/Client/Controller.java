package JavaFXClient.Client;

import CloudPackage.Helpers;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    private Helpers helpers = new Helpers("client_repository");
    //private DataInputStream in;
    //private DataOutputStream out;
    private SocketChannel socketChannel;
    private Controller controller = this;
    private int toggle;
    private boolean isAuthorized = false;

    private String selectedFileFromServer;
    private String selectedFileFromClient;

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public Helpers getHelpers() {
        return helpers;
    }

    @FXML
    HBox authPanel;
    @FXML
    TextField loginField;
    @FXML
    TextField passField;
    @FXML
    VBox mainUIbox;
    @FXML
    ListView listViewClient;
    @FXML
    ListView listViewServer;

    public Controller() {
        try {
            connectNetty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
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
*/
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
                            ch.pipeline().addLast(new ClientHandler(controller, helpers));
                            socketChannel = ch;
                            setAuthrized(false);
                            //SendRequestForFilesList();
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
        //Thread.sleep(5000);
        UpdateListClient();
    }

    public void SignIn(){
        if(loginField.getText().length() != 0 && passField.getText().length() != 0) {
            //System.out.print(loginField.getText().length());
            helpers.SendBytesForRename(socketChannel.pipeline().context(ClientHandler.class), loginField.getText(), passField.getText());
        }
    }

    public void SendFile(MouseEvent event){
        if (selectedFileFromClient != "") {
            helpers.SendBytesFromFile(socketChannel.pipeline().context(ClientHandler.class), selectedFileFromClient);
            OpenModalWindowProgress(event);
        }
    }

    public void GetFile(MouseEvent event){
        System.out.printf("\nжмем на кнопку отправить на сервер запрос запрос");
        if(selectedFileFromServer != "") {
            OpenModalWindowProgress(event);
            helpers.GetFileFromServerRequest(socketChannel.pipeline().context(ClientHandler.class), selectedFileFromServer);
        }
    }

    public void Delete(){
        switch (toggle) {
            case 0: // на сервере
                helpers.DeleteFileFromServerRequest(socketChannel.pipeline().context(ClientHandler.class), selectedFileFromServer);
                break;
            case 1: // на клиенте
                helpers.DeleteFile(selectedFileFromClient);
                UpdateListClient();
                break;
        }
    }

    public void SendRequestForFilesList(){
        helpers.FilesListRequestFromServer(socketChannel.pipeline().context(ClientHandler.class));
    }

    public void UpdateListClient(){
        Platform.runLater(() -> {
            String[] str = helpers.GetListFilesNames();
            listViewClient.getItems().clear();
            for (int i = 0; i < str.length; i++) {
                listViewClient.getItems().add(str[i]);
            }
        });
    }


    //переименование файлов на сервере и клиенте
    public void ToggleToServer (MouseEvent mouseEvent) {
        toggle = 0;
        OpenModalWindowRename(mouseEvent);
    }
    public void ToggleToClient (MouseEvent mouseEvent) {
        toggle = 1;
        OpenModalWindowRename(mouseEvent);
    }
    private void OpenModalWindowRename(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2) {
          //  Platform.runLater(() -> {
                Stage stage = new Stage();
                Parent root = null;
                try {
                    //root = FXMLLoader.load(getClass().getClassLoader().getResource("RenameModalWindow.fxml"));
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("RenameModalWindow.fxml"));
                    root = loader.load();
                    ControllerRenameWindow controllerRenameWindow = loader.getController();
                    controllerRenameWindow.setMainController(controller);
                    //убрать костыль
                    if(toggle == 0)
                        controllerRenameWindow.setTextToTextField(listViewServer.getSelectionModel().getSelectedItem().toString());
                    else
                        controllerRenameWindow.setTextToTextField(listViewClient.getSelectionModel().getSelectedItem().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stage.setTitle("Renaming file");
                stage.setScene(new Scene(root, 250, 100));
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(
                        ((Node) mouseEvent.getSource()).getScene().getWindow());

                stage.show();
           // });
        } else {
            // отраатываем одинарные клики
            switch (toggle) {
                case 0: // на сервере
                    if(listViewServer.getSelectionModel().getSelectedItem() != null) {
                        selectedFileFromServer = listViewServer.getSelectionModel().getSelectedItem().toString();
                        selectedFileFromClient = "";
                    }
                    break;
                case 1: // на клиенте
                    if(listViewClient.getSelectionModel().getSelectedItem() != null) {
                        selectedFileFromClient = listViewClient.getSelectionModel().getSelectedItem().toString();
                        selectedFileFromServer = "";
                    }
                    break;
            }
        }
    }
    public void RenameFle (String oldName, String curr){
        //отправляем
        switch (toggle) {
            case 0: // на сервере
                helpers.SendBytesForRename(socketChannel.pipeline().context(ClientHandler.class), oldName,curr);
                break;
            case 1: // на клиенте
                helpers.RenameFile(oldName, curr);
                UpdateListClient();
                break;
        }
    }

    public void setAuthrized(boolean isAuthorized) {
        if (!isAuthorized) {
            this.isAuthorized = false;
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            mainUIbox.setDisable(true);
            loginField.setText("");
            passField.setText("");
        } else {
            this.isAuthorized = true;
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            mainUIbox.setDisable(false);
        }
    }

    public void OpenModalWindowProgress(MouseEvent mouseEvent){
        Stage stage = new Stage();
        Parent root = null;
        try {
            //root = FXMLLoader.load(getClass().getClassLoader().getResource("RenameModalWindow.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ProgressModalWindow.fxml"));
            root = loader.load();
            ControllerProgressWindow controllerProgressWindow = loader.getController();
            controllerProgressWindow.setMainController(controller, stage);
            controllerProgressWindow.progressUpdate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setTitle("Processing");
        stage.setScene(new Scene(root, 250, 100));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(
                ((Node) mouseEvent.getSource()).getScene().getWindow());

        stage.show();
    }

}
