package course.reversi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BotTournament {

    private static final int TOTAL_GAMES = 5000;
    private static final int SIZE = 8;
    private static final char BLACK = '⚫';
    private static final char WHITE = '⚪';

    public static void main(String[] args) {
        int taiyakiWins = 0;
        int opponentWins = 0;
        int ties = 0;

        System.out.println("Starting Headless Tournament: TaiyakiBot vs MapleBot (" + TOTAL_GAMES + " games)...");
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= TOTAL_GAMES; i++) {

            // Alternate colors to ensure fairness: Taiyaki plays Black on odd games, White on even games
            boolean taiyakiIsBlack = (i % 2 != 0);

            SimpleBot blackBot = taiyakiIsBlack ? new TaiyakiBot() : new MapleBot(); // Swap with CastellaBot or MomijiManjuBot
            SimpleBot whiteBot = taiyakiIsBlack ? new MapleBot() : new TaiyakiBot();

            GameResult result = runSingleHeadlessGame(blackBot, whiteBot);

            // Determine the winner
            char winnerColor = result.winner;
            if (winnerColor == 'T') {
                ties++;

            } else if ((winnerColor == BLACK && taiyakiIsBlack) || (winnerColor == WHITE && !taiyakiIsBlack)) {
                taiyakiWins++;

            } else {
                opponentWins++;
                // Save the steps since an under-dog bot won!
                saveDefeatLog(i, taiyakiIsBlack ? "TaiyakiBot(B)" : "TaiyakiBot(W)", result.moveHistory, result.blackScore, result.whiteScore);
            }

            if (i % 1000 == 0) {
                System.out.printf("Processed %d/%d games... Current Taiyaki Record: %d Wins\n", i, TOTAL_GAMES, taiyakiWins);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("====================================================");
        System.out.println("\n TOURNAMENT RESULTS");
        System.out.printf("Total Execution Time: %.2f seconds\n", (endTime - startTime) / 1000.0);
        System.out.printf("TaiyakiBot Wins : %d (%.2f%%)\n", taiyakiWins, (taiyakiWins * 100.0 / TOTAL_GAMES));
        System.out.printf("Opponent Wins   : %d (%.2f%%)\n", opponentWins, (opponentWins * 100.0 / TOTAL_GAMES));
        System.out.printf("Ties            : %d\n", ties);
        System.out.println("====================================================");
    }

    private static GameResult runSingleHeadlessGame(SimpleBot blackBot, SimpleBot whiteBot) {
        char[][] board = new char[SIZE][SIZE];
        Reversi.initializeBoard(board);

        char currentPlayer = BLACK;
        List<String> moveHistory = new ArrayList<>();

        while (true) {
            List<String> validMoves = Reversi.getValidMoves(board, currentPlayer);

            if (validMoves.isEmpty()) {

                char opponent = (currentPlayer == BLACK) ? WHITE : BLACK;

                if (Reversi.getValidMoves(board, opponent).isEmpty()) {

                    break; // Game Over: Neither player can move

                }

                currentPlayer = opponent; // Pass turn
                continue;
            }

            SimpleBot ActiveBot = (currentPlayer == BLACK) ? blackBot : whiteBot;
            int[] move = ActiveBot.getBotMove(board, currentPlayer);

            if (move != null) {

                // Convert coordinates to standard notation (e.g., A1, H8) for human-readable logs
                String notation = String.format("%c%d", 'A' + move[0], move[1] + 1);
                moveHistory.add((currentPlayer == BLACK ? "Black " : "White ") + notation);

                Reversi.makeMove(board, move[0], move[1], currentPlayer);
            }

            currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        }

        // Count the scores for Black and White. Then determine the winner
        int blackCount = Reversi.countPieces(board, BLACK);
        int whiteCount = Reversi.countPieces(board, WHITE);
        char winner = (blackCount > whiteCount) ? BLACK : (whiteCount > blackCount) ? WHITE : 'T';

        return new GameResult(winner, blackCount, whiteCount, moveHistory);
    }

    private static void saveDefeatLog(int gameNumber, String taiyakiRole, List<String> history, int blackScore, int whiteScore) {
        // Only saves the Sequence for when Taiyaki is defeated
        String filename = "taiyaki_defeat_game_" + gameNumber + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            writer.write("Match Context: " + taiyakiRole + "\n");
            writer.write(String.format("Final Score - Black: %d | White: %d\n", blackScore, whiteScore));
            writer.write("---------------------------------------------\n");
            writer.write("Move Sequence:\n");

            for (int step = 0; step < history.size(); step++) {

                writer.write(String.format("Step %02d: %s\n", step + 1, history.get(step)));

            }

        } catch (IOException e) {
            System.err.println("Failed to write game log: " + e.getMessage());
        }
    }

    // Small helper structural class to wrap headless output data
    private static class GameResult {
        char winner;
        int blackScore;
        int whiteScore;
        List<String> moveHistory;

        GameResult(char winner, int blackScore, int whiteScore, List<String> moveHistory) {
            this.winner = winner;
            this.blackScore = blackScore;
            this.whiteScore = whiteScore;
            this.moveHistory = moveHistory;
        }
    }
}
