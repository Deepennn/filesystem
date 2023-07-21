package com.ljl.service.impl;

import com.ljl.pojo.*;
import com.ljl.service.DirService;
import com.ljl.service.DiskService;
import com.ljl.view.View;

import java.util.*;

/**
 * @description: 目录操作实现类
 */
public class DirServiceImpl implements DirService {
    private static final DiskService diskService = new DiskServiceImpl();
    private static final DirService dirService = new DirServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    @Override
    public void dir(String path) {
        Memory memory = Memory.getInstance();
        FCB fcb = null;
        if(path.trim().equals("")){
            fcb = memory.getCurDir();
        }
        else{
            fcb = dirService.pathResolve(path);
        }
        if(Objects.nonNull(fcb)){
            if(fcb.getType().equals("DIR")||fcb.getType().equals("DISK")){
                List<FCB> children = fcb.getChildren();
                View view = new View();
                System.out.println("名称\t\t\t\t修改日期\t\t\t\t类型\t\t\t\t大小");
                for (int i = 0; i < children.size(); i++) {
                    FCB child = children.get(i);
                    view.showFcb(child);
                }
            }
            else{
                System.out.println("[error] 无法展示非目录或磁盘");
            }
        }
        else {
            System.out.println("[error] 目标目录不存在");
        }
    }

    /**创建目录**/
    @Override
    public Boolean mkDir(String dirPath) {;

        FCB temp_fcb = dirService.pathResolve(dirPath);
        String[] splitDir = dirPath.split("/");
        String dirName = splitDir[splitDir.length - 1];
        if(Objects.nonNull(temp_fcb)){
            dirName+="-1";
            System.out.println("[error] 文件名重复 已重命名为\""+dirName+"\"");
        }

        /**begin: 寻找新目录的父目录**/

        dirPath = dirPath.trim();
        int index_last_slash = dirPath.lastIndexOf("/");
        if(index_last_slash!=-1){
            dirPath = dirPath.substring(0, index_last_slash);
            if(dirPath.equals(".")||dirPath.equals("..")){ // ./D->.
                dirPath+="/";
            }
        }
        else{// D->./
            dirPath="./";
        }

//        System.out.println("fatherDirPath is : "+dirPath);

        FCB curDir = Memory.getInstance().getCurDir();
        FCB rootDir = Memory.getInstance().getRootDir();
        FCB desDir = null;

        if(dirPath.startsWith("./")){//1
            dirPath = dirPath.substring(2);
            if(dirPath.equals("")){
                desDir = curDir;
            }
            else{
                splitDir = dirPath.split("/");
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

        else if(dirPath.startsWith("../")){//2

            FCB temp;
            if(curDir != rootDir){
                temp = curDir.getFather();
            }
            else{
                System.out.println("[error] 目标目录父目录不存在");
                return false;
            }
            dirPath = dirPath.substring(3);
            while (dirPath.startsWith("../")){
                if(temp != rootDir){
                    temp = temp.getFather();
                }
                else{
                    System.out.println("[error] 目标目录父目录不存在");
                    return false;
                }
                dirPath = dirPath.substring(3);
            }

            if(dirPath.equals("")){
                desDir=temp;
            }
            else {
                splitDir = dirPath.split("/");
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

        else if((dirPath.startsWith("/"))){//3
            //以/开头 从根目录逐层往下找
            dirPath = dirPath.substring(1);
            if(dirPath.equals("")){
                desDir=rootDir;
            }
            else{
                splitDir = dirPath.split("/");
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

        else if(dirPath.equals("..")){//4
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
            splitDir = dirPath.split("/");
            FCB temp = curDir;
            for (int i = 0; i < splitDir.length - 1; i++) {
                //找到目标文件所在目录
                for (FCB child : temp.getChildren()) {
                    if(child.getFileName().equals(splitDir[i])){
//                        System.out.println(splitDir[i]);
                        temp = child;
                        continue;
                    }
                }
            }
            //在该目录下找
            for (FCB child : temp.getChildren()) {
                if(child.getFileName().equals(splitDir[splitDir.length - 1])){
//                    System.out.println(splitDir[splitDir.length - 1]);
                    desDir=child;
                    break;
                }
            }
        }

        if(desDir==null){
            System.out.println("[error] 目标目录父目录不存在");
            return false;
        }

        /**end: 寻找新目录的父目录**/

        List<FCB> children = desDir.getChildren();
        //判空
        if(Objects.isNull(dirName)){
            System.out.println("[error] 目录名不可为空");
            return false;
        }
//        //判断重复
//        dirName = dirName.trim(); //去除首尾空格
//        for (FCB child : children) {
//            if(child.getFileName().equals(dirName)){
//                dirName+="-1";
//                System.out.println("[error] 目录名重复 已重命名为\""+dirName+"\"");
//                break;
//            }
//        }
        //创建索引节点 创建FCB 文件大小为0 空文件
        IndexNode indexNode = new IndexNode(0, -1, 0, new Date());
        FCB fcb = new FCB(dirName, "DIR", indexNode, desDir, new LinkedList<>());
        //将文件控制块放入磁盘的fcb集合
        Disk.getINSTANCE().getFcbList().add(fcb);
        //修改父目录的文件项 加入父目录儿子集合
        desDir.getIndexNode().addFcbNum();
        desDir.getChildren().add(fcb);
        System.out.println("[success] 创建目录成功！");
        return true;
    }

    /**切换目录**/
    @Override
    public Boolean chgDir(String path) {
        //解析路径
        FCB desDir = dirService.pathResolve(path);
        //null 不存在
        if(Objects.isNull(desDir)){
            System.out.println("[error] 目标目录不存在");
            return false;
        }else if(desDir.getType().equals("FILE")){
            //type FILE 不是磁盘或目录文件
            System.out.println("[error] 无法进入文件");
            return false;
        }else {
            //type DISK DIR 切换到对应磁盘分区 目录
            Memory.getInstance().setCurDir(desDir);
            //更改磁盘分区
            String[] splitDir = pwd(desDir).split("/");

//            System.out.println(splitDir.length);

            if(splitDir.length>1){
                FCB desDisk = dirService.pathResolve("/"+splitDir[1]);
                if(Objects.isNull(desDisk)){
                    System.out.println("[error] 磁盘分区更换错误");
                    return false;
                }
                Memory.getInstance().setCurDisk(desDisk);
            }else{
                Memory.getInstance().setCurDisk(null);
            }
        }
        return true;
    }

    /**删除目录**/
    @Override
    public Boolean delete(String dirPath) {
        //判断是否存在
        FCB fcb = dirService.pathResolve(dirPath);
        if(Objects.isNull(fcb)){
            System.out.println("[error] 目标目录不存在");
            return false;
        }
        //根目录判断
        else if(fcb == Memory.getInstance().getRootDir()){
            System.out.println("[error] 无法删除根目录");
            return false;
        }
        //磁盘分区判断
        else if(fcb.getType().equals("DISK")){
            //type DISK 磁盘
            System.out.println("[error] 无法删除磁盘");
            return false;
        }//文件判断
        else if(fcb.getType().equals("FILE")){
            //type FILE 文件
            System.out.println("[error] 无法删除文件");
            return false;
        }

//        //判断是否打开 打开要先关闭
//        //判断是否在openFileList中
//        String fill_path = dirService.pwd(fcb);
//        List<OpenFile> openFileList = Memory.getInstance().getOpenFileList();
//        OpenFile toWriteFile = null;
//        for (OpenFile openFile : openFileList) {
//            if(openFile.getFilePath().equals(fill_path)){
//                toWriteFile = openFile;
//            }
//        }
//        if(Objects.nonNull(toWriteFile)){
//            System.out.println("[error] 文件被打开 请先关闭");
//            return false;
//        }
        //重复确认
        String choice = null;
        while (true){
            System.out.println("确认删除该目录？（Y/N）");
            choice = scanner.nextLine();
            if(choice.equals("Y")) break;
            if(choice.equals("N")) {
                System.out.println("[success] 已取消删除！");
                return false;
            }
        }

        //非空目录判断
        if(fcb.getIndexNode().getSize() != 0 || fcb.getIndexNode().getFcbNum() != 0){

            if(fcb.getType().equals("DIR")){//非空文件夹
                //type DIR 目录
                //借助栈删除目录
//                diskService.freeDir(fcb);
                System.out.println("[error] 文件夹非空 无法删除");
                return false;
            }
//            else {//非空文件
//                //清空磁盘
//                diskService.freeFile(fcb);
//            }
        }
        //如果是空目录 不允许是当前目录
        if(fcb == Memory.getInstance().getCurDir()){
            System.out.println("[error] 无法删除当前目录 请先退出当前目录！");
            return false;
        }
        //从FCB集合中去除 修改父目录文件项 修改父目录儿子结点
        Disk.getINSTANCE().getFcbList().remove(fcb);
        fcb.getFather().getIndexNode().subFcbNum();
        fcb.getFather().getChildren().remove(fcb);
        //递归修改父目录文件大小
        dirService.updateSize(fcb,false,-1);
        System.out.println("[success] 删除成功");
        return true;
    }

    /**移动目录
     * @param dirPath
     * @param newDirPath**/
    @Override
    public Boolean move(String dirPath, String newDirPath){
        //判断是否存在
        FCB srcDir = dirService.pathResolve(dirPath);
        if(Objects.isNull(srcDir)){
            System.out.println("[error] 源文件不存在");
            return false;
        }
        //根目录判断
        else if(srcDir == Memory.getInstance().getRootDir()){
            System.out.println("[error] 无法移动根目录");
            return false;
        }
        //磁盘分区判断
        else if(srcDir.getType().equals("DISK")){
            //type DISK 磁盘
            System.out.println("[error] 无法移动磁盘");
            return false;
        }
        //文件判断
        else if(srcDir.getType().equals("FILE")){
            //type FILE 文件
            System.out.println("[error] 无法移动文件");
            return false;
        }
        //不允许是当前目录
        else if(srcDir == Memory.getInstance().getCurDir()){
            System.out.println("[error] 无法删除当前目录 请先退出当前目录！");
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



        /**begin: 把文件转移到目标目录下**/

        String temp_1=pwd(srcDir);
        String temp_2=pwd(desDir);
        String[] splitDir_1 = temp_1.split("/");
        String[] splitDir_2 = temp_2.split("/");

        //判断重复
        List<FCB> children = desDir.getChildren();
        String dirName=srcDir.getFileName();
        for (FCB child : children) {
            if(child.getFileName().equals(dirName)){
                dirName+="-1";
                System.out.println("[error] 目录名重复 已重命名为\""+dirName+"\"");
                break;
            }
        }

        //从原父目录中除名
        srcDir.getFather().getIndexNode().subFcbNum();
        srcDir.getFather().getChildren().remove(srcDir);
        //递归修改原父目录文件大小
        dirService.updateSize(srcDir,false,-1);

        //子目录修改父目录
        srcDir.setFileName(dirName);
        srcDir.setFather(desDir);
        srcDir.getIndexNode().setUpdateTime(new Date());

        //修改目标目录的文件项 加入目标目录儿子集合
        desDir.getIndexNode().addFcbNum();
        desDir.getChildren().add(srcDir);
        //递归修改目标目录目录文件大小
        dirService.updateSize(srcDir,true,-1);

        /**end: 把文件转移到目标目录下**/

        /**begin:(跨盘)转移文件地址**/

        if(!splitDir_1[1].equals(splitDir_2[1])){//目标目录跨盘
            //空文件判断
            if(srcDir.getIndexNode().getSize() != 0 || srcDir.getIndexNode().getFcbNum() != 0){
                if(srcDir.getType().equals("DIR")){//非空文件夹
                    //type DIR 目录
                    diskService.moveDir(srcDir, splitDir_1[1], splitDir_2[1]);//转移内部文件
                }
//                else {//非空文件
//                    //清空磁盘
//                    diskService.freeFile(fcb);
//                }
            }
        }

        /**end:(跨盘)转移文件地址**/

        System.out.println("[success] 转移目录成功！");

        return true;
    }

    /**解析路径**/
    @Override
    public FCB pathResolve(String path) {
        path = path.trim();
        FCB curDir = Memory.getInstance().getCurDir();
        FCB rootDir = Memory.getInstance().getRootDir();

        if(path.startsWith("./")){//1
            path = path.substring(2);
            if(path.equals("")){
                return curDir;
            }
            String[] splitDir = path.split("/");
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
                    return child;
                }
            }
        }

        else if(path.startsWith("../")){//2

            FCB temp;
            if(curDir != rootDir){
                temp = curDir.getFather();
            }
            else{
                System.out.println("[error] 目标目录不存在");
                return null;
            }
            path = path.substring(3);
            while (path.startsWith("../")){
                if(temp != rootDir){
                    temp = temp.getFather();
                }
                else{
                    System.out.println("[error] 目标目录不存在");
                    return null;
                }
                path = path.substring(3);
            }

            if(path.equals("")){
                return temp;
            }
            String[] splitDir = path.split("/");
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
                    return child;
                }
            }
        }

        else if((path.startsWith("/"))){//3
            //以/开头 从根目录逐层往下找
            path = path.substring(1);
            if(path.equals("")){
                return rootDir;
            }
            String[] splitDir = path.split("/");
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
                    return child;
                }
            }
        }

        else if(path.equals("..")){//4
            //判断是不是已经在根目录
            if(curDir != Memory.getInstance().getRootDir()){
                //改变当前目录为父目录
                return curDir.getFather();
            }
        }

        else {//5=1
            //在当前目录下找
            String[] splitDir = path.split("/");
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
                    return child;
                }
            }
        }

        return null;
    }

    /**更新目录大小**/
    @Override
    public void updateSize(FCB fcb, Boolean isAdd, int new_add) {
        FCB temp = fcb.getFather();
        while (!temp.getType().equals("DISK")){//temp != Memory.getInstance().getRootDir()
            //递归修改父目录的大小
            int size = temp.getIndexNode().getSize();
            if(isAdd){
                if(new_add == -1){
                    //增加目录大小
                    temp.getIndexNode().setSize(size + fcb.getIndexNode().getSize());
                }else {
                    temp.getIndexNode().setSize(size + new_add);
                }
            }else {
                temp.getIndexNode().setSize(size - fcb.getIndexNode().getSize());
            }
            temp = temp.getFather();
        }
        //更改磁盘分区
        if (temp.getType().equals("DISK"))
        {
            int size_unused = temp.getIndexNode().getSizeUnused();
            if(isAdd){
                if(new_add == -1){
                    //增加目录大小
                    temp.getIndexNode().setSizeUnused(size_unused - fcb.getIndexNode().getSize());
                }else {
                    temp.getIndexNode().setSizeUnused(size_unused - new_add);
                }
            }else {
                temp.getIndexNode().setSizeUnused(size_unused + fcb.getIndexNode().getSize());
            }
        }
    }

    /**显示全路径**/
    @Override
    public String pwd(FCB fcb) {
        Memory memory = Memory.getInstance();
        StringBuilder sb = new StringBuilder();
        FCB temp = fcb;
        while (temp != memory.getRootDir()){
            //还没打印到根目录
            sb.insert(0,temp.getFileName());
            sb.insert(0,'/');
            temp = temp.getFather();
        }
        return sb.toString();
    }

    /**输入命令时显示当前目录**/
    @Override
    public void showPath() {
        Memory memory = Memory.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(memory.getCurDir().equals(memory.getRootDir()) ? "/" : pwd(memory.getCurDir()));
        sb.append(">");
        System.out.print(sb);
    }

    /**树状显示分区目录**/
    @Override
    public void treeDir(String path,int level){
        Memory memory = Memory.getInstance();
        FCB fcb = null;
        if(path.trim().equals("")){
            fcb = memory.getCurDir();
        }
        else{
            fcb = dirService.pathResolve(path);
        }
        if(Objects.nonNull(fcb)){
            if(fcb.getType().equals("DIR")||fcb.getType().equals("DISK")){
                treeDirImpl(fcb,level);
            }
            else{
                System.out.println("[error] 无法展示非目录或磁盘");
            }
        }
        else {
            System.out.println("[error] 目标目录不存在");
        }
    }

    public void treeDirImpl(FCB fcb,int level) {
        StringBuffer str = new StringBuffer("");
        for (int i = 0; i < level; i++) {
            str.append("  "); //每一个层次缩进四个空格
        }
        str.append("├─");//└─
        if (fcb.getType().equals("FILE")) {
            System.out.println(str + fcb.getFileName());
            return;
        }

        System.out.println(str + fcb.getFileName());// 先打印目录出来
        List<FCB> children = fcb.getChildren();// 取出该目录下所有文件以及文件夹
        int i = 0;
        // 开始遍历每一个目录项
        for (i = 0; i < children.size(); i++) {
            FCB child = children.get(i);
            treeDirImpl(child, level + 1);
        }

    }

}
