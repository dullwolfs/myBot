package com.dullwolf.service;

import com.dullwolf.model.MyOSU;
import com.dullwolf.pojo.CqMsg;

import java.util.Map;

/**
 * @author dullwolf
 */
public interface BotService {

    /**
     * 处理酷Q的信息，并且发送消息
     */
    void sendCqMsg(String type,CqMsg cqMsg);

    /**
     * 根据用户名Id查找详细信息
     * @param userId 用户ID
     * @param mode 模式
     * @return
     */
    MyOSU selInfoByUserId(Integer userId, Integer mode);

    /**
     * 插入数据
     */
    void insertData(Map<String,Object> params);

    /**
     * 更新数据
     */
    void updateData();
}
