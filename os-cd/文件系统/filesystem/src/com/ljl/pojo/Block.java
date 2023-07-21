package com.ljl.pojo;

import java.io.Serializable;

/**
 * @description: 物理盘块
 */
public class Block implements Serializable {
    private int id; //块号
    private int blockSize; //块大小
    private String content; //块内容

    private FSM fsm;//(分区首块)空闲分区管理

    public Block(int id, int blockSize, String content) {
        this.id = id;
        this.blockSize = blockSize;
        this.content = content;
    }

    public Block() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public FSM getFsm() {
        return fsm;
    }

    public void setFsm(FSM fsm) {
        this.fsm = fsm;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", blockSize=" + blockSize +
                ", content=\"" + content + "\"" +
                ", fsm=" + fsm +
                '}';
    }
}
