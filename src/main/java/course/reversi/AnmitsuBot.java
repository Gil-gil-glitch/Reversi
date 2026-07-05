package course.reversi;

import java.util.List;

public class AnmitsuBot extends SimpleBot {

    public static final int NUM_FEATURES = 10;

    // Three separate weight layers for the three stages of the game
    private double[] openingWeights = new double[NUM_FEATURES];
    private double[] midgameWeights = new double[NUM_FEATURES];
    private double[] endgameWeights = new double[NUM_FEATURES];

    public AnmitsuBot() {
        setDefaultWeights();
    }

    public enum GamePhase {
        OPENING, MIDGAME, ENDGAME
    }

    public GamePhase getGamePhase(char[][] board) {

        int totalPieces = Reversi.countPieces(board, '⚫') + Reversi.countPieces(board, '⚪');

        if (totalPieces <= 20) return GamePhase.OPENING;
        if (totalPieces <= 48) return GamePhase.MIDGAME;
        return GamePhase.ENDGAME;
    }

    private double[] getWeightsForPhase(GamePhase phase) {

        switch (phase) {
            case OPENING: return openingWeights;
            case MIDGAME: return midgameWeights;
            case ENDGAME: return endgameWeights;
            default: return midgameWeights;
        }
    }

    @Override
    public int[] getBotMove(char[][] board, char player) {

        List<String> validMoves = Reversi.getValidMoves(board, player);
        if (validMoves.isEmpty()) return null;

        double bestValue = Double.NEGATIVE_INFINITY;
        String bestMove = validMoves.get(0);
        GamePhase currentPhase = getGamePhase(board);
        double[] weights = getWeightsForPhase(currentPhase);

        for (String move : validMoves) {
            int[] coords = convertMove(move);
            char[][] simulated = Reversi.copyBoard(board);
            Reversi.makeMove(simulated, coords[0], coords[1], player);

            double[] features = extractFeatures(simulated, player);
            double value = estimateValue(features, weights);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        return convertMove(bestMove);
    }

    public double estimateValue(double[] features, double[] weights) {
        double value = 0;
        for (int i = 0; i < weights.length; i++) {
            value += weights[i] * features[i];
        }
        return value;
    }

    public double[] extractFeatures(char[][] board, char player) {
        double[] features = new double[NUM_FEATURES];
        char opponent = Reversi.getOpponent(player);

        // F0: Piece parity (Raw score advantage)
        features[0] = (double)(Reversi.countPieces(board, player) - Reversi.countPieces(board, opponent)) / 64.0;
        // F1: Mobility (Available moves advantage)
        features[1] = (double)(Reversi.getValidMoves(board, player).size() - Reversi.getValidMoves(board, opponent).size()) / 64.0;
        // F2: Corners owned advantage
        features[2] = getCornerScore(board, player) / 4.0;
        // F3: Frontier discs (Fewer is better to avoid opening up the board)
        features[3] = (double)(countFrontierDiscs(board, opponent) - countFrontierDiscs(board, player)) / 64.0;

        // Danger Zones: F4-F7 penalize C and X squares near corners unless owned
        features[4] = getDangerZonePenalty(board, player, 1, 1, 0, 0); // Top-Left X
        features[5] = getDangerZonePenalty(board, player, 0, 1, 0, 0); // Top-Left C
        features[6] = getDangerZonePenalty(board, player, 6, 6, 7, 7); // Bottom-Right X
        features[7] = getDangerZonePenalty(board, player, 7, 6, 7, 7); // Bottom-Right C

        // F8 & F9: Edge stability tracking
        features[8] = (double)countEdgePieces(board, player) / 24.0;
        features[9] = (double)countEdgePieces(board, opponent) / 24.0;

        return features;
    }

    private double getCornerScore(char[][] board, char player) {
        char opponent = Reversi.getOpponent(player);
        int score = 0;
        int[][] corners = {{0, 0}, {0, 7}, {7, 0}, {7, 7}};
        for(int[] c : corners) {
            if (board[c[0]][c[1]] == player) score++;
            else if (board[c[0]][c[1]] == opponent) score--;
        }
        return score;
    }

    private double getDangerZonePenalty(char[][] board, char player, int r, int c, int cornerR, int cornerC) {
        char opponent = Reversi.getOpponent(player);
        if (board[cornerR][cornerC] != Reversi.EMPTY) return 0.0; // Corner taken, safe to step
        if (board[r][c] == player) return -1.0;
        if (board[r][c] == opponent) return 1.0;
        return 0.0;
    }

    private int countFrontierDiscs(char[][] board, char player) {
        int frontier = 0;
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == player) {
                    for (int i = 0; i < 8; i++) {
                        int nr = r + dr[i], nc = c + dc[i];
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

    private int countEdgePieces(char[][] board, char player) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (board[0][i] == player) count++;
            if (board[7][i] == player) count++;
            if (board[i][0] == player) count++;
            if (board[i][7] == player) count++;
        }
        return count;
    }

    private void setDefaultWeights() {

        // Opening Layer Vector:
        // [ 0.0934  0.3409  0.0048 -0.0383 -0.0345 -0.2866 -0.0239 -0.1037  0.2126
        // -0.0176]
        //Midgame Layer Vector:
        // [ 0.0527  0.5433 -0.1128 -0.1304  0.0832 -0.0405 -0.1147  0.0445  0.365
        //  0.0156]
        //Endgame Layer Vector:
        // [ 0.3201  0.3722  0.1433  0.0428  0.4225 -0.4183 -0.3915  0.2548  0.047
        //  0.086 ]
        //
        // Adjust these weights when training is complete
        this.openingWeights = new double[]{0.0934,  0.3409,  0.0048, -0.0383, -0.0345, -0.2866, -0.0239, -0.1037,  0.2126, -0.017};
        this.midgameWeights = new double[]{0.0934,  0.3409,  0.0048, -0.0383, -0.0345, -0.2866, -0.0239, -0.1037,  0.2126, -0.017};
        this.endgameWeights = new double[]{0.3201,  0.3722,  0.1433,  0.0428,  0.4225, -0.4183, -0.3915,  0.2548,  0.047, 0.086 };
    }

    public void setAllWeights(double[] opening, double[] midgame, double[] endgame) {
        if (opening.length == NUM_FEATURES) this.openingWeights = opening.clone();
        if (midgame.length == NUM_FEATURES) this.midgameWeights = midgame.clone();
        if (endgame.length == NUM_FEATURES) this.endgameWeights = endgame.clone();
    }
}