package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.City;
import data.CityTemperature;
import main.Utils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.openjdk.nashorn.internal.scripts.JD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class StartDatabase implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        try {
            Jdbi jdbi = Jdbi.create(Utils.DB_URL);

            createTable(jdbi);

            int rowAmount = 100000;
            insertRows(jdbi, rowAmount);

            String rowInfo = selectRows(jdbi);
            Utils.returnSuccess(h, rowInfo);
        } catch (Exception e) {
            Utils.returnFail(h, e.getMessage());
        }
    }

    private void createTable(Jdbi jdbi) throws IOException {
        dropTable(jdbi);

        String sql = """
                create table rows (
                    city text not null,
                    temperature real
                );
                """;

        try (Handle handle = jdbi.open()){
            handle.execute(sql);
        }
    }

    private void dropTable(Jdbi jdbi) throws IOException {
        String sql = """
                drop table
                if exists
                rows
                """;

        try (Handle handle = jdbi.open()){
            handle.execute(sql);
        }
    }

    private void insertRows(Jdbi jdbi, int rowAmount) throws IOException {
        String sql = """
                insert into rows
                (city, temperature) 
                values 
                (:city, :temperature)
                """;

        var cities = getCities(rowAmount);
        Random rng = new Random();

        try (Handle handler = jdbi.open()) {
            PreparedBatch batch = handler.prepareBatch(sql);

            for (int i = 0; i < rowAmount; i++) {
                batch.bind("city", cities.get(rng.nextInt(Utils.AMOUNT_OF_CITIES)))
                        .bind("temperature", (rng.nextInt(700) - 200) / 10f)
                        .add();
                if (i % (rowAmount / 100) == 0) {
                    System.out.println(STR."\{i}/\{rowAmount}");
                }
            }

            System.out.println("Executing batch...");
            batch.execute();
            System.out.println("Done!");
        }
    }

    private List<String> getCities(int amount) throws FileNotFoundException {
        List cities = new ArrayList(amount);
        File file = new File(STR."./src/main/files/cities.txt");
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            cities.add(reader.nextLine());
        }
        reader.close();
        return cities;
    }

    private String selectRows(Jdbi jdbi) {
        String sql = """
                select rows.city
                , rows.temperature
                from rows
                """;

        StringBuilder rows = new StringBuilder();

        List<CityTemperature> cities = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                .mapToBean(CityTemperature.class)
                .list());

        rows.append("""
                <table>
                    <tr>
                        <th>City</th>
                        <th>Temperature</th>
                    </tr>
                """);
        cities.forEach(city -> rows.append(STR."<tr><td>\{city.getCity()}</td><td>\{city.getTemperature()}</td></tr>"));
        rows.append("""
                </table>
                """);

        return rows.toString();
    }
}
