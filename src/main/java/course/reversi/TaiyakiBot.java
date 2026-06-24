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


}
