package alzlaz.hearthstone.deckpredictor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alzlaz.hearthstone.GameObjects.GameInfo;

import alzlaz.hearthstone.LogReader.HandleJson;
import alzlaz.hearthstone.LogReader.LineParser;
import alzlaz.hearthstone.LogReader.StandardLogReader;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;
import alzlaz.hearthstone.LogReader.Interfaces.PowerLogReader;
import alzlaz.hearthstone.eventhandler.EventSender;
import alzlaz.hearthstone.watcher.PowerLogFinder;

public class Main {
    public static void main(String[] args) throws IOException {
        Path folder = Paths.get("C:\\Program Files (x86)\\Hearthstone\\Logs");

        HandleJson cardLookup = new HandleJson();
        GameInfo gameInfo = new GameInfo();
        EventSender eventSender = new EventSender(); // One instance

        LineParser lineParser = new LineParser(gameInfo, eventSender);

        PowerLogFinder folderWatcher = new PowerLogFinder(folder, lineParser);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(folderWatcher);   // Thread-0: watches folder, feeds new log lines to parser
        executor.submit(eventSender);     // Thread-1: handles and sends events

        // Optional: Add shutdown hook for clean exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered.");
            folderWatcher.stop();    // Stop folder watcher loop
            eventSender.stop();      // Stop event handler loop
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }));

    
    }

}