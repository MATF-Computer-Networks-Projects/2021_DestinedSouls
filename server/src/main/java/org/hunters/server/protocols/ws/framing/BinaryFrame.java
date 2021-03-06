package org.hunters.server.protocols.ws.framing;

/**
 * Class to represent a binary frame
 */
public class BinaryFrame extends DataFrame {

  /**
   * constructor which sets the opcode of this frame to binary
   */
  public BinaryFrame() {
    super(Opcode.BINARY);
  }
}
