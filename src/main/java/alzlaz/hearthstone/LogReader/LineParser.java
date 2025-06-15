package alzlaz.hearthstone.LogReader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alzlaz.hearthstone.GameObjects.Player;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;

public class LineParser implements GameInfoLineParser {
    private static final Pattern cardPlayPattern = Pattern.compile(
        "BLOCK_START BlockType=PLAY Entity=\\[.*?id=(\\d+).*?cardId=([A-Z0-9_]*).*?player=(\\d)"
    );

    private final Pattern GAME_TYPE = Pattern.compile("GameType=([^.]*)");
    private final Pattern FORMAT_TYPE = Pattern.compile("FormatType=([^.]*)");
    private final Pattern PLAYER_ENTITY  =  Pattern.compile("PlayerID=(\\d+), PlayerName=([^.]*)");
    private final Pattern REPLACE_UNKNOWN_PLAYER = Pattern.compile("TAG_CHANGE Entity=([^.]*) tag=CURRENT_PLAYER");
    
    private final Map<Integer, String> playerNames = new HashMap<>();

    public Map<Integer, String> getPlayerNames() {
        return playerNames;
    }
    
    Player player1 = new Player();
    Player player2 = new Player();
    

    @Override
    public String cardPlayed(String line) {


        Matcher matcher = cardPlayPattern.matcher(line);

        if (matcher.find()) {
            String entityId = matcher.group(1);
            String cardId = matcher.group(2);
            String playerId = matcher.group(3);

            System.out.println("cardId=" + cardId + " player=" + playerId + " (id=" + entityId + ")");
            return line;
        }
        return null;
    }

    @Override
    public String parseLine(String logLine) {
        if (logLine.contains("PowerTaskList.DebugPrintPower()")) {
            return logLine; 
        }
        return null;
    }

    @Override
    public void gameStateParseLine(String logLine){

        //detect game type
        Matcher matcher = GAME_TYPE.matcher(logLine);
        if(matcher.find()){
            System.out.println("Game Type = " + matcher.group(1));
        }

        //detect format type
        matcher = FORMAT_TYPE.matcher(logLine);
        if(matcher.find()){
            System.out.println("Format Type = " + matcher.group(1));
        }

        //detect player Id and player name
        //if playerName is UNKNOWN HUMAN PLAYER, it will set player as opponent
        matcher = PLAYER_ENTITY.matcher(logLine);
        if(matcher.find()){
            int playerId = Integer.parseInt(matcher.group(1));
            String playerName = matcher.group(2);
            System.out.println("Player id = " + playerId + " Player Name = " + playerName);

            if(playerId == 1) {
                player1.setPlayerId(playerId);
                player1.setPlayerName(playerName);

                if (playerName.equals("UNKNOWN HUMAN PLAYER")) {
                    player1.setAsOpponent();
                }
            } else if (playerId == 2) {
                player2.setPlayerId(playerId);
                player2.setPlayerName(playerName);
                if (playerName.equals("UNKNOWN HUMAN PLAYER")) {
                    player2.setAsOpponent();
                }
            }
        }

        // If game state is not parsed, try to replace unknown player names
        if (!gameStateParse()) {

            matcher = REPLACE_UNKNOWN_PLAYER.matcher(logLine);

            if (matcher.find()) {
                String playerName = matcher.group(1);

                if ("UNKNOWN HUMAN PLAYER".equals(player1.getPlayerName())) {
                    player1.setPlayerName(playerName);
                } else if ("UNKNOWN HUMAN PLAYER".equals(player2.getPlayerName())) {
                    player2.setPlayerName(playerName);
                }
            }
        }
        
    }
    
    @Override
    public boolean gameStateParse(){
        String name1 = player1.getPlayerName();
        String name2 = player2.getPlayerName();
        if (name1 != null && name2 != null &&
            !name1.equals("UNKNOWN HUMAN PLAYER") &&
            !name2.equals("UNKNOWN HUMAN PLAYER")) {
            return true; 
        }
        return false;
    }


    //testing methods
    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}
