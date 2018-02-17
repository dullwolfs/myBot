package com.dullwolf.controller;

import com.alibaba.fastjson.JSONObject;
import com.dullwolf.common.utils.JsonUtils;
import com.dullwolf.common.utils.R;
import com.dullwolf.pojo.CqMsg;
import com.dullwolf.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dullwolf
 */
@RestController
@RequestMapping("/osu")
public class OsuController {

    @Autowired
    private BotService botService;

    @ResponseBody
    @RequestMapping("/getMsg")
    public R getMsg(@RequestBody JSONObject object){
        Object convert = JsonUtils.convert(object.toJSONString());
        String jsonString = JsonUtils.getJsonString(convert);
        CqMsg cqMsg = JsonUtils.getObjectByJson(jsonString, CqMsg.class);
        String type = cqMsg.getMessageType();
        botService.sendCqMsg(type,cqMsg);
        return R.ok("success");
    }

}
