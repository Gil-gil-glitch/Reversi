package course.reversi;
/*


    Content:

    Out of the two bots (so far), MapleBot is the harder to play against.... (I would like to think it is). MapleBot
    simply goes for corners, however, if not possible, then it will go for a random move.


 */
import java.util.List;

public class MapleBot extends SimpleBot {

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