package com.dullwolf.dao;

import com.dullwolf.model.MyOSU;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MyOSUDao {

    int deleteByPrimaryKey(Integer id);

    int insert(MyOSU record);

    int insertSelective(MyOSU record);

    MyOSU selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MyOSU record);

    int updateByPrimaryKey(MyOSU record);

    /**
     * 根据用户ID查找信息表ID
     * @param userId 用户Id
     * @return
     */
    Integer selectIdByUserId(@Param("userId") Integer userId, @Param("mode") Integer mode);

    /**
     * 查找所有OSU玩家的userID
     */
    List<Map<String,Object>> selAllOSUInfo();
}