package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.Utils;

import java.io.IOException;

public class Index implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        String content = Utils.readFile("index");
        Utils.returnSuccess(h, content);
    }
}
