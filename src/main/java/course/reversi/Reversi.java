package course.reversi;
/*



    Content:

    Hello!

    This is where all the code relevant to the game mechanics is defined.

 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Reversi {

    static List<String> moveHistory = new ArrayList<>(); // list that will be used to store turn History

    // game and board elements and characteristics
    static final char EMPTY = ' ';
    static final char BLACK = '⚫';
    static final char WHITE = '⚪';
    static final int SIZE = 8;


    // this function's purpose is setting up the initial board
    public static void initializeBoard(char[][] board)
    {
        // ensures that every square of the board is empty
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
            {
                board[i][j] = EMPTY;
            }
        }

        // puts the starting pieces in the four middle most squares
        board[3][3] = WHITE; // D4
        board[3][4] = BLACK; // E4
        board[4][3] = BLACK; // D5
        board[4][4] = WHITE; // E5
    }


    /*

        This function prints out the Board in a way easy to read for the players. Note that the format chosen is
        letter-number. Letters representing the vertical axis (rows) and numbers representing the horizontal
        axis (columns).
    */
    public static void printBoard(char[][] board) {

        // print top horizontal labels with padding to help fix spacing
        System.out.print("   "); // space for the row labels

        // horizontal number labels
        for (int col = 0; col < SIZE; col++)
        {
            System.out.print("  " + (col + 1) + "  "); // add spaces around numbers
        }
        System.out.println();

        // vertical letter labels
        for (int row = 0; row < SIZE; row++)
        {
            System.out.print((char) ('A' + row) + " "); // row labels: A, B, C, etc.
            for (int col = 0; col < SIZE; col++)
            {
                char cell = board[row][col];
                // makes sure that  cells are padded evenly
                if (cell == BLACK || cell == WHITE)
                {
                    System.out.print(" [" + cell + "] ");
                } else {
                    System.out.print(" [ " + cell + " ] ");
                }
            }
            System.out.println(); // move to the next row
        }
    }

    public static char[][] copyBoard(char[][] board) {
        char[][] newBoard = new char[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, board[i].length);
        }
        return newBoard;
    }

    public static char getOpponent(char player) {
        return (player == BLACK) ? WHITE : BLACK;
    }


    /*

        This function is responsible for making sure a move is valid. A move is only valid if placing a piece at a
        given position flips at least one of the opponent's pieces.
    */
    public static boolean isValidMove(char[][] board, int row, int col, char player) {

        // 1. checking if the cell is empty
        if (board[row][col] != EMPTY)
        {
            return false; // move is allowed only if there is an empty space
        }

        // 2. identifying if there is an opponent piece
        char opponent = (player == BLACK) ? WHITE : BLACK;
        boolean isValid = false;

        /*  3. define all 8 possible directions for movement

                Here are the 8 possible directions as referred:
                    Direction 0: Up-Left (-1, -1)
                    Direction 1: Up (-1, 0)
                    Direction 2: Up-Right (-1, +1)
                    Direction 3: Right (0, +1)
                    Direction 4: Down-Right (+1, +1)
                    Direction 5: Down (+1, 0)
                    Direction 6: Down-Left (+1, -1)
                    Direction 7: Left (0, -1)

         */
        int[] directionsX = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] directionsY = {-1, 0, 1, 1, 1, 0, -1, -1};


        // check each direction
        for (int dir = 0; dir < 8; dir++)
        {
            int x = row + directionsX[dir];
            int y = col + directionsY[dir];
            boolean foundOpponent = false;

            while (x >= 0 && x < SIZE && y >= 0 && y < SIZE)
            {
                if (board[x][y] == opponent)
                {
                    foundOpponent = true; // found at least one enemy piece
                } else if (board[x][y] == player) {
                    if (foundOpponent)
                    {
                        isValid = true; // sandwich is valid
                    }
                    break;
                } else {
                    break;
                }

                x += directionsX[dir];  // increments the position for x
                y += directionsY[dir]; //increments the position for y
            }
        }

        return isValid; // return that the move is valid
    }

    public static int countPieces(char[][] board, char player) {
        int count = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == player) {
                    count++;
                }
            }
        }
        return count;
    }



    // this function displays what moves are valid for a player
    public static List<String> getValidMoves(char[][] board, char player) {

        List<String> validMoves = new ArrayList<>();


        // traverse through the rows to find a valid move
        for (int row = 0; row < SIZE; row++) {
            // and traverse through the columns to find a valid move
            for (int col = 0; col < SIZE; col++) {

                // if there is a validMove, return it
                if (isValidMove(board, row, col, player))
                {
                    // convert row and col to LetterNumber format (e.g., D3)
                    char letter = (char) ('A' + row);
                    int number = col + 1;
                    validMoves.add(letter + "" + number);
                }
            }
        }

        System.out.println("Valid moves for player " + player + ": " + validMoves);
        return validMoves; // return valid Moves
    }

    // this function is responsible for applying the validMoves to the Board
    public static void makeMove(char[][] board, int row, int col, char player)
    {
        // Add the move to the history. Convert to letter-number format first.
        char moveLetter = (char) ('A' + row);
        int moveNumber = col + 1;
        moveHistory.add(player + ": " + moveLetter + moveNumber);

        // 1. identify the opponent's piece
        char opponent = (player == BLACK) ? WHITE : BLACK;

        // 2. define all 8 possible directions for flipping. Same directions as shown in validMoves
        int[] dx = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};

        // 3. put the player's piece on the board
        board[row][col] = player;

        // 4. check each direction for a valid sandwich
        for (int dir = 0; dir < 8; dir++)
        {
            int x = row + dx[dir];
            int y = col + dy[dir];
            boolean hasOpponent = false;

            // moving in the current direction
            while (x >= 0 && x < SIZE && y >= 0 && y < SIZE && board[x][y] == opponent) { // make sure that pieces are only with the board boundaries

                hasOpponent = true; // found at least one opponent piece
                x += dx[dir];
                y += dy[dir];
            }

            // flip the enemy's piece if enemy found
            if (hasOpponent && x >= 0 && x < SIZE && y >= 0 && y < SIZE && board[x][y] == player) {
                //backtrack to lip pieces
                x -= dx[dir];
                y -= dy[dir];
                while (x != row || y != col) {
                    board[x][y] = player; // flip the pieces
                    x -= dx[dir];
                    y -= dy[dir];
                }
            }
        }
    }

    // saves the turns to a txtfile
    public static void saveMovesToFile(String gameMode) {
        try {
            // Get the current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = dateFormat.format(new Date());

            // Format game mode to remove spaces and special characters for filename safety
            String safeGameMode = gameMode.replaceAll("[^a-zA-Z0-9]", "_");

            // Construct filename: "Human_vs_MapleBot_2025-01-31_14-30-45.txt"
            String filename = safeGameMode + "_" + timestamp + ".txt";

            // Create and write to the file
            FileWriter writer = new FileWriter(filename);

            // Write the game mode at the top
            writer.write("Game Mode: " + gameMode + "\n\n");

            //if ReversiGameFX.ga
            // writing each move to the file
            for (String move : moveHistory) {
                writer.write(move + "\n");
            }

            writer.close();
            System.out.println("Move history saved to " + filename);

            // catch block to check for an error while saving
        } catch (IOException e) {
            System.err.println("An error occurred while saving the move history: " + e.getMessage());
        }
    }


    // this function just determines the winner of the game
    public static String determineWinner(char[][] board) {
        int blackCount = 0;
        int whiteCount = 0;


        // count the pieces for both players
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
            {
                if (board[i][j] == BLACK) blackCount++;
                else if (board[i][j] == WHITE) whiteCount++;
            }
        }

        // print the final scores
        System.out.println("Game Over!");
        System.out.println("Black (⚫): " + blackCount);
        System.out.println("White (⚪): " + whiteCount);


        // determine the winner
        if (blackCount > whiteCount) System.out.println("⚫ wins!");
        else if (whiteCount > blackCount) System.out.println("⚪ wins!");
        else System.out.println("It's a tie!");


        return null;
    }
}