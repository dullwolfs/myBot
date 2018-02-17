package bot.dullwolf.common.utils;


import org.apache.commons.lang.ArrayUtils;

import java.util.List;

public class ArrayUtil {

    /**
     * 判断数组不为空
     * @param list 数组
     * @return 为空false 不为空true
     */
    public static boolean isNotEmpty(List list){
        return null != list && ArrayUtils.isNotEmpty(list.toArray());
    }

}
