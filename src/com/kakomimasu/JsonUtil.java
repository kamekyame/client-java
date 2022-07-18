package com.kakomimasu;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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

  // public static Map<String, Object> parse(String json) {
  // ObjectMapper mapper = new ObjectMapper();
  // try {
  // return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
  // });
  // } catch (IOException e) {
  // e.printStackTrace();
  // return null;
  // }
  // }
}
