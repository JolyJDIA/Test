package jolyjdia.test;

public abstract class Packet {
    public final int packetId = 6;

    public Packet() {}

    public abstract void handle();

    @Override
    public String toString() {
        return packetId+"";
    }
}