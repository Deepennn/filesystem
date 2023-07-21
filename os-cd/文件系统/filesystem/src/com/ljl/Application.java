package com.ljl;

import com.ljl.constant.Constants;
import com.ljl.service.*;
import com.ljl.service.impl.*;
import com.ljl.utils.Utility;
import com.ljl.view.View;

import java.io.IOException;
import java.util.Scanner;

/**
 * @description: 启动类
 */
public class Application {
    private static final DataService dataService = new DataServiceImpl();
    private static final FileService fileService = new FileServiceImpl();
    private static final DiskService diskService = new DiskServiceImpl();
    private static final DirService dirService = new DirServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws IOException {
//        加载磁盘数据
        Boolean flag = dataService.loadData(Constants.SAVE_PATH);
        if(!flag){
            //加载失败请求初始化新的
            System.out.println("是否重新初始化一个磁盘空间（Y/N）");
            if(scanner.nextLine().equalsIgnoreCase("Y")){
                dataService.initData();
            }else {
                System.exit(0);
            }
        }
//        dataService.init();
        while(true){
            dirService.showPath();
            String nextLine = scanner.nextLine();
            String[] inputs = Utility.inputResolve(nextLine);
            switch (inputs[0]){

                case "Help":/*0*/
                    new View().help();
                    break;

                case "InitDisk":/*1*/
                    if(inputs.length == 1 || inputs.length %2 == 0){ //>=3
                        System.out.println("InitDisk [DiskName#] [DiskSize]KB [DiskName#] [DiskSize]KB...");
                        System.out.println("提示: 磁盘分区名须以\"#\"结尾 磁盘分区大小须为[0,16384]的整数 [0,1024)使用空闲表空闲管理 [1024,16384]使用位示图空闲管理");
                        break;
                    }
                    dataService.initData();
                    diskService.initDisk(inputs);
                    break;

                case "ChgDisk":/*2*/
                    if(inputs.length == 1){
                        System.out.println("ChgDisk [DiskName#]");
                        break;
                    }
                    diskService.chgDisk(inputs[1]);
                    break;

                case "ShowDisk":/*3*/
                    if(inputs.length == 1){
                        System.out.println("ShowDisk [DiskName#]");
                        break;
                    }
                    diskService.showDisk(inputs[1]);
                    break;

                case "MkDir":/*4*/
                    if(inputs.length == 1){
                        System.out.println("MkDir [DirPath]");
                        break;
                    }
                    dirService.mkDir(inputs[1]);
                    break;

                case "DelDir":/*5*/
                    if(inputs.length == 1){
                        System.out.println("DelDir [DirPath]");
                        break;
                    }
                    dirService.delete(inputs[1]);
                    break;

                case "Dir":/*6*/
                    if(inputs.length == 1){
                        dirService.dir("");
                    }
                    else {
                        dirService.dir(inputs[1]);
                    }
                    break;

                case "ChgDir":/*7*/
                    if(inputs.length == 1){
                        System.out.println("ChgDir [DirPath]");
                        break;
                    }
                    dirService.chgDir(inputs[1]);
                    break;

                case "TreeDir":/*8*/
                    if(inputs.length == 1){
                        dirService.treeDir("",0);
                    }
                    else {
                        dirService.treeDir(inputs[1],0);
                    }
                    break;

                case "MoveDir":/*9*/
                    if(inputs.length < 3){
                        System.out.println("MoveDir [DirPath] [NewDirPath]");
                        break;
                    }
                    dirService.move(inputs[1],inputs[2]);
                    break;

                case "Create":/*10*/
                    if(inputs.length == 1){
                        System.out.println("Create [FilePath]");
                        break;
                    }
                    fileService.create(inputs[1]);
                    break;

                case "Copy":/*11*/
                    if(inputs.length < 3){
                        System.out.println("Copy [FilePath] [NewDirPath]");
                        break;
                    }
                    else if(inputs.length == 2){
                        fileService.copy(inputs[1],inputs[1]);
                        break;
                    }
                    fileService.copy(inputs[1],inputs[2]);
                    break;

                case "Delete":/*12*/
                    if(inputs.length == 1){
                        System.out.println("Delete [FilePath]");
                        break;
                    }
                    fileService.delete(inputs[1]);
                    break;

                case "Write":/*13*/
                    if(inputs.length == 1){
                        System.out.println("Write [FilePath]");
                        break;
                    }
                    fileService.open(inputs[1]);
                    fileService.write(inputs[1]);
                    fileService.close(inputs[1]);
                    break;

                case "Read":/*14*/
                    if(inputs.length == 1){
                        System.out.println("Read [FilePath]");
                        break;
                    }
                    fileService.open(inputs[1]);
                    fileService.read(inputs[1]);
                    fileService.close(inputs[1]);
                    break;

                case "Export":/*15*///C:\Users\JIALIANGLI\Desktop
                    if(inputs.length < 3){
                        System.out.println("Export [FilePath] [ExDirPath]");
                        break;
                    }
                    fileService.export(inputs[1],inputs[2]);
                    break;

                case "Exit":/*16*/
                    System.out.println("是否将更改保存（Y/N）");
                    if(scanner.nextLine().equalsIgnoreCase("Y")){
                        dataService.saveData(Constants.SAVE_PATH);
                        System.exit(0);

                    }else {
                        System.exit(0);
                    }
                    break;

                default:
                    System.out.println("'"+inputs[0]+"' 不是内部或外部命令");
                    break;
            }
        }
    }
}
