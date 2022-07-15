import com.kakomimasu.*;

public class App {
    public static void main(String[] args) throws Exception {
        KakomimasuAPI client = new KakomimasuAPI("http://localhost:8880");
        client.setPlayerWithGuest("sztm");
        client.matchWithFree();

    }
}
