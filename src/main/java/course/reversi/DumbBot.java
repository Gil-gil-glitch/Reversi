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
        List<String> validMoves = getValidMoves(board, player);

        if (validMoves.isEmpty()) {
            return null; // No valid moves
        }

        // pick a random move...not very smart hahaha
        Random random = new Random();

        String move = validMoves.get(random.nextInt(validMoves.size()));

        // convert move to row/col indices
        return convertMove(move);
    }
}
