package com.ljl.utils;

/**
 * @description: 工具类
 */
public class Utility {
    /**
     * 向上取整的除法 用于分配盘块
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 返回 (dividend / divisor) 的向上取整结果
     */
    public static int ceilDivide(int dividend,int divisor) {
        if (dividend % divisor == 0) {
            // 整除
            return dividend / divisor;
        } else {
            // 不整除，向上取整
            return (dividend + divisor) / divisor;
        }
    }

    /**
     * 判断字符串是否为空，或者是否全是空格符
     *
     * @param str 字符串
     * @return true-为空，或者全是空格符
     */
    public static boolean isAllSpace(String str) {
        return (str == null || "".equals(str.trim()));
    }

    /**
     * 解析用户输入的命令
     *
     * @param input 用户的输入
     * @return 解析结果
     */
    public static String[] inputResolve(String input) {
        if (Utility.isAllSpace(input)) {
            return new String[]{""};
        }

        return input.trim().split("\\s+"); //匹配多个空格
    }

}
