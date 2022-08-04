package com.kakomimasu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonUtil {
  public static <T> T parse(String json, Class<T> dto) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return (T) mapper.readValue(json, dto);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
