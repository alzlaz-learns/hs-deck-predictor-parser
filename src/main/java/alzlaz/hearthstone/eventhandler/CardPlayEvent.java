package alzlaz.hearthstone.eventhandler;

import java.util.List;

import alzlaz.hearthstone.GameObjects.Card;

import java.time.Instant;

public record CardPlayEvent(String playerName, List<Card> cards) {

}
