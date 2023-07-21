package com.ljl.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 索引结点
 */
public class IndexNode implements Serializable {//索引节点 指向文件的第一个FAT
    private int size; //文件大小
    private int firstBlock; //文件首地址（第一个盘块号）
    private int fcbNum; //文件项个数 如果是普通文件为0 目录看其下有多少个
    private Date updateTime; //文件修改时间

    //如果是磁盘分区
    private int sizeUnused; //磁盘分区可用大小

    public IndexNode(int size, int first_block, int fcbNum, Date updateTime) {
        this.size = size;
        this.sizeUnused =size;
        this.firstBlock = first_block;
        this.fcbNum = fcbNum;
        this.updateTime = updateTime;
    }

    public IndexNode() {}

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getFcbNum() {
        return fcbNum;
    }

    public void addFcbNum() {
        this.fcbNum++;
    }

    public void subFcbNum() {
        this.fcbNum--;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(int firstBlock) {
        this.firstBlock = firstBlock;
    }

    public int getSizeUnused() {
        return sizeUnused;
    }

    public void setSizeUnused(int sizeUnused) {
        this.sizeUnused = sizeUnused;
    }

}
