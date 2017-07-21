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

import com.skill.bot.xiaov.util.XiaoVs;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.*;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Baidu bot query service.
 *
 * @author <a href="https://github.com/qianqingchen">qianqingchen</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 31, 2016
 * @since 1.0.0
 */
@Service
public class BaiduQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(BaiduQueryService.class.getName());

    /**
     * Baidu user cookie.
     */
    private static final String BAIDU_COOKIE = XiaoVs.getString("baidu.cookie");

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Chat with Baidu Robot.
     *
     * @param msg the specified message
     * @return robot returned message, return {@code null} if not found
     */
    public String chat(String msg) {
        if (StringUtils.isBlank(msg)) {
            return null;
        }

        if (msg.startsWith(XiaoVs.QQ_BOT_NAME + " ")) {
            msg = msg.replace(XiaoVs.QQ_BOT_NAME + " ", "");
        }
        if (msg.startsWith(XiaoVs.QQ_BOT_NAME + "，")) {
            msg = msg.replace(XiaoVs.QQ_BOT_NAME + "，", "");
        }
        if (msg.startsWith(XiaoVs.QQ_BOT_NAME + ",")) {
            msg = msg.replace(XiaoVs.QQ_BOT_NAME + ",", "");
        }
        if (msg.startsWith(XiaoVs.QQ_BOT_NAME)) {
            msg = msg.replace(XiaoVs.QQ_BOT_NAME, "");
        }

        if (StringUtils.isBlank(msg)) {
            msg = "你好~";
        }

        String BAIDU_URL = "https://sp0.baidu.com/yLsHczq6KgQFm2e88IuM_a/s?sample_name=bear_brain&request_query=#MSG#&bear_type=2";

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);
        try {

            BAIDU_URL = BAIDU_URL.replace("#MSG#", URLEncoder.encode(msg, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            LOGGER.log(Level.ERROR, "Chat with Baidu Robot failed", e);

            return null;
        }

        try {
            final HTTPHeader header = new HTTPHeader("Cookie", BAIDU_COOKIE);
            request.addHeader(header);
            request.setURL(new URL(BAIDU_URL));

            final HTTPResponse response = URL_FETCH_SVC.fetch(request);
            final JSONObject data = new JSONObject(new String(response.getContent(), "UTF-8"));

            LOGGER.info(new String(response.getContent(), "UTF-8"));

            final String content = (String) data.getJSONArray("result_list").getJSONObject(0).get("result_content");
            String ret = (String) new JSONObject(content).get("answer");
            ret = ret.replaceAll("小度", XiaoVs.QQ_BOT_NAME);

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Chat with Baidu Robot failed", e);
        }

        return null;
    }
}
