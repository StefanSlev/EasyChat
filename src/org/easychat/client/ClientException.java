package org.easychat.client;

/**
 * Created by Slevy on 07.05.2018.
 */
public final class ClientException extends Exception {

    ClientException(ClientExceptionType exceptionType) {

        super(exceptionType.getMessage());
    }
}
