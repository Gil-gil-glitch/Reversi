package course.reversi;

/*

    Description:
    MomijiManjuBot is a Reversi (Othello) agent trained using a simple reinforcement learning
    algorithm based on Temporal-Difference (TD) learning with linear function approximation.

 */
import java.util.List;

public class MomijiManjuBot extends SimpleBot {

    private static final int NUM_FEATURES = 10;
    private double[] weights = new double[NUM_FEATURES];


    public MomijiManjuBot() {
        loadTrainedWeights();
    }

    // --- Move Selection ---
    @Override
    public int[] getBotMove(char[][] board, char player) {
        List<String> validMoves = Reversi.getValidMoves(board, player);
        if (validMoves.isEmpty()) {
            return null;
        }

        double bestValue = Double.NEGATIVE_INFINITY;
        String bestMove = validMoves.get(0);

        // Find the move that maximizes the estimated value of the resulting state V(S')
        for (String move : validMoves) {
            int[] coords = convertMove(move);
            char[][] simulated = Reversi.copyBoard(board);
            Reversi.makeMove(simulated, coords[0], coords[1], player);

            double[] features = extractFeatures(simulated, player);

            double value = estimateValue(features, this.weights);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return convertMove(bestMove);
    }

    // --- Value Estimation (The Linear Function) ---
    public double estimateValue(double[] features, double[] weights) {
        double value = 0;
        for (int i = 0; i < weights.length; i++) {
            value += weights[i] * features[i];
        }
        return value;
    }

    // --- Feature Extraction ---
    /**
     * Converts a board state into a feature vector f(S).
     */
    public double[] extractFeatures(char[][] board, char player) {
        double[] features = new double[NUM_FEATURES];
        char opponent = Reversi.getOpponent(player);

        features[0] = (double)(Reversi.countPieces(board, player) - Reversi.countPieces(board, opponent)) / 64.0;

        features[1] = (double)(Reversi.getValidMoves(board, player).size() - Reversi.getValidMoves(board, opponent).size()) / 64.0;

        features[2] = getCornerScore(board, player) / 4.0;

        int playerFrontier = countFrontierDiscs(board, player);
        int opponentFrontier = countFrontierDiscs(board, opponent);
        features[3] = (double)(opponentFrontier - playerFrontier) / 64.0;

        features[4] = board[1][1] == player ? 1.0 : (board[1][1] == opponent ? -1.0 : 0.0);
        features[5] = board[1][2] == player ? 1.0 : (board[1][2] == opponent ? -1.0 : 0.0);

        return features;
    }

    // --- Utility Methods ---

    private double getCornerScore(char[][] board, char player) {
        char opponent = Reversi.getOpponent(player);
        int score = 0;
        int[][] corners = {{0, 0}, {0, 7}, {7, 0}, {7, 7}};
        for(int[] c : corners) {
            if (board[c[0]][c[1]] == player) score++;
            else if (board[c[0]][c[1]] == opponent) score--;
        }
        return (double)score;
    }

    private int countFrontierDiscs(char[][] board, char player) {
        int frontier = 0;
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == player) {
                    for (int i = 0; i < 8; i++) {
                        int nr = r + dr[i];
                        int nc = c + dc[i];

                        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8 && board[nr][nc] == Reversi.EMPTY) {
                            frontier++;
                            break;
                        }
                    }
                }
            }
        }
        return frontier;
    }

    // Placeholder to load the weights learned by the trainer
    private void loadTrainedWeights() {
        // These are just a guess. The trainer will calculate the best values.
        this.weights = new double[]{
                0.50,
                1.50,
                4.00,
                -2.00,
                0.1, 0.1, 0.0, 0.0, 0.0, 0.0
        };
    }

    public void setWeights(double[] newWeights) {
        if (newWeights.length == NUM_FEATURES) {
            this.weights = newWeights;
        }
    }
}