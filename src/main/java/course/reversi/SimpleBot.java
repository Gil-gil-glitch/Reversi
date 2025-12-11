package course.reversi;


/*




    Content:

    SimpleBot is an abstract class that serves as a blueprint for the other two classes.
    SimpleBot has a function getBotMove that varies from bot to bot according to the
    bot's strategy.


 */
public abstract class SimpleBot extends Reversi{
    // abstract method that each bot must implement
    public abstract int[] getBotMove(char[][] board, char player);

    // method to convert a move to row/col indices (shared by all bots)
    protected int[] convertMove(String move) {
        int row = move.charAt(0) - 'A';
        int col = move.charAt(1) - '1';
        return new int[] { row, col };
    }

    public static class MomijiManjuBot {
    }
}