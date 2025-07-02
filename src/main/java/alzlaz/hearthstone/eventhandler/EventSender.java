package alzlaz.hearthstone.eventhandler;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;

import alzlaz.hearthstone.GameObjects.Card;

public class EventSender implements Runnable {

    private final BlockingQueue<CardPlayEvent> queue = new LinkedBlockingQueue<>();
    private final HttpClient client = HttpClient.newBuilder()
                                               .version(HttpClient.Version.HTTP_1_1) // suppress h2c warnings
                                               .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI endpoint = URI.create("http://127.0.0.1:8000/predict");
    private final AtomicBoolean running = new AtomicBoolean(true);

    public void publish(CardPlayEvent evt){ 
        queue.offer(evt); 
    
    }
    public void stop(){ 
        running.set(false);
        queue.offer(new CardPlayEvent("_SHUTDOWN_", List.of())); 
    } 

    @Override public void run() {
        while (running.get()) {
            try {
                CardPlayEvent evt = queue.take();          
                if (!running.get()) break;                  

                send(evt);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // sends a CardPlayEvent to the endpoint
    private void send(CardPlayEvent evt) throws Exception {
        List<String> cardIDs = new ArrayList<>();
        for(Card card : evt.cards()) {
            if (card.isGenerated()) continue; // skip generated cards
            cardIDs.add(card.getCardID());
        }

        Map<String, Object> body = Map.of("player_name", evt.playerName(), "card_ids", cardIDs);
        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenAccept(resp -> System.out.println(
                "POST /predict → player=" + evt.playerName() 
                + "  " + resp.statusCode()
                + "  " + resp.body()))
            .exceptionally(ex -> { ex.printStackTrace(); return null; });

    }
}
