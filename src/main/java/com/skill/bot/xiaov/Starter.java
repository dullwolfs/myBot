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
package com.skill.bot.xiaov;

import org.b3log.latke.Latkes;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

/**
 * XiaoV with embedded Jetty.
 *
 * <ul>
 * <li>Windows: java -cp WEB-INF/lib/*;WEB-INF/classes Starter</li>
 * <li>Unix-like: java -cp WEB-INF/lib/*:WEB-INF/classes Starter</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Sep 24, 2016
 * @since 2.2.0
 */
public class Starter {

    static {
        try {
            Log.setLog(new Slf4jLog());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Latkes.setScanPath("org.b3log.xiaov"); // For Latke IoC
        Latkes.initRuntimeEnv();

        final String classesPath = ClassLoader.getSystemResource("").getPath(); // Real path including maven sub folder
        String webappDirLocation = classesPath.replace("target/classes/", "src/main/webapp/"); // POM structure in dev env
        final File file = new File(webappDirLocation);
        if (!file.exists()) {
            webappDirLocation = "."; // production environment
        }

        final Server server = new Server(Integer.valueOf(Latkes.getServerPort()));
        final WebAppContext root = new WebAppContext();
        root.setParentLoaderPriority(true); // Use parent class loader
        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        server.setHandler(root);

        try {
            server.start();
        } catch (final Exception e) {
            e.printStackTrace();

            System.exit(-1);
        }
    }
}
