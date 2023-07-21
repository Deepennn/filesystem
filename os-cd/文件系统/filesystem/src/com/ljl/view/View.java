package com.ljl.view;

import com.ljl.pojo.FCB;
import com.ljl.pojo.IndexNode;

import java.text.SimpleDateFormat;

/**
 * @description: 展示交互类
 */
public class View {
    public void help(){
        System.out.println("*请输入如下格式的指令,填入\"[...]\"的内容时不需要再加\"[\" \"]\",\"(...)\"仅提示信息不需输入");
        System.out.println("InitDisk [DiskName#] [DiskSize](KB) [DiskName#] [DiskSize](KB)...");/*1*/
        System.out.println("ChgDisk [DiskName#]");/*2*/
        System.out.println("ShowDisk [DiskName#]");/*3*/
        System.out.println("MkDir [DirPath]");/*4*/
        System.out.println("DelDir [DirPath]");/*5*/
        System.out.println("Dir [DirPath]");/*6*/
        System.out.println("ChgDir [DirPath]");/*7*/
        System.out.println("TreeDir [DirPath]");/*8*/
        System.out.println("MoveDir [DirPath] [NewDirPath]");/*9*/
        System.out.println("Create [FilePath]");/*10*/
        System.out.println("Copy [FilePath] [NewDirPath]");/*11*/
        System.out.println("Delete [FilePath]");/*12*/
        System.out.println("Write [FilePath]");/*13*/
        System.out.println("Read [FilePath]");/*14*/
        System.out.println("Export [FilePath] [ExDirPath]");/*15*/
        System.out.println("Exit");/*16*/
    }
    public void showFcb(FCB fcb){
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm");
        IndexNode indexNode = fcb.getIndexNode();//name-time-type-size
        System.out.printf("%-15s\t%-15s\t%-15s\t%-15s",
//                indexNode.getFcbNum(),
                fcb.getFileName(),
                dateFormat.format(indexNode.getUpdateTime()),
                fcb.getType(),
                indexNode.getSize() +" KB"
                );
        System.out.println();
    }
    public void showFcb_disk(FCB fcb){
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm");
        IndexNode indexNode = fcb.getIndexNode();//name-total-used-unused
        System.out.printf("%-15s\t%-15s\t%-15s\t%-15s",
//                indexNode.getFcbNum(),
                fcb.getFileName(),
                indexNode.getSize() +" KB",
                indexNode.getSize()-indexNode.getSizeUnused() +" KB",
                indexNode.getSizeUnused() +" KB"
        );
        System.out.println();
    }
}
