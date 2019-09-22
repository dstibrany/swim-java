class NetTransportFactory implements TransportFactory {

    @Override
    public Transport create() {
        return new NetTransport();
    }

    @Override
    public Transport createListener(int port) {
        return new NetTransport(port);
    }

}
