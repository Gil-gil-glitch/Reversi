package course.reversi;

/*

 Description:
 Trains the MomijiManjuBot using a simple Temporal-Difference (TD(0))
 reinforcement learning algorithm with linear function approximation.
 The trainer plays repeated games of Reversi against increasingly
 strong opponents (curriculum learning), updates feature weights
 using the TD error, and returns the learned weight vector.

*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TDBotTrainer {

    // --- Learning Hyperparameters ---
    private static final double ALPHA = 0.01;      // Learning Rate
    private static final int NUM_FEATURES = 10;

    private static final int EPOCHS_PER_STAGE = 10000;
    private static final int TOTAL_EPOCHS = EPOCHS_PER_STAGE * 3;

    private double[] weights = new double[NUM_FEATURES];
    private MomijiManjuBot botAgent;

    // We use SimpleBot as the type so we can assign DumbBot, MapleBot, or CastellaBot
    private SimpleBot opponentAgent;
    private Random random = new Random();

    private static final int SIZE = 8;
    private static final char BLACK = '⚫';
    private static final char WHITE = '⚪';

    public TDBotTrainer() {

        for (int i = 0; i < NUM_FEATURES; i++) {
            this.weights[i] = (random.nextDouble() * 0.2) - 0.1;
        }

        this.botAgent = new MomijiManjuBot();
        this.botAgent.setWeights(this.weights);

        this.opponentAgent = new DumbBot(); // Default initialization
    }


    public double[] train() {
        System.out.println("Starting TD Learning Training with Curriculum Learning...");

        opponentAgent = new DumbBot();
        System.out.println("\n--- Stage 1: Training against DumbBot for " + EPOCHS_PER_STAGE + " epochs ---");
        runTrainingStage("DumbBot", 0);

        //opponentAgent = new DumbBot();
        //System.out.println("\n--- Stage 2: Training against DumbBot for " + EPOCHS_PER_STAGE + " epochs ---");
        //runTrainingStage("MapleBot", EPOCHS_PER_STAGE);

        //opponentAgent = new DumbBot();
        //System.out.println("\n--- Stage 3: Training against DumbBot for " + EPOCHS_PER_STAGE + " epochs ---");
        //runTrainingStage("CastellaBot", EPOCHS_PER_STAGE * 2);

        System.out.println("\nTraining finished. Final Weights: " + Arrays.toString(weights));
        return this.weights;
    }

    /**
     * Helper method to run a specific stage of training.
     * @param opponentName The name of the opponent for display.
     * @param startEpoch The global epoch count to start from.
     */
    private void runTrainingStage(String opponentName, int startEpoch) {

        for (int i = 0; i < EPOCHS_PER_STAGE; i++) {
            playSingleGameAndLearn();

            int currentGlobalEpoch = startEpoch + i + 1;

            if (currentGlobalEpoch % 1000 == 0) {
                System.out.printf("Epoch %d/%d (vs %s): Weight 0 is now %.4f\n",
                        currentGlobalEpoch, TOTAL_EPOCHS, opponentName, weights[0]);
            }
        }
    }


    private void playSingleGameAndLearn() {

        char[][] board = new char[SIZE][SIZE];
        Reversi.initializeBoard(board);

        char player1 = BLACK;
        char player2 = WHITE;
        char currentPlayer = player1;

        // Note: featureHistory only records states before P1's moves
        List<double[]> featureHistory = new ArrayList<>();

        // Loop continues as long as at least one player can make a move
        while (canAnyPlayerMove(board)) {

            int[] move = null;
            List<String> validMoves = Reversi.getValidMoves(board, currentPlayer);

            if (!validMoves.isEmpty()) {

                if (currentPlayer == player1) {
                    // --- TD Agent's Turn (Learning Player) ---

                    // 1. Record features before move (State S)
                    double[] featuresS = botAgent.extractFeatures(board, player1);
                    featureHistory.add(featuresS);

                    // 2. Select move using Epsilon-Greedy
                    move = getEpsilonGreedyMove(board, player1);

                } else {
                    // --- Opponent's Turn (Current Opponent) ---
                    // The opponentAgent instance is set in the train() method
                    move = opponentAgent.getBotMove(board, player2);
                }

                // 3. Execute Move
                if (move != null) {
                    Reversi.makeMove(board, move[0], move[1], currentPlayer);
                }
            }

            // 4. Switch Player
            currentPlayer = Reversi.getOpponent(currentPlayer);
        }

        // --- Learning Phase: Back-up from the Final Reward ---
        int p1Count = Reversi.countPieces(board, player1);
        int p2Count = Reversi.countPieces(board, player2);

        double finalReward = (p1Count > p2Count) ? 1.0 : (p1Count < p2Count) ? -1.0 : 0.0;

        double targetValue = finalReward;

        // Iterate backward through the game history
        for (int i = featureHistory.size() - 1; i >= 0; i--) {
            double[] featuresS = featureHistory.get(i);

            double valueS = botAgent.estimateValue(featuresS, weights);

            double tdError = targetValue - valueS;

            for (int j = 0; j < NUM_FEATURES; j++) {
                weights[j] += ALPHA * tdError * featuresS[j];
            }

            targetValue = valueS;
        }

        // Update the agent with the new weights
        botAgent.setWeights(this.weights);
    }

    private boolean canAnyPlayerMove(char[][] board) {
        return !Reversi.getValidMoves(board, BLACK).isEmpty() || !Reversi.getValidMoves(board, WHITE).isEmpty();
    }

    private int[] getEpsilonGreedyMove(char[][] board, char player) {
        List<String> validMoves = Reversi.getValidMoves(board, player);
        if (validMoves.isEmpty()) return null;

        double epsilon = 0.1; // 10% chance to explore (make a random move)

        if (random.nextDouble() < epsilon) {
            // Exploration: Choose a random valid move
            String randomMove = validMoves.get(random.nextInt(validMoves.size()));
            return opponentAgent.convertMove(randomMove); // Use the opponent's convertMove method
        } else {
            // Exploitation: Choose the best move based on current V(S') estimates
            return botAgent.getBotMove(board, player);
        }
    }
}