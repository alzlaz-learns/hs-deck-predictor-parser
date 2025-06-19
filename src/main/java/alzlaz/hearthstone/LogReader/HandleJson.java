package alzlaz.hearthstone.LogReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class HandleJson {

    private static final String FILE_NAME = "cards.json";
    private static final String API_URL = "https://api.hearthstonejson.com/v1/latest/enUS/cards.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private InputStream input = null;
    JsonNode root = null;
    public HandleJson(){
        try {
            //eventually handle if there is a malformed files and or there is an update for a new set
            Path cachedPath = Paths.get("cache", FILE_NAME);
            if (Files.exists(cachedPath)) {
                input = new FileInputStream(cachedPath.toFile());
            } else {
                input = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
                if (input == null) {
                    System.out.println("Local file not found. Downloading from HearthstoneJSON...");
                    downloadFileFromApi(API_URL, FILE_NAME);
                    input = new FileInputStream(cachedPath.toFile());
                }
            }


            root = objectMapper.readTree(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFileFromApi(String url, String outputFileName) throws IOException {
        Path localPath = Paths.get("cache", FILE_NAME);
        Files.createDirectories(localPath.getParent());
        try (InputStream in = java.net.URI.create(url).toURL().openStream()) {
            Files.copy(in, localPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String findCardById(String cardId) {
        
        if (root != null && root.isArray()) {
            for (JsonNode card : root) {
                if (card.has("id") && card.get("id").asText().equalsIgnoreCase(cardId)) {
                    return card.get("name").asText();
                }
            }

            
        } else {
            System.out.println("Invalid JSON structure: expected array under 'value'.");
        }
        return null;
    }

}
