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

        while (true) {
            for (int stage = 1; stage <= 4; stage++) {
                // Stage 1: DumbBot, Stage 2: CastellaBot, Stage 3: MomijiManjuBot, Stage 4: TaiyakiBot
                SimpleBot opponent = (stage == 1) ? new DumbBot() :
                        (stage == 2) ? new CastellaBot() :
                                (stage == 3) ? new MomijiManjuBot() : new TaiyakiBot();

                int winsInWindow = 0;

                for (int i = 1; i <= 1000; i++) {
                    boolean won = trainer.playSingleMatchWithResult(opponent);
                    if (won) winsInWindow++;

                    // Give your CPU a microsecond break during TaiyakiBot sessions
                    // so it doesn't starve the JavaFX Application UI Thread
                    if (stage == 3 && i % 10 == 0) {
                        try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                    }

                    if (i % windowSize == 0) {
                        double currentWinRate = (winsInWindow * 100.0) / windowSize;
                        winsInWindow = 0;
                        epochCounter++;

                        final double finalWinRate = currentWinRate;
                        final int currentEpoch = epochCounter;

                        // Push UI updates safely
                        Platform.runLater(() -> {
                            winRateSeries.getData().add(new XYChart.Data<>(currentEpoch, finalWinRate));

                            if (currentEpoch > MAX_DATA_POINTS) {
                                xAxis.setLowerBound(currentEpoch - MAX_DATA_POINTS);
                                xAxis.setUpperBound(currentEpoch);

                                // Keep memory low by actively pruning dead data points
                                if (winRateSeries.getData().size() > MAX_DATA_POINTS * 1.5) {
                                    winRateSeries.getData().remove(0, winRateSeries.getData().size() - MAX_DATA_POINTS);
                                }
                            }
                        });

                        // Increased sleep window slightly to allow JavaFX to clear its pulse cycles
                        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                    }
                }
            }
        }
    }
}