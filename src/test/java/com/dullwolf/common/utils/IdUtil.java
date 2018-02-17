package com.dullwolf.common.utils;

import com.dullwolf.common.utils.key.SnowflakeIdWorker;

public class IdUtil {

    /**
     * 获取全局唯一ID
     */
    public static long nextId() {
        SnowflakeIdWorker snowflakeIdWorker = SnowflakeIdWorker.getInstance();
        return snowflakeIdWorker.nextId();
    }


}
