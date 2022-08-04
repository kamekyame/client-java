package com.kakomimasu;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KakomimasuAPI {
  private enum PlayerType {
    ACCOUNT, GUEST
  }

  private PlayerType playerType = PlayerType.GUEST;
  private String bearerTokenOrName = "guest";
  private String spec;

  private Classes.MatchRes matchRes;

  private HttpClient httpClient = HttpClient.newBuilder().build();
  private URL url;

  private boolean ending = false;

  public KakomimasuAPI() {
    try {
      this.url = new URL("https://api.kakomimasu.com");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  public KakomimasuAPI(URL url) {
    this.url = url;
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

  public void connectMatchInFree(MatchListener listener) throws KakomimasuException {
    var req = new Classes.MatchReq();
    req.spec = this.spec;
    this.matchRes = this.match(req);
    this.connectWebSocket(this.matchRes, listener);
  }

  public void connectMatchInVsAI(Classes.MatchReq.AiOption aiOption, MatchListener listener)
      throws KakomimasuException {
    var req = new Classes.MatchReq();
    req.spec = this.spec;
    req.useAi = true;
    req.aiOption = aiOption;
    this.matchRes = this.match(req);
    this.connectWebSocket(this.matchRes, listener);
  }

  private Classes.MatchRes match(Classes.MatchReq matchReq) throws KakomimasuException {
    if (this.matchRes != null) {
      throw new KakomimasuException("既に別のゲームに参加済みです。");
    }
    String bearerToken = null;
    if (this.playerType == PlayerType.GUEST) {
      matchReq.guest = new Classes.MatchReq.Guest();
      matchReq.guest.name = this.bearerTokenOrName;
    } else if (this.playerType == PlayerType.ACCOUNT) {
      bearerToken = "Bearer " + this.bearerTokenOrName;
    }
    return this.post("/v1/match", bearerToken, Classes.MatchRes.class, matchReq);
  }

  public Classes.MatchActionRes action(Classes.MatchActionReq req) {
    return this.post("/v1/match/" + this.matchRes.gameId + "/action", this.matchRes.pic,
        Classes.MatchActionRes.class, req);
  }

  void connectWebSocket(Classes.MatchRes matchRes, MatchListener listener) {
    WebSocket.Builder wsBuilder = this.httpClient.newWebSocketBuilder();

    WebSocket.Listener wsListener = new WebSocket.Listener() {
      private String readData = "";

      @Override
      public void onOpen(WebSocket webSocket) {
        // System.out.println("WebSocket opened");
        String json = String.format("{\"q\":\"id:%s\"}", matchRes.gameId);
        webSocket.sendText(json, true);
        webSocket.request(1);
      }

      @Override
      public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        readData += data.toString();
        webSocket.request(1);

        if (last == false)
          return null;
        if (readData.equals(""))
          return null;
        String json = readData;
        readData = "";
        var type = JsonUtil.parse(json, Classes.WsGameRes.class).type;
        Classes.Game game;
        if (type.equals("initial")) {
          game = JsonUtil.parse(json, Classes.WsGameInitialRes.class).games[0];
        } else if (type.equals("update")) {
          game = JsonUtil.parse(json, Classes.WsGameUpdateRes.class).game;
        } else {
          return null;
        }
        try {
          if (game.gaming) {
            Classes.MatchActionReq actionReq = listener.onTurn(game, matchRes.index);
            KakomimasuAPI.this.action(actionReq);
          } else if (game.ending) {
            listener.onEnd(game, matchRes.index);
            KakomimasuAPI.this.ending = true;
          } else if (game.board != null) {
            listener.onStart(game, matchRes.index);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }

        return null;
      }
    };

    CompletableFuture<WebSocket> future = wsBuilder.buildAsync(
        /* URI.create("ws://localhost:8880" + "/v1/ws/game" */ getWebSocketURI("/v1/ws/game"),
        wsListener);
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    while (this.ending == false) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  // private <T> T get(String apiPath, String auth, Class<T> dto) {
  // URI uri = getHttpURI(apiPath);
  // HttpRequest.Builder builder = HttpRequest.newBuilder().GET().uri(uri);
  // if (auth != null) {
  // builder.setHeader("Authorization", auth);
  // }
  // HttpRequest request = builder.build();
  // HttpResponse<String> response = null;
  // try {
  // response = this.httpClient.send(request,
  // HttpResponse.BodyHandlers.ofString());
  // } catch (IOException | InterruptedException e) {
  // e.printStackTrace();
  // }
  // String body = response.body();
  // return JsonUtil.parse(body, dto);
  // }

  <T> T post(String apiPath, String auth, Class<T> resDto, Object reqDto) {
    ObjectMapper mapper = new ObjectMapper();
    String reqJson = null;
    try {
      reqJson = mapper.writeValueAsString(reqDto);
    } catch (JsonProcessingException e) {
      // e.printStackTrace();
    }

    URI uri = getHttpURI(apiPath);
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .POST(BodyPublishers.ofString(reqJson))
        .header("Content-Type", "application/json")
        .uri(uri);
    if (auth != null) {
      builder.setHeader("Authorization", auth);
    }
    HttpRequest request = builder.build();
    HttpResponse<String> response = null;
    try {
      response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    String body = response.body();
    return JsonUtil.parse(body, resDto);
  }

  private URI getHttpURI(String apiPath) {
    try {
      return new URI(this.url.getProtocol(), null, this.url.getHost(), this.url.getPort(), apiPath, null, null);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

  private URI getWebSocketURI(String apiPath) {
    String httpProtcol = this.url.getProtocol();
    String scheme = httpProtcol.equals("https") ? "wss" : "ws";
    try {
      return new URI(scheme, null, this.url.getHost(), this.url.getPort(), apiPath, null, null);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }
}
