package alzlaz.hearthstone.LogReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alzlaz.hearthstone.GameObjects.GameInfo;
import alzlaz.hearthstone.GameObjects.HeroPowerMatcher;
import alzlaz.hearthstone.GameObjects.Card;
import alzlaz.hearthstone.GameObjects.EnumRegex;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;
import alzlaz.hearthstone.eventhandler.CardPlayEvent;
import alzlaz.hearthstone.eventhandler.EventSender;

public class LineParser implements GameInfoLineParser {
    
    private static final Logger logger = LoggerFactory.getLogger(LineParser.class);
    

    //matches entityId to playerId so when it appears in SHOW_ENTITY, we can determine the card and then added to appropriate players deck
    private final Map<Integer, Integer> entityToPlayer = new HashMap<>(); 

    private boolean fullyParsed = false;
    private boolean gameEnded = false;

    private GameInfo gameInfo;
    private final EventSender handler;

    public LineParser( GameInfo gameInfo, EventSender handler) {
        this.gameInfo = gameInfo;
        this.handler = handler;
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

                
                if ("UNKNOWN HUMAN PLAYER".equals(gameInfo.getPlayer1().getPlayerName()) && !playerName.equals(gameInfo.getPlayer2().getPlayerName())) {
                    gameInfo.getPlayer1().setPlayerName(playerName);
                    logger.info("Replacing UNKNOWN HUMAN PLAYER with {} for Player 1", playerName);
                } else if ("UNKNOWN HUMAN PLAYER".equals(gameInfo.getPlayer2().getPlayerName()) && !playerName.equals(gameInfo.getPlayer2().getPlayerName())) {
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
        List<Card> snapshot = null;
        Card card = null;
        //this is probably going to be problematic in the future I need to research what the hero card ids are.
        // but temporarily it is a fix for not adding hero power actions into 
        if(!HeroPowerMatcher.isHeroPower(cardId)){
            entityToPlayer.put(entityId, playerId);
            if (cardId != null && !cardId.isEmpty()) {
                if (playerId == 1) {
                    card = new Card(cardId, entityId);
                    
                    gameInfo.getPlayer1().addCardPlayed(card);
                    if (gameInfo.getPlayer1().isOpponent()) {
                        // If the player is an opponent, we publish the event
                        // to notify about the card played.
                        snapshot = List.copyOf(gameInfo.getPlayer1().getCardsPlayed());
                        handler.publish(new CardPlayEvent(gameInfo.getPlayer1().getPlayerName(), snapshot));
                    }
                    
                    // System.out.printf("play Player: %d %s played: %s - %s\n", playerId, gameInfo.getPlayer1().getPlayerName(), cardLookup.findCardById(cardId), cardId);
                    logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer1().getPlayerName());
                } else if (playerId == 2) {
                    card = new Card(cardId, entityId);
                    gameInfo.getPlayer2().addCardPlayed(card);
                    if(gameInfo.getPlayer2().isOpponent()){
                        snapshot = List.copyOf(gameInfo.getPlayer2().getCardsPlayed());
                        handler.publish(new CardPlayEvent(gameInfo.getPlayer2().getPlayerName(), snapshot));
                    }
                    // System.out.printf("Play Player: %d %s played: %s - %s\n", playerId, gameInfo.getPlayer2().getPlayerName(), cardLookup.findCardById(cardId), cardId);
                    logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer2().getPlayerName());
                }
            }
        }  
    }

    public void onRevealAdd(Matcher matcher){
        int entityId = Integer.parseInt(matcher.group(1));
        String cardId = matcher.group(2);
        Integer playerId = entityToPlayer.get(entityId);
        List<Card> snapshot = null;
        Card card = null;
        if (playerId == null) {       
            return;
        }

        if (playerId != null) {
            if (playerId == 1) {
                card = new Card(cardId, entityId);
                gameInfo.getPlayer1().addCardPlayed(card);
                if (gameInfo.getPlayer1().isOpponent()) {
                    // If the player is an opponent, we publish the event
                    // to notify about the card played.
                    snapshot = List.copyOf(gameInfo.getPlayer1().getCardsPlayed());
                    handler.publish(new CardPlayEvent(gameInfo.getPlayer1().getPlayerName(), snapshot));
                }
                // System.out.printf("reveal Player: %d %s played: %s - %s\n", playerId, gameInfo.getPlayer1().getPlayerName(),cardLookup.findCardById(cardId), cardId);
                logger.info("Adding cardId={} to Player={} Name={} deck", cardId, playerId, gameInfo.getPlayer1().getPlayerName());
            } else if (playerId == 2) {
                card = new Card(cardId, entityId);
                gameInfo.getPlayer2().addCardPlayed(card);
                if(gameInfo.getPlayer2().isOpponent()){
                    snapshot = List.copyOf(gameInfo.getPlayer2().getCardsPlayed());
                    handler.publish(new CardPlayEvent(gameInfo.getPlayer2().getPlayerName(), snapshot));
                }
                // System.out.printf("Reveal Player: %d %s played: %s - %s\n", playerId, gameInfo.getPlayer2().getPlayerName(),cardLookup.findCardById(cardId), cardId);
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

    public void resetLineParserState(){
        gameInfo.reset();
        this.gameEnded = false;
        this.fullyParsed = false;
        this.entityToPlayer.clear();
        logger.info("resetting game state");
        logger.info("Checking Reset success");
        logger.info("Player {}: {}, deck {}, isOpponent: {}", gameInfo.getPlayer1().getPlayerId(), gameInfo.getPlayer1().getPlayerName(), gameInfo.getPlayer1().getCardsPlayed(), gameInfo.getPlayer1().isOpponent());
        logger.info("Player {}: {}, deck {}, isOpponent: {}", gameInfo.getPlayer2().getPlayerId(), gameInfo.getPlayer2().getPlayerName(), gameInfo.getPlayer2().getCardsPlayed(), gameInfo.getPlayer2().isOpponent());
    }

    public void handleGameEnd() {
        logger.info("GAME ENDED");
        logger.info("Player {}: {}, deck {}, isOpponent: {}", gameInfo.getPlayer1().getPlayerId(), gameInfo.getPlayer1().getPlayerName(), gameInfo.getPlayer1().getCardsPlayed(), gameInfo.getPlayer1().isOpponent());
        logger.info("Player {}: {}, deck {}, isOpponent: {}", gameInfo.getPlayer2().getPlayerId(), gameInfo.getPlayer2().getPlayerName(), gameInfo.getPlayer2().getCardsPlayed(), gameInfo.getPlayer2().isOpponent());

        
    }

}
