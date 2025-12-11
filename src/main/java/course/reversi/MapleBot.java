package course.reversi;
/*


    Content:

    Out of the two bots (so far), MapleBot is the harder to play against.... (I would like to think it is). MapleBot
    simply goes for corners, however, if not possible, then it will go for a random move.


 */
import java.util.List;

public class MapleBot extends SimpleBot {

    private static final int[][] POSITION_WEIGHTS = {
            {100, -20, 10, 5, 5, 10, -20, 100},
            {-20, -50, -2, -2, -2, -2, -50, -20},
            {10, -2, 2, 2, 2, 2, -2, 10},
            {5, -2, 2, 0, 0, 2, -2, 5},
            {5, -2, 2, 0, 0, 2, -2, 5},
            {10, -2, 2, 2, 2, 2, -2, 10},
            {-20, -50, -2, -2, -2, -2, -50, -20},
            {100, -20, 10, 5, 5, 10, -20, 100}
    };

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

            int moveScore = evaluateBoard(simulated, player, coords);
            if (moveScore > bestScore) {
                bestScore = moveScore;
                bestMove = move;
            }
        }

        return convertMove(bestMove);
    }

    private int evaluateBoard(char[][] board, char player, int[] move) {
        int positional = POSITION_WEIGHTS[move[0]][move[1]];
        int pieceCount = Reversi.countPieces(board, player);
        return pieceCount + positional;
    }
}