package com.kenvifire.memcached.client;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by hannahzhang on 15/5/25.
 */
public class MemcachedClient {
    private SocketConnector connector;
    private ConnectFuture future;
    private IoSession session;


    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println(MemcachedClient.class.getName() + " <hostname> <port>");
        }

        MemcachedClient client = new MemcachedClient();
        client.connect("182.92.77.50",11211);

        client.store("test","111",1,300);

    }

    public boolean connect(String host, int port){
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(30 * 1000);

        future = connector.connect(new InetSocketAddress(host,port));

        future.awaitUninterruptibly();

        session = future.getSession();

        return true;
    }

    public boolean store(String key,Serializable value,int flags, long exptime) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bos);

        long length = bos.size();

        outputStream.close();

        String msg = String.format("set %s %s %s %s\r\n",key,flags,exptime,length);

        session.write(msg);
        session.write(value);
        return true;
    }


    public boolean quit(){
        //TODO
        CloseFuture closeFuture = session.getCloseFuture();
        future.awaitUninterruptibly();
        connector.dispose();
        return true;
    }




}
