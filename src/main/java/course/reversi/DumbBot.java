package course.reversi;

/*


    Description:

    A basic strategic bot. It prioritizes corner positions (A1, A8, H1, H8) due to their high value.
    If no corner is available, it makes a random move.

 */


import java.util.List;
import java.util.Random;

public class DumbBot extends SimpleBot {


    @Override
    // defines its own strategy for getBotMove
    public int[] getBotMove(char[][] board, char player) {
        // get valid moves
        List<String> validMoves = Reversi.getValidMoves(board, player);

        if (validMoves.isEmpty()) {
            return null; // No valid moves
        }

        // goes for corners: A1, A8, H1, and H8.
        //for (String move : validMoves) {
        //    if (move.equals("A1") || move.equals("A8") || move.equals("H1") || move.equals("H8")) {
        //        return convertMove(move);
        //    }
        //}

        // if not possible, choose a random piece
        String move = validMoves.get((int) (Math.random() * validMoves.size()));
        return convertMove(move);
    }
}
