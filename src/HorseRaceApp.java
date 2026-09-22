import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class HorseRaceApp extends Application {

    private static final String[] HORSE_NAMES = {"Blanco", "Asabache", "Negro", "Castaño"};
    private static final Color[] HORSE_COLORS = {
        Color.web("#3B82F6"),
        Color.web("#EF4444"),
        Color.web("#10B981"),
        Color.web("#F59E0B")
    };

    private Horse[] horses;
    private ProgressBar[] progressBars;
    private Label[] progressLabels;
    private Image horseImage;
    private ImageView[] horseIcons;
    private Label statusLabel;
    private Button startButton;
    private Button resetButton;

    // Punto de entrada de la interfaz grafica (JavaFX)
    @Override
    public void start(Stage primaryStage) {
        horses = new Horse[4];
        progressBars = new ProgressBar[4];
        progressLabels = new Label[4];
        horseIcons = new ImageView[4];
        horseImage = loadHorseImage();

        // Layout raiz: cabecera arriba, pistas al centro, botones abajo
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        VBox topSection = createHeader();
        root.setTop(topSection);

        VBox raceTrack = createRaceTrack();
        root.setCenter(raceTrack);

        HBox bottomSection = createControls();
        root.setBottom(bottomSection);

        Scene scene = new Scene(root, 700, 520);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setTitle("Carrera de Caballos");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 0, 10, 0));
        header.getStyleClass().add("header");

        Label titleLabel = new Label("CARRERA DE CABALLOS");
        titleLabel.getStyleClass().add("title");

        try {
            FileInputStream fis = new FileInputStream(new File("images/speed.png"));
            Image icon = new Image(fis);
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(48);
            imageView.setFitHeight(48);
            titleLabel.setGraphic(imageView);
        } catch (FileNotFoundException e) {
            System.out.println("Icono no encontrado: images/speed.png");
        }

        statusLabel = new Label("Presiona INICIAR para comenzar la carrera");
        statusLabel.getStyleClass().add("status-label");

        header.getChildren().addAll(titleLabel, statusLabel);
        return header;
    }

    // Crea las 4 pistas: un hilo Horse, una barra y un icono de caballo por pista
    private VBox createRaceTrack() {
        VBox track = new VBox(18);
        track.setPadding(new Insets(20, 40, 20, 40));
        track.getStyleClass().add("race-track");

        String[] trackColors = {"track-blue", "track-red", "track-green", "track-yellow"};

        for (int i = 0; i < 4; i++) {
            horses[i] = new Horse(HORSE_NAMES[i]);

            VBox horseBox = new VBox(5);
            horseBox.getStyleClass().add("horse-box");

            HBox nameRow = new HBox(10);
            nameRow.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label("Horse " + HORSE_NAMES[i]);
            nameLabel.getStyleClass().add("horse-name");

            Label percentLabel = new Label("0%");
            percentLabel.getStyleClass().add("percent-label");
            progressLabels[i] = percentLabel;

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            nameRow.getChildren().addAll(nameLabel, spacer, percentLabel);

            ProgressBar pb = new ProgressBar(0);
            pb.getStyleClass().add("horse-progress");
            pb.getStyleClass().add(trackColors[i]);
            pb.setPrefWidth(580);
            pb.setPrefHeight(28);

            Pane lane = new Pane();
            lane.getStyleClass().add("race-lane");
            lane.setPrefWidth(580);
            lane.setPrefHeight(32);
            pb.prefWidthProperty().bind(lane.widthProperty());
            pb.setMaxWidth(Double.MAX_VALUE);

            ImageView horseIcon = new ImageView(horseImage);
            horseIcon.setFitWidth(42);
            horseIcon.setFitHeight(42);
            horseIcon.setPreserveRatio(true);
            horseIcon.setSmooth(true);
            horseIcon.setLayoutY((32 - 42) / 2.0);
            horseIcon.setLayoutX(2);
            horseIcons[i] = horseIcon;

            lane.getChildren().addAll(pb, horseIcon);
            progressBars[i] = pb;

            horseBox.getChildren().addAll(nameRow, lane);
            track.getChildren().add(horseBox);
        }

        return track;
    }

    private Image loadHorseImage() {
        try {
            return new Image(new FileInputStream(new File("images/speed.png")));
        } catch (FileNotFoundException e) {
            System.out.println("Icono no encontrado: images/speed.png");
            return null;
        }
    }

    private HBox createControls() {
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(15, 0, 25, 0));
        controls.getStyleClass().add("controls");

        startButton = new Button("INICIAR");
        startButton.getStyleClass().add("btn-start");
        startButton.setOnAction(e -> startRace());

        resetButton = new Button("REINICIAR");
        resetButton.getStyleClass().add("btn-reset");
        resetButton.setOnAction(e -> resetRace());
        resetButton.setDisable(true);

        controls.getChildren().addAll(startButton, resetButton);
        return controls;
    }

    // Inicia la carrera: arranca los 4 hilos y un hilo que refresca la UI
    private void startRace() {
        startButton.setDisable(true);
        resetButton.setDisable(true);
        statusLabel.setText("La carrera ha comenzado!...");
        statusLabel.getStyleClass().remove("winner-label");

        // Cada caballo corre en su propio hilo (concurrente)
        for (int i = 0; i < 4; i++) {
            horses[i].startRunning();
            horses[i].start();
        }

        // Hilo observador: lee el progreso y actualiza la interfaz cada 80 ms
        Thread uiUpdater = new Thread(() -> {
            boolean raceFinished = false;
            while (!raceFinished) {
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Platform.runLater: solo el hilo de JavaFX puede tocar la UI
                Platform.runLater(() -> {
                    for (int i = 0; i < 4; i++) {
                        double progress = horses[i].getProgress() / 100.0;
                        progressBars[i].setProgress(progress);
                        progressLabels[i].setText(horses[i].getProgress() + "%");
                        double laneW = progressBars[i].getWidth() > 0
                                ? progressBars[i].getWidth()
                                : 580;
                        double maxX = Math.max(0, laneW - 46);
                        horseIcons[i].setTranslateX(progress * maxX);
                    }
                });

                // Deteccion del ganador: el primero en llegar a 100
                for (int i = 0; i < 4; i++) {
                    if (horses[i].isFinished()) {
                        raceFinished = true;
                        final int winner = i;
                        Platform.runLater(() -> {
                            for (Horse h : horses) {
                                h.stopRunning();
                            }
                            statusLabel.setText("GANADOR: " + HORSE_NAMES[winner] + " Gana!");
                            statusLabel.getStyleClass().add("winner-label");
                            resetButton.setDisable(false);
                        });
                        break;
                    }
                }
            }
        });
        uiUpdater.setDaemon(true);
        uiUpdater.start();
    }

    private void resetRace() {
        for (int i = 0; i < 4; i++) {
            horses[i] = new Horse(HORSE_NAMES[i]);
            progressBars[i].setProgress(0);
            progressLabels[i].setText("0%");
            horseIcons[i].setTranslateX(0);
        }
        startButton.setDisable(false);
        resetButton.setDisable(true);
        statusLabel.setText("Presiona INICIAR para comenzar la carrera");
        statusLabel.getStyleClass().remove("winner-label");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
