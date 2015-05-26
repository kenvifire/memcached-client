package com.kenvifire.memcached.client;

import java.io.Closeable;

/**
 * Created by hannahzhang on 15/5/26.
 */
public class IOUtils {
    public static void closeQuietly(Closeable closeable){
        try{
           if(closeable != null) {
               closeable.close();
           }
        }catch (Exception e){
            //ignore
        }finally {

        }
    }
}
