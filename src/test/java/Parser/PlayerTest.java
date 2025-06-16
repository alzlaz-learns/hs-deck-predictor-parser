package Parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import alzlaz.hearthstone.GameObjects.Player;


public class PlayerTest {

    private Player player;
    @BeforeEach
    void setUp() {
        player = new Player();
    }

    @Test
    public void testSetAndGetEntityId() {
        Integer entityId = 1;
        player.setEntityId(entityId);
        assert player.getEntityId().equals(entityId) : "Entity ID should match the set value";
    }

    @Test
    public void testSetAndGetPlayerName(){
        String playerName = "TestPlayer";
        player.setPlayerName(playerName);
        assert player.getPlayerName().equals(playerName) : "Player name should match the set value";
    }

    @Test
    public void testSetAndGetOpponent() {
        player.setAsOpponent();
        assert player.isOpponent() : "Player should be marked as opponent";
    }

    @Test
    public void testSetAndGetAccountid() {
        String accountId = "12345";
        player.setAccountId(accountId);
        assert player.getAccountId().equals(accountId) : "Account ID should match the set value";
    }

    @Test
    public void testAddAndGetCardsPlayed() {
        String cardId = "Card123";
        player.addCardPlayed(cardId);
        assert player.getCardsPlayed().size() == 1 : "Player should have one card played";
        assert player.getCardsPlayed().get(0).equals(cardId) : "Card ID should match the added card ID";
        assert player.getCardsPlayed().contains(cardId) : "Card should be added to the player's played cards";
    }

    @Test 
    public void testSetAndGetPlayerId() {
        Integer playerId = 2;
        player.setPlayerId(playerId);
        assert player.getPlayerId().equals(playerId) : "Player ID should match the set value";
    }
    
}
