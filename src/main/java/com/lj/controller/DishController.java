package com.lj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lj.common.R;
import com.lj.dto.DishDto;
import com.lj.entity.Dish;
import com.lj.entity.DishFlavor;
import com.lj.service.CategoryService;
import com.lj.service.DishFlavorService;
import com.lj.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto){
        //清除缓存中的数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        //调用业务方法
        dishService.add(dishDto);
        return R.success("菜品新增成功");
    }
    
    /**
     * 分页查询菜品
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(Integer page,Integer pageSize,String name){
        //创建条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(name != null,Dish::getName,name).orderByDesc(Dish::getUpdateTime);
        //创建分页对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        //查询分页数据
        dishService.page(pageInfo,queryWrapper);
        //客户端要显示菜品对应的分类名称,所以要对dish进行处理
        Page<DishDto> list = new Page<>();
        //将基本信息进行拷贝
        BeanUtils.copyProperties(pageInfo,list,"records");
        //将分页数据中的records属性进行拷贝并修改成客户端需要的样式
        List<DishDto> listRecords = list.getRecords();
        listRecords = pageInfo.getRecords().stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            dishDto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());
            return dishDto;
        }).collect(Collectors.toList());
        
        
        //将listRecords添加到list中
        list.setRecords(listRecords);
        //执行查询
        dishService.page(pageInfo,queryWrapper);
        return R.success(list);
    }
    
    /**
     * 根据id查询菜品详细信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> findByIdWithFlavor(@PathVariable Long id){
        DishDto dishDto = dishService.findByIdWithFlavor(id);
        return R.success(dishDto);
    }
    
    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> edit(@RequestBody DishDto dishDto){
        //清除缓存中的数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);
        //修改菜品基本信息
        dishService.updateById(dishDto);
        //修改菜品口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
          item.setDishId(dishDto.getId());
          LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.eq(DishFlavor::getDishId,item.getDishId());
          dishFlavorService.update(item,queryWrapper);
          return item;
        });
        return R.success("菜品修改成功");
    }
    
    /**
     * 批量起售/停售
     * @param updateStatus
     * @param ids
     * @return
     */
    @PostMapping("/status/{updateStatus}")
    public R<String> status(@PathVariable Integer updateStatus,Long[] ids){
        List<Dish> dishList = dishService.listByIds(Arrays.asList(ids));
        //批量获取缓存中的key
        List<String> keys = dishList.stream().map((item) -> {
            String key = "dish_" + item.getCategoryId() + "_1";
            return key;
        }).collect(Collectors.toList());
        //批量删除缓存中的数据
        redisTemplate.delete(keys);
        dishService.updateStatusByIds(updateStatus, ids);
        return R.success("菜品状态修改成功");
    }
    
    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        List<Dish> dishList = dishService.listByIds(Arrays.asList(ids));
        //批量获取缓存中的key
        List<String> keys = dishList.stream().map((item) -> {
            String key = "dish_" + item.getCategoryId() + "_1";
            return key;
        }).collect(Collectors.toList());
        //批量删除缓存中的数据
        redisTemplate.delete(keys);
        dishService.deleteByIds(ids);
        return R.success("菜品删除成功");
    }
    
    
    /**
     * 条件查询菜品详细信息(菜品基本信息,口味,分类等)
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;
        //先从缓存中获取
        String key = "dish_" + dish.getCategoryId() + "_" +dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null && dishDtoList.size() > 0){
            //缓存中有数据
            return R.success(dishDtoList);
        }
        //换存中没有数据,查询数据库
        //查询菜品基本信息
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId())
                        .eq(Dish::getStatus,1)
                        .orderByAsc(Dish::getSort)
                        .orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);
        dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //封装菜品属性
            dishDto.setCategoryName(categoryService.getById(dishDto.getCategoryId()).getName());
            //封装口味属性
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        //将查询出来的数据放入缓存中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);//一小时过期
        return R.success(dishDtoList);
    }
    
}
