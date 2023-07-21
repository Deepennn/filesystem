package com.ljl.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @description: 整个磁盘
 */
public class Disk implements Serializable {
    private static Disk INSTANCE; //磁盘单例
    private Map<String, FCB> DPT; //磁盘分区表  //FCB fcb_disk = Disk.getINSTANCE().getDPT().get(diskName);
    private Block[] blocks; //文件数据块
    private List<FCB> fcbList; //存储在磁盘上的FCB集合
    private int[] fat;//文件分配表

    public static Disk getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Disk();
        }

        return INSTANCE;
    }

    public static void setINSTANCE(Disk INSTANCE) {
        Disk.INSTANCE = INSTANCE;
    }

    public Disk() {}

    public Block[] getBlocks() {
        return blocks;
    }

    public void setBlocks(Block[] blocks) {
        this.blocks = blocks;
    }


    public Map<String, FCB> getDPT() {
        return DPT;
    }

    public void setDPT(Map<String, FCB> DPT) {
        this.DPT = DPT;
    }

    public List<FCB> getFcbList() {
        return fcbList;
    }

    public void setFcbList(List<FCB> fcbList) {
        this.fcbList = fcbList;
    }

    public int[] getFat() {
        return fat;
    }

    public void setFat(int[] fat) {
        this.fat = fat;
    }
}
