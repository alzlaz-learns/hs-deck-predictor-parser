package alzlaz.hearthstone.deckpredictor;

import java.io.IOException;

import alzlaz.hearthstone.LogReader.LineParser;
import alzlaz.hearthstone.LogReader.StandardLogReader;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;
import alzlaz.hearthstone.LogReader.Interfaces.PowerLogReader;

public class Main {
    public static void main(String[] args) throws IOException {
        GameInfoLineParser lineParser = new LineParser();
        PowerLogReader logReader = new StandardLogReader(lineParser);
        String logFilePath = "./src/main/resources/Power.log";
        logReader.readLog(logFilePath);
    }
}