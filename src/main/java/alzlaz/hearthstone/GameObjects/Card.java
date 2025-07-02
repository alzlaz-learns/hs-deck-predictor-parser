package alzlaz.hearthstone.GameObjects;
//https://gaming.stackexchange.com/questions/236803/can-you-see-if-a-card-was-created-by-an-unstable-portal-or-webspinner-in-the-log
//explanation: cards that are generated are causing issues with the model recogition, so we are keeping it simple and not sending them to the model
public class Card {
    private final String cardID;
    private final int entityID;
    private final boolean generated;

    public Card(String cardID, int entityID) {
        this.cardID = cardID;
        this.entityID = entityID;
        this.generated = entityID > 67; // this wouldnt work wild
    }

    public String getCardID() {
        return cardID;
    }

    public boolean isGenerated() {
        return generated;
    }
}
