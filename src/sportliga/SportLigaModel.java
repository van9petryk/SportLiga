package sportliga;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.collections.FXCollections;

public class SportLigaModel {

    ObservableList<Tirage> searchResult = FXCollections.observableArrayList();
    TreeItem<String> root = new TreeItem("Статистика");

    private String filter = "";
    private boolean isDateFilter = false;

    SportLigaModel() {

    }

    public int updateDB() throws SQLException {
        SportLigaParser parser = new SportLigaParser();
        ArrayList<Tirage> updateSet = parser.parseUpToDate(getLastTirageDate());
        String insertStatement = "INSERT INTO sportliga VALUES (?, ?)";
        Connection conn = Database.getConnection();
        int i = 0;
        try (PreparedStatement stmt = conn.prepareStatement(insertStatement)) {
            conn.setAutoCommit(false);
            for (Tirage tirage : updateSet) {
                try {
                    stmt.setDate(1, Date.valueOf(tirage.getDate()));
                    stmt.setString(2, tirage.getResult());
                    stmt.executeUpdate();
                    conn.commit();
                    i++;
                } catch (SQLException e) {
                    SimpleLogger.log(e.getMessage());
                }
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw e;
        }
        return i;
    }

    private LocalDate getLastTirageDate() throws SQLException {
        LocalDate lastTirageDate = LocalDate.MIN;
        try (Statement stmt = Database.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT max(date) AS latest FROM sportliga");
            if (rs.next()) {
                lastTirageDate = rs.getDate("latest").toLocalDate();
            }
        } catch (SQLException e) {
            throw e;
        }
        return lastTirageDate;
    }

    void setFilter(LocalDate from, LocalDate to) {
        filter = " date BETWEEN '" + from.toString() + "' AND '" + to.toString() + "'";
        isDateFilter = true;
    }

    void setFilter(int limit) {
        filter = " LIMIT " + limit;
        isDateFilter = false;
    }

    void resetFilter() {
        filter = "";
    }

    private String prepareQuerySearchResult(String pattern) {
        pattern = "'" + pattern.replace('-', '_') + "%'";
        String query = "SELECT * FROM sportliga WHERE result LIKE " + pattern;
        if (isDateFilter) {
            query += " AND" + filter;
        }
        query += " ORDER BY date DESC";
        if (!isDateFilter && filter.length() != 0) {
            query += filter;
        }

        return query;
    }

    void searchResult(String pattern) throws SQLException {
        String query = prepareQuerySearchResult(pattern);
        try (Statement stmt = Database.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            searchResult.clear();
            while (rs.next()) {
                searchResult.add(new Tirage(rs.getDate("date").toLocalDate(), rs.getString("result")));
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    private String prepareQueryGetStatistics(ArrayList<Integer> selectedPair) {

        Collections.sort(selectedPair);
        int previous, next = selectedPair.get(0), length = 1;
        int first = next;
        int i = 1;
        ArrayList<String> substr = new ArrayList<>();
        while (i < selectedPair.size()) {
            previous = next;
            next = selectedPair.get(i);
            if (next == previous + 1) {
                length++;
            } else {
                substr.add(String.format("SUBSTR(result, %d, %d)", first, length));
                first = next;
                length = 1;
            }
            i++;
        }
        substr.add(String.format("SUBSTR(result, %d, %d)", first, length));

        String separatedSubstr = String.join(", ", substr);
        if (substr.size() > 1) {
            separatedSubstr = "CONCAT(" + separatedSubstr + ")";
        }

        String query = "SELECT COUNT(comb) as count, comb FROM (SELECT " + separatedSubstr + " AS comb FROM sportliga";
        if (isDateFilter) {
            query += " WHERE" + filter;
        } else if (filter.length() != 0) {
            query += " ORDER BY date DESC" + filter;
        }

        query += ") GROUP BY comb";
        return query;
    }

    void getStatistics(ArrayList<Integer> selectedPair, boolean showNonExistingComb) throws SQLException {
        root.getChildren().clear();
        String query = prepareQueryGetStatistics(selectedPair);
        HashMap<String, Integer> combinationCount = new HashMap<>();
        if (showNonExistingComb) {
            for (int i = 0; i < Math.pow(3, selectedPair.size()); i++) {
                combinationCount.put(intToComb(i, selectedPair.size()), 0);
            }
        }

        try (Statement stmt = Database.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                combinationCount.put(rs.getString("comb"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            throw e;
        }

        HashMap<Integer, ArrayList<String>> countCombination = new HashMap<>();
        for (Map.Entry<String, Integer> entry : combinationCount.entrySet()) {
            countCombination.putIfAbsent(entry.getValue(), new ArrayList<>());
            countCombination.get(entry.getValue()).add(entry.getKey());
        }
        combinationCount = null;
        //cache Flyweight pattern try
        for (Map.Entry<Integer, ArrayList<String>> entry : countCombination.entrySet()) {
            Collections.sort(entry.getValue());
            TreeItem<String> count = new TreeItem<>(entry.getKey().toString());
            for (String combination : entry.getValue()) {
                count.getChildren().add(new TreeItem<>(combination));
            }
            root.getChildren().add(count);
        }

    }

    private String intToComb(int i, int pairCount) {
        if (Math.pow(3, pairCount) <= i) {
            throw new IllegalArgumentException("pairCount too small");
        }

        StringBuilder combination = new StringBuilder(pairCount);
        do {
            int c = i % 3;
            combination.append(c);
            i /= 3;
        } while (--pairCount != 0);
        return combination.toString();
    }

}

class Database {

    private Database() {
    }

    private static Connection conn = null;

    public static void openConnection() throws ClassNotFoundException, SQLException {
        if (conn == null) {
            try {
                Class.forName("org.h2.Driver");
                conn = DriverManager.getConnection("jdbc:h2:./DB/SportLiga", "admin", "");
               
            } catch (ClassNotFoundException | SQLException e) {
                throw e;
            }
        }
    }

    public static Connection getConnection() {
        return conn;
    }

    public static void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }
}
