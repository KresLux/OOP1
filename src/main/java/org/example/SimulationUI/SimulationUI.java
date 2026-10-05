package org.example.SimulationUI;

import org.example.RestaurantBackend.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimulationUI extends Application {

    private final Restaurant restaurant = new Restaurant();

    private TextField arrivalIntervalField;
    private TextField cookingTimeField;
    private TextField registerTimeField;
    private TextField registersCountField;
    private TextField cooksCountField;

    private Button startButton;
    private Button stopButton;
    private Label warningLabel;

    private HBox registersBox;
    private List<ListView<String>> registerLists = new ArrayList<>();

    private ListView<String> kitchenQueueList;
    private ListView<String> cookingList;
    private ListView<String> serviceQueueList;

    private AnimationTimer uiUpdateTimer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Симулятор фастфуда");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));

        VBox topBox = createInputControlPanel();
        mainLayout.setTop(topBox);

        VBox centerBox = new VBox(20);

        registersBox = new HBox(20);
        registersBox.setAlignment(Pos.CENTER);

        ScrollPane registersScroll = new ScrollPane(registersBox);
        registersScroll.setFitToHeight(true);
        registersScroll.setFitToWidth(true);
        registersScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        registersScroll.setPrefHeight(250);

        HBox kitchenBox = createColumnsPanel();

        centerBox.getChildren().addAll(registersScroll, kitchenBox);
        mainLayout.setCenter(centerBox);

        setupUIUpdateTimer();

        Scene scene = new Scene(mainLayout, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            restaurant.stopSimulation();
            if (uiUpdateTimer != null) uiUpdateTimer.stop();
        });
        primaryStage.show();
    }

    private VBox createInputControlPanel() {
        HBox inputBar = new HBox(10);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        arrivalIntervalField = new TextField("2");
        arrivalIntervalField.setPrefWidth(45);

        cookingTimeField = new TextField("5");
        cookingTimeField.setPrefWidth(45);
        //sstat@grsu.by

        registerTimeField = new TextField("3");
        registerTimeField.setPrefWidth(45);

        registersCountField = new TextField("3");
        registersCountField.setPrefWidth(45);

        cooksCountField = new TextField("2");
        cooksCountField.setPrefWidth(45);

        startButton = new Button("Старт");
        stopButton = new Button("Стоп");
        stopButton.setDisable(true);

        startButton.setOnAction(e -> handleStart());
        stopButton.setOnAction(e -> handleStop());

        inputBar.getChildren().addAll(
                new Label("Прибытие (с):"), arrivalIntervalField,
                new Label("Готовка (с):"), cookingTimeField,
                new Label("Время кассы (с):"), registerTimeField,
                new Label("Касс:"), registersCountField,
                new Label("Поваров:"), cooksCountField,
                startButton, stopButton
        );

        warningLabel = new Label();
        warningLabel.setTextFill(Color.RED);

        VBox topBox = new VBox(8, inputBar, warningLabel);
        topBox.setPadding(new Insets(0, 0, 15, 0));
        return topBox;
    }

    private HBox createColumnsPanel() {
        HBox columnsBox = new HBox(20);
        columnsBox.setAlignment(Pos.CENTER);

        kitchenQueueList = createColoredListView("red");
        cookingList = createColoredListView("#D4AF37");
        serviceQueueList = createColoredListView("green");

        VBox col1 = createColumnBox("Очередь на кухню", kitchenQueueList);
        VBox col2 = createColumnBox("Готовится на плите", cookingList);
        VBox col3 = createColumnBox("Готовые к выдаче", serviceQueueList);

        columnsBox.getChildren().addAll(col1, col2, col3);
        return columnsBox;
    }

    private VBox createColumnBox(String title, ListView<String> listView) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox box = new VBox(10, titleLabel, listView);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(250);

        HBox.setHgrow(box, Priority.ALWAYS);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return box;
    }

    private ListView<String> createColoredListView(String colorHex) {
        ListView<String> listView = new ListView<>();
        listView.setSelectionModel(new NoSelectionModel<>());
        listView.setPrefHeight(200);

        listView.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-alignment: center;");
                }
            }
        });
        return listView;
    }

    private void handleStart() {
        warningLabel.setText("");
        try {
            int arrivalInterval = Integer.parseInt(arrivalIntervalField.getText().trim());
            int cookingTime = Integer.parseInt(cookingTimeField.getText().trim());
            int registerTime = Integer.parseInt(registerTimeField.getText().trim());
            int numRegisters = Integer.parseInt(registersCountField.getText().trim());
            int numCooks = Integer.parseInt(cooksCountField.getText().trim());

            if (arrivalInterval <= 0 || cookingTime <= 0 || registerTime <= 0 || numRegisters <= 0 || numCooks <= 0) {
                warningLabel.setText("Предупреждение: Все значения должны быть положительными целыми числами!");
                return;
            }

            registersBox.getChildren().clear();
            registerLists.clear();
            for (int i = 0; i < numRegisters; i++) {
                ListView<String> lv = createColoredListView("#0073e6");
                registerLists.add(lv);
                VBox col = createColumnBox("Касса " + (i + 1), lv);
                registersBox.getChildren().add(col);
            }

            restaurant.startSimulation(arrivalInterval, cookingTime, registerTime, numRegisters, numCooks);
            uiUpdateTimer.start();

            startButton.setDisable(true);
            stopButton.setDisable(false);
            arrivalIntervalField.setDisable(true);
            cookingTimeField.setDisable(true);
            registerTimeField.setDisable(true);
            registersCountField.setDisable(true);
            cooksCountField.setDisable(true);

        } catch (NumberFormatException ex) {
            warningLabel.setText("Предупреждение: Пожалуйста, введите корректные целые числа!");
        }
    }

    private void handleStop() {
        restaurant.stopSimulation();
        uiUpdateTimer.stop();

        clearDisplayFields();

        startButton.setDisable(false);
        stopButton.setDisable(true);
        arrivalIntervalField.setDisable(false);
        cookingTimeField.setDisable(false);
        registerTimeField.setDisable(false);
        registersCountField.setDisable(false);
        cooksCountField.setDisable(false);
    }

    private void setupUIUpdateTimer() {
        uiUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!restaurant.isRunning()) return;


                List<CashRegister> registers = restaurant.getCashRegisters();
                if (registers != null) {
                    for (int i = 0; i < registers.size() && i < registerLists.size(); i++) {
                        CashRegister cr = registers.get(i);
                        List<String> items = new ArrayList<>();
                        Customer current = cr.getCurrentCustomer();
                        if (current != null) {
                            items.add(current.getId() + " (оформляет)");
                        }
                        cr.getQueue().forEach(c -> items.add(String.valueOf(c.getId())));
                        registerLists.get(i).getItems().setAll(items);
                    }
                }


                List<String> waiting = restaurant.getKitchenQueue().stream()
                        .map(order -> String.valueOf(order.getId()))
                        .collect(Collectors.toList());
                kitchenQueueList.getItems().setAll(waiting);


                List<String> cookingStatus = new ArrayList<>();
                List<Cook> cooks = restaurant.getCooks();
                if (cooks != null) {
                    for (Cook cook : cooks) {
                        Order currentOrder = cook.getCurrentCookingOrder();
                        if (currentOrder != null) {
                            cookingStatus.add("Повар " + cook.getId() + ": №" + currentOrder.getId());
                        } else {
                            cookingStatus.add("Повар " + cook.getId() + ": Свободен");
                        }
                    }
                }
                cookingList.getItems().setAll(cookingStatus);

                List<String> ready = restaurant.getServiceQueue().stream()
                        .map(order -> String.valueOf(order.getId()))
                        .collect(Collectors.toList());

                Server server = restaurant.getServer();
                if (server != null && server.getCurrentPickupOrder() != null) {
                    ready.add(String.valueOf(server.getCurrentPickupOrder().getId()));
                }
                serviceQueueList.getItems().setAll(ready);
            }
        };
    }

    private void clearDisplayFields() {
        registerLists.forEach(lv -> lv.getItems().clear());
        kitchenQueueList.getItems().clear();
        cookingList.getItems().clear();
        serviceQueueList.getItems().clear();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
        @Override public javafx.collections.ObservableList<Integer> getSelectedIndices() { return javafx.collections.FXCollections.emptyObservableList(); }
        @Override public javafx.collections.ObservableList<T> getSelectedItems() { return javafx.collections.FXCollections.emptyObservableList(); }
        @Override public void selectIndices(int index, int... indices) {}
        @Override public void selectAll() {}
        @Override public void selectFirst() {}
        @Override public void selectLast() {}
        @Override public void clearAndSelect(int index) {}
        @Override public void select(int index) {}
        @Override public void select(T obj) {}
        @Override public void clearSelection(int index) {}
        @Override public void clearSelection() {}
        @Override public boolean isSelected(int index) { return false; }
        @Override public boolean isEmpty() { return true; }
        @Override public void selectPrevious() {}
        @Override public void selectNext() {}
    }
}