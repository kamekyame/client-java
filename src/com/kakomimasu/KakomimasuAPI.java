package com.kakomimasu;

public class KakomimasuAPI {
  public enum PlayerType {
    ACCOUNT, GUEST
  }

  public enum MatchType {
    Free, Custom
  }

  private PlayerType playerType = PlayerType.GUEST;
  private String bearerTokenOrName = "guest";
  private String spec;

  private String gameId;

  private KakomimasuHttpClient httpClient;

  public KakomimasuAPI() {
    this.httpClient = new KakomimasuHttpClient("https://api.kakomimasu.com");
  }

  public KakomimasuAPI(String host) {
    this.httpClient = new KakomimasuHttpClient(host);
  }

  public void setPlayerWithBearerToken(String bearerToken) {
    this.playerType = PlayerType.ACCOUNT;
    this.bearerTokenOrName = bearerToken;
  }

  public void setPlayerWithGuest(String guestName) {
    this.playerType = PlayerType.GUEST;
    this.bearerTokenOrName = guestName;
  }

  public void setSpec(String spec) {
    this.spec = spec;
  }

  public void matchInFree(MatchListener listener) throws KkmmException {
    if (this.gameId != null) {
      throw new KkmmException("既に別のゲームに参加しています。");
    }
    var req = new Classes.MatchReq();
    req.spec = this.spec;
    if (this.playerType == PlayerType.GUEST) {
      req.guest = new Classes.MatchReq.Guest();
      req.guest.name = this.bearerTokenOrName;
    }
    var res = this.httpClient.match(req);
    // System.out.println(res);
    this.gameId = res.gameId;

    this.httpClient.connectWebSocket(listener);
  }
}
