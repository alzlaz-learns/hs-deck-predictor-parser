package alzlaz.hearthstone.GameObjects;

import java.util.List;

public class HeroPowerMatcher {
    //this is a temporary measure until i figure out how the hearthstone
    // List of common substrings found in hero power cardIds
    private static final List<String> HERO_POWER_PATTERNS = List.of(
        "HERO_09",  // Priest
          // Demon Hunter
        "CS1h_001", // Classic Priest heal
        "EDR_449p", // Hero power token
        "VAN_HERO",
        "HERO_11",
        "HERO_10",
        "HERO_09",
        "HERO_08",
        "HERO_07",
        "HERO_06",
        "HERO_05",
        "HERO_04",
        "HERO_03",
        "HERO_02",
        "HERO_01",
        "EDR_847p", //blessing of the golem
        "EDR_445p",  //Blessing of the Dragon
        "TOY_829hp" // Pulsing Pumpkins headless horseman hp
    );

    public static boolean isHeroPower(String cardId) {
        if (cardId == null || cardId.isEmpty()) {
            return false;
        }

        for (String pattern : HERO_POWER_PATTERNS) {
            if (cardId.contains(pattern)) {
                // Optional: log it for debugging
                System.out.println("Matched hero power: " + cardId + " (via pattern: " + pattern + ")");
                return true;
            }
        }

        return false;
    }
}
