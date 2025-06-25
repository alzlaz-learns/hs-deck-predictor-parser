package alzlaz.hearthstone.GameObjects;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HeroPowerMatcher {
    //this is a temporary measure until i figure out how the hearthstone
    // List of common substrings found in hero power cardIds
    private static final List<String> HERO_POWER_PATTERNS = new ArrayList<>();

    static {
        try (InputStream is = HeroPowerMatcher.class.getResourceAsStream("/grouped_hero_powers.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("===")) {
                    continue;
                }
                String[] parts = line.split(":", 2);
                if (parts.length > 0) {
                    HERO_POWER_PATTERNS.add(parts[0].trim());
                }
            }
            System.out.println(" Loaded " + HERO_POWER_PATTERNS.size() + " hero power patterns.");
        } catch (Exception e) {
            System.err.println(" Failed to load hero power patterns: " + e.getMessage());
        }
    }

    public static boolean isHeroPower(String cardId) {
        if (cardId == null || cardId.isEmpty()) {
            return false;
        }

        for (String pattern : HERO_POWER_PATTERNS) {
            if (cardId.equals(pattern)) {
                // Optional: log it for debugging
                System.out.println("Matched hero power: " + cardId + " (via pattern: " + pattern + ")");
                return true;
            }
        }

        return false;
    }
}
