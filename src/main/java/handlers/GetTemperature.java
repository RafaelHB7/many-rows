package handlers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.CityCount;
import data.CityTemperatures;
import main.Utils;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetTemperature implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        try {
            String temperatureData = getTemperature(h);
            Utils.returnSuccess(h, temperatureData);
        } catch (Exception e) {
            Utils.returnFail(h, e.getMessage());
        }
    }

    private String getTemperature(HttpExchange h) throws IOException {
        String sql = """
                select rows.temperature
                from rows
                where rows.city=:city
                order by rows.temperature
                """;

        HashMap<String, String> request = Utils.getRequestMap(h);

        Jdbi jdbi = Jdbi.create(Utils.DB_URL);

        List<Float> temperatures = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                    .bind("city", request.get("city"))
                    .mapTo(Float.class)
                    .list());

        CityTemperatures cityTemperatures = new CityTemperatures(request.get("city"), temperatures);

        Handlebars handlebars = Utils.handleBars();
        Template template = handlebars.compile("temperatureList");

        return template.apply(cityTemperatures);
    }
}
