package com.ljl.service.impl;

import com.ljl.constant.Constants;
import com.ljl.pojo.*;
import com.ljl.service.DirService;
import com.ljl.service.DiskService;
import com.ljl.service.FileService;
import com.ljl.utils.Utility;
import com.ljl.view.View;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 磁盘操作实现类
 */
public class DiskServiceImpl implements DiskService {
    private static final DiskService diskService = new DiskServiceImpl();
    private static final DirService dirService = new DirServiceImpl();
    private static final FileService fileService = new FileServiceImpl();

    @Override
    public void initDisk(String[] initInfo) {
        Disk disk = Disk.getINSTANCE();

        //磁盘分区表
        Map<String, FCB> diskMap = disk.getDPT();
        int address_cnt=0;
        int driveSize;
        for (int i = 1; i < initInfo.length - 1; i++){
            if(i%2 != 0){//0 [1 2] [3 4]
                try {
                    driveSize = Integer.parseInt(initInfo[i + 1]);
                }catch(NumberFormatException e){
                    System.out.println("[error] 磁盘分区大小非整数 请重新初始化");
                    return;
                }
                int needNum = Utility.ceilDivide(driveSize, Constants.BLOCK_SIZE);//块数=磁盘分区大小/向上整除/块大小
                if(address_cnt+needNum-1 > Constants.BLOCK_COUNT-1){
                    System.out.println("[error] 超出磁盘空间大小 请重新初始化");
                    return;
                }
                if(!initInfo[i].endsWith("#")){
                    System.out.println("[error] 磁盘分区名未以\"#\"结尾 请重新初始化");
                    return;
                }
                FCB fcb = diskService.mkDisk(initInfo[i],address_cnt,driveSize);
                diskMap.put(initInfo[i],fcb);
                address_cnt+=needNum;
            }
        }
        disk.setDPT(diskMap);
        System.out.println("[success] 磁盘分区初始化成功");

        if(dirService.chgDir("/" + initInfo[1])) { //chgDisk is included
            System.out.println("[success] 已切换至首个磁盘分区");
        }
    }

    @Override
    public void showDisk(String diskName) {

        FCB fcb_disk;
        if(diskName.equals("")){
            fcb_disk = Memory.getInstance().getCurDisk();
        }
        else{
            String path = "/"+diskName;
            fcb_disk = dirService.pathResolve(path);
            //null 不存在
            if(Objects.isNull(fcb_disk)){
                System.out.println("[error] 目标磁盘分区不存在");
                return;
            }
        }

        View view = new View();
        System.out.println("名称\t\t\t\t总大小\t\t\t已用大小\t\t\t可用大小");

        view.showFcb_disk(fcb_disk);
        showFree(diskName);
    }

    /**创建磁盘分区**/
    @Override
    public FCB mkDisk(String diskName, int diskAddress, int diskSize) {
        FCB curDir = Memory.getInstance().getCurDir();
        List<FCB> children = curDir.getChildren();
//        //判空
//        if(Objects.isNull(diskName)){
//            System.out.println("[error] 磁盘分区名不可为空");
//            return null;
//        }
        //判断重复
        diskName = diskName.trim(); //去除首尾空格
        for (FCB child : children) {
            if(child.getFileName().equals(diskName)){
                System.out.println("[error] 磁盘分区名重复 请重新命名");
                return null;
            }
        }
        //创建索引节点 创建FCB 文件大小为0 空文件
        IndexNode indexNode = new IndexNode(diskSize, diskAddress, 0, new Date());
        FCB fcb = new FCB(diskName, "DISK", indexNode, curDir, new LinkedList<>());
        //在首块设置空闲空间管理
        FSM fsm = diskSize>=Constants.THRESHOLD?new Bitmap(Constants.BITMAP,diskSize):new Freetable(Constants.FREETABLE,diskAddress,diskSize);
        Disk.getINSTANCE().getBlocks()[diskAddress].setFsm(fsm);
        //将文件控制块放入磁盘的fcb集合
        Disk.getINSTANCE().getFcbList().add(fcb);
        //修改父目录的文件项 加入父目录儿子集合
        curDir.getIndexNode().addFcbNum();
        curDir.getChildren().add(fcb);
        System.out.println("[success] 创建磁盘分区 " + diskName + " 成功");
        return fcb;
    }

    /**切换目录**/
    @Override
    public Boolean chgDisk(String diskName) {
        String path = "/"+diskName;
        FCB fcb = dirService.pathResolve(path);
        //null 不存在
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标磁盘分区不存在");
            return false;
        }else {
            Memory.getInstance().setCurDisk(fcb);
            Memory.getInstance().setCurDir(fcb);
        }
        return true;
    }

    /**(跨盘)转移目录中所有文件**/
    @Override
    public Boolean moveDir(FCB fcb_dir, String diskName_src, String diskName_des) {

        FCB fcb_disk;
        if(diskName_des.equals("")){
            fcb_disk = Memory.getInstance().getCurDisk();
        }
        else{
            String path = "/"+ diskName_des;
            fcb_disk = dirService.pathResolve(path);
            //null 不存在
            if(Objects.isNull(fcb_disk)){
                System.out.println("[error] 目标磁盘分区不存在");
                return false;
            }
        }

        Queue<FCB> que = new LinkedList<>();
        que.add(fcb_dir);
        Stack<FCB> fcbStack = new Stack<>();
        fcbStack.push(fcb_dir);
        FCB fcb_temp;
        //层序遍历入队列
        while (!que.isEmpty()) {
            fcb_temp=que.remove();
            List<FCB> children = fcb_temp.getChildren();
            if (Objects.nonNull(children)){
                //入队列
                for (FCB child : children) {
                    if (Objects.nonNull(child)) que.add(child);
                }
                //入栈
                for (FCB child : children) {
                    if (Objects.nonNull(child)) fcbStack.push(child);
                }
            }
        }
        //依次出栈 file->copy
        while (!fcbStack.isEmpty()) {
            fcb_temp=fcbStack.pop();
            if(fcb_temp.getType().equals("FILE")){

                /**复制为新盘下新文件**/
                //创建索引节点 创建FCB 文件大小为0 空文件
                IndexNode indexNode = new IndexNode(0, -1, 0, new Date());
                FCB fcb_new = new FCB(fcb_temp.getFileName(), "FILE", indexNode, fcb_temp.getFather(), null);
                /*begin: read from old file and write into new file*/

                StringBuilder content = new StringBuilder();

                //read
                int[] fat = Memory.getInstance().getFat();
                Block[] disk = Disk.getINSTANCE().getBlocks();
                //从磁盘读取
                if(fcb_temp.getIndexNode().getSize() == 0){
                    //空文件
                }
                else{
//            int temp = fat[fcb.getIndexNode().getFirst_block()];
                    int temp = fcb_temp.getIndexNode().getFirstBlock();
//          while (fat[temp] != -1){
                    while (temp != -1){
                        //遍历读出
                        content.append(disk[temp].getContent());
                        temp = fat[temp];
                    }
//          content.append(disk[temp].getContent());
                }
                System.out.println("[success] 读出成功");

                //write
                int size = content.toString().toCharArray().length;//文件大小为输入字数
                int first = diskService.writeToDisk(content.toString(), diskName_des);//write的方式 0 1
                //将文件指向第一块
                fcb_new.getIndexNode().setFirstBlock(first);
                //修改索引结点大小
                fcb_new.getIndexNode().setSize(size);
                //修改父目录项 以及一直递归修改父目录的大小
                dirService.updateSize(fcb_new,true,-1);
                System.out.println("[success] 写入成功");

                //将文件控制块放入磁盘的fcb集合
                Disk.getINSTANCE().getFcbList().add(fcb_new);
                //修改父目录的文件项 加入父目录儿子集合
                fcb_new.getFather().getIndexNode().addFcbNum();
                fcb_new.getFather().getChildren().add(fcb_new);
                /*end: read from old file and write into new file*/

                /**清除原fcb的内存**/

//                System.out.println("disksrc = "+diskName_src+" diskdes = "+diskName_des);

                diskService.freeFile(fcb_temp, diskName_src);//free的方式 0 1
                //从FCB集合中去除 修改父目录文件项 修改父目录儿子结点
                Disk.getINSTANCE().getFcbList().remove(fcb_temp);
                fcb_temp.getFather().getIndexNode().subFcbNum();
                fcb_temp.getFather().getChildren().remove(fcb_temp);
            }
        }
        return true;
    }

    /**释放目录中所有文件占用内存**/
    @Override
    public Boolean freeDir(FCB fcb, String diskName) {

        Queue<FCB> que = new LinkedList<>();
        que.add(fcb);
        Stack<FCB> fcbStack = new Stack<>();
        fcbStack.push(fcb);
        FCB temp;
        //层序遍历入队列
        while (!que.isEmpty()) {
            temp=que.remove();
            List<FCB> children = temp.getChildren();
            if (Objects.nonNull(children)){//入队列
                for (FCB child : children) {
                    if (Objects.nonNull(child)) que.add(child);
                }
                //入栈
                for (FCB child : children) {
                    if (Objects.nonNull(child)) fcbStack.push(child);
                }
            }
        }
        //依次出栈删除
        while (!fcbStack.isEmpty()) {
            temp=fcbStack.pop();
            if(temp.getType().equals("FILE")){
                diskService.freeFile(temp,diskName);
            }
            //从FCB集合中去除 修改父目录文件项 修改父目录儿子结点
            Disk.getINSTANCE().getFcbList().remove(temp);
            temp.getFather().getIndexNode().subFcbNum();
            temp.getFather().getChildren().remove(temp);
            //递归修改父目录文件大小
            dirService.updateSize(temp,false,-1);
        }
        return true;
    }

    /**清除文件占据的磁盘空间及改变FAT表**/
    @Override
    public Boolean freeFile(FCB fcb,String diskName) {

        FCB fcb_disk;
        if(diskName.equals("")){
            fcb_disk = Memory.getInstance().getCurDisk();
        }
        else{
            String path = "/"+diskName;
            fcb_disk = dirService.pathResolve(path);
            //null 不存在
            if(Objects.isNull(fcb_disk)){
                System.out.println("[error] 目标磁盘分区不存在");
                return false;
            }
        }

        Block[] disk = Disk.getINSTANCE().getBlocks();
        FSM fsm=disk[fcb_disk.getIndexNode().getFirstBlock()].getFsm();
        if(fsm.getMethod()==0)
        {
            int[][] bm = ((Bitmap)fsm).getBitmap();
            int[] fat = Memory.getInstance().getFat();
            int start=fcb_disk.getIndexNode().getFirstBlock();
//            int temp_1 = fat[fcb.getIndexNode().getFirst_block()];
            int temp_1 = fcb.getIndexNode().getFirstBlock();
            int temp_2;
            int i,j,freeNum=0;
            //1.修改FAT表 双指针改法
            while(temp_1 != -1){
                temp_2 = temp_1;
                temp_1 = fat[temp_1];
                //断开前后连接
                fat[temp_2]=-1;
                //将占据的盘块对应内容置空
                disk[temp_2].setContent(null);
                i=(temp_2-start)/Constants.COLUMN_COUNT;
                j=(temp_2-start)%Constants.COLUMN_COUNT;

//                System.out.println("i = " + i + " ,j = " + j );

                bm[i][j]=0;
                freeNum++;
            }
            //更新空闲块数
            fsm.setFreeNum(fsm.getFreeNum()+freeNum);
        }
        else //1
        {
            List<Pair<Integer, Integer>> freetable = ((Freetable)fsm).getFreetable();
            int[] fat = Memory.getInstance().getFat();
            int start = fcb_disk.getIndexNode().getFirstBlock();
            int end = start+fsm.getTotNum();//结束的下一块
            int first_block = fcb.getIndexNode().getFirstBlock();
//            int temp_1 = fat[fcb.getIndexNode().getFirst_block()];
            int temp_1 = fcb.getIndexNode().getFirstBlock();
            int temp_2;
            int i,j,freeNum=0;
            //1.修改FAT表 双指针改法
            while(temp_1 != -1){
                temp_2 = temp_1;
                temp_1 = fat[temp_1];
                //断开前后连接
                fat[temp_2]=-1;
                //将占据的盘块对应内容置空
                disk[temp_2].setContent(null);
                freeNum++;
            }
            /**begin:合并**/
            Pair<Integer,Integer> pre = null;
            Pair<Integer,Integer> post = null;
            for(Pair<Integer,Integer> p:freetable){
                if(p.getKey()+p.getValue()==first_block){
                    pre=p;
                }
                if(p.getKey()==first_block+freeNum){
                    post=p;
                }
            }
            if(Objects.nonNull(pre)){
                first_block=pre.getKey();
                freeNum+=pre.getValue();
                freetable.remove(pre);
            }
            if(Objects.nonNull(post)){
                freeNum+=post.getValue();
                freetable.remove(post);
            }
            /**end:合并**/
            if(first_block>=start&&first_block+freeNum<=end) {
                freetable.add(new Pair(first_block, freeNum));
                freetable = freetable.stream().sorted(Comparator.comparingInt(Pair::getKey)).collect(Collectors.toList());
                ((Freetable) fsm).setFreetable(freetable);
                //更新空闲块数
                fsm.setFreeNum(fsm.getFreeNum() + freeNum);
            }
            else{
                System.out.println("[error] 磁盘分区回收时越界 " + first_block + ":" + freeNum);
            }
        }
        //3.递归修改父目录文件大小
        dirService.updateSize(fcb,false,-1);
        //4.索引结点大小变为0 空文件
        fcb.getIndexNode().setSize(0);
        return true;
    }

    /**将内容写入磁盘块**/
    @Override
    public int writeToDisk(String content, String diskName) {

        FCB fcb_disk;
        if(diskName.equals("")){
            fcb_disk = Memory.getInstance().getCurDisk();
        }
        else{
            String path = "/"+diskName;
            fcb_disk = dirService.pathResolve(path);
            //null 不存在
            if(Objects.isNull(fcb_disk)){
                System.out.println("[error] 目标磁盘分区不存在");
                return -1;
            }
        }

        //判断是否有足够的磁盘空间
        Block[] disk = Disk.getINSTANCE().getBlocks();

        FSM fsm=disk[fcb_disk.getIndexNode().getFirstBlock()].getFsm();
        int needNum = Utility.ceilDivide(content.length(), Constants.BLOCK_SIZE);//块数=字数/向上整除/块大小
        if(fsm.getMethod()==0)
        {
            int freeNum = ((Bitmap)fsm).getFreeNum();
            if(needNum > freeNum){
                System.out.println("[error] 磁盘空间不足");
                return -1;
            }
            //开始写入 双指针写入法
            int[] fat = Memory.getInstance().getFat();
            int first = -1;
            //找到第一个
            first = findEmpty(needNum,diskName);
            int temp_1 = first;
            int temp_2 = -1;
            int i = 0;
            for (; i < needNum - 1; i++) {
                String splitString = content.substring(i*Constants.BLOCK_SIZE,(i+1)*Constants.BLOCK_SIZE);
                //存储到磁盘
                disk[temp_1].setContent(splitString);
                temp_2 = temp_1;
                //寻找下一个空闲块
                temp_1 = findEmpty(needNum,diskName);
                fat[temp_2]=temp_1;
            }
            //设置最后一个块
            disk[temp_1].setContent(content.substring((i)*Constants.BLOCK_SIZE));
            fat[temp_1]=-1;
            //更新空闲块数
            fsm.setFreeNum(fsm.getFreeNum()-needNum);
            //返回第一个磁盘块号
            return first;
        }
        else //1
        {
            //开始写入 双指针写入法
            int[] fat = Memory.getInstance().getFat();
            int first = -1;
            //找到第一个
            first = findEmpty(needNum,diskName);
            if(first == -1){
                System.out.println("[error] 磁盘空间不足");
                return -1;
            }
            int temp_1 = first;
            int temp_2 = -1;
            int i = 0;
            for (; i < needNum - 1; i++) {
                String splitString = content.substring(i*Constants.BLOCK_SIZE,(i+1)*Constants.BLOCK_SIZE);
                //存储到磁盘
                disk[temp_1].setContent(splitString);
                temp_2 = temp_1;
                //进入下一个空闲块
                temp_1++;
                fat[temp_2]=temp_1;
            }
            //设置最后一个块
            disk[temp_1].setContent(content.substring((i)*Constants.BLOCK_SIZE));
            fat[temp_1]=-1;
            //更新空闲块数
            fsm.setFreeNum(fsm.getFreeNum()-needNum);
            //返回第一个磁盘块号
            return first;
        }
    }

    /**寻找空闲块**/
    @Override
    public int findEmpty(int needNum, String diskName) {

        FCB fcb_disk;
        if(diskName.equals("")){
            fcb_disk = Memory.getInstance().getCurDisk();
        }
        else{
            String path = "/"+diskName;
            fcb_disk = dirService.pathResolve(path);
            //null 不存在
            if(Objects.isNull(fcb_disk)){
                System.out.println("[error] 目标磁盘分区不存在");
                return -1;
            }
        }

        Block[] disk = Disk.getINSTANCE().getBlocks();
        FSM fsm=disk[fcb_disk.getIndexNode().getFirstBlock()].getFsm();
        if(fsm.getMethod()==0)
        {
            int[][] bm = ((Bitmap)fsm).getBitmap();
            int start=fcb_disk.getIndexNode().getFirstBlock();
            int blockNum = ((Bitmap)fsm).getTotNum();
            int k=0;
            for (int i = 0; k<blockNum; i++) {
                for (int j = 0; j < Constants.COLUMN_COUNT && k<blockNum; j++) {
                    if(bm[i][j] == 0){
                        //占位
                        bm[i][j]=1;
                        return start+i*Constants.COLUMN_COUNT+j;
                    }
                    k++;
                }
            }
            return -1;
        }
        else //1
        {
            List<Pair<Integer, Integer>> freetable = ((Freetable)fsm).getFreetable();
            Pair<Integer, Integer> temp = null;
            for(Pair<Integer, Integer> p : freetable) {
                if (p.getValue() >= needNum) {
                    temp = p;
                    break;
                }
            }
            if(Objects.nonNull(temp)) {
                freetable.remove(temp);
                if(temp.getValue()-needNum != 0){
                    freetable.add(new Pair<>(temp.getKey()+needNum, temp.getValue()-needNum));
                }
                freetable = freetable.stream().sorted(Comparator.comparingInt(Pair::getKey)).collect(Collectors.toList());
                ((Freetable)fsm).setFreetable(freetable);
                return temp.getKey();
            }
            else {
                return -1;
            }

        }
    }

    /**显示空闲空间**/
    @Override
    public void showFree(String diskName) {

        FCB fcb_disk;
        if(diskName.equals("")){
            fcb_disk = Memory.getInstance().getCurDisk();
        }
        else{
            String path = "/"+diskName;
            fcb_disk = dirService.pathResolve(path);
            //null 不存在
            if(Objects.isNull(fcb_disk)){
                System.out.println("[error] 目标磁盘分区不存在");
                return;
            }
        }

        Block[] disk = Disk.getINSTANCE().getBlocks();
        FSM fsm=disk[fcb_disk.getIndexNode().getFirstBlock()].getFsm();
        if(fsm.getMethod()==0)
        {
            int[][] bm = ((Bitmap)fsm).getBitmap();
            int start=fcb_disk.getIndexNode().getFirstBlock();
            int blockNum = ((Bitmap)fsm).getTotNum();
            int k=0;
            System.out.println("<!--BITMAP-->");
            System.out.println("{Location:Allocated:Content}");
            for (int i = 0; k<blockNum; i++) {
                for (int j = 0; j < Constants.COLUMN_COUNT && k<blockNum; j++) {
                    System.out.print(" {" + "[" + i + "," + j + "]" + ":" + bm[i][j] + ":"+ disk[start+i*Constants.COLUMN_COUNT+j].getContent() + "} ");
                    k++;
                }
                System.out.println();
            }
        }
        else
        {
            List<Pair<Integer, Integer>> freetable = ((Freetable)fsm).getFreetable();
            System.out.println("<!--FREETABLE-->");
            System.out.println("<Address:FreeBlockNum>");
            for(Pair<Integer, Integer> p : freetable){
                System.out.println(" <" + p.getKey() + ":" + p.getValue() + "> ");
            }
        }
    }

}
