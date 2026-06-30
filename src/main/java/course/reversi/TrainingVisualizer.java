package course.reversi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class TrainingVisualizer extends Application {

    private XYChart.Series<Number, Number> winRateSeries;
    private int epochCounter = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("AnmitsuBot Training Dashboard");

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10); // Win rate scale from 0% to 100%
        xAxis.setLabel("Training Epochs (x100)");
        yAxis.setLabel("Win Rate (%) vs Opponents");

        // Create the Line Chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("AnmitsuBot Evolution Metrics");
        lineChart.setCreateSymbols(false); // Keeps the line clean without bulky dots

        winRateSeries = new XYChart.Series<>();
        winRateSeries.setName("AnmitsuBot Performance");
        lineChart.getData().add(winRateSeries);

        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();


        Thread trainingThread = new Thread(this::runBackgroundTraining);
        trainingThread.setDaemon(true); // Shuts down thread automatically when window closes
        trainingThread.start();
    }

    private void runBackgroundTraining() {
        AnmitsuBotTrainer trainer = new AnmitsuBotTrainer();

        int windowSize = 100;

        // Accessing the internal loop via a refactored telemetry hook
        for (int stage = 1; stage <= 3; stage++) {
            SimpleBot opponent = (stage == 1) ? new CastellaBot() :
                    (stage == 2) ? new MomijiManjuBot() : new TaiyakiBot();

            int winsInWindow = 0;

            for (int i = 1; i <= 2000; i++) { // Let's run 2,000 games per tier
                boolean won = trainer.playSingleMatchWithResult(opponent);
                if (won) winsInWindow++;

                if (i % windowSize == 0) {
                    double currentWinRate = (winsInWindow * 100.0) / windowSize;
                    winsInWindow = 0; // Reset window tracker
                    epochCounter++;

                    // CRITICAL: Update the chart UI safely from the background thread
                    final double finalWinRate = currentWinRate;
                    final int currentEpoch = epochCounter;

                    Platform.runLater(() -> {
                        winRateSeries.getData().add(new XYChart.Data<>(currentEpoch, finalWinRate));
                    });

                    // Add a tiny sleep so the graph animation renders smoothly to human eyes
                    try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                }
            }
        }
    }
}