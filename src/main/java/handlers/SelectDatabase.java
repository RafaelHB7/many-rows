package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.CityCount;
import main.Utils;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.List;

public class SelectDatabase implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        try {
            String rowInfo = selectRows();
            Utils.returnSuccess(h, rowInfo);
        } catch (Exception e) {
            Utils.returnFail(h, e.getMessage());
        }
    }

    private String selectRows() {
        String sql = """
                select count(rows.city) as count
                , rows.city
                from rows
                group by
                rows.city
                order by
                rows.city
                """;

        Jdbi jdbi = Jdbi.create(Utils.DB_URL);

        List<CityCount> cities = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                .mapToBean(CityCount.class)
                .list());

        StringBuilder rows = new StringBuilder();
        rows.append("""
                <table>
                    <thead>
                        <tr>
                            <th>City</th>
                            <th>Temperatures</th>
                        </tr>
                    </thead>
                    <tbody>
                """);
        cities.forEach(city -> rows.append(STR."<tr hx-get='/getTemperatures' hx-trigger='mouseenter' hx-target='#rowTemperature' hx-vars='city:\"\{city.getCity()}\"'><td>\{city.getCity()}</td><td>Amount: \{city.getCount()}</td></tr>"));
        rows.append("""
                    </tbody>
                </table>
                """);

        return rows.toString();
    }
}
