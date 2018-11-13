package sportliga;

import java.time.LocalDate;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Tirage {
    private final SimpleObjectProperty<LocalDate> date;
    private final SimpleStringProperty result;
   
    public Tirage(LocalDate date, String result) {
        this.date = new SimpleObjectProperty<>(date);
        this.result = new SimpleStringProperty(result);
    }
    
    public String getResult() {
        return result.get();
    }
    public void setResult(String result) {
        this.result.set(result);
    }
    public SimpleStringProperty resultProperty() {
        return result;
    }
    public LocalDate getDate() {
        return date.get();
    }
    public void setDate(LocalDate date) {
        this.date.set(date);
    }
    public SimpleObjectProperty dateProperty() {
        return date;
    }
}
