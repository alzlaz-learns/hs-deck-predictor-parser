package Parser;

import org.junit.jupiter.api.Test;

import alzlaz.hearthstone.GameObjects.GameInfo;

import org.junit.jupiter.api.BeforeEach;
public class GameInfoTest {
    private GameInfo gameInfo;
    
    @BeforeEach
    void setUp() {
        gameInfo = new GameInfo();
    }


    @Test
    public void testPlayerNotNull() {
        assert gameInfo.getPlayer1() != null : "Player 1 should not be null";
        assert gameInfo.getPlayer2() != null : "Player 2 should not be null";
    }

    @Test
    public void testSetAndGetFormatType() {
        String formatType = "FT_STANDARD";
        gameInfo.setFormatType(formatType);
        assert gameInfo.getFormatType().equals(formatType) : "Format type should match the set value";
    }

    @Test
    public void testSetAndGetGameType() {
        String gameType = "GT_RANKED";
        gameInfo.setGameType(gameType);
        assert gameInfo.getGameType().equals(gameType) : "Game type should match the set value";
    }
}
