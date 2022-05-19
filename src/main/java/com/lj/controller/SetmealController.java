package com.lj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lj.common.R;
import com.lj.dto.DishDto;
import com.lj.dto.SetmealDto;
import com.lj.entity.Dish;
import com.lj.entity.Setmeal;
import com.lj.entity.SetmealDish;
import com.lj.service.CategoryService;
import com.lj.service.DishService;
import com.lj.service.SetmealDishService;
import com.lj.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;
    
    /**
     * 新增套餐
     * @CacheEvict 清除缓存数据 allEntries: 将缓存名为setmealCache的缓存数据全部清除
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("套餐新增成功");
    }
    
    /**
     * 分页查询套餐
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(Integer page,Integer pageSize,String name){
        //套餐的基本分页数据
        Page<Setmeal> pageInfo = new Page<>(page,pageSize,pageSize);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo);
        //套餐的基本分页数据和套餐所属的分类
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize,pageSize);
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<SetmealDto> collect = pageInfo.getRecords().stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //将基本属性拷贝到每一个setmealDto中
            BeanUtils.copyProperties(item, setmealDto);
            //为setmealDto的categoryName属性赋值
            setmealDto.setCategoryName(categoryService.getById(setmealDto.getCategoryId()).getName());
            return setmealDto;
        }).collect(Collectors.toList());
        //为setmealDtoPage添加records属性
        setmealDtoPage.setRecords(collect);
        return R.success(setmealDtoPage);
    }
    
    /**
     * 根据id查询套餐详情(包括里面的菜品)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> findSetmealDtoById(@PathVariable Long id){
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //查询菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);
    }
    
    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("套餐修改成功");
    }
    
    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @DeleteMapping
    public R<String> delete(Long[] ids){
        setmealService.deleteWithDishByIds(ids);
        return R.success("批量删除套餐成功");
    }
    
    /**
     * 批量起售/停售
     * @param updateStatus
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping("/status/{updateStatus}")
    public R<String> status(@PathVariable Integer updateStatus,Long[] ids){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(updateStatus);
        setmealService.update(setmeal,queryWrapper);
        return R.success("套餐状态修改成功");
    }
    
    /**
     * 条件查询套餐详细信息
     * @param setmeal
     * @return
     * @Cacheable 将方法的返回值存入缓存中,value 缓存名称 key redis的key unless 触发缓存的条件
     */
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + _ + #setmeal.status",unless = "#result == null")
    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        //查询套餐基本信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId()).eq(Setmeal::getStatus,1);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        List<SetmealDto> setmealDtoList= setmealList.stream().map((item) -> {
            //封装基本属性
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //封装分类属性
            setmealDto.setCategoryName(categoryService.getById(setmealDto.getCategoryId()).getName());
            //封装菜品属性
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
            List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
            setmealDto.setSetmealDishes(setmealDishList);
            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(setmealDtoList);
    }
    
    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @GetMapping("/dish/{setmealId}")
    public R<List<DishDto>> list(@PathVariable Long setmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        List<DishDto> dishDtoList = setmealDishes.stream().map((item) -> {
            Dish dish = dishService.getById(item.getDishId());
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            dishDto.setCopies(item.getCopies());
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
}
