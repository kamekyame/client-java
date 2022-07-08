import com.kakomimasu.*;

public class App {
    public static void main(String[] args) throws Exception {
        var builder = new KakomimasuAPI.ClientBuilder().setGuestName("sztm");
        KakomimasuAPI client = builder.build();
        System.out.println(client.clientBuilder.bearerTokenOrName);
        var client2 = new KakomimasuAPI.ClientBuilder().setGuestName("sztm").setGuestName("client2").build();

        System.out.println(client.clientBuilder.bearerTokenOrName);
        System.out.println(client2.clientBuilder.bearerTokenOrName);

    }
}
