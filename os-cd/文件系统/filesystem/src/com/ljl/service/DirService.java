package com.ljl.service;

import com.ljl.pojo.FCB;

/**
 * @description: 目录操作
 */
public interface DirService {
    /**
     * 显示当前目录下的文件名称 修改日期 类型 大小
     *
     * @param dirPath    目录路径
     */
    void dir(String dirPath);

    /**
     * 创建目录
     *
     * @param dirPath    目录路径
     * @return {@link Boolean}
     */
    Boolean mkDir(String dirPath);

    /**
     * 切换目录
     * cd .. 可切换上一级
     *
     * @param dirPath 目录路径
     * @return {@link Boolean}
     */
    Boolean chgDir(String dirPath);

    /**
     * 删除目录
     *
     * @param dirPath 目录路径
     * @return {@link Boolean}
     */
    Boolean delete(String dirPath);

    /**
     * 移动目录
     *
     * @param dirPath 文件路径
     * @param newDirPath 新文件路径
     * @return {@link Boolean}
     */
    Boolean move(String dirPath, String newDirPath);

    /**
     * 路径解析 查看是否存在该文件或目录
     *
     * @param path 路径
     * @return {@link FCB}
     */
    FCB pathResolve(String path);

    /**
     * 递归修改父目录大小
     *
     * @param fcb   FCB
     * @param isAdd 添加文件 add
     */
    void updateSize(FCB fcb,Boolean isAdd,int new_add);

    /**
     * 显示当前全目录路径
     * /ljl/a
     *
     * @param fcb 指定目录
     * @return {@link String} 全路径
     */
    String pwd(FCB fcb);

    /**
     * 显示当前目录
     * /a
     */
    void showPath();

    /**
     * 树状显示目录
     *
     * @param dirPath 目录路径
     */
    void treeDir(String dirPath,int level);
}
