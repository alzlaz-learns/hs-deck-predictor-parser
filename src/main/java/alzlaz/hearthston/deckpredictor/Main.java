package alzlaz.hearthston.deckpredictor;

import java.io.IOException;

import alzlaz.hearthston.LogReader.PowerLogReader;
import alzlaz.hearthston.LogReader.StandardLogReader;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        PowerLogReader logReader = new StandardLogReader();
        String logFilePath = "deckpredictor/src/main/resources/Power.log";
        logReader.readLog(logFilePath);
    }
}