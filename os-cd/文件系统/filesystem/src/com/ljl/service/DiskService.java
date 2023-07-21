package com.ljl.service;

import com.ljl.pojo.FCB;

/**
 * @description: 磁盘操作
 */
public interface DiskService {
    void initDisk(String[] initInfo);

    /**
     * 显示磁盘分区名称 修改日期 类型 容量
     */
    void showDisk(String diskName);

    /**
     * 创建磁盘分区
     *
     * @param diskName    磁盘分区名
     * @param diskAddress 磁盘地址
     * @param diskSize    磁盘分区大小
     * @return {@link Boolean}
     */
    public FCB mkDisk(String diskName, int diskAddress, int diskSize);

    /**
     * 切换磁盘分区
     * chgDisk .. 可切换上一级
     *
     * @param diskName    磁盘分区名
     * @return {@link Boolean}
     */
    Boolean chgDisk(String diskName);

    /**
     * (跨盘)转移目录中所有文件（借助栈）
     *
     * @param fcb_src FCB(源)
     * @param diskName_src 磁盘分区名(源)
     * @param diskName_des 磁盘分区名(目标)
     * @return {@link Boolean}
     */
    Boolean moveDir(FCB fcb_src, String diskName_src, String diskName_des);

    /**
     * 释放目录中所有文件占用内存（借助栈）
     *
     * @param fcb FCB
     * @param diskName   磁盘分区名
     * @return {@link Boolean}
     */
    Boolean freeDir(FCB fcb, String diskName);

    /**
     * 释放文件空间 修改FAT表和位示图
     *
     * @param fcb FCB
     * @param diskName   磁盘分区名
     * @return {@link Boolean}
     */
    Boolean freeFile(FCB fcb, String diskName);

    /**
     * 文件内容写入磁盘分区
     *
     * @param content   文件内容
     * @param diskName   磁盘分区名
     * @return int 返回第一块的磁盘号
     */
    int writeToDisk(String content, String diskName);

    /**
     * 寻找空闲块
     *
     * @param needNum  需求块数
     * @param diskName   磁盘分区名
     * @return int 块号
     */
    int findEmpty(int needNum, String diskName);

    /**
     * 显示空闲空间
     *
     * @param diskName   磁盘分区名
     */
    void showFree(String diskName);


}
