package ru.atlassian.jira;
import java.io.IOException;
import ru.atlassian.jira.service.KVServer;
import ru.atlassian.jira.service.HttpTaskServer;


public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        httpManagerTest();
    }

    public static void httpManagerTest() throws IOException {
        new KVServer().start();
        HttpTaskServer server = new HttpTaskServer();
    }
}
