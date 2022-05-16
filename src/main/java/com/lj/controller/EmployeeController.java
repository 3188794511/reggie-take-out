package com.lj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lj.common.R;
import com.lj.entity.Employee;
import com.lj.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将用户提交的密码加密
        String inputPassword = employee.getPassword();
        inputPassword = DigestUtils.md5DigestAsHex(inputPassword.getBytes());
        //对比用户提交的用户名是否存在
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername,employee.getUsername());
        Employee queryEmployee = employeeService.getOne(wrapper);
        if(queryEmployee == null){
            return R.error("用户名不存在");
        }
        //比较用户输入的密码和数据库中的密码
        if(!queryEmployee.getPassword().equals(inputPassword)){
            return R.error("密码错误");
        }
        //查询员工状态   1代表可用,0或其他数字代表禁用
        if(queryEmployee.getStatus() != 1){
            return R.error("账号已被禁用");
        }
        //登陆成功,将账号id存在session中,将结果返回客户端
        request.getSession().setAttribute("employee",queryEmployee.getId());
        return R.success(queryEmployee);
    }
    
    /**
     * 员工退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");//将员工的session移除
        return R.success("退出登录成功");
    }
    
    /**
     * 添加员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        //完善员工的基本信息
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());
        // Long empId = (Long) request.getSession().getAttribute("employee");//获取当前登录员工id
        // employee.setCreateUser(empId);
        // employee.setUpdateUser(empId);
        
        //给创建的员工账号添加一个初始密码123456,需要加密
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);
        //将员工添加到数据库
        employeeService.save(employee);
        return R.success("员工添加成功");
    }
    
    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(Integer page,Integer pageSize,String name){
        //创建分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);
        //创建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(Strings.isNotEmpty(name),Employee::getName,name).orderByDesc(Employee::getCreateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    
    /**
     * 修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //添加修改的基本信息
        // employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //执行修改员工信息
        employeeService.updateById(employee);
        return R.success("修改员工信息成功");
    }
    
    /**
     * 根据员工id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查找到该员工");
    }
    
}
