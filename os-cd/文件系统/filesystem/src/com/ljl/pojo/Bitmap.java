package com.ljl.pojo;

import com.ljl.constant.Constants;
import com.ljl.utils.Utility;

/**
 * @description: 位示图
 */
public class Bitmap extends FSM{
    private int[][] bitmap;// 0表示空闲 1表示占用

    public Bitmap(int method,int diskSize) {
        this.method = method;
        this.totNum = Utility.ceilDivide(diskSize, Constants.BLOCK_SIZE);//块数=磁盘分区大小/向上整除/块大小
        this.freeNum = Utility.ceilDivide(diskSize, Constants.BLOCK_SIZE);//块数=磁盘分区大小/向上整除/块大小
        this.bitmap = new int[totNum/Constants.COLUMN_COUNT+1][Constants.COLUMN_COUNT];
    }

    public int[][] getBitmap() {
        return bitmap;
    }

    public void setBitmap(int[][] bitmap) {
        this.bitmap = bitmap;
    }

}
