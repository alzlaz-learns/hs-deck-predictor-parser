package alzlaz.hearthstone.GameObjects;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private Integer entityId;
    private Integer playerId;
    private String accountId;
    private String playerName;
    private boolean opponent = false;
    private List<String> cardsPlayed = new ArrayList<>();

    public void setEntityId(Integer i){
        this.entityId = i;
    }

    public void setPlayerId(Integer i){
        this.playerId = i;
    }

    public void setAccountId(String i){
        this.accountId = i;
    }

    public void setPlayerName(String i){
        this.playerName = i;
    }

    public void setAsOpponent(){
        this.opponent = true;
    }

    public Integer getEntityId(){
        return this.entityId;
    }

    public Integer getPlayerId(){
        return this.playerId;
    }

    public String getAccountId(){
        return this.accountId;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public boolean isOpponent(){
        return this.opponent;
    }

    public void addCardPlayed(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            cardsPlayed.add(cardId);
        }
    }

    public List<String> getCardsPlayed() {
        return cardsPlayed;
    }

    
}