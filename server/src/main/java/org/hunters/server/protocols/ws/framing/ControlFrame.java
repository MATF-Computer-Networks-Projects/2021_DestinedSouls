package org.hunters.server.protocols.ws.framing;

/**
 * Abstract class to represent control frames
 */
public abstract class ControlFrame extends Frame {

  /**
   * Class to represent a control frame
   *
   * @param opcode the opcode to use
   */
  protected ControlFrame(Opcode opcode) {
    super(opcode);
  }
}
