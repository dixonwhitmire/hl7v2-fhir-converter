/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.whi.hl7.exception;

public class DataExtractionException extends RuntimeException {

  public DataExtractionException(String message, Exception e) {
    super(message, e);
  }


}
