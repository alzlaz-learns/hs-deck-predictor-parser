package alzlaz.hearthstone.GameObjects;

import java.util.regex.Pattern;

//https://stackoverflow.com/questions/28359858/java-regex-pattern-compilation-best-practice-with-enum
public enum EnumRegex {
    BLOCK_START_PLAY("BLOCK_START BlockType=PLAY Entity=\\[.*?id=(\\d+).*?cardId=([A-Za-z0-9_]*).*?player=(\\d)"),
    SHOW_ENTITY("SHOW_ENTITY - Updating Entity=\\[.*?id=(\\d+).*?\\] CardID=([A-Z0-9_]+)"),
    GAME_TYPE("GameType=([^.]*)"),
    FORMAT_TYPE("FormatType=([^.]*)"),
    PLAYER_ENTITY("PlayerID=(\\d+), PlayerName=([^.]*)"),
    REPLACE_UNKNOWN_PLAYER("TAG_CHANGE Entity=([^.]*) tag=CURRENT_PLAYER"),

    GAME_COMPLETED("TAG_CHANGE Entity=GameEntity tag=STATE value=COMPLETE"),
    DATE_PATTERN("Hearthstone_(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})");
    private final Pattern pattern;

    EnumRegex(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return this.pattern;
    }

}
