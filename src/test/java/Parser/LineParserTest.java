package Parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import alzlaz.hearthstone.LogReader.LineParser;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;

import static org.junit.jupiter.api.Assertions.*;

public class LineParserTest {
      private GameInfoLineParser parser;

    @BeforeEach
    void setUp() {
        parser = new LineParser();
    }

    @Test
    public void testParseLineReturnsLineWhenItContainsPowerTask() {
        String line = "D 11:45:17.9314078 PowerTaskList.DebugPrintPower() -         tag=CONTROLLER value=1";
        String result = parser.parseLine(line);
        assertEquals(line, result);
    }

    @Test
    public void testParseLineReturnsNullWhenLineDoesNotMatch() {
        String line = "D 11:44:32.9535549 GameState.DebugPrintPower() -         tag=SPAWN_TIME_COUNT value=1";
        String result = parser.parseLine(line);
        assertNull(result);
    }



}

