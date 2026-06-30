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
    private NumberAxis xAxis;
    private int epochCounter = 0;

    // How many data points to show on screen at once before rolling forward
    private static final int MAX_DATA_POINTS = 50;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("AnmitsuBot Continuous Training Live Stream");

        xAxis = new NumberAxis(0, MAX_DATA_POINTS, 5);
        xAxis.setLabel("Training Epochs");
        xAxis.setAutoRanging(false); // We handle the scrolling manually

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Win Rate (%)");

        // Setup Line Chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("AnmitsuBot Live Reinforcement Tracking");
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false); // Disable layout animations to prevent visual stuttering during rapid updates

        winRateSeries = new XYChart.Series<>();
        winRateSeries.setName("Win Rate (Last 100 Games)");
        lineChart.getData().add(winRateSeries);

        Scene scene = new Scene(lineChart, 900, 600);
        stage.setScene(scene);
        stage.show();

        // Kick off the infinite background engine
        Thread streamingThread = new Thread(this::runInfiniteTraining);
        streamingThread.setDaemon(true);
        streamingThread.start();
    }

    private void runInfiniteTraining() {
        AnmitsuBotTrainer trainer = new AnmitsuBotTrainer();
        int windowSize = 100;

        // This outer loop keeps the training running forever
        while (true) {
            for (int stage = 1; stage <= 3; stage++) {
                SimpleBot opponent = (stage == 1) ? new CastellaBot() :
                        (stage == 2) ? new MomijiManjuBot() : new TaiyakiBot();

                int winsInWindow = 0;

                // Let's run chunks of 1,000 games per opponent tier
                for (int i = 1; i <= 1000; i++) {
                    boolean won = trainer.playSingleMatchWithResult(opponent);
                    if (won) winsInWindow++;

                    if (i % windowSize == 0) {
                        double currentWinRate = (winsInWindow * 100.0) / windowSize;
                        winsInWindow = 0;
                        epochCounter++;

                        final double finalWinRate = currentWinRate;
                        final int currentEpoch = epochCounter;

                        // Push UI changes to the JavaFX Application Thread safely
                        Platform.runLater(() -> {
                            winRateSeries.getData().add(new XYChart.Data<>(currentEpoch, finalWinRate));

                            // Handle the sliding viewport effect
                            if (currentEpoch > MAX_DATA_POINTS) {
                                xAxis.setLowerBound(currentEpoch - MAX_DATA_POINTS);
                                xAxis.setUpperBound(currentEpoch);

                                // Optional memory management: trim old data points off-screen
                                // so the chart array doesn't grow infinitely in RAM over days of running
                                if (winRateSeries.getData().size() > MAX_DATA_POINTS * 2) {
                                    winRateSeries.getData().remove(0);
                                }
                            }
                        });

                        // Control pacing: a 30ms delay lets you watch the logic unfold smoothly
                        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
                    }
                }
            }
        }
    }
}