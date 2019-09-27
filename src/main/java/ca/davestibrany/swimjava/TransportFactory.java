package ca.davestibrany.swimjava;

interface TransportFactory {

    Transport create();

    Transport createListener(int port);
}
