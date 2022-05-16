package com.lj.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lj.entity.Dish;
import com.lj.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
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
    @Scheduled(cron = "0 0 0 1/7 * ?")//每7天清理一次
    /**
     * windows图片定时清理
     */
    public void picCleanTask(){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Dish::getImage);
        //查询数据库中的菜单的所有图片
        List<Dish> dishList = dishService.list(queryWrapper);
        List<String> databasePicNameList = dishList.stream().map((item) -> {
            return item.getImage();
        }).collect(Collectors.toList());
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
