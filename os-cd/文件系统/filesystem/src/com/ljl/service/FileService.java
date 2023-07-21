package com.ljl.service;

import java.io.IOException;

/**
 * @description: 文件操作
 */
public interface FileService {

    /**
     * 创建文件
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    Boolean create(String filePath);

    /**
     * 打开文件
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    Boolean open(String filePath);

    /**
     * 读取文件
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    Boolean read(String filePath);

    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    Boolean write(String filePath);

    /**
     * 关闭文件
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    Boolean close(String filePath);

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    Boolean delete(String filePath);

    /**
     * 复制
     *
     * @param filePath 文件路径
     * @param newDirPath 新目录路径
     * @return {@link Boolean}
     */
    Boolean copy(String filePath,String newDirPath);

    /**
     * 导出
     *
     * @param filePath 文件路径
     * @param exDirPath 外目录路径
     * @return {@link Boolean}
     */
    Boolean export(String filePath,String exDirPath) throws IOException;
}
