package course.reversi;

import java.util.List;

public class MomijiManjuBot extends SimpleBot {

    private static final int[][] POSITION_WEIGHTS = {
            {120, -20, 20, 5, 5, 20, -20, 120},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {5, -5, 3, 0, 0, 3, -5, 5},
            {5, -5, 3, 0, 0, 3, -5, 5},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {-20, -40, -5, -5, -5, -5, -40, -20},
            {120, -20, 20, 5, 5, 20, -20, 120}
    };

    private static final int SEARCH_DEPTH = 3; // You can increase for stronger play (3–5)

    @Override
    public int[] getBotMove(char[][] board, char player) {
        List<String> validMoves = Reversi.getValidMoves(board, player);
        if (validMoves.isEmpty()) {
            return null;
        }

        int bestScore = Integer.MIN_VALUE;
        String bestMove = validMoves.get(0);

        for (String move : validMoves) {
            int[] coords = convertMove(move);
            char[][] simulated = Reversi.copyBoard(board);
            Reversi.makeMove(simulated, coords[0], coords[1], player);

            int score = minimax(simulated, SEARCH_DEPTH - 1, false, player, Reversi.getOpponent(player), Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return convertMove(bestMove);
    }

    private int minimax(char[][] board, int depth, boolean maximizing, char player, char opponent, int alpha, int beta) {
        if (depth == 0) {
            return evaluateBoard(board, player);
        }

        List<String> validMoves = Reversi.getValidMoves(board, maximizing ? player : opponent);
        if (validMoves.isEmpty()) {
            return evaluateBoard(board, player);
        }

        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (String move : validMoves) {
            int[] coords = convertMove(move);
            char[][] simulated = Reversi.copyBoard(board);
            Reversi.makeMove(simulated, coords[0], coords[1], maximizing ? player : opponent);

            int score = minimax(simulated, depth - 1, !maximizing, player, opponent, alpha, beta);

            if (maximizing) {
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, score);
            } else {
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, score);
            }

            if (beta <= alpha) break; // Alpha-beta pruning
        }

        return bestScore;
    }

    private int evaluateBoard(char[][] board, char player) {
        char opponent = Reversi.getOpponent(player);
        int playerCount = Reversi.countPieces(board, player);
        int opponentCount = Reversi.countPieces(board, opponent);

        int mobility = Reversi.getValidMoves(board, player).size() - Reversi.getValidMoves(board, opponent).size();
        int positionScore = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == player) positionScore += POSITION_WEIGHTS[i][j];
                else if (board[i][j] == opponent) positionScore -= POSITION_WEIGHTS[i][j];
            }
        }

        // Weighted evaluation
        return (10 * (playerCount - opponentCount)) + (5 * mobility) + positionScore;
    }
}