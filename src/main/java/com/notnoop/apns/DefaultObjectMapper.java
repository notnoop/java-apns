package com.notnoop.apns;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link ObjectMapper} holder.
 */
class DefaultObjectMapper {

  private static final ObjectMapper defaultMapper = new ObjectMapper();

  /**
   * Returns the default {@link ObjectMapper} instance.
   */
  public static ObjectMapper get() {
    return defaultMapper;
  }

}
