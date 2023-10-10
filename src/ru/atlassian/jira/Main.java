package ru.atlassian.jira;
import java.io.IOException;
import ru.atlassian.jira.service.KVServer;
import ru.atlassian.jira.service.HttpTaskServer;
import ru.atlassian.jira.service.Managers;


public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        httpManagerTest();
    }

    public static void httpManagerTest() throws IOException {
        new KVServer().start();
        try {
            HttpTaskServer server = new HttpTaskServer(Managers.getHttp("http://localhost:8078"));
        } catch (InterruptedException exception) {
            System.out.println("Ошибка инициализации http server'а");
        }
    }
}
