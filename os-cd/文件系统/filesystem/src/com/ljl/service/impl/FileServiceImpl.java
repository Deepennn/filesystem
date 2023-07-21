package com.ljl.service.impl;

import com.ljl.pojo.*;
import com.ljl.service.DirService;
import com.ljl.service.DiskService;
import com.ljl.service.FileService;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * @description: 文件操作实现类
 */
public class FileServiceImpl implements FileService {
    private static final DiskService diskService = new DiskServiceImpl();
    private static final DirService dirService = new DirServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    /**创建文件**/
    @Override
    public Boolean create(String filePath) {

        FCB temp_fcb = dirService.pathResolve(filePath);
        String[] splitDir = filePath.split("/");
        String fileName = splitDir[splitDir.length - 1];
        if(Objects.nonNull(temp_fcb)){
            fileName+="-1";
            System.out.println("[error] 文件名重复 已重命名为\""+fileName+"\"");
        }

        /**begin: 寻找新文件的父目录**/

        filePath = filePath.trim();
        int index_last_slash = filePath.lastIndexOf("/");
        if(index_last_slash!=-1){
            filePath = filePath.substring(0, index_last_slash);
            if(filePath.equals(".")||filePath.equals("..")){ // ./F->.
                filePath+="/";
            }
        }
        else{// F->./
            filePath="./";
        }

//        System.out.println("fatherDirPath is : "+filePath);

        FCB curDir = Memory.getInstance().getCurDir();
        FCB rootDir = Memory.getInstance().getRootDir();
        FCB desDir = null;

        if(filePath.startsWith("./")){//1
            filePath = filePath.substring(2);
            if(filePath.equals("")){
                desDir = curDir;
            }
            else{
                splitDir = filePath.split("/");
                FCB temp = curDir;
                for (int i = 0; i < splitDir.length - 1; i++) {
                    //找到目标文件所在目录
                    for (FCB child : temp.getChildren()) {
                        if(child.getFileName().equals(splitDir[i])){
                            temp = child;
                            continue;
                        }
                    }
                }
                //在该目录下找
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                        desDir=child;
                        break;
                    }
                }
            }
        }

        else if(filePath.startsWith("../")){//2

            FCB temp;
            if(curDir != rootDir){
                temp = curDir.getFather();
            }
            else{
                System.out.println("[error] 目标目录父目录不存在");
                return false;
            }
            filePath = filePath.substring(3);
            while (filePath.startsWith("../")){
                if(temp != rootDir){
                    temp = temp.getFather();
                }
                else{
                    System.out.println("[error] 目标目录父目录不存在");
                    return false;
                }
                filePath = filePath.substring(3);
            }

            if(filePath.equals("")){
                desDir=temp;
            }
            else {
                splitDir = filePath.split("/");
                for (int i = 0; i < splitDir.length - 1; i++) {
                    //找到目标文件所在目录
                    for (FCB child : temp.getChildren()) {
                        if(child.getFileName().equals(splitDir[i])){
                            temp = child;
                            continue;
                        }
                    }
                }
                //在该目录下找
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                        desDir=child;
                        break;
                    }
                }
            }
        }

        else if((filePath.startsWith("/"))){//3
            //以/开头 从根目录逐层往下找
            filePath = filePath.substring(1);
            if(filePath.equals("")){
                desDir=rootDir;
            }
            else{
                splitDir = filePath.split("/");
                FCB temp = rootDir;
                for (int i = 0; i < splitDir.length - 1; i++) {
                    //找到目标文件所在目录
                    for (FCB child : temp.getChildren()) {
                        if(child.getFileName().equals(splitDir[i])){
                            temp = child;
                            continue;
                        }
                    }
                }
                //在该目录下找
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                        desDir=child;
                        break;
                    }
                }
            }
        }

        else if(filePath.equals("..")){//4
            //判断是不是已经在根目录
            if(curDir != Memory.getInstance().getRootDir()){
                //改变当前目录为父目录
                desDir=curDir.getFather();
            }
            else{
                System.out.println("[error] 目标目录父目录不存在");
                return false;
            }

        }

        else {//5->1
            //在当前目录下找
            splitDir = filePath.split("/");
            FCB temp = curDir;
            for (int i = 0; i < splitDir.length - 1; i++) {
                //找到目标文件所在目录
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[i])){
                        temp = child;
                        continue;
                    }
                }
            }
            //在该目录下找
            for (FCB child : temp.getChildren()) {
                if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                    desDir=child;
                    break;
                }
            }
        }

        if(desDir==null){
            System.out.println("[error] 目标目录父目录不存在");
            return false;
        }

        /**end: 寻找新文件的父目录**/

        List<FCB> children = desDir.getChildren();

        //判空
        if(Objects.isNull(fileName)) {
            System.out.println("[error] 文件名不可为空");
            return false;
        }
//        //判断重复
//        fileName = fileName.trim(); //去除首尾空格
//        for (FCB child : children) {
//            if(child.getFileName().equals(fileName)){
//                fileName+="-1";
//                System.out.println("[warning] 文件名重复 已重命名为\""+fileName+"\"");
//                break;
//            }
//        }
        //创建索引节点 创建FCB 文件大小为0 空文件
        IndexNode indexNode = new IndexNode(0, -1, 0, new Date());
        FCB fcb = new FCB(fileName, "FILE", indexNode, desDir, null);
        //将文件控制块放入磁盘的fcb集合
        Disk.getINSTANCE().getFcbList().add(fcb);
        //修改父目录的文件项 加入父目录儿子集合
        desDir.getIndexNode().addFcbNum();
        desDir.getChildren().add(fcb);
        System.out.println("[success] 创建文件成功");
        return true;
    }

    /**打开文件**/
    @Override
    public Boolean open(String filePath) {
        //使用pathResolve解析
        FCB fcb = dirService.pathResolve(filePath);
        //null 不存在
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标文件不存在");
            return false;
        }else if(!fcb.getType().equals("FILE")){
            //type DISK DIR 不是普通文件
            System.out.println("[error] 无法打开非文件");
            return false;
        }else {
            //type FILE 普通文件
            //判断是否已经打开
            //判断是否在openFileList中
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            OpenFile toWriteFile = null;
            for (OpenFile openFile : openFileList) {
                if(openFile.getFilePath().equals(fill_path)){
                    toWriteFile = openFile;
                }
            }
            if(Objects.nonNull(toWriteFile)){
                System.out.println("[error] 文件已打开");
                return false;
            }
            //加入openFileList中
            OpenFile openFile = new OpenFile(fcb, fill_path);
            Memory.getInstance().getOpenFileList().add(openFile);
            System.out.println("[success] 打开成功");
            return true;
        }
    }

    /**读文件**/
    @Override
    public Boolean read(String filePath) {
        //判断是否存在
        FCB fcb = dirService.pathResolve(filePath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标文件不存在");
            return false;
        }else if(!fcb.getType().equals("FILE")){
            //type DISK DIR 不是普通文件
            System.out.println("[error] 无法读非文件");
            return false;
        }else {
            //type FILE 普通文件
            //判断是否在openFileList中
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            OpenFile toWriteFile = null;
            for (OpenFile openFile : openFileList) {
                if(openFile.getFilePath().equals(fill_path)){
                    toWriteFile = openFile;
                }
            }
            if(Objects.nonNull(toWriteFile)){
                int[] fat = Memory.getInstance().getFat();
                Block[] disk = Disk.getINSTANCE().getBlocks();
                //从磁盘读取
                System.out.println("--------BEGIN--------");
                if(fcb.getIndexNode().getSize() == 0){
                    System.out.println("<!--EMPTY FILE-->");
                    System.out.println("---------END---------");
                    return false;
                }
//                int temp = fat[fcb.getIndexNode().getFirst_block()];
                int temp = fcb.getIndexNode().getFirstBlock();
//                while (fat[temp] != -1){
                while (temp != -1){
                        //遍历输出
                        System.out.print(disk[temp].getContent());
                        temp = fat[temp];
                }
//                System.out.print(disk[temp].getContent());
                System.out.println();
                System.out.println("---------END---------");
            }else {
                System.out.println("[error] 文件未打开 请先打开");
                return false;
            }
        }
        return true;
    }

    /**写文件**/
    @Override
    public Boolean write(String filePath) {
        //判断是否存在
        FCB fcb = dirService.pathResolve(filePath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标文件不存在");
            return false;
        }else if(!fcb.getType().equals("FILE")){
            //type DISK DIR 不是普通文件
            System.out.println("[error] 无法写非文件");
            return false;
        }else {
                //type FILE 普通文件
                //判断是否在openFileList中
                String fill_path = dirService.pwd(fcb);
                List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
                OpenFile toWriteFile = null;
                for (OpenFile openFile : openFileList) {
                    if(openFile.getFilePath().equals(fill_path)){
                        toWriteFile = openFile;
                    }
                }
                if(Objects.nonNull(toWriteFile)){
                    StringBuilder content = new StringBuilder();
                    System.out.println("请输入要写入的内容 [...^]:");
                    //获取用户输入 输入//end结束
                    while (true){
                        String nextLine = scanner.nextLine();
                        if(nextLine.endsWith("^")){
                            content.append(nextLine,0,nextLine.length()-1);
                            break;
                        }else {
                            content.append(nextLine);
                            content.append("\n");
                        }
                    }
                    String choice = null;
                    if(fcb.getIndexNode().getSize() == 0){
                        //空文件 默认覆盖
                        choice = "1";
                    }else {
                        //有内容 让用户选择写入模式
                        while (true){
                            System.out.println("原文件有内容 请选择覆盖写[1]/追加写[2]:");
                            choice = scanner.nextLine();
                            if(choice.equals("1") || choice.equals("2")){
                                break;
                            }
                        }
                    }
                    int[] fat = Memory.getInstance().getFat();
                    int size = content.toString().toCharArray().length;//文件大小为输入字数

                    String temp_1=dirService.pwd(fcb);
                    String[] splitDir_1 = temp_1.split("/");
                    String diskName=splitDir_1[1];

                    if(choice.equals("1")){
                        //覆盖写入
                        //1.如果不是空文件 则清空之前占据的盘块
                        if(fcb.getIndexNode().getSize() != 0){
                            diskService.freeFile(fcb,diskName);
                        }
                        //2.重新写入
                        int first = diskService.writeToDisk(content.toString(),diskName);
                        //3.将文件指向第一块
                        fcb.getIndexNode().setFirstBlock(first);
                        //4.修改索引结点大小
                        fcb.getIndexNode().setSize(size);
                        //修改父目录项 以及一直递归修改父目录的大小
                        dirService.updateSize(fcb,true,-1);
                    }else {
                        //追加写入
                        //1.从第一块往下找  直到-1的块的块号
//                        int temp = fat[fcb.getIndexNode().getFirst_block()];
                        int temp = fcb.getIndexNode().getFirstBlock();
                        int temp2 = temp;
//                      while (fat[temp] != -1){
                        while (temp != -1){
                            temp2 = temp;
                            temp = fat[temp];
                        }
                        //2.写入要追加的内容
//                        content.insert(0,'\n');
                        int append_begin = diskService.writeToDisk(content.toString(),diskName);
                        //3.修改最后一块指向新的内容
                        fat[temp2]=append_begin;
                        //4.修改索引结点大小 加上原来的
                        int size_origin = fcb.getIndexNode().getSize();
                        fcb.getIndexNode().setSize(size + size_origin);
                        //修改父目录项 以及一直递归修改父目录的大小
                        dirService.updateSize(fcb,true,size);
                    }
                    System.out.println("[success] 写入成功");
                    return true;
                }else {
                    System.out.println("[error] 文件未打开 请先打开");
                    return false;
                }
//            }
        }
    }

    /**关闭文件**/
    @Override
    public Boolean close(String filePath) {
        //判断是否存在
        FCB fcb = dirService.pathResolve(filePath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标文件不存在");
            return false;
        }else if(!fcb.getType().equals("FILE")){
            //type DISK DIR 不是普通文件
            System.out.println("[error] 无法关闭非文件");
            return false;
        }else {
            //type FILE 普通文件
            //判断是否在openFileList中
            String fill_path = dirService.pwd(fcb);
            List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
            for (OpenFile openFile : openFileList) {
                if(openFile.getFilePath().equals(fill_path)){
                    //修改fcb的updateTime
                    fcb.getIndexNode().setUpdateTime(new Date());
                    //从openFileList中移除
                    openFileList.remove(openFile);
                    System.out.println("[success] 关闭成功");
                    return true;

                }
            }
            System.out.println("[error] 文件未打开 无需关闭");
            return false;
        }
    }

    /**删除文件**/
    @Override
    public Boolean delete(String filePath) {
        //判断是否存在
        FCB fcb = dirService.pathResolve(filePath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标文件不存在");
            return false;
        }
        else if(!fcb.getType().equals("FILE")){
            System.out.println("[error] 不能删除非文件");
            return false;
        }
        //判断是否打开 打开要先关闭
        //判断是否在openFileList中
        String fill_path = dirService.pwd(fcb);
        List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
        OpenFile toWriteFile = null;
        for (OpenFile openFile : openFileList) {
            if(openFile.getFilePath().equals(fill_path)){
                toWriteFile = openFile;
            }
        }
        if(Objects.nonNull(toWriteFile)){
            System.out.println("[error] 文件被打开 请先关闭");
            return false;
        }
        //重复确认
        String choice = null;
        while (true){
            System.out.println("确认删除该文件（Y/N）");
            choice = scanner.nextLine();
            if(choice.equals("Y")) break;
            if(choice.equals("N")) {
                System.out.println("[success] 已取消删除");
                return false;
            }
        }
        //非空文件判断
        if(fcb.getIndexNode().getSize() != 0 || fcb.getIndexNode().getFcbNum() != 0){
//            if(fcb.getType().equals("DIR")){//非空文件夹
//                //type DIR 目录
//                //借助栈删除目录
////                diskService.freeDir(fcb);
//                System.out.println("[error] 文件夹非空 无法删除");
//                return false;
//            }else {
//                //清空磁盘
//                diskService.freeFile(fcb);
//            }

            String temp_1=dirService.pwd(fcb);
            String[] splitDir_1 = temp_1.split("/");
            String diskName=splitDir_1[1];

            diskService.freeFile(fcb,diskName);
        }
//        //如果是空目录 不允许是当前目录
//        if(fcb == Memory.getInstance().getCurDir()){
//            System.out.println("[error] 无法删除当前目录 请先退出当前目录！");
//            return false;
//        }
        //从FCB集合中去除 修改父目录文件项 修改父目录儿子结点
        Disk.getINSTANCE().getFcbList().remove(fcb);
        fcb.getFather().getIndexNode().subFcbNum();
        fcb.getFather().getChildren().remove(fcb);
        //递归修改父目录文件大小
        dirService.updateSize(fcb,false,-1);
        System.out.println("[success] 删除成功");
        return true;
    }

    /**复制文件**/
    @Override
    public Boolean copy(String filePath, String newDirPath){
        //判断是否存在
        FCB fcb = dirService.pathResolve(filePath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 源文件不存在");
            return false;
        }
        else if(!fcb.getType().equals("FILE")){
            System.out.println("[error] 不能拷贝非文件");
            return false;
        }

        /**begin: 寻找目标目录**/

        newDirPath = newDirPath.trim();
        FCB curDir = Memory.getInstance().getCurDir();
        FCB rootDir = Memory.getInstance().getRootDir();
        FCB desDir = null;

        if(newDirPath.startsWith("./")){//1
            newDirPath = newDirPath.substring(2);
            if(newDirPath.equals("")){
                desDir = curDir;
            }
            else{
                String[] splitDir = newDirPath.split("/");
                FCB temp = curDir;
                for (int i = 0; i < splitDir.length - 1; i++) {
                    //找到目标文件所在目录
                    for (FCB child : temp.getChildren()) {
                        if(child.getFileName().equals(splitDir[i])){
                            temp = child;
                            continue;
                        }
                    }
                }
                //在该目录下找
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                        desDir=child;
                        break;
                    }
                }
            }
        }

        else if(newDirPath.startsWith("../")){//2

            FCB temp;
            if(curDir != rootDir){
                temp = curDir.getFather();
            }
            else{
                System.out.println("[error] 目标目录不存在");
                return false;
            }
            newDirPath = newDirPath.substring(3);
            while (newDirPath.startsWith("../")){
                if(temp != rootDir){
                    temp = temp.getFather();
                }
                else{
                    System.out.println("[error] 目标目录不存在");
                    return false;
                }
                newDirPath = newDirPath.substring(3);
            }

            if(newDirPath.equals("")){
                desDir=temp;
            }
            else {
                String[] splitDir = newDirPath.split("/");
                for (int i = 0; i < splitDir.length - 1; i++) {
                    //找到目标文件所在目录
                    for (FCB child : temp.getChildren()) {
                        if(child.getFileName().equals(splitDir[i])){
                            temp = child;
                            continue;
                        }
                    }
                }
                //在该目录下找
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                        desDir=child;
                        break;
                    }
                }
            }
        }

        else if((newDirPath.startsWith("/"))){//3
            //以/开头 从根目录逐层往下找
            newDirPath = newDirPath.substring(1);
            if(newDirPath.equals("")){
                desDir=rootDir;
            }
            else{
                String[] splitDir = newDirPath.split("/");
                FCB temp = rootDir;
                for (int i = 0; i < splitDir.length - 1; i++) {
                    //找到目标文件所在目录
                    for (FCB child : temp.getChildren()) {
                        if(child.getFileName().equals(splitDir[i])){
                            temp = child;
                            continue;
                        }
                    }
                }
                //在该目录下找
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                        desDir=child;
                        break;
                    }
                }
            }
        }

        else if(newDirPath.equals("..")){//4
            //判断是不是已经在根目录
            if(curDir != Memory.getInstance().getRootDir()){
                //改变当前目录为父目录
                desDir=curDir.getFather();
            }
            else{
                System.out.println("[error] 目标目录不存在");
                return false;
            }

        }

        else {//5=1
            //在当前目录下找
            String[] splitDir = newDirPath.split("/");
            FCB temp = curDir;
            for (int i = 0; i < splitDir.length - 1; i++) {
                //找到目标文件所在目录
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[i])){
                        temp = child;
                        continue;
                    }
                }
            }
            //在该目录下找
            for (FCB child : temp.getChildren()) {
                if(child.getFileName().equals(splitDir[splitDir.length - 1])){
                    desDir=child;
                    break;
                }
            }
        }

        if(desDir==null){
            System.out.println("[error] 目标目录不存在");
            return false;
        }

        /**end: 寻找目标目录**/


        /**begin: 在目标目录下创建复制文件**/
        //判断重复
        List<FCB> children = desDir.getChildren();
        String fileName=fcb.getFileName();
        for (FCB child : children) {
            if(child.getFileName().equals(fileName)){
                fileName+="-1";
                System.out.println("[error] 文件名重复 已重命名为\""+fileName+"\"");
                break;
            }
        }
        //创建索引节点 创建FCB 文件大小为0 空文件
        IndexNode indexNode = new IndexNode(0, -1, 0, new Date());
        FCB fcb_new = new FCB(fileName, "FILE", indexNode, desDir, null);

        /*begin: read from old file and write into new file*/

        StringBuilder content = new StringBuilder();

        //read
        int[] fat = Memory.getInstance().getFat();
        Block[] disk = Disk.getINSTANCE().getBlocks();
        //从磁盘读取
        if(fcb.getIndexNode().getSize() == 0){
            //空文件
        }
        else{
//            int temp = fat[fcb.getIndexNode().getFirst_block()];
            int temp = fcb.getIndexNode().getFirstBlock();
//          while (fat[temp] != -1){
            while (temp != -1){
                //遍历读出
                content.append(disk[temp].getContent());
                temp = fat[temp];
            }
//          content.append(disk[temp].getContent());
        }
        System.out.println("[success] 读出成功");


        String temp_1=dirService.pwd(fcb_new);
        String[] splitDir_1 = temp_1.split("/");
        String diskName=splitDir_1[1];


        //write
        int size = content.toString().toCharArray().length;//文件大小为输入字数
        int first = diskService.writeToDisk(content.toString(),diskName);
        //将文件指向第一块
        fcb_new.getIndexNode().setFirstBlock(first);
        //修改索引结点大小
        fcb_new.getIndexNode().setSize(size);
        //修改父目录项 以及一直递归修改父目录的大小
        dirService.updateSize(fcb_new,true,-1);
        System.out.println("[success] 写入成功");

        /*end: read from old file and write into new file*/

        //将文件控制块放入磁盘的fcb集合
        Disk.getINSTANCE().getFcbList().add(fcb_new);
        //修改父目录的文件项 加入父目录儿子集合
        desDir.getIndexNode().addFcbNum();
        desDir.getChildren().add(fcb_new);
        System.out.println("[success] 复制文件成功");
        /**end: 在目标目录下创建复制文件**/

        return true;
    }

    /**导出文件**/
    @Override
    public Boolean export(String filePath, String exDirPath) throws IOException {
        //判断是否存在
        FCB fcb = dirService.pathResolve(filePath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 源文件不存在");
            return false;
        }
        else if(!fcb.getType().equals("FILE")){
            System.out.println("[error] 不能导出非文件");
            return false;
        }


        /**begin: 在目标文件夹下创建复制文件**/
        /*begin: read from old file and write into new file*/
        //判断重复
        String fileName=fcb.getFileName();
        File f = new File(exDirPath);
        File[] listFile = f.listFiles();// 取出该目录下所有文件以及文件夹
        int i = 0;
        // 开始遍历每一个目录项
        for (i = 0; i < listFile.length; i++) {
            f = listFile[i];
            if (f.isFile()&&f.getName().equals(fileName+".txt")) {
                fileName+="-1";
                System.out.println("[error] 文件名重复 已重命名为\""+fileName+".txt"+"\"");
                break;
            }
        }

        exDirPath = exDirPath +"\\"+fileName+".txt";;
//        System.out.println(newFilePath);
        File file=new File(exDirPath);
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(exDirPath);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        StringBuilder content = new StringBuilder();

        //read
        int[] fat = Memory.getInstance().getFat();
        Block[] disk = Disk.getINSTANCE().getBlocks();
        //从磁盘读取
        if(fcb.getIndexNode().getSize() == 0){
            //空文件
        }
        else{
//            int temp = fat[fcb.getIndexNode().getFirst_block()];
            int temp = fcb.getIndexNode().getFirstBlock();
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
        bufferedWriter.write(content.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
        System.out.println("[success] 写入成功");

        /*end: read from old file and write into new file*/

        System.out.println("[success] 导出文件成功");
        /**end: 在目标文件夹下创建复制文件**/

        return true;
    }
}
