package course.reversi;

/*


    2600248450

    Content:

    Out of the two bots (so far), DumbBot is the easiest to play against.... (I would like to think it is). Easy in the
    sense that DumbBot literally just iterates through its possible moves and using Java's Random library, picks
    a random move.


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
        for (String move : validMoves) {
            if (move.equals("A1") || move.equals("A8") || move.equals("H1") || move.equals("H8")) {
                return convertMove(move);
            }
        }

        // if not possible, choose a random piece
        String move = validMoves.get((int) (Math.random() * validMoves.size()));
        return convertMove(move);
    }
}
