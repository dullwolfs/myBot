package com.dullwolf.common.task;

import com.dullwolf.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 *
 * @author dullwolf
 */
@Configuration
@EnableScheduling // 启用定时任务
public class Jobs {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BotService botService;

    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void scheduler() {
        botService.updateData();
    }

}
