package alzlaz.hearthstone.deckpredictor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alzlaz.hearthstone.GameObjects.GameInfo;
import alzlaz.hearthstone.GameObjects.HeroPowerMatcher;
import alzlaz.hearthstone.LogReader.HandleJson;
import alzlaz.hearthstone.LogReader.LineParser;
import alzlaz.hearthstone.LogReader.StandardLogReader;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;
import alzlaz.hearthstone.LogReader.Interfaces.PowerLogReader;

public class Main {
    
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("=== STARTED Deck Predictor ===");
        HandleJson HandleJson = new HandleJson();
        GameInfo gameInfo = new GameInfo();
        GameInfoLineParser lineParser = new LineParser(HandleJson, gameInfo);
        PowerLogReader logReader = new StandardLogReader(lineParser);
        String logFilePath = ".\\src\\main\\resources\\Power.log"; // deckpredictor\src\main\resources\Power.log
        logReader.readLog(logFilePath);

    }
}