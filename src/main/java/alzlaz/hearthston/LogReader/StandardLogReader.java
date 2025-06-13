package alzlaz.hearthston.LogReader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class StandardLogReader implements PowerLogReader {

    @Override
    public void readLog(String line) throws IOException{
        File logFile = new File(line);
        System.out.println("test from StandardLogReader");
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
                        System.out.println("Pointer: " + filePointer + " / File size: " + reader.length() + " " + logLine);
                    }
                    filePointer = reader.getFilePointer();
                }

                try {
                    System.out.println("Waiting for new log entries...");
                    Thread.sleep(1000); // Sleep for a second before checking again
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
