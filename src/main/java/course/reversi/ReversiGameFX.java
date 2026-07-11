package course.reversi;

/*
    Content:
    ReversiGameFX with an intermediate Preview Stage displaying Bot Profiles,
    Difficulty Levels, Custom Strategies, Win/Loss Records, and Graphical Placeholders.
 */


import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class ReversiGameFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static final int SIZE = 8;
    private static final double CELL_SIZE = 60.0;
    private static final char BLACK = '⚫';
    private static final char WHITE = '⚪';

    private char[][] board = new char[SIZE][SIZE];
    private char currentPlayer = BLACK;
    private SimpleBot bot = null;

    private GridPane boardGrid;
    private Label scoreLabel;
    private Label currentPlayerLabel;

    private VBox root;
    private Stage primaryStage;

    // --- REUSABLE UI STYLE SHEET CONSTANTS ---
    private static final String APP_BACKGROUND = "-fx-background-color: #2c3e50;"; // Rich dark navy
    private static final String CARD_BACKGROUND = "-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4);";

    private static final String TEXT_PRIMARY = "-fx-text-fill: #ecf0f1; -fx-font-family: 'Segoe UI', Helvetica, sans-serif;";
    private static final String TEXT_DARK = "-fx-text-fill: #2c3e50; -fx-font-family: 'Segoe UI', Helvetica, sans-serif;";

    private static final String BTN_PRIMARY = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24 10 24; -fx-cursor: hand;";
    private static final String BTN_SECONDARY = "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-background-radius: 6px; -fx-padding: 8 16 8 16; -fx-cursor: hand;";
    private static final String BTN_DANGER = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8 16 8 16; -fx-cursor: hand;";

    private static final String COMBO_BOX_STYLE = "-fx-background-color: #ffffff; -fx-border-color: #bdc3c7; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 4; -fx-font-size: 14px;";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        VBox menuRoot = new VBox(25);
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.setPadding(new Insets(40));
        menuRoot.setStyle(APP_BACKGROUND);

        // Core App Title
        Label titleLabel = new Label("REVERSI ARENA");
        titleLabel.setStyle(TEXT_PRIMARY + "-fx-font-size: 32px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");

        // Central Menu Card
        VBox cardContainer = new VBox(15);
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setPadding(new Insets(30, 40, 30, 40));
        cardContainer.setMaxWidth(400);
        cardContainer.setStyle(CARD_BACKGROUND);

        Label selectLabel = new Label("Select Battle Variant");
        selectLabel.setStyle(TEXT_DARK + "-fx-font-size: 15px; -fx-font-weight: bold;");

        ComboBox<String> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll(
                "Human vs Human",
                "Human vs DumbBot (Easy)",
                "Human vs MapleBot (Medium)",
                "Human vs CastellaBot (Positional Matrix)",
                "Human vs MomijiManjuBot (Advanced)",
                "Human vs TaiyakiBot (6-Depth AlphaBeta)",
                "Human vs AnmitsuBot (Adaptive RL Phase Weighting)"
        );
        modeSelector.setValue("Human vs Human");
        modeSelector.setStyle(COMBO_BOX_STYLE);
        modeSelector.setPrefWidth(320);

        Button playButton = new Button("LAUNCH MATCH");
        playButton.setStyle(BTN_PRIMARY + "-fx-font-size: 15px; -fx-pref-width: 200px;");

        playButton.setOnAction(e -> {
            String selected = modeSelector.getValue();
            GameMode selectedMode = GameMode.HUMAN_VS_HUMAN;

            if (selected.contains("DumbBot")) selectedMode = GameMode.HUMAN_VS_DUMB_BOT;
            else if (selected.contains("MapleBot")) selectedMode = GameMode.HUMAN_VS_MAPLE_BOT;
            else if (selected.contains("CastellaBot")) selectedMode = GameMode.HUMAN_VS_CASTELLA_BOT;
            else if (selected.contains("MomijiManjuBot")) selectedMode = GameMode.HUMAN_VS_MOMIJI_MANJU_BOT;
            else if (selected.contains("TaiyakiBot")) selectedMode = GameMode.HUMAN_VS_TAIYAKI_BOT;
            else if (selected.contains("AnmitsuBot")) selectedMode = GameMode.HUMAN_VS_ANMITSU_BOT;

            if (selectedMode == GameMode.HUMAN_VS_HUMAN) {
                startGame(selectedMode);
            } else {
                showBotPreviewStage(selectedMode);
            }
        });

        cardContainer.getChildren().addAll(selectLabel, modeSelector, playButton);

        Button helpButton = new Button("Game Manual & Rules");
        helpButton.setStyle(BTN_SECONDARY);
        helpButton.setOnAction(e -> showHelp());

        menuRoot.getChildren().addAll(titleLabel, cardContainer, helpButton);

        Scene menuScene = new Scene(menuRoot, 600, 450);
        primaryStage.setTitle("Reversi Game Arena");
        primaryStage.setResizable(false);
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void showBotPreviewStage(GameMode gameMode) {
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.initOwner(primaryStage);
        previewStage.setTitle("Opponent Blueprint Analysis");
        previewStage.setResizable(false);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8f9fa;");

        String name, difficulty, strategy, wins, losses, hexColor;

        switch (gameMode) {
            case HUMAN_VS_DUMB_BOT:
                name = "DumbBot"; difficulty = "★☆☆☆☆ (Easy)";
                strategy = "Executes arbitrary valid moves across the board space without tactical analysis.";
                wins = "12"; losses = "2"; hexColor = "#95a5a6";
                break;
            case HUMAN_VS_MAPLE_BOT:
                name = "MapleBot"; difficulty = "★★☆☆☆ (Normal)";
                strategy = "Implements a basic greedy approach optimized to maximize immediate disc capture.";
                wins = "8"; losses = "5"; hexColor = "#e67e22";
                break;
            case HUMAN_VS_CASTELLA_BOT:
                name = "CastellaBot"; difficulty = "★★★☆☆ (Medium)";
                strategy = "Utilizes a static position matrix grid weighting evaluation to target edges and corners.";
                wins = "6"; losses = "9"; hexColor = "#f1c40f";
                break;
            case HUMAN_VS_MOMIJI_MANJU_BOT:
                name = "MomijiManjuBot"; difficulty = "★★★☆☆ (Advanced)";
                strategy = "Mid-range search tree configuration focused on aggressive opponent mobility restriction.";
                wins = "3"; losses = "11"; hexColor = "#e74c3c";
                break;
            case HUMAN_VS_TAIYAKI_BOT:
                name = "TaiyakiBot"; difficulty = "★★★★☆ (Expert)";
                strategy = "Runs an alpha-beta pruned minimax look-ahead tree that transitions to point sweeps during endgame steps.";
                wins = "1"; losses = "14"; hexColor = "#d35400";
                break;
            case HUMAN_VS_ANMITSU_BOT:
                name = "AnmitsuBot"; difficulty = "★★★★★ (Master)";
                strategy = "Tracks 10 distinct topological feature properties evaluated across separate opening, mid, and late game layers.";
                wins = "0"; losses = "20"; hexColor = "#9b59b6";
                break;
            default:
                return;
        }

        // Avatar Module
        Rectangle imagePlaceholder = new Rectangle(100, 100);
        imagePlaceholder.setArcWidth(20);
        imagePlaceholder.setArcHeight(20);
        imagePlaceholder.setFill(Color.web(hexColor));
        Label avatarLabel = new Label(name.substring(0, 2).toUpperCase());
        avatarLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        GridPane imageStack = new GridPane();
        imageStack.setAlignment(Pos.CENTER);
        imageStack.add(imagePlaceholder, 0, 0);
        imageStack.add(avatarLabel, 0, 0);

        Label titleLabel = new Label(name);
        titleLabel.setStyle(TEXT_DARK + "-fx-font-size: 24px; -fx-font-weight: bold;");

        Label diffLabel = new Label("Threat Level: " + difficulty);
        diffLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b; -fx-font-size: 14px;");

        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.CENTER);
        Label stratHeader = new Label("Tactical Methodology:");
        stratHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Label stratLabel = new Label(strategy);
        stratLabel.setWrapText(true);
        stratLabel.setStyle(TEXT_DARK + "-fx-text-alignment: center; -fx-font-size: 13px;");
        detailsBox.getChildren().addAll(stratHeader, stratLabel);

        Label recordLabel = new Label(String.format("Historical Records Card » Wins: %s  |  Losses: %s", wins, losses));
        recordLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-background-color: #e8f4f8; -fx-padding: 6 12 6 12; -fx-background-radius: 4px;");

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button startMatchButton = new Button("BATTLE ENGINE");
        startMatchButton.setStyle(BTN_PRIMARY);
        startMatchButton.setOnAction(e -> {
            previewStage.close();
            startGame(gameMode);
        });

        Button backButton = new Button("CANCEL");
        backButton.setStyle(BTN_SECONDARY);
        backButton.setOnAction(e -> previewStage.close());

        actions.getChildren().addAll(backButton, startMatchButton);
        layout.getChildren().addAll(imageStack, titleLabel, diffLabel, detailsBox, recordLabel, actions);

        Scene scene = new Scene(layout, 440, 500);
        previewStage.setScene(scene);
        previewStage.showAndWait();
    }

    private void goToMainMenu() {
        start(primaryStage);
    }

    private void startGame(GameMode gameMode) {
        Reversi.initializeBoard(board);
        currentPlayer = BLACK;

        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle(APP_BACKGROUND);

        // Header Metrics Dashboard Panel
        HBox scorePanel = new HBox(40);
        scorePanel.setAlignment(Pos.CENTER);
        scorePanel.setPadding(new Insets(12));
        scorePanel.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8px;");

        scoreLabel = new Label("Black: 2 | White: 2");
        scoreLabel.setStyle(TEXT_PRIMARY + "-fx-font-size: 16px; -fx-font-weight: bold;");

        currentPlayerLabel = new Label("Turn: Black (⚫)");
        currentPlayerLabel.setStyle(TEXT_PRIMARY + "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        scorePanel.getChildren().addAll(scoreLabel, currentPlayerLabel);

        // Board Frame Wrapper
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setStyle("-fx-background-color: #1e272e; -fx-padding: 8px; -fx-background-radius: 10px;");
        updateBoard();

        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        Button homeButton = new Button("Main Menu");
        Button resetButton = new Button("Reset Board");
        Button helpButton = new Button("Help");

        homeButton.setStyle(BTN_SECONDARY);
        resetButton.setStyle(BTN_DANGER);
        helpButton.setStyle(BTN_SECONDARY);

        homeButton.setOnAction(e -> goToMainMenu());
        resetButton.setOnAction(e -> resetGame());
        helpButton.setOnAction(e -> showHelp());

        controls.getChildren().addAll(homeButton, resetButton, helpButton);
        root.getChildren().addAll(scorePanel, boardGrid, controls);

        Scene gameScene = new Scene(root, 650, 750);
        primaryStage.setScene(gameScene);
        primaryStage.show();

        if (gameMode == GameMode.HUMAN_VS_DUMB_BOT) bot = new DumbBot();
        else if (gameMode == GameMode.HUMAN_VS_MAPLE_BOT) bot = new MapleBot();
        else if (gameMode == GameMode.HUMAN_VS_CASTELLA_BOT) bot = new CastellaBot();
        else if (gameMode == GameMode.HUMAN_VS_MOMIJI_MANJU_BOT) bot = new MomijiManjuBot();
        else if (gameMode == GameMode.HUMAN_VS_TAIYAKI_BOT) bot = new TaiyakiBot();
        else if (gameMode == GameMode.HUMAN_VS_ANMITSU_BOT) bot = new AnmitsuBot();
        else bot = null;
    }

    private void updateBoard() {
        List<String> currentValidMoves = Reversi.getValidMoves(board, currentPlayer);

        if (currentValidMoves.isEmpty()) {
            char opponent = (currentPlayer == BLACK) ? WHITE : BLACK;
            if (Reversi.getValidMoves(board, opponent).isEmpty()) {
                javafx.application.Platform.runLater(this::showGameOver);
                return;
            } else {
                switchPlayer();
                return;
            }
        }

        boardGrid.getChildren().clear();
        boolean isBotTurn = (bot != null && currentPlayer == WHITE);
        boardGrid.setDisable(isBotTurn);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);

                // Matte Dark Green felt design pattern
                cell.setFill(Color.web("#27ae60"));
                cell.setStroke(Color.web("#1e272e"));
                cell.setStrokeWidth(1.5);

                if (board[row][col] == BLACK || board[row][col] == WHITE) {
                    cell.setFill(createPieceFill(Color.web("#27ae60"), board[row][col] == BLACK ? Color.web("#2c3e50") : Color.web("#f5f6fa")));
                }

                String move = String.format("%c%d", 'A' + row, col + 1);
                if (!isBotTurn && Reversi.getValidMoves(board, currentPlayer).contains(move)) {
                    // Soft translucent highlight color for hint layers
                    cell.setFill(createPieceFill(Color.web("#27ae60"), Color.web("rgba(231, 76, 60, 0.45)")));
                }

                final int r = row, c = col;
                cell.setOnMouseClicked(e -> {
                    if (!isBotTurn && Reversi.isValidMove(board, r, c, currentPlayer)) {
                        Reversi.makeMove(board, r, c, currentPlayer);
                        updateScores();
                        switchPlayer();
                    }
                });

                boardGrid.add(cell, col, row);
            }
        }

        if (isBotTurn) {
            handleBotMove();
        }
    }

    private RadialGradient createPieceFill(Color baseColor, Color pieceColor) {
        return new RadialGradient(
                0, 0.1, 0.5, 0.5, 0.85, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, pieceColor),
                new Stop(0.5, pieceColor),
                new Stop(0.55, pieceColor.darker()), // Smooth outer rim shading illusion
                new Stop(0.62, baseColor),
                new Stop(1, baseColor)
        );
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        currentPlayerLabel.setText("Turn: " + (currentPlayer == BLACK ? "Black (⚫)" : "White (⚪)"));
        updateBoard();
    }

    private void updateScores() {
        int blackScore = 0, whiteScore = 0;
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == BLACK) blackScore++;
                else if (cell == WHITE) whiteScore++;
            }
        }
        scoreLabel.setText(String.format("Black: %d | White: %d", blackScore, whiteScore));
    }

    private void resetGame() {
        Reversi.initializeBoard(board);
        currentPlayer = BLACK;
        updateScores();
        currentPlayerLabel.setText("Turn: Black (⚫)");
        updateBoard();
    }

    private void showGameOver() {
        int blackCount = 0, whiteCount = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == BLACK) blackCount++;
                if (board[row][col] == WHITE) whiteCount++;
            }
        }

        String winner = (blackCount > whiteCount) ? "Black Wins!" : (whiteCount > blackCount) ? "White Wins!" : "Draw Match!";
        String modeText = (bot == null) ? "Human vs Human" : "vs " + bot.getClass().getSimpleName();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tournament Results");
        alert.setHeaderText(winner);
        alert.setContentText(String.format("Mode: %s\n\nFinal Score Matrix:\nBlack Discs: %d\nWhite Discs: %d", modeText, blackCount, whiteCount));

        ButtonType restart = new ButtonType("Play Again");
        ButtonType menu = new ButtonType("Exit to Menu");
        alert.getButtonTypes().setAll(restart, menu);

        alert.showAndWait().ifPresent(response -> {
            if (response == restart) resetGame();
            else if (response == menu) goToMainMenu();
        });
    }

    private void handleBotMove() {
        if (bot == null) return;

        Alert thinkingDialog = new Alert(Alert.AlertType.INFORMATION);
        thinkingDialog.setTitle("Engine Processing");
        thinkingDialog.setHeaderText(null);
        thinkingDialog.setContentText("Calculating tactical branches...");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        VBox dialogContent = new VBox(progressIndicator);
        dialogContent.setAlignment(Pos.CENTER);
        thinkingDialog.getDialogPane().setContent(dialogContent);
        thinkingDialog.show();

        PauseTransition pause = new PauseTransition(Duration.seconds(1.2)); // Snappier delay loop
        pause.setOnFinished(event -> {
            thinkingDialog.close();
            int[] botMove = bot.getBotMove(board, WHITE);
            if (botMove != null) {
                Reversi.makeMove(board, botMove[0], botMove[1], WHITE);
                updateScores();
                switchPlayer();
            }
        });
        pause.play();
    }

    private void showHelp() {
        String helpMessage = "REVERSI RULES & CONTROLS:\n\n" +
                "• Capture strategy relies on trapping opponent discs between your pieces on straight linear paths.\n" +
                "• Trapped opponent elements flip over immediately to matches your current active profile color.\n" +
                "• Target high-value coordinates like corners to anchor structural pieces safely.\n\n" +
                "New to Reversi? Watch this video tutorial online:\nhttps://www.youtube.com/watch?v=4XdyAZhzJW8";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rule Book");
        alert.setHeaderText("How to Play Reversi");
        alert.setContentText(helpMessage);
        alert.showAndWait();
    }

    private enum GameMode {
        HUMAN_VS_HUMAN,
        HUMAN_VS_DUMB_BOT,
        HUMAN_VS_MAPLE_BOT,
        HUMAN_VS_CASTELLA_BOT,
        HUMAN_VS_MOMIJI_MANJU_BOT,
        HUMAN_VS_TAIYAKI_BOT,
        HUMAN_VS_ANMITSU_BOT
    }
}
