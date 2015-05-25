package com.kenvifire.memcached.client;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by hannahzhang on 15/5/25.
 */
public enum MemcachedCommand {
    SET("set"),
    ADD("add"),
    REPLACE("replace"),
    APPEND("append"),
    PREPEND("prepend");

    private String command;

    private MemcachedCommand(String command){
        this.command = command;
    }

    public static MemcachedCommand parse(String command){
        for(MemcachedCommand commandEnum : values()){
            if(StringUtils.equals(command,commandEnum.command)){
                return commandEnum;
            }
        }
        return null;
    }
}
