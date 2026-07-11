package course.reversi;

/*
    Content:
    ReversiGameFX is a JavaFX implementation of Reversi/Othello. Originally developed
    as the final project for my programming language class, this game now includes
    several bots with different strategies that the user can play against,
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

    // Theme state persistence toggle
    private boolean isClassicMode = false;

    // --- REUSABLE UI STYLE SHEET CONSTANTS ---
    private static final String APP_BACKGROUND = "-fx-background-color: #2c3e50;";
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

        // Background application behavior based on selected style engine state
        if (isClassicMode) {
            Color shogiWood = Color.rgb(222, 184, 135);
            menuRoot.setBackground(Background.fill(shogiWood));
        } else {
            menuRoot.setStyle(APP_BACKGROUND);
        }

        Label titleLabel = new Label("REVERSI ARENA");
        if (isClassicMode) {
            titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        } else {
            titleLabel.setStyle(TEXT_PRIMARY + "-fx-font-size: 32px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");
        }

        VBox cardContainer = new VBox(15);
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setPadding(new Insets(30, 40, 30, 40));
        cardContainer.setMaxWidth(400);

        if (!isClassicMode) {
            cardContainer.setStyle(CARD_BACKGROUND);
        }

        Label selectLabel = new Label(isClassicMode ? "Choose Your Opponent:" : "Select Battle Variant");
        selectLabel.setStyle(isClassicMode ? "-fx-font-size: 14px; -fx-font-weight: 500;" : TEXT_DARK + "-fx-font-size: 15px; -fx-font-weight: bold;");

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

        if (isClassicMode) {
            modeSelector.setStyle("-fx-font-size: 14px; -fx-pref-width: 320px;");
        } else {
            modeSelector.setStyle(COMBO_BOX_STYLE);
            modeSelector.setPrefWidth(320);
        }

        // Classic Mode Interface Switch Option
        CheckBox classicToggle = new CheckBox("Classic Mode");
        classicToggle.setSelected(isClassicMode);
        if (isClassicMode) {
            classicToggle.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        classicToggle.setOnAction(e -> {
            isClassicMode = classicToggle.isSelected();
            goToMainMenu(); // Re-trigger application draw sequence to clear styles cleanly
        });

        Button playButton = new Button(isClassicMode ? "Launch Game" : "LAUNCH MATCH");
        if (isClassicMode) {
            playButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-pref-width: 180px;");
        } else {
            playButton.setStyle(BTN_PRIMARY + "-fx-font-size: 15px; -fx-pref-width: 200px;");
        }

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

        cardContainer.getChildren().addAll(selectLabel, modeSelector, classicToggle, playButton);

        Button helpButton = new Button(isClassicMode ? "Help & Rules" : "Game Manual & Rules");
        if (isClassicMode) {
            helpButton.setPrefWidth(120);
        } else {
            helpButton.setStyle(BTN_SECONDARY);
        }
        helpButton.setOnAction(e -> showHelp());

        menuRoot.getChildren().addAll(titleLabel, cardContainer, helpButton);

        Scene menuScene = new Scene(menuRoot, 600, 450);
        primaryStage.setTitle(isClassicMode ? "Reversi Game" : "Reversi Game Arena");
        primaryStage.setResizable(false);
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void showBotPreviewStage(GameMode gameMode) {
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.initOwner(primaryStage);
        previewStage.setTitle(isClassicMode ? "Opponent Profile Evaluation" : "Opponent Blueprint Analysis");
        previewStage.setResizable(false);

        VBox layout = new VBox(isClassicMode ? 15 : 20);
        layout.setPadding(new Insets(isClassicMode ? 20 : 25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: " + (isClassicMode ? "#f5f6fa;" : "#f8f9fa;"));

        String name, difficulty, strategy, wins, losses, hexColor;

        switch (gameMode) {
            case HUMAN_VS_DUMB_BOT:
                name = "DumbBot"; difficulty = "★☆☆☆☆ (Easy)";
                strategy = isClassicMode ? "Executes pure pseudo-random valid moves without positional analysis." : "Executes arbitrary valid moves across the board space without tactical analysis.";
                wins = "12"; losses = "2"; hexColor = "#7f8c8d";
                break;
            case HUMAN_VS_MAPLE_BOT:
                name = "MapleBot"; difficulty = "★★☆☆☆ (Normal)";
                strategy = isClassicMode ? "Prioritizes immediate raw piece count gains per turn." : "Implements a basic greedy approach optimized to maximize immediate disc capture.";
                wins = "8"; losses = "5"; hexColor = "#e67e22";
                break;
            case HUMAN_VS_CASTELLA_BOT:
                name = "CastellaBot"; difficulty = "★★★☆☆ (Medium)";
                strategy = isClassicMode ? "Utilizes a static heuristic grid weighting evaluation to balance board positions." : "Utilizes a static position matrix grid weighting evaluation to target edges and corners.";
                wins = "6"; losses = "9"; hexColor = "#f1c40f";
                break;
            case HUMAN_VS_MOMIJI_MANJU_BOT:
                name = "MomijiManjuBot"; difficulty = "★★★☆☆ (Advanced)";
                strategy = isClassicMode ? "Mid-range tree lookahead focusing heavily on mobility restriction strategies." : "Mid-range search tree configuration focused on aggressive opponent mobility restriction.";
                wins = "3"; losses = "11"; hexColor = "#e74c3c";
                break;
            case HUMAN_VS_TAIYAKI_BOT:
                name = "TaiyakiBot"; difficulty = "★★★★☆ (Expert)";
                strategy = isClassicMode ? "Deploys a 6-Depth Alpha-Beta Minimax search tree shifting to raw piece sweeps at late game thresholds." : "Runs an alpha-beta pruned minimax look-ahead tree that transitions to point sweeps during endgame steps.";
                wins = "1"; losses = "14"; hexColor = "#d35400";
                break;
            case HUMAN_VS_ANMITSU_BOT:
                name = "AnmitsuBot"; difficulty = "★★★★★ (Master)";
                strategy = isClassicMode ? "Utilizes 10 distinct board feature extractions across specialized Opening, Midgame, and Endgame weight layers." : "Tracks 10 distinct topological feature properties evaluated across separate opening, mid, and late game layers.";
                wins = "0"; losses = "20"; hexColor = "#8e44ad";
                break;
            default:
                return;
        }

        // Card Avatar Construction Matrix
        Rectangle imagePlaceholder = new Rectangle(isClassicMode ? 120 : 100, isClassicMode ? 120 : 100);
        imagePlaceholder.setArcWidth(15);
        imagePlaceholder.setArcHeight(15);
        imagePlaceholder.setFill(Color.web(hexColor));
        Label avatarLabel = new Label(name.substring(0, 2).toUpperCase());
        avatarLabel.setStyle("-fx-font-size: " + (isClassicMode ? "28px;" : "32px;") + " -fx-font-weight: bold; -fx-text-fill: white;");
        GridPane imageStack = new GridPane();
        imageStack.setAlignment(Pos.CENTER);
        imageStack.add(imagePlaceholder, 0, 0);
        imageStack.add(avatarLabel, 0, 0);

        Label titleLabel = new Label(name);
        titleLabel.setStyle(isClassicMode ? "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;" : TEXT_DARK + "-fx-font-size: 24px; -fx-font-weight: bold;");

        Label diffLabel = new Label((isClassicMode ? "Difficulty Level: " : "Threat Level: ") + difficulty);
        diffLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;" + (isClassicMode ? "" : " -fx-font-size: 14px;"));

        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.CENTER);
        Label stratHeader = new Label(isClassicMode ? "Tactical Strategy Profile:" : "Tactical Methodology:");
        stratHeader.setStyle("-fx-font-weight: bold;" + (isClassicMode ? "" : " -fx-text-fill: #7f8c8d;"));
        Label stratLabel = new Label(strategy);
        stratLabel.setWrapText(true);
        stratLabel.setStyle(isClassicMode ? "-fx-alignment: center; -fx-text-alignment: center;" : TEXT_DARK + "-fx-text-alignment: center; -fx-font-size: 13px;");
        detailsBox.getChildren().addAll(stratHeader, stratLabel);

        Label recordLabel = new Label(String.format(isClassicMode ? "Historical Analytics Record -> Wins: %s | Losses: %s" : "Historical Records Card » Wins: %s  |  Losses: %s", wins, losses));
        if (isClassicMode) {
            recordLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #2980b9;");
        } else {
            recordLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-background-color: #e8f4f8; -fx-padding: 6 12 6 12; -fx-background-radius: 4px;");
        }

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button startMatchButton = new Button(isClassicMode ? "Battle Bot" : "BATTLE ENGINE");
        if (isClassicMode) {
            startMatchButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20;");
        } else {
            startMatchButton.setStyle(BTN_PRIMARY);
        }
        startMatchButton.setOnAction(e -> {
            previewStage.close();
            startGame(gameMode);
        });

        Button backButton = new Button(isClassicMode ? "Return" : "CANCEL");
        if (isClassicMode) {
            backButton.setStyle("-fx-padding: 8 20 8 20;");
        } else {
            backButton.setStyle(BTN_SECONDARY);
        }
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

        root = new VBox(isClassicMode ? 10 : 20);
        root.setAlignment(Pos.CENTER);

        if (!isClassicMode) {
            root.setPadding(new Insets(20));
            root.setStyle(APP_BACKGROUND);
        }

        scoreLabel = new Label("Black: 0 | White: 0");
        currentPlayerLabel = new Label("Current Player: Black");

        HBox scorePanel = new HBox(40);
        scorePanel.setAlignment(Pos.CENTER);

        if (isClassicMode) {
            scoreLabel.setStyle("-fx-font-size: 16px;");
            currentPlayerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            scorePanel.getChildren().addAll(scoreLabel, currentPlayerLabel);
        } else {
            scorePanel.setPadding(new Insets(12));
            scorePanel.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8px;");
            scoreLabel.setStyle(TEXT_PRIMARY + "-fx-font-size: 16px; -fx-font-weight: bold;");
            currentPlayerLabel.setStyle(TEXT_PRIMARY + "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");
            scorePanel.getChildren().addAll(scoreLabel, currentPlayerLabel);
        }

        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        if (!isClassicMode) {
            boardGrid.setStyle("-fx-background-color: #1e272e; -fx-padding: 8px; -fx-background-radius: 10px;");
        }
        updateBoard();

        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        Button homeButton = new Button(isClassicMode ? "Home" : "Main Menu");
        Button resetButton = new Button(isClassicMode ? "Reset Game" : "Reset Board");
        Button helpButton = new Button("Help");

        if (!isClassicMode) {
            homeButton.setStyle(BTN_SECONDARY);
            resetButton.setStyle(BTN_DANGER);
            helpButton.setStyle(BTN_SECONDARY);
        }

        homeButton.setOnAction(e -> goToMainMenu());
        resetButton.setOnAction(e -> resetGame());
        helpButton.setOnAction(e -> showHelp());

        controls.getChildren().addAll(homeButton, resetButton, helpButton);

        if (isClassicMode) {
            root.getChildren().addAll(scoreLabel, currentPlayerLabel, boardGrid, controls);
        } else {
            root.getChildren().addAll(scorePanel, boardGrid, controls);
        }

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
                if (isClassicMode) System.out.println("No moves for " + currentPlayer + ". Skipping turn...");
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

                if (isClassicMode) {
                    cell.setFill(Color.SPRINGGREEN);
                    cell.setStroke(Color.BLACK);
                    if (board[row][col] == BLACK || board[row][col] == WHITE) {
                        cell.setFill(createPieceFill(Color.SPRINGGREEN, board[row][col] == BLACK ? Color.BLACK : Color.WHITE));
                    }
                } else {
                    cell.setFill(Color.web("#27ae60"));
                    cell.setStroke(Color.web("#1e272e"));
                    cell.setStrokeWidth(1.5);
                    if (board[row][col] == BLACK || board[row][col] == WHITE) {
                        cell.setFill(createPieceFill(Color.web("#27ae60"), board[row][col] == BLACK ? Color.web("#2c3e50") : Color.web("#f5f6fa")));
                    }
                }

                String move = String.format("%c%d", 'A' + row, col + 1);
                if (!isBotTurn && Reversi.getValidMoves(board, currentPlayer).contains(move)) {
                    if (isClassicMode) {
                        cell.setFill(createPieceFill(Color.SPRINGGREEN, Color.PALEVIOLETRED));
                    } else {
                        cell.setFill(createPieceFill(Color.web("#27ae60"), Color.web("rgba(231, 76, 60, 0.45)")));
                    }
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
        if (isClassicMode) {
            return new RadialGradient(
                    0, 0.1, 0.5, 0.5, 0.9, true,
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, pieceColor),
                    new Stop(0.4, pieceColor),
                    new Stop(0.5, baseColor),
                    new Stop(1, baseColor)
            );
        } else {
            return new RadialGradient(
                    0, 0.1, 0.5, 0.5, 0.85, true,
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new Stop(0, pieceColor),
                    new Stop(0.5, pieceColor),
                    new Stop(0.55, pieceColor.darker()),
                    new Stop(0.62, baseColor),
                    new Stop(1, baseColor)
            );
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        if (isClassicMode) {
            currentPlayerLabel.setText("Current Player: " + (currentPlayer == BLACK ? "Black" : "White"));
        } else {
            currentPlayerLabel.setText("Turn: " + (currentPlayer == BLACK ? "Black (⚫)" : "White (⚪)"));
        }
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
        if (isClassicMode) System.out.println("Resetting game...");
        Reversi.initializeBoard(board);
        currentPlayer = BLACK;
        updateScores();
        if (isClassicMode) {
            currentPlayerLabel.setText("Current Player: Black");
            System.out.println("Game reset successfully.");
        } else {
            currentPlayerLabel.setText("Turn: Black (⚫)");
        }
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

        String winner, modeText;
        if (isClassicMode) {
            winner = (blackCount > whiteCount) ? "Black (⚫) wins!" : (whiteCount > blackCount) ? "White (⚪) wins!" : "It's a tie!";
            modeText = (bot == null) ? "Human vs Human" :
                    (bot instanceof DumbBot) ? "Human vs DumbBot" :
                            (bot instanceof MapleBot) ? "Human vs MapleBot" :
                                    (bot instanceof CastellaBot) ? "Human vs CastellaBot":
                                            (bot instanceof MomijiManjuBot) ? "Human vs MomijiManjuBot":
                                                    (bot instanceof TaiyakiBot) ? "Human vs TaiyakiBot" : "Human vs AnmitsuBot";

            String playerBlack = "Human";
            String playerWhite = (bot == null) ? "Human2" : bot.getClass().getSimpleName();
            Reversi.saveMovesToFile(modeText, playerBlack, playerWhite);
        } else {
            winner = (blackCount > whiteCount) ? "Black Wins!" : (whiteCount > blackCount) ? "White Wins!" : "Draw Match!";
            modeText = (bot == null) ? "Human vs Human" : "vs " + bot.getClass().getSimpleName();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (isClassicMode) {
            alert.setTitle("Game Over");
            alert.setHeaderText("Game Over - " + modeText);
            alert.setContentText("Final Score:\nBlack: " + blackCount + " | White: " + whiteCount + "\n\n" + winner);
        } else {
            alert.setTitle("Tournament Results");
            alert.setHeaderText(winner);
            alert.setContentText(String.format("Mode: %s\n\nFinal Score Matrix:\nBlack Discs: %d\nWhite Discs: %d", modeText, blackCount, whiteCount));
        }

        ButtonType restart = new ButtonType(isClassicMode ? "Restart" : "Play Again");
        ButtonType menu = new ButtonType(isClassicMode ? "Main Menu" : "Exit to Menu");
        alert.getButtonTypes().setAll(restart, menu);

        alert.showAndWait().ifPresent(response -> {
            if (response == restart) resetGame();
            else if (response == menu) goToMainMenu();
        });
    }

    private void handleBotMove() {
        if (bot == null) return;

        Alert thinkingDialog = new Alert(Alert.AlertType.INFORMATION);
        thinkingDialog.setTitle(isClassicMode ? "Bot Thinking..." : "Engine Processing");
        thinkingDialog.setHeaderText(null);
        thinkingDialog.setContentText(isClassicMode ? "The bot is thinking..." : "Calculating tactical branches...");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        VBox dialogContent = new VBox(progressIndicator);
        dialogContent.setAlignment(Pos.CENTER);
        thinkingDialog.getDialogPane().setContent(dialogContent);
        thinkingDialog.show();

        PauseTransition pause = new PauseTransition(Duration.seconds(isClassicMode ? 2.0 : 1.2));
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
        String helpMessage;
        if (isClassicMode) {
            helpMessage = "REVERSI GAME HELP:\n\n" +
                    "1. ||Human v. Human||: Two human players take turns placing their pieces (⚫ or ⚪) on the board.\n" +
                    "2. ||Human v. DumbBot||: A human player competes against a bot that makes random moves (EASY).\n" +
                    "3. ||Human v. MapleBot||: A human player competes against with a basic strategy (MEDIUM).\n" +
                    "4. ||Human v. CastellaBot||: A human player competes against with a smart strategy (Medium Difficulty.\n\n" +
                    "GAMEPLAY RULES:\n" +
                    "- The objective is to have the most pieces of your color on the board at the end of the game.\n" +
                    "- Players take turns placing their pieces on the board, flipping opponent's pieces.\n" +
                    "- A valid move must surround one or more of the opponent's pieces with the player's own pieces.\n" +
                    "If you never played Reversi before, please check this video out:\n https://www.youtube.com/watch?v=4XdyAZhzJW8";
        } else {
            helpMessage = "REVERSI RULES & CONTROLS:\n\n" +
                    "• Capture strategy relies on trapping opponent discs between your pieces on straight linear paths.\n" +
                    "• Trapped opponent elements flip over immediately to matches your current active profile color.\n" +
                    "• Target high-value coordinates like corners to anchor structural pieces safely.\n\n" +
                    "New to Reversi? Watch this video tutorial online:\nhttps://www.youtube.com/watch?v=4XdyAZhzJW8";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(isClassicMode ? "Game Help" : "Rule Book");
        alert.setHeaderText(isClassicMode ? "How to Play Reversi" : "How to Play Reversi");
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