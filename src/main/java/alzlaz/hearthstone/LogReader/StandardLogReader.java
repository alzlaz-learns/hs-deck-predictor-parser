package alzlaz.hearthstone.LogReader;


import java.io.File;

import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.regex.Pattern;

import alzlaz.hearthstone.GameObjects.Player;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;
import alzlaz.hearthstone.LogReader.Interfaces.PowerLogReader;


public class StandardLogReader implements PowerLogReader {

    private final Pattern PLAYER_ENTITY  =  Pattern.compile("PlayerID=(\\d+), PlayerName=([^.]*)");


    private final GameInfoLineParser parser;
    public StandardLogReader(GameInfoLineParser parser) {
        this.parser = parser;

        //testing block for shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("\nShutdown detected. Final game info:");

        if (parser instanceof LineParser) {
            LineParser lp = (LineParser) parser;
            Player p1 = lp.getPlayer1();
            Player p2 = lp.getPlayer2();

            if (p1 != null && p1.getPlayerName() != null) {
                System.out.println("Player 1:");
                System.out.println(" - ID: " + p1.getPlayerId());
                System.out.println(" - Name: " + p1.getPlayerName());
                System.out.println(" - Opponent: " + p1.isOpponent());
                System.out.println(" - Cards Played: " + p1.getCardsPlayed());
            }

            if (p2 != null && p2.getPlayerName() != null) {
                System.out.println("\nPlayer 2:");
                System.out.println(" - ID: " + p2.getPlayerId());
                System.out.println(" - Name: " + p2.getPlayerName());
                System.out.println(" - Opponent: " + p2.isOpponent());
                System.out.println(" - Cards Played: " + p2.getCardsPlayed());
            }
        }
    }));
    }
    
    @Override
    public void readLog(String logFilePath) throws IOException{
        File logFile = new File(logFilePath);
        // String parsedLine = null; //might not be needed, but keeping for now

        try (RandomAccessFile reader = new RandomAccessFile(logFile, "r")){
        
            long filePointer = 0;
            while(true){

                long fileLength = reader.length();
                //check if file has been modified and resetspointer 
                if (fileLength < filePointer) {
                    filePointer = 0;
                }

                //reads logfile if pointer is less than file length
                if (fileLength > filePointer) {
                    reader.seek(filePointer);
                    String logLine;
                    while((logLine = reader.readLine()) != null) {
                        parser.parseLine(logLine);
                    }
                    filePointer = reader.getFilePointer();
                }

                try {
                    System.out.println("Waiting for new log entries");
                    Thread.sleep(1000); // Sleep for a second before checking again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
