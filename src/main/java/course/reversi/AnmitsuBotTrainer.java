package course.reversi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AnmitsuBotTrainer {

    private static final double ALPHA = 0.01;
    private static final int EPOCHS_PER_STAGE = 200;

    private double[] oWeights = new double[AnmitsuBot.NUM_FEATURES];
    private double[] mWeights = new double[AnmitsuBot.NUM_FEATURES];
    private double[] eWeights = new double[AnmitsuBot.NUM_FEATURES];

    private AnmitsuBot botAgent;
    private SimpleBot opponentAgent;
    private Random random = new Random();

    public AnmitsuBotTrainer() {
        // Initialize weights randomly
        for (int i = 0; i < AnmitsuBot.NUM_FEATURES; i++) {
            oWeights[i] = (random.nextDouble() * 0.2) - 0.1;
            mWeights[i] = (random.nextDouble() * 0.2) - 0.1;
            eWeights[i] = (random.nextDouble() * 0.2) - 0.1;
        }
        botAgent = new AnmitsuBot();
        botAgent.setAllWeights(oWeights, mWeights, eWeights);
    }

    public void train() {
        System.out.println("Starting AnmitsuBot Phase-Dependent Training...");

        // Stage 1: Beat the random bot
        opponentAgent = new DumbBot();
        runTrainingStage("DumbBot");

        // Stage 2: Beat the structural bots
        opponentAgent = new CastellaBot();
        runTrainingStage("CastellaBot");

        // Stage 3: Self-play against MomijiManjuBot
        opponentAgent = new MomijiManjuBot();
        runTrainingStage("MomijiManjuBot");

        // Stage 4: High-level refinement against TaiyakiBot (Look-ahead)
        opponentAgent = new TaiyakiBot();
        runTrainingStage("TaiyakiBot");

        System.out.println("\nTraining Finished!");
        System.out.println("Final Opening Weights: " + Arrays.toString(oWeights));
        System.out.println("Final Midgame Weights: " + Arrays.toString(mWeights));
        System.out.println("Final Endgame Weights: " + Arrays.toString(eWeights));
    }

    private void runTrainingStage(String opponentName) {
        for (int i = 0; i < EPOCHS_PER_STAGE; i++) {
            playSingleGameAndLearn();
        }
        System.out.println("Completed stage against " + opponentName);
    }

    private void playSingleGameAndLearn() {
        char[][] board = new char[8][8];
        Reversi.initializeBoard(board);

        char player1 = '⚫';
        char player2 = '⚪';
        char currentPlayer = player1;

        List<HistoricalState> history = new ArrayList<>();

        while (!Reversi.getValidMoves(board, '⚫').isEmpty() || !Reversi.getValidMoves(board, '⚪').isEmpty()) {
            List<String> validMoves = Reversi.getValidMoves(board, currentPlayer);

            if (!validMoves.isEmpty()) {
                int[] move;
                if (currentPlayer == player1) {
                    // Record state, phase context, and feature values before moving
                    AnmitsuBot.GamePhase phase = botAgent.getGamePhase(board);
                    double[] features = botAgent.extractFeatures(board, player1);
                    history.add(new HistoricalState(features, phase));

                    // Epsilon-Greedy Exploration
                    if (random.nextDouble() < 0.1) {
                        String rMove = validMoves.get(random.nextInt(validMoves.size()));
                        move = botAgent.convertMove(rMove);
                    } else {
                        move = botAgent.getBotMove(board, player1);
                    }
                } else {
                    move = opponentAgent.getBotMove(board, player2);
                }

                if (move != null) Reversi.makeMove(board, move[0], move[1], currentPlayer);
            }
            currentPlayer = Reversi.getOpponent(currentPlayer);
        }

        // Back-propagation phase
        // NEW DENSE REWARD: Normalizes the score difference between -1.0 and +1.0
        int p1Count = Reversi.countPieces(board, player1);
        int p2Count = Reversi.countPieces(board, player2);

        double targetValue = (double)(p1Count - p2Count) / 64.0;

        for (int i = history.size() - 1; i >= 0; i--) {
            HistoricalState state = history.get(i);
            double[] activeWeights = getTargetWeightArray(state.phase);

            double valueS = botAgent.estimateValue(state.features, activeWeights);
            double tdError = targetValue - valueS;

            // Gradient descent step exclusively on the active layer
            for (int j = 0; j < AnmitsuBot.NUM_FEATURES; j++) {
                activeWeights[j] += ALPHA * tdError * state.features[j];
            }
            targetValue = valueS;
        }

        botAgent.setAllWeights(oWeights, mWeights, eWeights);
    }

    private double[] getTargetWeightArray(AnmitsuBot.GamePhase phase) {
        if (phase == AnmitsuBot.GamePhase.OPENING) return oWeights;
        if (phase == AnmitsuBot.GamePhase.MIDGAME) return mWeights;
        return eWeights;
    }

    private static class HistoricalState {
        double[] features;
        AnmitsuBot.GamePhase phase;

        HistoricalState(double[] f, AnmitsuBot.GamePhase p) {
            this.features = f;
            this.phase = p;
        }
    }

    // Helper function for AnmitsuBotTrainer.java so the Visualizer can feed data from it
    public boolean playSingleMatchWithResult(SimpleBot opponentInstance) {
        char[][] board = new char[8][8];
        Reversi.initializeBoard(board);

        int p1Count = Reversi.countPieces(board, '⚫');
        int p2Count = Reversi.countPieces(board, '⚪');

        return p1Count > p2Count; // Return true if AnmitsuBot secured a victory
    }

    public static class Main {
        public static void main(String[] args) {
            AnmitsuBotTrainer trainer = new AnmitsuBotTrainer();
            trainer.train();
        }
    }

}