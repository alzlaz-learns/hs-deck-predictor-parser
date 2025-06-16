package alzlaz.hearthstone.GameObjects;

import java.util.regex.Pattern;



/*
 * private static final Pattern BLOCK_START_PLAY = Pattern.compile(
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
 */
//https://stackoverflow.com/questions/28359858/java-regex-pattern-compilation-best-practice-with-enum
public enum PowerLogRegexEnum {
    BLOCK_START_PLAY("BLOCK_START BlockType=PLAY Entity=\\[.*?id=(\\d+).*?cardId=([A-Z0-9_]*).*?player=(\\d)"),
    SHOW_ENTITY("SHOW_ENTITY - Updating Entity=\\[.*?id=(\\d+).*?\\] CardID=([A-Z0-9_]+)"),
    GAME_TYPE("GameType=([^.]*)"),
    FORMAT_TYPE("FormatType=([^.]*)"),
    PLAYER_ENTITY("PlayerID=(\\d+), PlayerName=([^.]*)"),
    REPLACE_UNKNOWN_PLAYER("TAG_CHANGE Entity=([^.]*) tag=CURRENT_PLAYER"),

    GAME_COMPLERTED("PowerTaskList.DebugPrintPower() -     TAG_CHANGE Entity=GameEntity tag=STATE value=COMPLETE");
    
    private final Pattern pattern;

    PowerLogRegexEnum(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return this.pattern;
    }

}
