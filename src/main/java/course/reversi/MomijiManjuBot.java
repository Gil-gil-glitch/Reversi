package course.reversi;

import java.util.List;

public class MomijiManjuBot extends SimpleBot {

    private static final int[][] POSITION_WEIGHTS = {
            {120, -30, 20, 5, 5, 20, -30, 120},
            {-30, -50, -5, -5, -5, -5, -50, -30},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {5, -5, 3, 0, 0, 3, -5, 5},
            {5, -5, 3, 0, 0, 3, -5, 5},
            {20, -5, 15, 3, 3, 15, -5, 20},
            {-30, -50, -5, -5, -5, -5, -50, -30},
            {120, -30, 20, 5, 5, 20, -30, 120}
    };

    private static final int SEARCH_DEPTH = 4;


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

            if (beta <= alpha) break;
        }

        return bestScore;
    }



    private int evaluateBoard(char[][] board, char player) {
        char opponent = Reversi.getOpponent(player);

        int pieceCountPlayer = Reversi.countPieces(board, player);
        int pieceCountOpponent = Reversi.countPieces(board, opponent);
        int pieceScore = 10 * (pieceCountPlayer - pieceCountOpponent);

        int playerMoves = Reversi.getValidMoves(board, player).size();
        int opponentMoves = Reversi.getValidMoves(board, opponent).size();
        int mobilityScore = 80 * (playerMoves - opponentMoves);

        int positionScore = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == player) positionScore += POSITION_WEIGHTS[i][j];
                else if (board[i][j] == opponent) positionScore -= POSITION_WEIGHTS[i][j];
            }
        }

        int playerFrontier = countFrontierDiscs(board, player);
        int opponentFrontier = countFrontierDiscs(board, opponent);
        int frontierScore = -50 * (playerFrontier - opponentFrontier);
        return pieceScore + mobilityScore + positionScore + frontierScore;
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


}