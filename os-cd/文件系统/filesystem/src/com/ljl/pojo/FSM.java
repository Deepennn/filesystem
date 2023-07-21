package com.ljl.pojo;

import java.io.Serializable;

/**
 * @description: 空闲空间管理 (Free Space Management)
 */
public abstract class FSM implements Serializable {
    protected int method; //空闲管理方法

    protected int totNum; //磁盘分区总块数

    protected int freeNum; //磁盘分区空闲块数

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getTotNum() {
        return totNum;
    }

    public void setTotNum(int totNum) {
        this.totNum = totNum;
    }

    public int getFreeNum() {
        return freeNum;
    }

    public void setFreeNum(int freeNum) {
        this.freeNum = freeNum;
    }
}
