package com.lj.task;

import com.lj.service.DishService;
import com.lj.service.SetmealService;
import com.lj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务
 */
@Component
@Slf4j
public class TimeTask {
    @Value("${reigie.basepath}")
    private String path;
    @Autowired
    DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private UserService userService;
    //@Scheduled(cron = "0/5 * * * * ? ")//每5秒清理一次
    @Scheduled(cron = "0 0 0 1/7 * ?  ")//每7天清理一次
    /**
     * windows图片定时清理
     */
    public void picCleanTask(){
        //查询数据库中的菜单的所有图片
        List<String> collect1 = dishService.list().stream().map((item) -> {
            return item.getImage();
        }).collect(Collectors.toList());
        List<String> collect2 = setmealService.list().stream().map((item) -> {
            return item.getImage();
        }).collect(Collectors.toList());
        List<String> collect3 = userService.list().stream().map((item) -> {
            return item.getAvatar();
        }).collect(Collectors.toList());
        List<String> databasePicNameList = new ArrayList<>();
        databasePicNameList.addAll(collect1);
        databasePicNameList.addAll(collect2);
        databasePicNameList.addAll(collect3);
        //查询windows指定目录下的所有图片
        File mkdir = new File(path);
        File[] windowsFiles = mkdir.listFiles();
        //删除windows中多余的图片(数据库中没有的图片)
        Arrays.stream(windowsFiles).map((item) -> {
            if(!databasePicNameList.contains(item.getName())){
                item.delete();
            }
            return item;
        }).collect(Collectors.toList());
    }
}
