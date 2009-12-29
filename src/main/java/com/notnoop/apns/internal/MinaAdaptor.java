package com.notnoop.apns.internal;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.notnoop.apns.ApnsNotification;

public class MinaAdaptor extends AbstractApnsService {
    NioSocketConnector connector;
    ConnectFuture cf;
    private final String host;
    private final int port;

    public MinaAdaptor(SSLContext sslContext, String host, int port) {
        this(sslContext, host, port, null);
    }

    public MinaAdaptor(SSLContext sslContext, String host,
            int port, ApnsFeedbackConnection feedback) {
        super(feedback);
        this.host = host;
        this.port = port;
        this.connector = createNioSocketConnector(sslContext);
    }

    private NioSocketConnector createNioSocketConnector(SSLContext sslContext) {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(30 * 1000L);
        connector.setHandler(new IoHandlerAdapter());
        SslFilter sslFilter = new SslFilter(sslContext);
        sslFilter.setUseClientMode(true);
        connector.getFilterChain().addLast("SSL", sslFilter);
        return connector;
    }

    @Override
    public void push(ApnsNotification message) {
        byte[] msg = message.marshall();
        IoBuffer buf = IoBuffer.allocate(msg.length);
        System.arraycopy(msg, 0, buf.array(), 0, msg.length);
        cf.getSession().write(buf);
    }

    public void start() {
        cf = connector.connect(new InetSocketAddress(host, port));
        cf.awaitUninterruptibly();
    }

    public void stop() {
        Utilities.sleep(1000);
        cf.getSession().close(false).awaitUninterruptibly(100000);
        connector.dispose();
    }
}
