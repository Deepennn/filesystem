package com.ljl.constant;

/**
 * @description: 常量类
 */
public interface Constants {

    String SAVE_PATH = "./filesystem.txt"; //系统文件保存位置

    int DISK_SIZE = 64; //磁盘大小 单位MB 64*1024=65,536KB
    int BLOCK_SIZE = 4; //块大小 单位KB

    int BLOCK_COUNT = 16384; //磁盘块数 65,536/4=16,384
    int COLUMN_COUNT = 8; //列数
    int ROW_COUNT = 2048; //行数 16,384/8=2,048

    int THRESHOLD = 1024; //[0,1024)使用空闲表空闲管理 [1024,16384]使用位示图空闲管理
    int BITMAP = 0; //位示图
    int FREETABLE = 1; //空闲表
}
