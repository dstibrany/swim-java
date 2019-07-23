class TransportFactory {

    Transport create() {
        return new NetTransport();
    }

    Transport createListener(int port) {
        return new NetTransport(port);
    }
}
