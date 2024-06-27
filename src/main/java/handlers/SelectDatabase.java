package handlers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.CityCount;
import main.Utils;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.Arrays;
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

    private String selectRows() throws IOException {
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

        Handlebars handlebars = Utils.handleBars();
        Template template = handlebars.compile("rowsTable");

        return template.apply(cities);
    }
}
