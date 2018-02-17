package bot.dullwolf.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 * 
 * @author dullwolf
 */
public class R extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public static R ok(String data) {
		R r = new R();
		r.put("code", 200);
		r.put("data", data);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R error(String data) {
		R r = new R();
		r.put("code", 400);
		r.put("data", data);
		return r;
	}


}
