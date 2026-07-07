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

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        VBox menuRoot = new VBox(15);
        menuRoot.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Reversi Game Arena");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label selectLabel = new Label("Choose Your Opponent:");
        selectLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500;");

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
        modeSelector.setStyle("-fx-font-size: 14px; -fx-pref-width: 320px;");

        Button playButton = new Button("Launch Game");
        playButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-pref-width: 180px;");

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

        Button helpButton = new Button("Help & Rules");
        helpButton.setPrefWidth(120);
        helpButton.setOnAction(e -> showHelp());

        menuRoot.getChildren().addAll(titleLabel, selectLabel, modeSelector, playButton, helpButton);

        Color shogiWood = Color.rgb(222, 184, 135);
        menuRoot.setBackground(Background.fill(shogiWood));

        Scene menuScene = new Scene(menuRoot, 600, 400);
        primaryStage.setTitle("Reversi Game");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    /**
     * Spawns a modal profile view stage describing the chosen artificial opponent.
     */
    private void showBotPreviewStage(GameMode gameMode) {
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.initOwner(primaryStage);
        previewStage.setTitle("Opponent Profile Evaluation");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f5f6fa;");

        // Profile structural variables
        String name, difficulty, strategy, wins, losses, hexColor;

        switch (gameMode) {
            case HUMAN_VS_DUMB_BOT:
                name = "DumbBot"; difficulty = "★☆☆☆☆ (Easy)";
                strategy = "Executes pure pseudo-random valid moves without positional analysis.";
                wins = "12"; losses = "2"; hexColor = "#7f8c8d";
                break;
            case HUMAN_VS_MAPLE_BOT:
                name = "MapleBot"; difficulty = "★★☆☆☆ (Normal)";
                strategy = "Prioritizes immediate raw piece count gains per turn.";
                wins = "8"; losses = "5"; hexColor = "#e67e22";
                break;
            case HUMAN_VS_CASTELLA_BOT:
                name = "CastellaBot"; difficulty = "★★★☆☆ (Medium)";
                strategy = "Utilizes a static heuristic grid weighting evaluation to balance board positions.";
                wins = "6"; losses = "9"; hexColor = "#f1c40f";
                break;
            case HUMAN_VS_MOMIJI_MANJU_BOT:
                name = "MomijiManjuBot"; difficulty = "★★★☆☆ (Advanced)";
                strategy = "Mid-range tree lookahead focusing heavily on mobility restriction strategies.";
                wins = "3"; losses = "11"; hexColor = "#e74c3c";
                break;
            case HUMAN_VS_TAIYAKI_BOT:
                name = "TaiyakiBot"; difficulty = "★★★★☆ (Expert)";
                strategy = "Deploys a 6-Depth Alpha-Beta Minimax search tree shifting to raw piece sweeps at late game thresholds.";
                wins = "1"; losses = "14"; hexColor = "#d35400";
                break;
            case HUMAN_VS_ANMITSU_BOT:
                name = "AnmitsuBot"; difficulty = "★★★★★ (Master)";
                strategy = "Utilizes 10 distinct board feature extractions across specialized Opening, Midgame, and Endgame weight layers.";
                wins = "0"; losses = "20"; hexColor = "#8e44ad";
                break;
            default:
                return;
        }

        // Graphical Image Placeholder Profile Box
        Rectangle imagePlaceholder = new Rectangle(120, 120);
        imagePlaceholder.setArcWidth(15);
        imagePlaceholder.setArcHeight(15);
        imagePlaceholder.setFill(Color.web(hexColor));
        Label avatarLabel = new Label(name.substring(0, 2).toUpperCase());
        avatarLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        GridPane imageStack = new GridPane();
        imageStack.setAlignment(Pos.CENTER);
        imageStack.add(imagePlaceholder, 0, 0);
        imageStack.add(avatarLabel, 0, 0);

        // UI text data bindings
        Label titleLabel = new Label(name);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label diffLabel = new Label("Difficulty Level: " + difficulty);
        diffLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;");

        Label stratHeader = new Label("Tactical Strategy Profile:");
        stratHeader.setStyle("-fx-font-weight: bold;");
        Label stratLabel = new Label(strategy);
        stratLabel.setWrapText(true);
        stratLabel.setStyle("-fx-alignment: center; -fx-text-alignment: center;");

        Label recordLabel = new Label(String.format("Historical Analytics Record -> Wins: %s | Losses: %s", wins, losses));
        recordLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #2980b9;");

        // Operational Actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button startMatchButton = new Button("Battle Bot");
        startMatchButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20;");
        startMatchButton.setOnAction(e -> {
            previewStage.close();
            startGame(gameMode);
        });

        Button backButton = new Button("Return");
        backButton.setStyle("-fx-padding: 8 20 8 20;");
        backButton.setOnAction(e -> previewStage.close());

        actions.getChildren().addAll(backButton, startMatchButton);
        layout.getChildren().addAll(imageStack, titleLabel, diffLabel, stratHeader, stratLabel, recordLabel, actions);

        Scene scene = new Scene(layout, 420, 480);
        previewStage.setScene(scene);
        previewStage.showAndWait();
    }

    private void goToMainMenu() {
        start(primaryStage);
    }

    private void startGame(GameMode gameMode) {
        Reversi.initializeBoard(board);
        currentPlayer = BLACK;

        root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        updateBoard();

        scoreLabel = new Label("Black: 0 | White: 0");
        scoreLabel.setStyle("-fx-font-size: 16px;");

        currentPlayerLabel = new Label("Current Player: Black");
        currentPlayerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Button homeButton = new Button("Home");
        Button resetButton = new Button("Reset Game");
        Button helpButton = new Button("Help");

        homeButton.setOnAction(e -> goToMainMenu());
        resetButton.setOnAction(e -> resetGame());
        helpButton.setOnAction(e -> showHelp());

        controls.getChildren().addAll(homeButton, resetButton, helpButton);
        root.getChildren().addAll(scoreLabel, currentPlayerLabel, boardGrid, controls);

        Scene gameScene = new Scene(root, 600, 700);
        primaryStage.setScene(gameScene);
        primaryStage.show();

        if (gameMode == GameMode.HUMAN_VS_DUMB_BOT) {
            bot = new DumbBot();
        } else if (gameMode == GameMode.HUMAN_VS_MAPLE_BOT) {
            bot = new MapleBot();
        } else if (gameMode == GameMode.HUMAN_VS_CASTELLA_BOT){
            bot = new CastellaBot();
        } else if (gameMode == GameMode.HUMAN_VS_MOMIJI_MANJU_BOT) {
            bot = new MomijiManjuBot();
        } else if (gameMode == GameMode.HUMAN_VS_TAIYAKI_BOT) {
            bot = new TaiyakiBot();
        } else if (gameMode == GameMode.HUMAN_VS_ANMITSU_BOT){
            bot = new AnmitsuBot();
        } else {
            bot = null;
        }
    }

    private void showHelp() {
        String helpMessage = "REVERSI GAME HELP:\n\n" +
                "1. ||Human v. Human||: Two human players take turns placing their pieces (⚫ or ⚪) on the board.\n" +
                "2. ||Human v. DumbBot||: A human player competes against a bot that makes random moves (EASY).\n" +
                "3. ||Human v. MapleBot||: A human player competes against with a basic strategy (MEDIUM).\n" +
                "4. ||Human v. CastellaBot||: A human player competes against with a smart strategy (Medium Difficulty.\n\n" +
                "GAMEPLAY RULES:\n" +
                "- The objective is to have the most pieces of your color on the board at the end of the game.\n" +
                "- Players take turns placing their pieces on the board, flipping opponent's pieces.\n" +
                "- A valid move must surround one or more of the opponent's pieces with the player's own pieces.\n" +
                "If you never played Reversi before, please check this video out:\n https://www.youtube.com/watch?v=4XdyAZhzJW8";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Help");
        alert.setHeaderText("How to Play Reversi");
        alert.setContentText(helpMessage);
        alert.showAndWait();
    }

    private void updateBoard() {
        List<String> currentValidMoves = Reversi.getValidMoves(board, currentPlayer);

        if (currentValidMoves.isEmpty()) {
            char opponent = (currentPlayer == BLACK) ? WHITE : BLACK;
            if (Reversi.getValidMoves(board, opponent).isEmpty()) {
                javafx.application.Platform.runLater(() -> showGameOver());
                return;
            } else {
                System.out.println("No moves for " + currentPlayer + ". Skipping turn...");
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
                cell.setFill(Color.SPRINGGREEN);
                cell.setStroke(Color.BLACK);

                if (board[row][col] == BLACK || board[row][col] == WHITE) {
                    cell.setFill(createPieceFill(Color.SPRINGGREEN, board[row][col] == BLACK ? Color.BLACK : Color.WHITE));
                }

                String move = String.format("%c%d", 'A' + row, col + 1);
                if (!isBotTurn && Reversi.getValidMoves(board, currentPlayer).contains(move)) {
                    cell.setFill(createPieceFill(Color.SPRINGGREEN, Color.PALEVIOLETRED));
                }

                final int r = row, c = col;
                cell.setOnMouseClicked(e -> {
                    if (!isBotTurn) {
                        if (Reversi.isValidMove(board, r, c, currentPlayer)) {
                            Reversi.makeMove(board, r, c, currentPlayer);
                            switchPlayer();
                        }
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
                0, 0.1, 0.5, 0.5, 0.9, true,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, pieceColor),
                new Stop(0.4, pieceColor),
                new Stop(0.5, baseColor),
                new Stop(1, baseColor)
        );
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        currentPlayerLabel.setText("Current Player: " + (currentPlayer == BLACK ? "Black" : "White"));
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
        System.out.println("Resetting game...");
        Reversi.initializeBoard(board);
        currentPlayer = BLACK;
        updateBoard();
        updateScores();
        currentPlayerLabel.setText("Current Player: Black");
        System.out.println("Game reset successfully.");
    }

    private void showGameOver() {
        int blackCount = 0, whiteCount = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == BLACK) blackCount++;
                if (board[row][col] == WHITE) whiteCount++;
            }
        }

        String winner = (blackCount > whiteCount) ? "Black (⚫) wins!" : (whiteCount > blackCount) ? "White (⚪) wins!" : "It's a tie!";
        String modeText = (bot == null) ? "Human vs Human" :
                (bot instanceof DumbBot) ? "Human vs DumbBot" :
                        (bot instanceof MapleBot) ? "Human vs MapleBot" :
                                (bot instanceof CastellaBot) ? "Human vs CastellaBot":
                                        (bot instanceof MomijiManjuBot) ? "Human vs MomijiManjuBot":
                                                (bot instanceof TaiyakiBot) ? "Human vs TaiyakiBot" : "Human vs AnmitsuBot";

        String playerBlack = "Human";
        String playerWhite = (bot == null) ? "Human2" : bot.getClass().getSimpleName();

        Reversi.saveMovesToFile(modeText, playerBlack, playerWhite);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over - " + modeText);
        alert.setContentText("Final Score:\nBlack: " + blackCount + " | White: " + whiteCount + "\n\n" + winner);

        ButtonType restart = new ButtonType("Restart");
        ButtonType menu = new ButtonType("Main Menu");
        alert.getButtonTypes().setAll(restart, menu);

        alert.showAndWait().ifPresent(response -> {
            if (response == restart) {
                resetGame();
            } else if (response == menu) {
                goToMainMenu();
            }
        });
    }

    private void handleBotMove() {
        if (bot == null) return;

        Alert thinkingDialog = new Alert(Alert.AlertType.INFORMATION);
        thinkingDialog.setTitle("Bot Thinking...");
        thinkingDialog.setHeaderText(null);
        thinkingDialog.setContentText("The bot is thinking...");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        VBox dialogContent = new VBox(progressIndicator);
        dialogContent.setAlignment(Pos.CENTER);
        thinkingDialog.getDialogPane().setContent(dialogContent);

        thinkingDialog.show();
        List<String> validMoves = Reversi.getValidMoves(board, WHITE);
        if (validMoves.isEmpty()) {
            switchPlayer();
            return;
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
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