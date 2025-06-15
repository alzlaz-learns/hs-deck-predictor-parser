package alzlaz.hearthstone.LogReader.Interfaces;

public interface GameInfoLineParser {
    String parseLine(String logLine);
    String cardPlayed(String parsedLine);
    void gameStateParseLine(String logLine);
    boolean gameStateParse();
}
