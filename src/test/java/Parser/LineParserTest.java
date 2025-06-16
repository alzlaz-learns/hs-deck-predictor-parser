package Parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import alzlaz.hearthstone.LogReader.LineParser;
import alzlaz.hearthstone.LogReader.Interfaces.GameInfoLineParser;



public class LineParserTest {
    private GameInfoLineParser parser;

    @BeforeEach
    void setUp() {
        parser = new LineParser();
    }


}

