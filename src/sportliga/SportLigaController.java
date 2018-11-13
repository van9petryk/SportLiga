package sportliga;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import java.util.LinkedList;

public class SportLigaController {

    private SportLigaModel model = new SportLigaModel();
    private ArrayList<Integer> selectedPair = new ArrayList();

    @FXML
    private TableView<Tirage> table;
    @FXML
    private TableColumn<Tirage, LocalDate> dateColumn;
    @FXML
    private TableColumn<Tirage, String> resultColumn;
    @FXML
    private TextField tfSearchPattern;
    @FXML
    private Button btSearchByFilter, btShowStatistics;
    @FXML
    private GridPane panePairGroup;
    @FXML
    private TreeView<String> tvStatistics;
    @FXML
    private CheckBox cbShowNonExisting;
    @FXML
    private ToggleGroup rbFilterGroup;
    @FXML
    private TextField limit;
    @FXML
    private DatePicker from;
    @FXML
    private DatePicker to;
    @FXML
    private RadioButton rbLimit;
    @FXML
    private RadioButton rbDate;

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        table.setItems(model.searchResult);
        tvStatistics.setRoot(model.root);

        UnaryOperator<TextFormatter.Change> filter = (t) -> {
            if (t.isAdded()) {
                if (t.getText().matches("[^0-2-]") || t.getControlNewText().length() > 12) {
                    t.setText("");
                }
            }
            return t;
        };
        tfSearchPattern.setTextFormatter(new TextFormatter(filter));

        UnaryOperator<TextFormatter.Change> numFilter = (t) -> {
            if (t.isAdded()) {
                if (t.getText().matches("[^0-9]") || t.getControlNewText().length() > 3) {
                    t.setText("");
                }
            }
            return t;
        };
        limit.setTextFormatter(new TextFormatter(numFilter));
        limit.setOnMouseClicked((e) -> rbFilterGroup.selectToggle(rbLimit));
        to.setOnShowing((e) -> rbFilterGroup.selectToggle(rbDate));
        from.setOnShowing((e) -> rbFilterGroup.selectToggle(rbDate));
        to.getEditor().setOnMouseClicked((e) -> to.show());
        from.getEditor().setOnMouseClicked((e) -> from.show());
    }

    @FXML
    private void handleBtShowStatisticsAction(ActionEvent e) {
        if (selectedPair.size() > 0) {
            if (updateFilter()) {
                try {
                    Scene scene = btShowStatistics.getScene();
                    scene.setCursor(Cursor.WAIT);
                    model.getStatistics(selectedPair, cbShowNonExisting.isSelected());
                    scene.setCursor(Cursor.DEFAULT);
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("SQLException");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Не вибрані пари");
            alert.setHeaderText("Немає вибраних пар. Виберіть пари.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBtSearchByFilterAction(ActionEvent e) {
        String resultPattern = tfSearchPattern.getText();
        if (updateFilter()) {
            try {
                model.searchResult(resultPattern);
            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("SQLException");
                alert.setHeaderText(ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleToggleButtonAction(ActionEvent e) {
        ToggleButton tb = (ToggleButton) e.getSource();
        if (tb.isSelected()) {
            selectedPair.add(Integer.parseInt(tb.getText()));
        } else {
            selectedPair.remove(selectedPair.indexOf(Integer.parseInt(tb.getText())));
        }
    }

    private boolean updateFilter() {
        String filterType = ((RadioButton) rbFilterGroup.getSelectedToggle()).getAccessibleText();
        boolean filterError = false;
        if (filterType.equals("all")) {
            model.resetFilter();
        } else if (filterType.equals("limit")) {
            if (limit.getText().length() != 0) {
                model.setFilter(Integer.parseInt(limit.getText()));
            } else {
                filterError = true;
            }
        } else {
            if (from.getValue() != null && to.getValue() != null) {
                model.setFilter(from.getValue(), to.getValue());
            } else {
                filterError = true;
            }
        }

        if (filterError) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Порожній фільтр");
            alert.setHeaderText("Заповніть дані поля фільтру");
            alert.showAndWait();
            return false;
        }

        return true;
    }

    public SportLigaController() {
        try {
            int inserted = model.updateDB();
            LinkedList<String> log = SimpleLogger.getLog();
            String logText = "";
            for (String msg : log) {
                logText += msg + "\n";
            }

            if (inserted != 0 || logText.length() != 0) {
                Alert alert;
                if (inserted != 0) {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Оновлення БД");
                    alert.setHeaderText("Оновлено записів " + inserted);
                }
                else {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Помилки парсингу");
                    alert.setHeaderText(null);
                }
                alert.setContentText(logText);
                alert.showAndWait();
            }

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Update Error");
            alert.setHeaderText(e.getMessage() + "\n");
            String errorText = "";
            LinkedList<String> log = SimpleLogger.getLog();
            for (String msg : log) {
                errorText += msg + "\n";
            }
            alert.setContentText(errorText);
            alert.showAndWait();
        }
    }
}
