package jolyjdia.test;

public class Packet {
    public String s;

    public Packet(String s) {
        this.s = s;
    }
    public Packet() {}

    @Override
    public String toString() {
        return s;
    }
}