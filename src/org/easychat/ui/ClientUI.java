package org.easychat.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.easychat.client.ChatClient;
import org.easychat.client.ClientException;
import org.easychat.design.ChatObserver;
import org.easychat.design.UsersObserver;
import java.util.*;

/**
 * Created by Slevy on 16.05.2018.
 */
public class ClientUI extends Application {

    private static final double ENTER_WIDTH = 600;
    private static final double ENTER_HEIGHT = 320;
    private static final double CHAT_WIDTH = 800;
    private static final double CHAT_HEIGHT = 600;

    private ChatClient chatClient = null;
    private boolean clientCreated = false;
    private Stage appStage = null;
    private final ObservableList<String> nicknames = FXCollections.observableArrayList("Public");
    private final ListView<String> usersConnected = new ListView<>(nicknames);

    private String chatSelected = "Public";
    private final TextArea messagesArea = new TextArea();

    private void connectToServer(String nickname) throws ClientException {

        clientCreated = true;
        chatClient = new ChatClient(nickname);
        new UsersObserver(chatClient, this);
        new ChatObserver(chatClient, this);
    }

    private void refreshChat() {

        final StringBuilder messages = new StringBuilder();
        final String[] conversation;

        switch (this.chatSelected.toLowerCase()) {

            case "public":
                conversation = chatClient.getPublicConversation();
                break;
            default:
                conversation = chatClient.getConversation(chatSelected);
                break;
        }

        if (conversation != null && conversation.length > 0) {

            Arrays.stream(conversation)
                    .map(message -> message + "\n")
                    .forEach(messages::append);

            messagesArea.setText(messages.toString());
            messagesArea.positionCaret(messages.length());

        } else messagesArea.setText("");
    }

    private void refreshNicknames() {

        if (usersConnected.getSelectionModel().getSelectedIndex() == -1) {

            boolean chatChanged = false;

            if (!nicknames.contains(this.chatSelected)) {

                this.chatSelected = "Public";
                chatChanged = true;
            }

            final int index = nicknames.indexOf(this.chatSelected);
            this.usersConnected.getSelectionModel().select(index);
            this.usersConnected.requestFocus();

            if (chatChanged) {

                chatClient.openPublicChat();
                this.refreshChat();
            }
        }
    }

    public void updateChat(String chatNickname) {

        if (this.chatSelected.equals(chatNickname))
            this.refreshChat();
        else {

            if (nicknames.contains(chatNickname)) {

                System.out.println("MeSAJ PE ALT CHAT");
            }
        }
    }

    public void updateNicknames(List<String> users) {

        this.nicknames.clear();
        this.nicknames.add("Public");

        users.forEach((nickname) -> {

            if (!nickname.equals(chatClient.getName())) {

                nicknames.add(nickname);
            }
        });

        this.refreshNicknames();
    }

    private Pane buildEnterScreen() {

        /** create enterScene elements **/
        final GridPane enterPane = new GridPane();
        enterPane.setMinSize(ENTER_WIDTH, ENTER_HEIGHT);
        enterPane.setPadding(new Insets(10, 10, 10, 10));
        enterPane.setVgap(20);
        enterPane.setHgap(10);
        enterPane.setAlignment(Pos.CENTER);
        enterPane.setStyle("-fx-background-color: whitesmoke;");

        final TextField enterNickname = new TextField();
        enterNickname.setFont(new Font(18));
        enterNickname.setMinSize(237, 39);
        enterNickname.setPrefSize(237, 39);
        enterNickname.setMaxSize(237, 39);

        final Label nicknameLabel = new Label();
        nicknameLabel.setText("Nickname: ");
        nicknameLabel.setLabelFor(enterNickname);
        nicknameLabel.setFont(new Font(20));

        final Button enter = new Button();
        enter.setText("Enter");
        enter.setFont(new Font(20));
        enter.setStyle("-fx-background-color: darkcyan; -fx-text-fill: whitesmoke;");

        final Text loginMessage = new Text();
        loginMessage.setFont(new Font(20));

        enterPane.add(nicknameLabel, 0, 0);
        enterPane.add(enterNickname, 1, 0);
        enterPane.add(enter, 1, 1);
        enterPane.add(loginMessage, 0, 2, 2, 1);

        enter.setOnMouseClicked((mouseEvent) -> {

            final String nickname = enterNickname.getText().trim();

            if (!clientCreated) {

                if (nickname.equals(""))
                    loginMessage.setText("Nickname cannot be empty !");
                else if (nickname.length() > 20)
                    loginMessage.setText("Nickname is too long !");
                else try {

                        this.connectToServer(nickname);
                        this.setChatScreen();

                    } catch (ClientException ex) {

                        clientCreated = false;
                        loginMessage.setText(ex.getMessage());
                    }
            }
        });

        return enterPane;
    }

    private Pane buildChatScreen() {

        /** create chatScene elements **/

        final GridPane chatPane = new GridPane();
        chatPane.setPadding(new Insets(10, 10, 10, 10));
        chatPane.setVgap(10);
        chatPane.setHgap(10);
        chatPane.setStyle("-fx-background-color: darkslategray ;");

        /** create messages text area **/

        messagesArea.setWrapText(true);
        messagesArea.setFont(new Font(20));
        messagesArea.setEditable(false);

        messagesArea.setMinSize((CHAT_WIDTH - 20) * 0.75 - 5, (CHAT_HEIGHT - 20) * 0.8 - 5);
        messagesArea.setPrefSize((CHAT_WIDTH - 20) * 0.75 - 5, (CHAT_HEIGHT - 20) * 0.8 - 5);
        messagesArea.setMaxSize((CHAT_WIDTH - 20) * 0.75 - 5, (CHAT_HEIGHT - 20) * 0.8 - 5);

        /** create write_message text area **/

        final TextArea writeArea = new TextArea();
        writeArea.setWrapText(true);
        writeArea.setFont(new Font(20));
        writeArea.setPadding(new Insets(5, 5, 5, 5));

        writeArea.setMinSize((CHAT_WIDTH - 20) * 0.63, (CHAT_HEIGHT - 20) * 0.2 - 5);
        writeArea.setPrefSize((CHAT_WIDTH - 20) * 0.63, (CHAT_HEIGHT - 20) * 0.2 - 5);
        writeArea.setMaxSize((CHAT_WIDTH - 20) * 0.63, (CHAT_HEIGHT - 20) * 0.2 - 5);

        /** create send button **/

        final Button send = new Button();
        send.setText("Send");
        send.setFont(new Font(20));
        send.setStyle("-fx-background-color: darkcyan; -fx-text-fill: whitesmoke;");

        send.setOnMouseClicked((mouseEvent) -> {

            /** send the message **/
            final String message = writeArea.getText().trim();

            if (!message.equals("")) {

                try {

                    chatClient.sendMessage(message);

                } catch (ClientException ex) {

                    ex.printStackTrace();
                }

                writeArea.setText("");
            }
        });


        /** create the users_connected list **/

        usersConnected.setMinSize((CHAT_WIDTH - 20) * 0.25 - 5, CHAT_HEIGHT - 20);
        usersConnected.setPrefSize((CHAT_WIDTH - 20) * 0.25 - 5, CHAT_HEIGHT - 20);
        usersConnected.setMaxSize((CHAT_WIDTH - 20) * 0.25 - 5, CHAT_HEIGHT - 20);

        usersConnected.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

                    if (oldValue != null && newValue != null) {

                        this.chatSelected = newValue;

                        switch (newValue.toLowerCase()) {

                            case "public":
                                chatClient.openPublicChat();
                                break;
                            default:
                                try {

                                    chatClient.openPrivateChat(this.chatSelected);

                                } catch (ClientException ex) {

                                    ex.printStackTrace();
                                }
                                break;
                        }

                        this.refreshChat();
                    }
                });

        /** add all elements to the GridPane **/
        chatPane.add(messagesArea, 0, 0, 2, 1);
        chatPane.add(writeArea, 0, 1);
        chatPane.add(send, 1, 1);
        chatPane.add(usersConnected, 2, 0, 1, 2);

        return chatPane;
    }

    private void setEnterScreen() {

        if (appStage != null) {

            final Scene enterScene = new Scene(this.buildEnterScreen(), ENTER_WIDTH, ENTER_HEIGHT);
            this.appStage.setScene(enterScene);
            this.appStage.sizeToScene();
        }
    }

    private void setChatScreen() {

        if (appStage != null) {

            final Scene chatScene = new Scene(this.buildChatScreen(), CHAT_WIDTH, CHAT_HEIGHT);
            this.appStage.setScene(chatScene);
            this.appStage.sizeToScene();
        }
    }

    @Override
    public void start(Stage primaryStage) {

        this.appStage = primaryStage;

        /** configure stage **/
        appStage.setTitle("Easy chat");
        appStage.setResizable(false);
        appStage.setOnCloseRequest(windowEvent -> {

            if (chatClient != null)
                chatClient.sendDisconnect();

            Platform.exit();
            System.exit(0);
        });

        this.setEnterScreen();
        appStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
