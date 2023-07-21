package com.ljl.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 内存
 */
public class Memory implements Serializable {
    private static Memory INSTANCE; //内存单例
    private FCB curDisk; //当前磁盘分区
    private FCB curDir; //当前目录
    private FCB rootDir;  //根目录常驻内存 根目录可以引出整个文件目录 从磁盘读取
    private int[] fat;//文件分配表 从磁盘读取
    private List<OpenFile> openFileList; //打开的文件集合


    public Memory() {
    }
    public static Memory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Memory();
        }

        return INSTANCE;
    }

    public FCB getCurDisk() {
        return curDisk;
    }

    public void setCurDisk(FCB curDisk) {
        this.curDisk = curDisk;
    }

    public FCB getCurDir() {
        return curDir;
    }

    public void setCurDir(FCB curDir) {
        this.curDir = curDir;
    }

    public FCB getRootDir() {
        return rootDir;
    }

    public void setRootDir(FCB rootDir) {
        this.rootDir = rootDir;
    }

    public int[] getFat() {
        return fat;
    }

    public void setFat(int[] fat) {
        this.fat = fat;
    }

    public List<OpenFile> getOpenFileList() {
        return openFileList;
    }

    public void setOpenFileList(List<OpenFile> openFileList) {
        this.openFileList = openFileList;
    }

}
