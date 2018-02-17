package bot.dullwolf.common.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class StringUtil {

	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

	/**
	 * 判断字符串不为空
	 * 
	 * @param str 需判断的字符串
	 * @return 是|非
	 */
	public static boolean notEmpty(String str) {
		return StringUtils.isNotEmpty(str);
	}

	/**
	 * 判断对象不为空
	 *
	 * @param obj 需判断的对象
	 * @return 是|非
	 */
	public static boolean isNullOrEmpty(Object obj) {
		return null == obj || "".equals(obj);
	}
	
	
	/**
	 * 通过MD5方式对字符串进行加密
	 * 
	 * @return 加密后的字符串
	 */
	public static String setMD5Format(String passWord){
		MessageDigest md;//创建MD5加密对象
		String result = "";
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(passWord.getBytes("UTF-8"));//进行加密
			byte[] md5Bytes = md.digest();//获取加密后的字节数组长
			for (byte md5Byte : md5Bytes) {
				int temp = md5Byte & 0xFF;
				if (temp <= 0xf) {
					result += "0";
				}
				result += Integer.toHexString(temp);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return result;
	}



	/**
	 * 读取流的字符串信息
	 * @param is 输入流
	 */
	public static String inputStreamToString(InputStream is) {
		StringBuilder s = new StringBuilder();
		String line = "";

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) { s.append(line); }
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Return full string
		return s.toString();
	}

	/**
	 * sha1加密
	 * @param str 需要加密的字符串
	 */
	public static String getSha1(String str){
		return DigestUtils.shaHex(str);
	}

	/**
	 * 随机生成的字符串
	 * @param length 长度
	 */
	public static String getRandomString(int length) { //length表示生成字符串的长度
		String base = "ABCDEFGHIJKLMNOPQRSTUVWSYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * String转换成List
	 * @param str id数组字符串
	 */
	public static List<Long> stringToLongList(String str){
		if(StringUtils.isNotBlank(str)){
			Set set;
			try{
				set = JsonUtils.getObjectByJson(str, Set.class);
			}catch (Exception e){
				return null;
			}
			if(set != null && set.size() > 0){
				List<Long> list=new ArrayList<>();
				for(Object item:set){
					long id = Long.parseLong(item.toString());
					if(id > 0)
						list.add(id);
				}
				if(ArrayUtils.isNotEmpty(list.toArray()))
					return list;
			}
		}
		return null;
	}

	/**
	 * String转换成List<String>
	 * @param str id数组json字符串
	 */
	public static List<String> stringToStringList(String str){
		if(StringUtils.isNotBlank(str)){
			Set set;
			try{
				set = JsonUtils.getObjectByJson(str, Set.class);
			}catch (Exception e){
				return null;
			}
			if(set != null && set.size() > 0){
				List<String> list=new ArrayList<>();
				for(Object item:set){
					String id = item.toString();
					if(notEmpty(id))
						list.add(id);
				}
				if(ArrayUtils.isNotEmpty(list.toArray()))
					return list;
			}
		}
		return null;
	}

	/**
	 * 每隔三个数字加一个逗号
	 * @param str1 数字
	 * @return
	 */
	public static String fen(String str1){
		str1 = new StringBuilder(str1).reverse().toString();//先将字符串颠倒顺序
		String str2 = "";
		for(int i=0;i<str1.length();i++){
			if(i*3+3>str1.length()){
				str2 += str1.substring(i*3, str1.length());
				break;
			}
			str2 += str1.substring(i*3, i*3+3)+",";
		}
		if(str2.endsWith(",")){
			str2 = str2.substring(0, str2.length()-1);
		}
		//最后再将顺序反转过来
		return new StringBuilder(str2).reverse().toString();
	}
}
