import com.kakomimasu.*;
import com.kakomimasu.Classes.*;

public class App {
    public static void main(String[] args) throws Exception {
        KakomimasuAPI client = new KakomimasuAPI("http://localhost:8880");
        client.setPlayerWithGuest("sztm");
        try {
            client.matchInFree(new MatchListener() {
                @Override
                public void onStart(Game game) {
                    System.out.println("start");
                }

                @Override
                public void onTurn(Game game) {
                    System.out.println("turn : " + game.turn);
                }

                @Override
                public void onEnd(Game game) {
                    System.out.println("end");
                }

            });
        } catch (KkmmException e) {
            System.out.println(e.getMessage());
        }
        System.in.read();
        // while (true)
    }
}
