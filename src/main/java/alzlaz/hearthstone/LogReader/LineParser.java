package alzlaz.hearthstone.LogReader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alzlaz.hearthstone.GameObjects.GameInfo;
import alzlaz.hearthstone.GameObjects.Player;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;

public class LineParser implements GameInfoLineParser {
    private static final Pattern BLOCK_START_PLAY = Pattern.compile(
        "BLOCK_START BlockType=PLAY Entity=\\[.*?id=(\\d+).*?cardId=([A-Z0-9_]*).*?player=(\\d)"
    );

    //SHOW_ENTITY - Updating Entity=[entityName=UNKNOWN ENTITY [cardType=INVALID] id=24 zone=DECK zonePos=0 cardId= player=1] CardID=EDR_000
    private static final Pattern SHOW_ENTITY = Pattern.compile(
        "SHOW_ENTITY - Updating Entity=\\[.*?id=(\\d+).*?\\] CardID=([A-Z0-9_]+)"
    );


    private final Pattern GAME_TYPE = Pattern.compile("GameType=([^.]*)");
    private final Pattern FORMAT_TYPE = Pattern.compile("FormatType=([^.]*)");
    private final Pattern PLAYER_ENTITY  =  Pattern.compile("PlayerID=(\\d+), PlayerName=([^.]*)");
    private final Pattern REPLACE_UNKNOWN_PLAYER = Pattern.compile("TAG_CHANGE Entity=([^.]*) tag=CURRENT_PLAYER");


    private boolean fullyParsed = false;
    GameInfo gameInfo = new GameInfo();



    @Override
    public void parseLine(String logLine) {


        if(!fullyParsed) {
            gameStateParseLine(logLine);
        }

        if (logLine.contains("PowerTaskList.DebugPrintPower()")) {
            Matcher matcher = BLOCK_START_PLAY.matcher(logLine);
            if (matcher.find()){
                parseBlockStartPlay(matcher);
            }

            matcher = SHOW_ENTITY.matcher(logLine);
            if(matcher.find()) {
                onRevealAdd(matcher);
            }

        }
    }

    public void gameStateParseLine(String logLine){

        

        // If game state is not parsed, try to replace unknown player names
        if (!fullyParsed) {

            //detect game type
            Matcher matcher = GAME_TYPE.matcher(logLine);
            if(matcher.find()){
                System.out.println("Game Type = " + matcher.group(1));
                gameInfo.setGameType(matcher.group(1));
            }

            //detect format type
            matcher = FORMAT_TYPE.matcher(logLine);
            if(matcher.find()){
                System.out.println("Format Type = " + matcher.group(1));
                gameInfo.setFormatType(matcher.group(1));
            }

            //detect player Id and player name
            //if playerName is UNKNOWN HUMAN PLAYER, it will set player as opponent
            matcher = PLAYER_ENTITY.matcher(logLine);
            
            if(matcher.find()){
                int playerId = Integer.parseInt(matcher.group(1));
                String playerName = matcher.group(2);
                System.out.println("Player id = " + playerId + " Player Name = " + playerName);

                if(playerId == 1) {
                    gameInfo.getPlayer1().setPlayerId(playerId);
                    gameInfo.getPlayer1().setPlayerName(playerName);

                    if (playerName.equals("UNKNOWN HUMAN PLAYER")) {
                        gameInfo.getPlayer1().setAsOpponent();
                    }
                } else if (playerId == 2) {
                    gameInfo.getPlayer2().setPlayerId(playerId);
                    gameInfo.getPlayer2().setPlayerName(playerName);
                    if (playerName.equals("UNKNOWN HUMAN PLAYER")) {
                        gameInfo.getPlayer2().setAsOpponent();
                    }
                }
            }
            matcher = REPLACE_UNKNOWN_PLAYER.matcher(logLine);

            if (matcher.find()) {
                String playerName = matcher.group(1);

                if ("UNKNOWN HUMAN PLAYER".equals(gameInfo.getPlayer1().getPlayerName())) {
                    gameInfo.getPlayer1().setPlayerName(playerName);
                    System.out.println("Replacing UNKNOWN HUMAN PLAYER with " + playerName + " for Player 1");
                } else if ("UNKNOWN HUMAN PLAYER".equals(gameInfo.getPlayer2().getPlayerName())) {
                    gameInfo.getPlayer2().setPlayerName(playerName);
                    System.out.println("Replacing UNKNOWN HUMAN PLAYER with " + playerName + " for Player 2");
                }
            }

            isGameStateParse();
        }
        
    }

    //matches entityId to playerId so when it appears in SHOW_ENTITY, we can determine the card and then added to appropriate players deck
    private final Map<Integer, Integer> entityToPlayer = new HashMap<>(); 

    public void parseBlockStartPlay(Matcher matcher) {

        int entityId = Integer.parseInt(matcher.group(1));
        String cardId = matcher.group(2);
        int playerId = Integer.parseInt(matcher.group(3));
        entityToPlayer.put(entityId, playerId);
        // System.out.println("cardId=" + cardId + " player=" + playerId + " (id=" + entityId + ")");
        if (cardId != null && !cardId.isEmpty()) {
            if (playerId == 1) {
                gameInfo.getPlayer1().addCardPlayed(cardId);
            } else if (playerId == 2) {
                System.out.println("Adding cardId=" + cardId + " to Player 2's deck");
                gameInfo.getPlayer2().addCardPlayed(cardId);
            }
        }
    }

    public void onRevealAdd(Matcher matcher){
        int entityId = Integer.parseInt(matcher.group(1));
        String cardId = matcher.group(2);

        if (cardId == null || cardId.isEmpty()) {
            System.out.println("Skipping blank cardId for entityId=" + entityId);
            return; // Skip blank cardIds
        }

        Integer playerId = entityToPlayer.get(entityId);
        if (playerId != null) {
            if (playerId == 1) {
                gameInfo.getPlayer1().addCardPlayed(cardId);
            } else if (playerId == 2) {
                gameInfo.getPlayer2().addCardPlayed(cardId);
            }
        }

        // for (Map.Entry<Integer, Integer> entry : entityToPlayer.entrySet()) {
        //     if (entry.getKey() == entityId) {
        //         int playerId = entry.getValue();
                
        //         if (playerId == 1) {
        //             gameInfo.getPlayer1().addCardPlayed(cardId);
        //         } else if (playerId == 2) {
        //             gameInfo.getPlayer2().addCardPlayed(cardId);
        //         }
        //     }
        // }
    }
 
    public void isGameStateParse(){
        String name1 = gameInfo.getPlayer1().getPlayerName();
        String name2 = gameInfo.getPlayer2().getPlayerName();
        String gameType = gameInfo.getGameType();
        String formatType = gameInfo.getFormatType();
        if (name1 != null && name2 != null &&
            !name1.equals("UNKNOWN HUMAN PLAYER") &&
            !name2.equals("UNKNOWN HUMAN PLAYER") && 
            gameType != null && 
            formatType != null) {

            fullyParsed = true;
            System.out.println("Game state fully parsed.");
        }
    }


    //testing methods
    public Player getPlayer1() {
        return gameInfo.getPlayer1();
    }

    public Player getPlayer2() {
        return gameInfo.getPlayer2();
    }
}
