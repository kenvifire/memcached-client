package com.kenvifire.memcached.client;

import org.apache.commons.lang3.StringUtils;
import sun.jvm.hotspot.utilities.Assert;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hannahzhang on 15/5/25.
 */
public class MemcachedClient {
    private OutputStream out;
    private InputStream in;



    public static void main(String[] args) throws Exception{
//        if (args.length != 2) {
//            System.out.println(MemcachedClient.class.getName() + " <hostname> <port>");
//        }

        MemcachedClient client = new MemcachedClient();
        client.connect("182.92.77.50",11211);

        Assert.that(client.store("test1", "111", 1, 300), "stored");
        //Assert.that(client.add("test2", "112", 1, 300), "stored");
//        Assert.that(client.replace("test2", "113", 1, 300), "stored");
        client.get("test1");

    }

    public boolean connect(String host, int port) throws IOException{
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host,port));
        out = socket.getOutputStream();
        in = socket.getInputStream();
        return true;
    }

    public boolean store(String key,Serializable value,int flags, long exptime) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bos);
        long length = bos.size();

        outputStream.close();

        String msg = String.format("set %s %s %s %s\r\n",key,flags,exptime,length);
        out.write(msg.getBytes());
        out.write(bos.toByteArray());
        out.write("\r\n".getBytes());
        out.flush();
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        String result = new String(buf,0,len);

        System.out.println(result);
        return StringUtils.equals(result, CacheResult.STORED);

    }

    public boolean add(String key, Serializable value, int flags, long exptime) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bos);
        long length = bos.size();

        outputStream.close();

        String msg = String.format("add %s %s %s %s\r\n",key,flags,exptime,length);
        out.write(msg.getBytes());
        out.write(bos.toByteArray());
        out.write("\r\n".getBytes());
        out.flush();
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        String result = new String(buf,0,len);

        System.out.println(result);
        return StringUtils.equals(result, CacheResult.STORED);

    }

    public boolean replace(String key, Serializable value, int flags, long exptime) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bos);
        long length = bos.size();

        outputStream.close();

        String msg = String.format("replace %s %s %s %s\r\n",key,flags,exptime,length);
        out.write(msg.getBytes());
        out.write(bos.toByteArray());
        out.write("\r\n".getBytes());
        out.flush();
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        String result = new String(buf,0,len);

        System.out.println(result);
        return StringUtils.equals(result, CacheResult.STORED);
    }

    public boolean append(String key, Serializable value, int flags, long exptime) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bos);
        long length = bos.size();

        outputStream.close();

        String msg = String.format("append %s %s %s %s\r\n",key,flags,exptime,length);
        out.write(msg.getBytes());
        out.write(bos.toByteArray());
        out.write("\r\n".getBytes());
        out.flush();
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        String result = new String(buf,0,len);

        System.out.println(result);
        return StringUtils.equals(result, CacheResult.STORED);
    }

    public boolean cas(String key, Serializable value, int flags, long exptime, long casUnique) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(bos);
        long length = bos.size();

        outputStream.close();

        String msg = String.format("cas %s %s %s %s %s\r\n",key,flags,exptime,length,casUnique);
        out.write(msg.getBytes());
        out.write(bos.toByteArray());
        out.write("\r\n".getBytes());
        out.flush();
        byte[] buf = new byte[1024];
        int len = in.read(buf);
        String result = new String(buf,0,len);

        System.out.println(result);
        return StringUtils.equals(result, CacheResult.STORED);
    }


    public Object get(String key) throws IOException,ClassNotFoundException{
        List<String> keys = new ArrayList<String>(1);
        keys.add(key);
        List<Object> result = get(keys);
        if(result != null && result.size() > 0){
            return result.get(0);
        }
        return null;
    }

    public List<Object> get(List<String> keys) throws IOException,ClassNotFoundException{
        StringBuilder sb = new StringBuilder("gets ");

        for(String key : keys){
            sb.append(key).append(' ');
        }
        sb.setCharAt(sb.length()-1,'\r');
        sb.append('\n');

        out.write(sb.toString().getBytes());
        String line = readLine();
        List<Object> cachedObjList = new ArrayList<Object>();
        while (!StringUtils.equals(line,CacheResult.END)){
            String[] data = StringUtils.split(line,' ');
            System.out.println(String.format("key:%s,flag:%s,bytes:%s",data[1],data[2],data[3]));

            // read object
            byte[] result = readBlock();
            ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(result));
            cachedObjList.add(oi.readObject());
            line = readLine();
        }

        return cachedObjList;


    }

    private String readLine() throws IOException{

        byte[] b = new byte[1];
        boolean eol = false;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while( in.read(b, 0, 1) != -1){
            if( b[0] == 13) {
                eol = true;
                bos.write(b,0,1);
                continue;
            } else {
                if( eol ){
                    if( b[0] == 10) {
                        bos.write(b,0,1);
                        break;
                    }

                    eol = false;
                }
            }

            bos.write(b, 0, 1);
        }

        if( bos == null || bos.size()<=0 ){
            throw new IOException("no data read");
        }

        return bos.toString();
    }

    private byte[] readBlock() throws IOException{
        byte[] b = new byte[1];
        boolean eol = false;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while( in.read(b, 0, 1) != -1){
            if( b[0] == 13) {
                eol = true;
                continue;
            } else {
                if( eol ){
                    if( b[0] == 10)
                        break;

                    eol = false;
                }
            }

            bos.write(b, 0, 1);
        }

        if( bos == null || bos.size()<=0 ){
            throw new IOException("no data read");
        }

        return bos.toByteArray();

    }


    public boolean quit(){
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        return true;
    }




}
