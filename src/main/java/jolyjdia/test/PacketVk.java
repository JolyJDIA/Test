package jolyjdia.test;

public class PacketVk extends Packet {
    public PacketVk() {}

    @Override
    public void handle() {
        System.out.println("VK");
    }
}
