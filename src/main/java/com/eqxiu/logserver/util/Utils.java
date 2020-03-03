package com.eqxiu.logserver.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	public static String toJson(Object o){
		return JSON.toJSONString(o);
	}

	public static String isNulDefault(String val,String def){
		if (StrUtil.isEmpty(val)){
			return def;
		}else{
			return val;
		}
	}

	/**
	 *  判断字符串是否是乱码
	 * @param strName
	 */
	public static boolean isMessyCode(String strName) {
        try {
            Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
            Matcher m = p.matcher(strName);
            String after = m.replaceAll("");
            String temp = after.replaceAll("\\p{P}", "");
            char[] ch = temp.trim().toCharArray();

            int length = (ch != null) ? ch.length : 0;
            for (int i = 0; i < length; i++) {
                char c = ch[i];
                if (!Character.isLetterOrDigit(c)) {
                    String str = "" + ch[i];
                    if (!str.matches("[\u4e00-\u9fa5]+")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
	public static String hash(String key ,int len){
//		int len = 8;
		String c1 = Math.abs(key.hashCode())+"";
		if(c1.length() >= len){
			return c1.substring(0, len);
		}
		String c2 = Math.abs(StrUtil.reverse(key).hashCode())+"";
		String c = c1+c2;
		if(c.length() < len){
			while(c.length() < len){
				c += "0";
			}
		}
		return c.substring(0, len);
	}
	public static Double formatDouble(double number ,String format){
		DecimalFormat df = new DecimalFormat(format);
		 return Double.valueOf(df.format(number));
	}
	
	public static void main(String[] args) {
		System.out.println(formatDouble(0.666, "0.0"));
	}
}
