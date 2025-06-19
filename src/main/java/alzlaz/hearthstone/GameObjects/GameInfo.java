package alzlaz.hearthstone.GameObjects;

public class GameInfo {
    private String gameType;
    private String formatType;
    private Player player1;
    private Player player2;


    //maybe we will consider adding a list or map to handle game modes with multiple players in the future
    public GameInfo() {
        this.player1 = new Player();
        this.player2 = new Player();
    }

    public Player getPlayer1() {
        return player1;
    }
    public Player getPlayer2() {
        return player2;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    public String getGameType() {
        return gameType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String getFormatType() {
        return formatType;
    }

    public void reset(){
        this.player1 = new Player();
        this.player2 = new Player();
        this.gameType = null;
        this.formatType = null;
    }
}
