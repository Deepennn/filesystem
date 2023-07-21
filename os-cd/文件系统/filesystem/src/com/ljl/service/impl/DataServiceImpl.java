package com.ljl.service.impl;

import com.ljl.constant.Constants;
import com.ljl.pojo.*;
import com.ljl.pojo.Disk;
import com.ljl.pojo.Memory;
import com.ljl.service.DataService;
import com.ljl.service.DiskService;

import java.io.*;
import java.util.*;

/**
 * @description: 持久化接口实现类
 */
public class DataServiceImpl implements DataService {
    private static final DiskService diskService = new DiskServiceImpl();

    @Override
    public void initData() {
        Disk newDisk = Disk.getINSTANCE();
        Block[] blocks = new Block[Constants.BLOCK_COUNT];

        //初始化磁盘
        for (int i = 0; i < Constants.BLOCK_COUNT; i++) {
            blocks[i] = new Block();
            blocks[i].setId(i);
            blocks[i].setBlockSize(Constants.BLOCK_SIZE);
            blocks[i].setContent(null);
        }
        newDisk.setBlocks(blocks);

        //初始化FCB集合
        List<FCB> fcbList = new ArrayList<>();
        newDisk.setFcbList(fcbList);
        IndexNode indexNode = new IndexNode(0,-1,0,new Date());
        FCB root = new FCB("root","DIR",indexNode,null,new LinkedList<>());
        fcbList.add(root);

        //初始化FAT表
        int[] fat = new int[Constants.BLOCK_COUNT];
        for (int i = 0; i < Constants.BLOCK_COUNT; i++) {
            fat[i]=-1;
        }
        newDisk.setFat(fat);

        //初始化内存
        Memory memory = Memory.getInstance();
        //初始化磁盘分区
        Map<String, FCB> diskMap = new HashMap<>();
        newDisk.setDPT(diskMap);

        memory.setCurDir(root);
        memory.setCurDisk(null);
        memory.setRootDir(root);
        memory.setFat(fat);
        ArrayList<OpenFile> openFiles = new ArrayList<>();
        memory.setOpenFileList(openFiles);

        System.out.println("[success] 数据初始化成功");
    }

    @Override
    public Boolean loadData(String dataPath) {
        File file = new File(dataPath);
        if (!file.exists()) {
            System.out.println("[error] 找不到文件");
            return false;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            //加载磁盘数据
            Disk.setINSTANCE((Disk) ois.readObject());
            Disk instance = Disk.getINSTANCE();
            //将磁盘数据调入内存
            Memory memory = Memory.getInstance();
            memory.setRootDir(instance.getFcbList().get(0));
            memory.setCurDisk(instance.getFcbList().get(0));
            memory.setCurDir(instance.getFcbList().get(0));
            int[] fat = instance.getFat();
            memory.setFat(fat);
            List<OpenFile> openFileList = new ArrayList<>();
            memory.setOpenFileList(openFileList);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[error] IO异常");
            return false;
        }finally {
            try {
                if (Objects.nonNull(ois)) {
                    ois.close();
                }
            } catch (IOException ignored) {
            }
        }
        System.out.println("[success] 加载数据成功");
        return true;
    }

    @Override
    public Boolean saveData(String savePath) {
        File file = new File(savePath);
        // 检查文件是否存在
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                if (new File(file.getParentFile().getPath()).mkdirs()) {
                    try {
                        if (!file.createNewFile()) {
                            System.out.println("[error] 保存失败");
                            return false;
                        }
                    } catch (IOException ioException) {
                        System.out.println("[error] IO异常");
                        return false;
                    }
                } else {
                    System.out.println("[error] 保存失败");
                    return false;
                }
            }
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            // 持久化到保存文件中
            oos.writeObject(Disk.getINSTANCE());
            oos.flush();
            System.out.println("[success] 保存数据成功");
            return true;
        } catch (IOException e) {
            System.out.println("[error] 保存失败");
            return false;
        } finally {
            try {
                if (Objects.nonNull(oos)) {
                    oos.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
