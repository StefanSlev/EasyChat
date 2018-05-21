package org.easychat.client;

/**
 * Created by Slevy on 07.05.2018.
 */
enum ClientExceptionType {

    CLIENT_EXISTS {

        @Override
        public String getMessage() {

            return EXISTS;
        }
    },

    CLIENT_NOT_EXISTS {

        @Override
        public String getMessage() {

            return NOT_EXISTS;
        }
    },

    CLIENT_CONNECT {

        @Override
        public String getMessage() {

            return CONNECT;
        }
    },

    CLIENT_SEND {

        @Override
        public String getMessage() {

            return SEND;
        }
    };

    private static final String EXISTS = "Client already exists on the server !";
    private static final String NOT_EXISTS = "Client does not exists on the server !";
    private static final String CONNECT = "Client could not connect to the server !";
    private static final String SEND = "Client could not send the message !";
    public abstract String getMessage();
}
