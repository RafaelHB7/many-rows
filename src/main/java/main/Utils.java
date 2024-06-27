package main;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Utils {
    public static final String WEB_PATH = "./src/main/web/";
    public static final String TEMPLATE_PATH = "./src/main/handlebars/";
    public static final String DB_URL = "jdbc:sqlite:ogrc.db";
    public static final int AMOUNT_OF_CITIES = 44691;
    public static String readFile(String name) throws FileNotFoundException {
        StringBuilder content = new StringBuilder();
        File file = new File(STR."\{WEB_PATH}\{name}.html");
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            content.append(reader.nextLine());
        }
        reader.close();
        return content.toString();
    }

    public static void returnFail(HttpExchange h, String data) throws IOException {
        returnRequest(h, data, 500);
    }
    public static void returnSuccess(HttpExchange h, String data) throws IOException {
        returnRequest(h, data, 200);
    }

    private static void returnRequest(HttpExchange h, String data, int responseCode) throws IOException {
        OutputStream outputStream = h.getResponseBody();
        h.sendResponseHeaders(responseCode, data.getBytes().length);
        outputStream.write(data.getBytes());
        outputStream.close();
    }

    public static HashMap<String, String> getRequestMap(HttpExchange h) {
        String query = h.getRequestURI().getQuery();
        if (!query.contains("?")) {
            int index = query.indexOf("=");
            HashMap<String, String> map = new HashMap<>(1);
            map.put(query.substring(0, index), query.substring(index+1));
            return map;
        }
        HashMap<String, String> map = new HashMap<>();
        Arrays.stream(query.split("\\?")).forEach(param -> {
            int index = param.indexOf("=");
            map.put(param.substring(0, index), param.substring(index+1));
        });
        return map;
    }

    public static Handlebars handleBars() {
        FileTemplateLoader loader = new FileTemplateLoader(Utils.TEMPLATE_PATH);
        return new Handlebars(loader);
    }
}
