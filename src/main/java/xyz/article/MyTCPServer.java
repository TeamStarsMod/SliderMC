package xyz.article;

import org.geysermc.mcprotocollib.network.AbstractServer;
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class MyTCPServer extends TcpServer {
    private static final Logger log = LoggerFactory.getLogger(MyTCPServer.class);

    public MyTCPServer(String host, int port, Supplier<? extends PacketProtocol> protocol) {
        super(host, port, protocol);
    }

    @Override
    public AbstractServer bind () {
        log.info("已在 {}:{} 上启动服务端", this.getHost(), this.getPort());
        return super.bind();
    }
}
