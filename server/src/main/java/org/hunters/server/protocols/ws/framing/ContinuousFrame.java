package org.hunters.server.protocols.ws.framing;

/**
 * Class to represent a continuous frame
 */
public class ContinuousFrame extends DataFrame {

  /**
   * constructor which sets the opcode of this frame to continuous
   */
  public ContinuousFrame() {
    super(Opcode.CONTINUOUS);
  }
}
