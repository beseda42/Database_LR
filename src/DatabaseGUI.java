import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DatabaseGUI extends Application {
    private Database database = new Database();
    private ListView<Order> listView = new ListView<>();
    private TextField idField, addressField, dateField, weightField;
    private ComboBox<String> searchField;
    private TextField searchValueField;
    private ComboBox<String> removeField;
    private TextField removeValueField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Database");

        idField = new TextField();
        idField.setPromptText("ID (key)");
        addressField = new TextField();
        addressField.setPromptText("Address");
        dateField = new TextField();
        dateField.setPromptText("Date (dd.MM.yyyy)");
        weightField = new TextField();
        weightField.setPromptText("Weight");

        searchField = new ComboBox<>();
        searchField.getItems().addAll("ID", "Address", "Date", "Weight");
        searchField.setPromptText("Select field to search");

        searchValueField = new TextField();
        searchValueField.setPromptText("Enter value to search");

        removeField = new ComboBox<>();
        removeField.getItems().addAll("ID", "Address", "Date", "Weight");
        removeField.setPromptText("Select field to remove from");

        removeValueField = new TextField();
        removeValueField.setPromptText("Enter value to remove");

        Button saveButton = new Button("Save Database");
        saveButton.setOnAction(e -> saveDatabase(primaryStage));

        Button loadButton = new Button("Load Database");
        loadButton.setOnAction(e -> loadDatabase(primaryStage));

        Button clearButton = new Button("Clear Database");
        clearButton.setOnAction(e -> clearDatabase());

        Button restoreButton = new Button("Restore");
        restoreButton.setOnAction(e -> restoreDatabase());

        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(e -> showAllRecords());

        Button exportButton = new Button("Export to .xlsx");
        exportButton.setOnAction(e -> exportToXlsx(primaryStage));

        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(saveButton, loadButton, clearButton, exportButton, restoreButton);

        Button addButton = new Button("Add Order");
        addButton.setOnAction(e -> addRecord());

        Button editButton = new Button("Edit Selected");
        editButton.setOnAction(e -> editSelectedRecord());

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> removeRecord());

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchRecord());

        Button saveChangesButton = new Button("Save Changes");
        saveChangesButton.setOnAction(e -> saveChanges());

        HBox recordButtonLayout = new HBox(10);
        recordButtonLayout.getChildren().addAll(addButton, editButton, saveChangesButton);

        HBox recordButtonLayout2 = new HBox(10);
        recordButtonLayout2.getChildren().addAll(searchField, searchButton, showAllButton);

        HBox recordButtonLayout3 = new HBox(10);
        recordButtonLayout3.getChildren().addAll(removeField, removeButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(buttonLayout, idField, addressField, dateField, weightField, recordButtonLayout,
                listView,
                recordButtonLayout2, searchValueField,
                recordButtonLayout3, removeValueField);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addRecord() {
        try {
            database.backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int id;
        try {
            id = Integer.parseInt(idField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Id must be an integer.");
            return;
        }
        if (id < 1){showAlert("Error", "ID must be positive."); return;}
        String address = addressField.getText();
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            showAlert("Error", "Date must be in format dd.MM.yyyy.");
            return;
        }
        double weight;

        try {
            weight = Double.parseDouble(weightField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Weight must be a number.");
            return;
        }
        if (weight <= 0){showAlert("Error", "Weight must be positive."); return;}

        Order order = new Order(id, address, date, weight);
        if (database.addRecord(order)) {
            listView.getItems().add(order);
            clearFields();
        } else {
            showAlert("Error", "Record with this ID already exists.");
        }
    }

    private void editSelectedRecord() {
        Order selectedOrder = listView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            idField.setText(String.valueOf(selectedOrder.getId()));
            addressField.setText(selectedOrder.getAddress());
            dateField.setText(selectedOrder.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            weightField.setText(String.valueOf(selectedOrder.getWeight()));
        } else {
            showAlert("Error", "Please select an order to edit.");
        }
    }

    private void saveChanges() {
        try {
            database.backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int id;
        try {
            id = Integer.parseInt(idField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "ID must be an integer.");
            return;
        }
        if (id < 1){showAlert("Error", "ID must be positive."); return;}
        String address = addressField.getText();
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            showAlert("Error", "Date must be in format dd.MM.yyyy.");
            return;
        }
        double weight;
        try {
            weight = Double.parseDouble(weightField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Weight must be a number.");
            return;
        }
        if (weight <= 0){showAlert("Error", "Weight must be positive."); return;}


        if (database.editRecord(id, address, date, weight)) {
            int index = listView.getSelectionModel().getSelectedIndex();
            listView.getItems().set(index, new Order(id, address, date, weight));
            clearFields();
        } else {
            showAlert("Error", "Order with this ID does not exist.");
        }
    }

    private void removeRecord() {
        try {
            database.backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String selectedField = removeField.getValue();
        String removeValue = removeValueField.getText();

        if (selectedField != null && !removeValue.isEmpty()) {
            switch (selectedField) {
                case "ID":
                    try {
                        int id = Integer.parseInt(removeValue);
                        database.removeRecord(id);
                        listView.getItems().removeIf(order -> order.getId() == id);
                    } catch (NumberFormatException e) {
                        showAlert("Error", "ID must be an integer.");
                    }
                    break;
                case "Address":
                    database.removeRecordsByAddress(removeValue);
                    listView.getItems().removeIf(order -> order.getAddress().equals(removeValue));
                    break;
                case "Date":
                    try {
                        LocalDate date = LocalDate.parse(removeValue, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        database.removeRecordsByDate(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                        listView.getItems().removeIf(order -> order.getDate().equals(date));
                    } catch (DateTimeParseException e) {
                        showAlert("Error", "Date must be in format dd.MM.yyyy.");
                    }
                    break;
                case "Weight":
                    try {
                        double weight = Double.parseDouble(removeValue);
                        database.removeRecordsByWeight(weight);
                        listView.getItems().removeIf(order -> order.getWeight() == weight);
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Weight must be a number.");
                    }
                    break;
                default:
                    showAlert("Error", "Select a valid field to remove.");
            }
        } else {
            showAlert("Error", "Please select a field and enter a value to remove.");
        }
    }

    private void searchRecord() {
        String selectedField = searchField.getValue();
        String searchValue = searchValueField.getText();
        listView.getItems().clear();

        if (selectedField != null && !searchValue.isEmpty()) {
            switch (selectedField) {
                case "ID":
                    try {
                        int id = Integer.parseInt(searchValue);
                        Order order = database.findById(id);
                        if (order != null) {
                            listView.getItems().add(order);
                        } else {
                            showAlert("Error", "Order not found.");
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Error", "ID must be an integer.");
                    }
                    break;
                case "Address":
                    listView.getItems().addAll(database.findByAddress(searchValue));
                    break;
                case "Date":
                    try {
                        LocalDate date = LocalDate.parse(searchValue, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        listView.getItems().addAll(database.findByDate(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
                    } catch (DateTimeParseException e) {
                        showAlert("Error", "Date must be in format dd.MM.yyyy.");
                    }
                    break;
                case "Weight":
                    try {
                        double weight = Double.parseDouble(searchValue);
                        listView.getItems().addAll(database.findByWeight(weight));
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Weight must be a number.");
                    }
                    break;
                default:
                    showAlert("Error", "Select a valid field to search.");
            }
        } else {
            showAlert("Error", "Please select a field and enter a value to search.");
        }
    }

    private void showAllRecords() {
        listView.getItems().clear();
        listView.getItems().addAll(database.getRecords());
    }

    private void saveDatabase(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Database");
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            database.setFilePath(file.getAbsolutePath());
            try {
                database.saveDatabase();
            } catch (IOException e) {
                showAlert("Error", "Failed to save database.");
            }
        }
    }

    private void loadDatabase(Stage primaryStage) {
        try {
            database.backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Database");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            database.setFilePath(file.getAbsolutePath());
            try {
                database.loadDatabase();
                listView.getItems().clear();
                listView.getItems().addAll(database.getRecords());
            } catch (IOException | ClassNotFoundException e) {
                showAlert("Error", "Failed to load database.");
            }
        }
    }

    private void restoreDatabase() {
        try {
            database.restoreDatabase();
            listView.getItems().clear();
            listView.getItems().addAll(database.getRecords());
        } catch (IOException | ClassNotFoundException e) {
            showAlert("Error", "Failed to restore database.");
        }
    }

    private void exportToXlsx(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Database to .xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                database.exportToXlsx(file.getAbsolutePath());
                showAlert("Success", "Database exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Error", "Failed to export database.");
                e.printStackTrace();
            }
        }
    }

    private void clearDatabase() {
        try {
            database.backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        database.clear();
        listView.getItems().clear();
    }

    private void clearFields() {
        idField.clear();
        addressField.clear();
        dateField.clear();
        weightField.clear();
        searchField.setValue(null);
        searchValueField.clear();
        removeField.setValue(null);
        removeValueField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
