package com.eqxiu.logserver.util;

import com.eqxiu.logserver.ipip.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class LogConvert {


    private static final Logger logger = LoggerFactory.getLogger(LogConvert.class);

    // 加载字段映射表
    public static Map<String, String> netTypeMap = new HashMap<>(); // n_t
    public static Map<String, String> mobileOperatorMap = new HashMap<>(); // m_o
    public static Map<String, String> manufacturerMap = new HashMap<>(); // man
    public static Map<String, String> operationSystemMap = new HashMap<>(); // os

    public static City city;
    static {

        // 加载IP库
        String path = "";
        try {
            URL resource = LogConvert.class.getClassLoader().getResource("mydata4vipweek2.datx");
            path = resource.getPath();
            city = new City(path);
        } catch (Exception e) {
            logger.warn("未找到IP库文件：" + path);
        }


        netTypeMap.put("WIFI", "WIFI");
        netTypeMap.put("wifi", "WIFI");
        netTypeMap.put("4G", "4G");
        netTypeMap.put("4g", "4G");
        netTypeMap.put("LTE", "4G");
        netTypeMap.put("LTE-CA", "4G");
        netTypeMap.put("3G", "3G");
        netTypeMap.put("3g", "3G");

        mobileOperatorMap.put("鹏博士/电信", "电信");
        mobileOperatorMap.put("电信", "电信");
        mobileOperatorMap.put("方正宽带/电信", "电信");
        mobileOperatorMap.put("科技网/电信", "电信");
        mobileOperatorMap.put("电信/联通", "电信");
        mobileOperatorMap.put("鹏博士/联通", "联通");
        mobileOperatorMap.put("联通", "联通");
        mobileOperatorMap.put("天威视讯/联通", "联通");
        mobileOperatorMap.put("浙江华数/联通", "联通");
        mobileOperatorMap.put("科技网/联通", "联通");
        mobileOperatorMap.put("联通/电信", "联通");
        mobileOperatorMap.put("铁通", "移动");
        mobileOperatorMap.put("移动", "移动");
        mobileOperatorMap.put("鹏博士/铁通", "移动");

        manufacturerMap.put("apple", "Apple");
        manufacturerMap.put("Apple", "Apple");
        manufacturerMap.put("iPhone7plus", "Apple");
        manufacturerMap.put("iPhone", "Apple");
        manufacturerMap.put("Huawei", "华为");
        manufacturerMap.put("HUAWEI", "华为");
        manufacturerMap.put("huawei", "华为");
        manufacturerMap.put("OPPO", "Oppo");
        manufacturerMap.put("Oppo", "Oppo");
        manufacturerMap.put("oppo", "Oppo");
        manufacturerMap.put("vivo", "Vivo");
        manufacturerMap.put("Vivo", "Vivo");
        manufacturerMap.put("Xiaomi", "小米");
        manufacturerMap.put("xiaomi", "小米");
        manufacturerMap.put("XiaoMi", "小米");
        manufacturerMap.put("blackshark", "小米");
        manufacturerMap.put("samsung", "Samsung");
        manufacturerMap.put("Samsung", "Samsung");
        manufacturerMap.put("SAMSUNG", "Samsung");
        manufacturerMap.put("Meizu", "魅族");
        manufacturerMap.put("meizu", "魅族");
        manufacturerMap.put("GIONEE", "金立");
        manufacturerMap.put("Gionee", "金立");
        manufacturerMap.put("GiONEE", "金立");
        manufacturerMap.put("Meitu", "美图");
        manufacturerMap.put("smartisan", "锤子");
        manufacturerMap.put("Smartisan", "锤子");
        manufacturerMap.put("360", "360");
        manufacturerMap.put("OnePlus", "一加");
        manufacturerMap.put("ONEPLUS", "一加");
        manufacturerMap.put("LeMobile", "乐视");
        manufacturerMap.put("lemobile", "乐视");
        manufacturerMap.put("Letv", "乐视");
        manufacturerMap.put("letv", "乐视");
        manufacturerMap.put("Nubia", "努比亚");
        manufacturerMap.put("nubia", "努比亚");
        manufacturerMap.put("ZTE", "中兴");
        manufacturerMap.put("Zte", "中兴");
        manufacturerMap.put("zte", "中兴");
        manufacturerMap.put("Coolpad", "酷派");
        manufacturerMap.put("coolpad", "酷派");
        manufacturerMap.put("HMD Global", "诺基亚");
        manufacturerMap.put("HMD Global Oy", "诺基亚");
        manufacturerMap.put("nokia", "诺基亚");
        manufacturerMap.put("Nokia", "诺基亚");
        manufacturerMap.put("LENOVO", "联想");
        manufacturerMap.put("ZUK", "联想");
        manufacturerMap.put("Lenovo", "联想");
        manufacturerMap.put("DOOV", "朵唯");
        manufacturerMap.put("Doov", "朵唯");
        manufacturerMap.put("hisense", "海信");
        manufacturerMap.put("Hisense", "海信");
        manufacturerMap.put("Sony", "索尼");
        manufacturerMap.put("sony", "索尼");
        manufacturerMap.put("Sony ericsson", "索尼");
        manufacturerMap.put("Gree", "格力");
        manufacturerMap.put("gree", "格力");
        manufacturerMap.put("GREE", "格力");
        manufacturerMap.put("asus", "华硕");
        manufacturerMap.put("ASUS", "华硕");
        manufacturerMap.put("Asus", "华硕");
        manufacturerMap.put("LGE", "LG");
        manufacturerMap.put("LG", "LG");
        manufacturerMap.put("Lg", "LG");
        manufacturerMap.put("BlackBerry", "黑莓");

        operationSystemMap.put("Android", "Android");
        operationSystemMap.put("android", "Android");
        operationSystemMap.put("iOS", "iOS");
        operationSystemMap.put("ios", "iOS");
        operationSystemMap.put("Windows", "PC");
        operationSystemMap.put("windows", "PC");
        operationSystemMap.put("Mac OS X", "PC");
        operationSystemMap.put("mac os x", "PC");
    }

    // 国家、地区、城市、运营商
    public static  Map<? extends String, ?> getIpipInfo(String ip) {
        Map<String, String> map = new HashMap<>();
        try {
            String[] info = city.find(ip);
            if (info != null && info.length >= 3) {
                map.put("cou", info[0]); // country
                map.put("pro", info[1]); // province
                map.put("cit", info[2]); // city
            }
            if (info != null && info.length >= 5) {
                map.put("m_o", info[4]); // mobile operator
            }
        } catch (Exception e) {
//            System.err.println("区域信息解析失败！ ip: " + ip);
        }

        return map;
    }


}
