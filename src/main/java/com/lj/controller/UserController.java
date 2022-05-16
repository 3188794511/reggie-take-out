package com.lj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lj.common.BaseContext;
import com.lj.common.R;
import com.lj.entity.User;
import com.lj.service.UserService;
import com.lj.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 发送短信验证码
     * @param user
     * @return
     */
    @PostMapping("/code")
    public R<String> code(@RequestBody User user){
        //获取用户名
        String phone = user.getPhone();
        //生成验证码
        if (StringUtils.hasLength(phone)){
            String validateCode = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("登录验证码为:{}",validateCode);
            //发送短信验证码
            //SMSUtils.sendMessage(SMSUtils.SMS_LOGIN,new String[]{validateCode},new String[]{"86"+ phone});
            //将短信验证码存储到Redis
            redisTemplate.opsForValue().set(phone,validateCode,5, TimeUnit.MINUTES);//5分钟后过期
            return R.success("短信验证码发送成功");
        }
        return R.error("短信验证码发送失败");
    }
    
    /**
     * 登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody Map<String,String> map, HttpServletRequest request){
        String phone = map.get("phone");
        String code = map.get("code");
        //比对用户提交的验证码和redis中的验证码是否一致
        if (!redisTemplate.opsForValue().get(phone).equals(code)){
            return R.error("输入验证码错误");
        }
        //判断当前手机号是否已经注册,未注册,自动注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,phone);
        User user = userService.getOne(queryWrapper);
        if(Objects.isNull(user)){
            user = new User();
            user.setPhone(phone);
            userService.save(user);
        }
        //将用户信息保存到session
        request.getSession().setAttribute("user",user.getId());
        return R.success("登录成功");
    }
    
    /**
     * 登出
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpServletRequest request){
        //移除session信息
        request.getSession().removeAttribute("user");
        return R.success("退出登录成功");
    }
    
    /**
     * 获取当前登录的用户信息
     * @return
     */
    @PostMapping("/userInfo")
    public R<User> userInfo(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,userId);
        User user = userService.getOne(queryWrapper);
        return R.success(user);
    }
    
    /**
     * 修改用户信息(完善个人信息)
     * @return
     */
    @PutMapping
    public R<String> updateUserInfo(@RequestBody User user){
        log.info(user.toString());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,BaseContext.getCurrentId());
        userService.update(user,queryWrapper);
        return R.success("用户信息修改成功");
    }
}
