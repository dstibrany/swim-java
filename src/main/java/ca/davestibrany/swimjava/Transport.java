package ca.davestibrany.swimjava;

public interface Transport {
    Message receive();

    void send(Message message);

    void close();
}
