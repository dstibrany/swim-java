public interface Transport {
    Message receive();
    void send(Message message);
    void close();
    abstract static Transport create();
}
