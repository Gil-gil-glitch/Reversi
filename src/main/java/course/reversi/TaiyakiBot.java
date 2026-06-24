package course.reversi;

import java.util.List;

public class TaiyakiBot extends SimpleBot {

    private static final int MAX_DEPTH = 6; // look 6 steps ahead
    private static final char BLACK = '⚫';
    private static final char WHITE = '⚪';


    // Heuristic Matrix
    private static final int[][] BOARD_VALUE_MATRIX = {
            {100, -20,  10,   5,   5,  10, -20, 100},
            {-20, -50,  -2,  -2,  -2,  -2, -50, -20},
            { 10,  -2,   5,   1,   1,   5,  -2,  10},
            {  5,  -2,   1,   1,   1,   1,  -2,   5},
            {  5,  -2,   1,   1,   1,   1,  -2,   5},
            { 10,  -2,   5,   1,   1,   5,  -2,  10},
            {-20, -50,  -2,  -2,  -2,  -2, -50, -20},
            {100, -20,  10,   5,   5,  10, -20, 100}
    };

    @Override
    public int[] getBotMove(char[][] board, char player) {

        List<String> validMoves = Reversi.getValidMoves(board, player);
        if (validMoves.isEmpty()) return null;

        String bestMove = validMoves.get(0);
        int bestScore = Integer.MIN_VALUE;


        return convertMove(bestMove);
    }

    private int alphaBeta(char[][] board, int depth, int alpha, int beta, boolean isMaximizing, char botPlayer){

        char opponent = (botPlayer == BLACK) ? WHITE : BLACK;
        char currentPlayer = isMaximizing ? botPlayer : opponent;

        List<String> validMoves = Reversi.getValidMoves(board, currentPlayer);

        // Base case: leaf node or game over
        if (depth == 0 || validMoves.isEmpty()){
            return evaluateBoard(board, botPlayer);
        }
    }

    private int evaluateBoard(char[][] board, char botPlayer) {
        char opponent = (botPlayer == BLACK) ? WHITE : BLACK;
        int score = 0;

        // Positional Matrix Weighting

        for (int r = 0; r < 8; r++){

            for (int c = 0; c < 8; c++){

                if (board[r][c] == botPlayer) score += BOARD_VALUE_MATRIX[r][c];
                else if (board[r][c] == opponent) score -= BOARD_VALUE_MATRIX[r][c];

            }
        }

        // Mobility Advantage for Mid-Game
        int botMoves = Reversi.getValidMoves(board, botPlayer).size();
        int oppMoves = Reversi.getValidMoves(board, opponent).size();
        score += (botMoves - oppMoves) * 15;

        return score;
    }
}
