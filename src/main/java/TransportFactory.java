public interface TransportFactory {
    Transport create();

    Transport createListener(int port);
}
