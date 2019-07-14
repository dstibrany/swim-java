
public interface Transport {
    Message receive();
    void send(Message message, Member member);
    void close();
}
