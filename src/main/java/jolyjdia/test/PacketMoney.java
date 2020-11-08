package jolyjdia.test;

public class PacketMoney extends Packet {
    public PacketMoney() {

    }

    @Override
    public void handle() {
        System.out.println("money");
    }
}
