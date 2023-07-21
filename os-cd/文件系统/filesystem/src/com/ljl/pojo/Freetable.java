package com.ljl.pojo;

import com.ljl.constant.Constants;
import com.ljl.utils.Utility;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 空闲表
 */
public class Freetable extends FSM{
    List<Pair<Integer, Integer>> freetable; //<第一个盘块号,盘块数>

    public Freetable(int method,int diskAddress,int diskSize) {
        this.method = method;
        this.freetable = new ArrayList<Pair<Integer, Integer>>();
        this.totNum = Utility.ceilDivide(diskSize, Constants.BLOCK_SIZE);//块数=磁盘分区大小/向上整除/块大小
        this.freetable.add(new Pair(diskAddress,totNum));
    }

    public List<Pair<Integer, Integer>> getFreetable() {
        return freetable;
    }

    public void setFreetable(List<Pair<Integer, Integer>> freetable) {
        this.freetable = freetable;
    }

}
