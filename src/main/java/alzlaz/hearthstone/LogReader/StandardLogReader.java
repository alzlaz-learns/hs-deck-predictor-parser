package alzlaz.hearthstone.LogReader;


import java.io.File;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;
import alzlaz.hearthstone.LogReader.Interfaces.PowerLogReader;


public class StandardLogReader implements PowerLogReader {

    private static final Logger logger = LoggerFactory.getLogger(StandardLogReader.class);
    private final GameInfoLineParser parser;
    public StandardLogReader(GameInfoLineParser parser) {
        this.parser = parser;
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

                        // check game end
                        if (parser instanceof LineParser lp && lp.hasGameEnded()) {
                            logger.info("Detected game end. Exiting read loop.");
                            System.out.println("Game has ended, exiting read loop.");
                            return; // clean exit
                        }
                    }
                    filePointer = reader.getFilePointer();
                }

                try {
                    
                    Thread.sleep(500); // Sleep for a second before checking again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
