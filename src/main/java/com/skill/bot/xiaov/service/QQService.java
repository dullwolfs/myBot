/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skill.bot.xiaov.service;

import com.skill.bot.smartqq.callback.MessageCallback;
import com.skill.bot.smartqq.client.SmartQQClient;
import com.skill.bot.smartqq.model.*;
import com.skill.bot.xiaov.util.XiaoVs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.latke.util.Strings;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * QQ service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.3.9, Aug 28, 2016
 * @since 1.0.0
 */
@Service
public class QQService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(QQService.class.getName());

    /**
     * QQ groups.
     *
     * &lt;groupId, group&gt;
     */
    private final Map<Long, Group> QQ_GROUPS = new ConcurrentHashMap<>();

    /**
     * The latest group ad time.
     *
     * &lt;groupId, time&gt;
     */
    private final Map<Long, Long> GROUP_AD_TIME = new ConcurrentHashMap<>();

    /**
     * QQ discusses.
     *
     * &lt;discussId, discuss&gt;
     */
    private final Map<Long, Discuss> QQ_DISCUSSES = new ConcurrentHashMap<>();

    /**
     * The latest discuss ad time.
     *
     * &lt;discussId, time&gt;
     */
    private final Map<Long, Long> DISCUSS_AD_TIME = new ConcurrentHashMap<>();

    /**
     * 是否启用小薇的守护来进行消息送达确认.
     */
    private final boolean MSG_ACK_ENABLED = XiaoVs.getBoolean("qq.bot.ack");

    /**
     * QQ client.
     */
    private SmartQQClient xiaoV;

    /**
     * QQ client listener.
     */
    private SmartQQClient xiaoVListener;

    /**
     * Group sent messages.
     */
    private final List<String> GROUP_SENT_MSGS = new CopyOnWriteArrayList<>();

    /**
     * Discuss sent messages.
     */
    private final List<String> DISCUSS_SENT_MSGS = new CopyOnWriteArrayList<>();

    /**
     * Turing query service.
     */
    @Inject
    private TuringQueryService turingQueryService;

    /**
     * Baidu bot query service.
     */
    @Inject
    private BaiduQueryService baiduQueryService;

    /**
     * ITPK query service.
     */
    @Inject
    private ItpkQueryService itpkQueryService;

    /**
     * Bot type.
     */
    private static final int QQ_BOT_TYPE = XiaoVs.getInt("qq.bot.type");

    /**
     * Advertisements.
     */
    private static final List<String> ADS = new ArrayList<>();

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * XiaoV self intro. Built-in advertisement.
     */
    private static final String XIAO_V_INTRO = "你好，我是小薇~ 关于我的更多资料请看帖 https://hacpai.com/article/1467011936362";

    /**
     * XiaoV listener self intro.
     */
    private static final String XIAO_V_LISTENER_INTRO = "你好，我是小薇的守护，关于我的更多资料请看帖 https://hacpai.com/article/1467011936362";

    /**
     * No listener message.
     */
    private static final String NO_LISTENER = "请把我的守护也拉进群，否则会造成大量消息重复（如果已经拉了，那就稍等 10 秒钟，我的守护可能在醒瞌睡 O(∩_∩)O哈哈~）\n\nPS：小薇机器人使用问题请看帖 https://hacpai.com/article/1467011936362";

    /**
     * 超过 {@value #PUSH_GROUP_USER_COUNT} 个成员的群才推送.
     */
    private static int PUSH_GROUP_USER_COUNT = XiaoVs.getInt("qq.bot.pushGroupUserCnt");

    /**
     * 记录未群推过的群 id 集合.
     */
    private static final Set<Long> UNPUSH_GROUPS = new CopyOnWriteArraySet<>();

    /**
     * 一次群推操作最多只推送 {@value #PUSH_GROUP_COUNT} 个群（为了尽量保证成功率）.
     */
    private static final int PUSH_GROUP_COUNT = 5;

    static {
        String adConf = XiaoVs.getString("ads");
        if (StringUtils.isNotBlank(adConf)) {
            final String[] ads = adConf.split("#");
            ADS.addAll(Arrays.asList(ads));
        }

        ADS.add(XIAO_V_INTRO);
        ADS.add(XIAO_V_INTRO);
        ADS.add(XIAO_V_INTRO);
    }

    /**
     * Initializes QQ client.
     */
    public void initQQClient() {
        LOGGER.info("开始初始化小薇");

        xiaoV = new SmartQQClient(new MessageCallback() {
            @Override
            public void onMessage(final Message message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500 + RandomUtils.nextInt(1000));

                            final String content = message.getContent();
                            final String key = XiaoVs.getString("qq.bot.key");
                            if (!StringUtils.startsWith(content, key)) { // 不是管理命令，只是普通的私聊
                                // 让小薇进行自我介绍
                                xiaoV.sendMessageToFriend(message.getUserId(), XIAO_V_INTRO);

                                return;
                            }

                            final String msg = StringUtils.substringAfter(content, key);
                            LOGGER.info("Received admin message: " + msg);
                            sendToPushQQGroups(msg);
                        } catch (final Exception e) {
                            LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                        }
                    }
                }).start();
            }

            @Override
            public void onGroupMessage(final GroupMessage message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500 + RandomUtils.nextInt(1000));

                            onQQGroupMessage(message);
                        } catch (final Exception e) {
                            LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                        }
                    }
                }).start();
            }

            @Override
            public void onDiscussMessage(final DiscussMessage message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500 + RandomUtils.nextInt(1000));

                            onQQDiscussMessage(message);
                        } catch (final Exception e) {
                            LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                        }
                    }
                }).start();
            }
        });

        // Load groups & disscusses
        reloadGroups();
        reloadDiscusses();

        LOGGER.info("小薇初始化完毕");

        if (MSG_ACK_ENABLED) { // 如果启用了消息送达确认
            LOGGER.info("开始初始化小薇的守护（细节请看：https://github.com/b3log/xiaov/issues/3）");

            xiaoVListener = new SmartQQClient(new MessageCallback() {
                @Override
                public void onMessage(final Message message) {
                    try {
                        Thread.sleep(500 + RandomUtils.nextInt(1000));
                        final String content = message.getContent();
                        final String key = XiaoVs.getString("qq.bot.key");
                        if (!StringUtils.startsWith(content, key)) { // 不是管理命令
                            // 让小薇的守护进行自我介绍
                            xiaoVListener.sendMessageToFriend(message.getUserId(), XIAO_V_LISTENER_INTRO);

                            return;
                        }

                        final String msg = StringUtils.substringAfter(content, key);
                        LOGGER.info("Received admin message: " + msg);
                        sendToPushQQGroups(msg);
                    } catch (final Exception e) {
                        LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                    }
                }

                @Override
                public void onGroupMessage(final GroupMessage message) {
                    final String content = message.getContent();

                    if (GROUP_SENT_MSGS.contains(content)) { // indicates message received
                        GROUP_SENT_MSGS.remove(content);
                    }
                }

                @Override
                public void onDiscussMessage(final DiscussMessage message) {
                    final String content = message.getContent();

                    if (DISCUSS_SENT_MSGS.contains(content)) { // indicates message received
                        DISCUSS_SENT_MSGS.remove(content);
                    }
                }
            });

            LOGGER.info("小薇的守护初始化完毕");
        }

        LOGGER.info("小薇 QQ 机器人服务开始工作！");
    }

    private void sendToForum(final String msg, final String user) {
        final String forumAPI = XiaoVs.getString("forum.api");
        final String forumKey = XiaoVs.getString("forum.key");

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(forumAPI));

            final String body = "key=" + URLEncoder.encode(forumKey, "UTF-8")
                    + "&msg=" + URLEncoder.encode(msg, "UTF-8")
                    + "&user=" + URLEncoder.encode(user, "UTF-8");
            request.setPayload(body.getBytes("UTF-8"));

            final HTTPResponse response = URL_FETCH_SVC.fetch(request);
            final int sc = response.getResponseCode();
            if (HttpServletResponse.SC_OK != sc) {
                LOGGER.warn("Sends message to Forum status code is [" + sc + "]");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends message to Forum failed: " + e.getMessage());
        }
    }

    /**
     * Closes QQ client.
     */
    public void closeQQClient() {
        if (null == xiaoV) {
            return;
        }

        try {
            xiaoV.close();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Closes QQ client failed", e);
        }
    }

    /**
     * Sends the specified article to QQ groups.
     *
     * @param msg the specified message
     */
    public void sendToPushQQGroups(final String msg) {
        try {
            final String pushGroupsConf = XiaoVs.getString("qq.bot.pushGroups");
            if (StringUtils.isBlank(pushGroupsConf)) {
                return;
            }

            // Push to all groups
            if (StringUtils.equals(pushGroupsConf, "*")) {
                int totalUserCount = 0;
                int groupCount = 0;

                if (UNPUSH_GROUPS.isEmpty()) { // 如果没有可供推送的群（群都推送过了）
                    reloadGroups();
                }

                for (final Map.Entry<Long, Group> entry : QQ_GROUPS.entrySet()) {
                    long groupId = 0;
                    int userCount = 0;

                    try {
                        final Group group = entry.getValue();
                        groupId = group.getId();

                        final GroupInfo groupInfo = xiaoV.getGroupInfo(group.getCode());
                        userCount = groupInfo.getUsers().size();
                        if (userCount < PUSH_GROUP_USER_COUNT) {
                            // 把人不多的群过滤掉
                            UNPUSH_GROUPS.remove(groupId);

                            continue;
                        }

                        if (!UNPUSH_GROUPS.contains(groupId)) {
                            // 如果该群已经被推送过则跳过本次推送
                            continue;
                        }

                        if (groupCount >= PUSH_GROUP_COUNT) { // 如果本次群推操作已推送群数大于设定的阈值
                            break;
                        }

                        LOGGER.info("群发 [" + msg + "] 到 QQ 群 [" + group.getName() + ", 成员数=" + userCount + "]");
                        xiaoV.sendMessageToGroup(groupId, msg); // Without retry

                        UNPUSH_GROUPS.remove(groupId); // 从未推送中移除（说明已经推送过）

                        totalUserCount += userCount;
                        groupCount++;

                        Thread.sleep(1000 * 10);
                    } catch (final Exception e) {
                        LOGGER.log(Level.ERROR, "群发异常", e);
                    }
                }

                LOGGER.info("一共推送了 [" + groupCount + "] 个群，覆盖 [" + totalUserCount + "] 个 QQ");

                return;
            }

            // Push to the specified groups
            final String[] groups = pushGroupsConf.split(",");
            for (final Map.Entry<Long, Group> entry : QQ_GROUPS.entrySet()) {
                final Group group = entry.getValue();
                final String name = group.getName();

                if (Strings.contains(name, groups)) {
                    final GroupInfo groupInfo = xiaoV.getGroupInfo(group.getCode());
                    final int userCount = groupInfo.getUsers().size();
                    if (userCount < 100) {
                        continue;
                    }

                    LOGGER.info("Pushing [msg=" + msg + "] to QQ qun [" + group.getName() + "]");
                    xiaoV.sendMessageToGroup(group.getId(), msg); // Without retry

                    Thread.sleep(1000 * 10);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Push message [" + msg + "] to groups failed", e);
        }
    }

    private void sendMessageToGroup(final Long groupId, final String msg) {
        Group group = QQ_GROUPS.get(groupId);
        if (null == group) {
            reloadGroups();

            group = QQ_GROUPS.get(groupId);
        }

        if (null == group) {
            LOGGER.log(Level.ERROR, "Group list error [groupId=" + groupId + "], 请先参考项目主页 FAQ 解决"
                    + "（https://github.com/b3log/xiaov#报错-group-list-error-groupidxxxx-please-report-this-bug-to-developer-怎么破），"
                    + "如果还有问题，请到论坛讨论帖中进行反馈（https://hacpai.com/article/1467011936362）");

            return;
        }

        LOGGER.info("Pushing [msg=" + msg + "] to QQ qun [" + group.getName() + "]");
        xiaoV.sendMessageToGroup(groupId, msg);

        if (MSG_ACK_ENABLED) { // 如果启用了消息送达确认
            // 进行消息重发

            GROUP_SENT_MSGS.add(msg);

            if (GROUP_SENT_MSGS.size() > QQ_GROUPS.size() * 5) {
                GROUP_SENT_MSGS.remove(0);
            }

            final int maxRetries = 3;
            int retries = 0;
            int sentTries = 0;
            while (retries < maxRetries) {
                retries++;

                try {
                    Thread.sleep(3500);
                } catch (final Exception e) {
                    continue;
                }

                if (GROUP_SENT_MSGS.contains(msg)) {
                    LOGGER.info("Pushing [msg=" + msg + "] to QQ qun [" + group.getName() + "] with retries [" + retries + "]");
                    xiaoV.sendMessageToGroup(groupId, msg);
                    sentTries++;
                }
            }

            if (maxRetries == sentTries) {
                LOGGER.info("Pushing [msg=" + msg + "] to QQ qun [" + group.getName() + "]");
                xiaoV.sendMessageToGroup(groupId, NO_LISTENER);
            }
        }
    }

    private void sendMessageToDiscuss(final Long discussId, final String msg) {
        Discuss discuss = QQ_DISCUSSES.get(discussId);
        if (null == discuss) {
            reloadDiscusses();

            discuss = QQ_DISCUSSES.get(discussId);
        }

        if (null == discuss) {
            LOGGER.log(Level.ERROR, "Discuss list error [discussId=" + discussId + "], 请先参考项目主页 FAQ 解决"
                    + "（https://github.com/b3log/xiaov#报错-group-list-error-groupidxxxx-please-report-this-bug-to-developer-怎么破），"
                    + "如果还有问题，请到论坛讨论帖中进行反馈（https://hacpai.com/article/1467011936362）");

            return;
        }

        LOGGER.info("Pushing [msg=" + msg + "] to QQ discuss [" + discuss.getName() + "]");
        xiaoV.sendMessageToDiscuss(discussId, msg);

        if (MSG_ACK_ENABLED) { // 如果启用了消息送达确认
            // 进行消息重发

            DISCUSS_SENT_MSGS.add(msg);

            if (DISCUSS_SENT_MSGS.size() > QQ_DISCUSSES.size() * 5) {
                DISCUSS_SENT_MSGS.remove(0);
            }

            final int maxRetries = 3;
            int retries = 0;
            int sentTries = 0;
            while (retries < maxRetries) {
                retries++;

                try {
                    Thread.sleep(3500);
                } catch (final Exception e) {
                    continue;
                }

                if (GROUP_SENT_MSGS.contains(msg)) {
                    LOGGER.info("Pushing [msg=" + msg + "] to QQ discuss [" + discuss.getName() + "] with retries [" + retries + "]");
                    xiaoV.sendMessageToDiscuss(discussId, msg);
                    sentTries++;
                }
            }

            if (maxRetries == sentTries) {
                LOGGER.info("Pushing [msg=" + msg + "] to QQ discuss [" + discuss.getName() + "]");
                xiaoV.sendMessageToDiscuss(discussId, NO_LISTENER);
            }
        }
    }

    public void onQQGroupMessage(final GroupMessage message) {
        final long groupId = message.getGroupId();

        final String content = message.getContent();
        final String userName = Long.toHexString(message.getUserId());
        // Push to forum
        String qqMsg = content.replaceAll("\\[\"face\",[0-9]+\\]", "");
        if (StringUtils.isNotBlank(qqMsg)) {
            qqMsg = "<p>" + qqMsg + "</p>";
            sendToForum(qqMsg, userName);
        }

        String msg = "";
        if (StringUtils.contains(content, XiaoVs.QQ_BOT_NAME)
                || (StringUtils.length(content) > 6
                && (StringUtils.contains(content, "?") || StringUtils.contains(content, "？") || StringUtils.contains(content, "问")))) {
            msg = answer(content, userName);
        }

        if (StringUtils.isBlank(msg)) {
            return;
        }

        if (RandomUtils.nextFloat() >= 0.9) {
            Long latestAdTime = GROUP_AD_TIME.get(groupId);
            if (null == latestAdTime) {
                latestAdTime = 0L;
            }

            final long now = System.currentTimeMillis();

            if (now - latestAdTime > 1000 * 60 * 30) {
                msg = msg + "\n\n（" + ADS.get(RandomUtils.nextInt(ADS.size())) + "）";

                GROUP_AD_TIME.put(groupId, now);
            }
        }

        sendMessageToGroup(groupId, msg);
    }

    public void onQQDiscussMessage(final DiscussMessage message) {
        final long discussId = message.getDiscussId();

        final String content = message.getContent();
        final String userName = Long.toHexString(message.getUserId());
        // Push to forum
        String qqMsg = content.replaceAll("\\[\"face\",[0-9]+\\]", "");
        if (StringUtils.isNotBlank(qqMsg)) {
            qqMsg = "<p>" + qqMsg + "</p>";
            sendToForum(qqMsg, userName);
        }

        String msg = "";
        if (StringUtils.contains(content, XiaoVs.QQ_BOT_NAME)
                || (StringUtils.length(content) > 6
                && (StringUtils.contains(content, "?") || StringUtils.contains(content, "？") || StringUtils.contains(content, "问")))) {
            msg = answer(content, userName);
        }

        if (StringUtils.isBlank(msg)) {
            return;
        }

        if (RandomUtils.nextFloat() >= 0.9) {
            Long latestAdTime = DISCUSS_AD_TIME.get(discussId);
            if (null == latestAdTime) {
                latestAdTime = 0L;
            }

            final long now = System.currentTimeMillis();

            if (now - latestAdTime > 1000 * 60 * 30) {
                msg = msg + "\n\n（" + ADS.get(RandomUtils.nextInt(ADS.size())) + "）";

                DISCUSS_AD_TIME.put(discussId, now);
            }
        }

        sendMessageToDiscuss(discussId, msg);
    }

    private String answer(final String content, final String userName) {
        String keyword = "";
        String[] keywords = StringUtils.split(XiaoVs.getString("bot.follow.keywords"), ",");
        keywords = Strings.trimAll(keywords);
        for (final String kw : keywords) {
            if (StringUtils.containsIgnoreCase(content, kw)) {
                keyword = kw;

                break;
            }
        }

        String ret = "";
        if (StringUtils.isNotBlank(keyword)) {
            try {
                ret = XiaoVs.getString("bot.follow.keywordAnswer");
                ret = StringUtils.replace(ret, "{keyword}",
                        URLEncoder.encode(keyword, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                LOGGER.log(Level.ERROR, "Search key encoding failed", e);
            }
        } else if (StringUtils.contains(content, XiaoVs.QQ_BOT_NAME)) {
            if (1 == QQ_BOT_TYPE) {
                ret = turingQueryService.chat(userName, content);
                ret = StringUtils.replace(ret, "图灵机器人", XiaoVs.QQ_BOT_NAME + "机器人");
                ret = StringUtils.replace(ret, "默认机器人", XiaoVs.QQ_BOT_NAME + "机器人");

                ret = StringUtils.replace(ret, "<br>", "\n");
            } else if (2 == QQ_BOT_TYPE) {
                ret = baiduQueryService.chat(content);
            } else if (3 == QQ_BOT_TYPE) {
                ret = itpkQueryService.chat(content);
            }

            if (StringUtils.isBlank(ret)) {
                ret = "嗯~";
            }
        }

        return ret;
    }

    private void reloadGroups() {
        final List<Group> groups = xiaoV.getGroupList();
        QQ_GROUPS.clear();
        GROUP_AD_TIME.clear();
        UNPUSH_GROUPS.clear();

        final StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Reloaded groups: \n");
        for (final Group g : groups) {
            QQ_GROUPS.put(g.getId(), g);
            GROUP_AD_TIME.put(g.getId(), 0L);
            UNPUSH_GROUPS.add(g.getId());

            msgBuilder.append("    ").append(g.getName()).append(": ").append(g.getId()).append("\n");
        }

        LOGGER.log(Level.INFO, msgBuilder.toString());
    }

    private void reloadDiscusses() {
        final List<Discuss> discusses = xiaoV.getDiscussList();
        QQ_DISCUSSES.clear();
        DISCUSS_AD_TIME.clear();

        final StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Reloaded discusses: \n");
        for (final Discuss d : discusses) {
            QQ_DISCUSSES.put(d.getId(), d);
            DISCUSS_AD_TIME.put(d.getId(), 0L);

            msgBuilder.append("    ").append(d.getName()).append(": ").append(d.getId()).append("\n");
        }

        LOGGER.log(Level.INFO, msgBuilder.toString());
    }
}
