package org.hunters.server.protocols.ws.framing;


/**
 * Abstract class to represent data frames
 */
public abstract class DataFrame extends Frame {

  /**
   * Class to represent a data frame
   *
   * @param opcode the opcode to use
   */
  public DataFrame(Opcode opcode) {
    super(opcode);
  }
}
