package alzlaz.hearthstone.LogReader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alzlaz.hearthstone.GameObjects.GameInfo;

import alzlaz.hearthstone.GameObjects.EnumRegex;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;

public class LineParser implements GameInfoLineParser {
    
    private static final Logger logger = LoggerFactory.getLogger(LineParser.class);
    

    //matches entityId to playerId so when it appears in SHOW_ENTITY, we can determine the card and then added to appropriate players deck
    private final Map<Integer, Integer> entityToPlayer = new HashMap<>(); 

    private boolean fullyParsed = false;
    private boolean gameEnded = false;

    private GameInfo gameInfo;

    public LineParser() {
        gameInfo = new GameInfo();
    }


    @Override
    public void parseLine(String logLine) {

        if(!fullyParsed) {
            gameStateParseLine(logLine);
        }

        if (logLine.contains("PowerTaskList.DebugPrintPower()")) {

            /*  This if statement is used for non-opponent player
                if it is non-opponent player, alot of the relevant information 
                can be found by this regex pattern:
                    "BLOCK_START BlockType=PLAY Entity=\\[.*?id=(\\d+).*?cardId=([A-Z0-9_]*).*?player=(\\d)"
            */ 
            Matcher matcher = EnumRegex.BLOCK_START_PLAY.getPattern().matcher(logLine);
            if (matcher.find()){
                parseBlockStartPlay(matcher);
            }

            /*
                This if statement is used for opponent player
                if it is opponent player, the relevant information can be found by this regex pattern:
                    "SHOW_ENTITY - Updating Entity=\\[.*?id=(\\d+).*?\\] CardID=([A-Z0-9_]+)
             */
            matcher = EnumRegex.SHOW_ENTITY.getPattern().matcher(logLine);
            if(matcher.find()) {
                onRevealAdd(matcher);
            }

            matcher = EnumRegex.GAME_COMPLETED.getPattern().matcher(logLine);
            if (matcher.find()) {
                System.out.println("Game completed detected.");
                gameEnded = true;
                handleGameEnd();
            }

        }
    }

    public void gameStateParseLine(String logLine){
        Matcher matcher;

        // If game state is not parsed, try to replace unknown player names
        if (!fullyParsed) {
            //detect game type
            matcher = EnumRegex.GAME_TYPE.getPattern().matcher(logLine);
            if(matcher.find()){
                gameInfo.setGameType(matcher.group(1));
                logger.info("Game Type = {}", matcher.group(1));
            }

            //detect format type
            matcher = EnumRegex.FORMAT_TYPE.getPattern().matcher(logLine);
            if(matcher.find()){
                gameInfo.setFormatType(matcher.group(1));
                logger.info("Format Type = {}", matcher.group(1));
            }

            //TODO: detect entityId. I think it might not be necessary.


            //TODO: 

            //detect player Id and player name
            //if playerName is UNKNOWN HUMAN PLAYER, it will set player as opponent
            matcher = EnumRegex.PLAYER_ENTITY.getPattern().matcher(logLine);
            if(matcher.find()){
                int playerId = Integer.parseInt(matcher.group(1));
                String playerName = matcher.group(2);
                if(playerId == 1) {
                    gameInfo.getPlayer1().setPlayerId(playerId);
                    gameInfo.getPlayer1().setPlayerName(playerName);
                    logger.info("Player 1: ID = {}, Name = {}", playerId, playerName);
                    if (playerName.equals("UNKNOWN HUMAN PLAYER")) {
                        gameInfo.getPlayer1().setAsOpponent();
                        logger.info("Player 1 is set as opponent due to UNKNOWN HUMAN PLAYER name.");
                    }
                } else if (playerId == 2) {
                    gameInfo.getPlayer2().setPlayerId(playerId);
                    gameInfo.getPlayer2().setPlayerName(playerName);
                    logger.info("Player 2: ID = {}, Name = {}", playerId, playerName);
                    if (playerName.equals("UNKNOWN HUMAN PLAYER")) {
                        gameInfo.getPlayer2().setAsOpponent();
                        logger.info("Player 2 is set as opponent due to UNKNOWN HUMAN PLAYER name.");
                    }
                }
            }

            matcher = EnumRegex.REPLACE_UNKNOWN_PLAYER.getPattern().matcher(logLine);
            if (matcher.find()) {
                String playerName = matcher.group(1);

                if ("UNKNOWN HUMAN PLAYER".equals(gameInfo.getPlayer1().getPlayerName())) {
                    gameInfo.getPlayer1().setPlayerName(playerName);
                    logger.info("Replacing UNKNOWN HUMAN PLAYER with {} for Player 1", playerName);
                } else if ("UNKNOWN HUMAN PLAYER".equals(gameInfo.getPlayer2().getPlayerName())) {
                    gameInfo.getPlayer2().setPlayerName(playerName);
                    logger.info("Replacing UNKNOWN HUMAN PLAYER with {} for Player 2", playerName);
                }
            }

            isGameStateParse();
        }
    }



    /*
     * This method parses the Block Start Play line.
     * It puts the entityId to playerId mapping in the entityToPlayer map.
     * If the cardId is not null or empty, it adds the card to the appropriate player's deck. which is should typically be non-opponent player
     */
    public void parseBlockStartPlay(Matcher matcher) {
        int entityId = Integer.parseInt(matcher.group(1));
        String cardId = matcher.group(2);
        int playerId = Integer.parseInt(matcher.group(3));

        entityToPlayer.put(entityId, playerId);
    
        if (cardId != null && !cardId.isEmpty()) {
            if (playerId == 1) {
                gameInfo.getPlayer1().addCardPlayed(cardId);
                logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer1().getPlayerName());
            } else if (playerId == 2) {
                gameInfo.getPlayer2().addCardPlayed(cardId);
                logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer2().getPlayerName());
            }
        }
    }

    public void onRevealAdd(Matcher matcher){
        int entityId = Integer.parseInt(matcher.group(1));
        String cardId = matcher.group(2);

        
        if (cardId == null || cardId.isEmpty()) {
            return; // Skip blank cardIds
        }

        Integer playerId = entityToPlayer.get(entityId);

        if (playerId == null) {       
            return;
        }

        if (playerId != null) {
            if (playerId == 1) {
                gameInfo.getPlayer1().addCardPlayed(cardId);
                logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer1().getPlayerName());
            } else if (playerId == 2) {
                gameInfo.getPlayer2().addCardPlayed(cardId);
                logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer2().getPlayerName());
            }
        }

    }
 
    /*
     * Checks if the game state has been fully parsed.
     */
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
            logger.info("Game state fully parsed.");
        }
    }

    public boolean hasGameEnded() {
        return gameEnded;
    }

    public void handleGameEnd() {
        logger.info("GAME ENDED");
        logger.info("Player {}: {}, deck {}", gameInfo.getPlayer1().getPlayerId(), gameInfo.getPlayer1().getPlayerName(), gameInfo.getPlayer1().getCardsPlayed());
        logger.info("Player {}: {}, deck {}", gameInfo.getPlayer2().getPlayerId(), gameInfo.getPlayer2().getPlayerName(), gameInfo.getPlayer2().getCardsPlayed());
    }

}
