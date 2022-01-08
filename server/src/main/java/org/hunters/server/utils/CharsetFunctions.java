package org.hunters.server.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class CharsetFunctions {

  private CharsetFunctions() {
  }

  private static final CodingErrorAction codingErrorAction = CodingErrorAction.REPORT;

  public static String stringUtf8(ByteBuffer bytes) {
    CharsetDecoder decode = StandardCharsets.UTF_8.newDecoder();
    decode.onMalformedInput(codingErrorAction);
    decode.onUnmappableCharacter(codingErrorAction);
    String s;
    try {
      bytes.mark();
      s = decode.decode(bytes).toString();
      bytes.reset();
    } catch (CharacterCodingException e) {
      return null;
    }
    return s;
  }

}
