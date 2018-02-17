package bot.dullwolf.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 读取配置文件中系统参数配置信息的工具类
 * 
 * @author dullwolf
 */
public final class PropertiesUtil {
	private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
	
	private static final String DEFULT_CONFIG_FILE = "app.properties";//默认的配置文件


	private static class MCDHInstance {
		private static final PropertiesUtil Pro = new PropertiesUtil();
	}

	/**
	 * 从默认的配置文件中读取参数值
	 * @param key 参数key
	 * @return
	 */
	public static String readKey(String key) {
		Properties proper = MCDHInstance.Pro.readProperties(DEFULT_CONFIG_FILE);
		return (String) proper.get(key);
	}

	private Properties readProperties(String path) {
		if ("".equals(path) || path == null) {
			path = DEFULT_CONFIG_FILE;
		}
		Properties proper = new Properties();
		try {
			proper.load(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path), "UTF-8"));
		} catch (IOException e) {
			logger.error("加载系统参数配置文件 app.properties 出错!");
		}
		return proper;
	}
	

}
