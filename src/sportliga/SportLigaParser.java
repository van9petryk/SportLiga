package sportliga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.ArrayList;
import java.io.IOException;
import java.text.ParseException;

public class SportLigaParser {

    final String baseUrlArchive = "http://msl.ua/uk/sportprognoz/archive/";
    int idxPage = 1;
    int idxLastPage = 26;

    private Tirage parseTirage(Element row) throws ParseException {
        String dateStr = row.select(".span2").text();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("uk"));
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getMessage(), e.getErrorIndex());
        }

        Elements mb5 = row.select(".span10 .mb5");
        String result = mb5.get(1).select("span").text().replace(", ", "");
        if (result.length() != 12) {
            throw new ParseException("Ошибка парсинга для " + result + ".\n"
                    + "Дата: " + dateStr + ".", result.length());
        }
        
        return new Tirage(date, result);
    }

    public ArrayList<Tirage> parseUpToDate(LocalDate date) {
        ArrayList<Tirage> lTirage = new ArrayList();
        do {
            try {
                Element yw1 = Jsoup.connect(baseUrlArchive + idxPage).get().getElementById("yw1");
                Elements mb20 = yw1.select(".mb20");
                for (Element row : mb20) {
                    try {
                        Tirage tirage = parseTirage(row);
                        if (tirage.getDate().isEqual(date) || tirage.getDate().isBefore(date)) {
                            return lTirage;
                        }
                        lTirage.add(tirage);
                    } catch (ParseException e) {
                        SimpleLogger.log(e.getMessage());
                    }
                }
            } catch (IOException e) {
                SimpleLogger.log(e.getMessage());
            }
            idxPage++;
        } while (idxPage <= idxLastPage);
        return lTirage;
    }
}
