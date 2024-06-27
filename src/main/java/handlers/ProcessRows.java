package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.CityTemperature;
import main.TemperatureData;
import main.Utils;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ProcessRows implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        try {
            String result = processRows();
            Utils.returnSuccess(h, result);
        } catch (Exception e) {
            Utils.returnFail(h, e.getMessage());
        }
    }

    private String processRows() {
        String sql = """
                select rows.city
                , rows.temperature
                from rows
                """;

        Jdbi jdbi = Jdbi.create(Utils.DB_URL);

        List<CityTemperature> cities = jdbi.withHandle(handler ->
                handler.createQuery(sql)
                .mapToBean(CityTemperature.class)
                .list());

        StringBuilder result = new StringBuilder();

        result.append("""
                <table>
                    <thead>
                        <tr>
                            <th>City</th>
                            <th>Min</th>
                            <th>Max</th>
                            <th>Average</th>
                        </tr>
                    </thead>
                    <tbody>
                """);

        var data = new HashMap<String, TemperatureData>();
        cities.forEach(city -> {
            if (data.containsKey(city.getCity())) {
                data.get(city.getCity()).addTemperature(city.getTemperature());
            } else {
                data.put(city.getCity(), new TemperatureData(city.getTemperature()));
            }
        });
        ArrayList<String> citiesData = new ArrayList<>(data.keySet());
        Collections.sort(citiesData);
        for (String cityData : citiesData) {
            result.append(STR."""
                        <tr hx-get='/getTemperatures' hx-trigger='mouseenter throttle:1s' hx-target='#rowTemperature' hx-vars='city:"\{cityData}"'>
                            <td>\{cityData}
                            <td>\{data.get(cityData).min}
                            <td>\{data.get(cityData).max}
                            <td>\{String.format("%.1f", data.get(cityData).getAverage())}
                        </tr>
                    """);
        }
        result.append("""
                    </tbody>
                </table>
                """);

        return result.toString();
    }
}
