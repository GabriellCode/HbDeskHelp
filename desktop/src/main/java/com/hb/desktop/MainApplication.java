package com.hb.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class MainApplication extends Application {

    private BorderPane root;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("HbDeskHelp - App");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #000000; -fx-font-family: 'Segoe UI', Tahoma, sans-serif;");

        // Top Navigation
        HBox navBar = new HBox(15);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #333333; -fx-border-color: #555555; -fx-border-width: 0 0 2 0;");

        Button btnHome = createNavButton("Início");
        Button btnTicket = createNavButton("Abrir Chamado");
        Button btnChat = createNavButton("Falar com Cornelius");

        btnHome.setOnAction(e -> showHome());
        btnTicket.setOnAction(e -> showTicketForm());
        btnChat.setOnAction(e -> showChat());

        navBar.getChildren().addAll(btnHome, btnTicket, btnChat);
        root.setTop(navBar);

        // Initial view
        showHome();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-border-color: #555555; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 14px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-border-color: #555555; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 14px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-border-color: #555555; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 14px;"));
        return btn;
    }

    private void showHome() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        Label title = new Label("Bem-vindo ao HbDeskHelp");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Sistema de gerenciamento de chamados de suporte da HeadBlack.");
        subtitle.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 16px;");

        content.getChildren().addAll(title, subtitle);
        root.setCenter(content);
    }

    private void showTicketForm() {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(40, 100, 40, 100));

        Label title = new Label("Abrir um Novo Chamado");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label lblTitle = new Label("Título:");
        lblTitle.setStyle("-fx-text-fill: #ffffff;");
        TextField txtTitle = new TextField();
        txtTitle.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-border-color: #555555;");

        Label lblDesc = new Label("Descrição do problema:");
        lblDesc.setStyle("-fx-text-fill: #ffffff;");
        TextArea txtDesc = new TextArea();
        txtDesc.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: #ffffff; -fx-border-color: #555555;");
        txtDesc.setPrefRowCount(5);

        Button btnSubmit = new Button("Enviar Chamado");
        btnSubmit.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");

        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-text-fill: #00ff00;");

        btnSubmit.setOnAction(e -> {
            String t = txtTitle.getText();
            String d = txtDesc.getText();
            if(t.isEmpty() || d.isEmpty()) {
                lblStatus.setStyle("-fx-text-fill: #ff0000;");
                lblStatus.setText("Preencha todos os campos!");
                return;
            }

            try {
                String jsonBody = objectMapper.writeValueAsString(Map.of("title", t, "description", d));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/api/tickets"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                lblStatus.setText("Enviando...");
                lblStatus.setStyle("-fx-text-fill: #cccccc;");

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                lblStatus.setStyle("-fx-text-fill: #00ff00;");
                                lblStatus.setText("Chamado aberto com sucesso!");
                                txtTitle.clear();
                                txtDesc.clear();
                            } else {
                                lblStatus.setStyle("-fx-text-fill: #ff0000;");
                                lblStatus.setText("Erro ao enviar chamado. (Backend off?)");
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                lblStatus.setStyle("-fx-text-fill: #ff0000;");
                                lblStatus.setText("Erro de conexão: " + ex.getMessage());
                            });
                            return null;
                        });

            } catch (Exception ex) {
                lblStatus.setStyle("-fx-text-fill: #ff0000;");
                lblStatus.setText("Erro ao processar dados.");
            }
        });

        content.getChildren().addAll(title, lblTitle, txtTitle, lblDesc, txtDesc, btnSubmit, lblStatus);
        root.setCenter(content);
    }

    private void showChat() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 50, 20, 50));

        Label title = new Label("Atendimento com Cornelius (IA)");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");

        ListView<String> chatBox = new ListView<>();
        chatBox.setStyle("-fx-control-inner-background: #111111; -fx-text-fill: #ffffff;");
        chatBox.getItems().add("Cornelius: Olá! Eu sou o Cornelius, assistente de IA. Como posso ajudar?");
        VBox.setVgrow(chatBox, Priority.ALWAYS);

        HBox inputArea = new HBox(10);
        TextField txtInput = new TextField();
        txtInput.setPromptText("Digite sua mensagem...");
        txtInput.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; -fx-border-color: #555555;");
        HBox.setHgrow(txtInput, Priority.ALWAYS);

        Button btnSend = new Button("Enviar");
        btnSend.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-weight: bold;");

        Runnable sendAction = () -> {
            String msg = txtInput.getText().trim();
            if (msg.isEmpty()) return;

            chatBox.getItems().add("Você: " + msg);
            txtInput.clear();
            chatBox.scrollTo(chatBox.getItems().size() - 1);

            try {
                String jsonBody = objectMapper.writeValueAsString(Map.of("message", msg));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8081/api/chat"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> Platform.runLater(() -> {
                            try {
                                if (response.statusCode() == 200) {
                                    Map<String, String> resData = objectMapper.readValue(response.body(), Map.class);
                                    String aiResponse = resData.get("response");
                                    String source = resData.get("source");
                                    chatBox.getItems().add("Cornelius (" + source + "): " + aiResponse);
                                } else {
                                    chatBox.getItems().add("Sistema: Erro ao conectar com o backend.");
                                }
                            } catch (Exception ex) {
                                chatBox.getItems().add("Sistema: Erro ao ler resposta.");
                            }
                            chatBox.scrollTo(chatBox.getItems().size() - 1);
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                chatBox.getItems().add("Sistema: Erro de conexão. O servidor está rodando?");
                                chatBox.scrollTo(chatBox.getItems().size() - 1);
                            });
                            return null;
                        });
            } catch (Exception ex) {
                chatBox.getItems().add("Sistema: Erro interno.");
            }
        };

        btnSend.setOnAction(e -> sendAction.run());
        txtInput.setOnAction(e -> sendAction.run()); // Trigger on Enter

        inputArea.getChildren().addAll(txtInput, btnSend);
        content.getChildren().addAll(title, chatBox, inputArea);
        root.setCenter(content);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
