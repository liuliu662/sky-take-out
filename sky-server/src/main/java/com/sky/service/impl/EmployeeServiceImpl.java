package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {


    /**
     * 启用或禁用员工账号
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                //为毛生成一个新的employee,然后丢给mapper,是要配合mapper的,新的employee只设置了id和status,其他属性为null,我的mapper就可以对null的不进行更新,对非null有参数的属性进行精准更新.
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }



    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //select * from employee limit 0,10
        //开启分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //用来自动分页,给sql语句加limit的,里面的参数是获取dto的page(页码)和pagesize(分页参数)的

        Page<Employee> page= employeeMapper.pageQuery(employeePageQueryDTO);

        //获得的是page,但是我们要返回pageresult,加工一下,pageresult需要total和records

        long total = page.getTotal();
        List<Employee> records = page.getResult();//employee装了多个员工对象,每个每个对象又封装了其对应数据

        return new PageResult(total, records);
    }









    /**
     * 新增员工
     * 接口的实现类要实现接口的所有方法,你在接口多加了一个save方法,就要在实现类实现save方法,不然报错.
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();//sevice层dto用来传输,entity用来操作

        //属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);//属性拷贝,dto属性拷贝到实体,不过实体属性更多,多的属性要自己设置

        //设置账号状态,默认1表示正常,0表示锁定
        employee.setStatus(StatusConstant.ENABLE);//用常量代表1,方便阅读

        //设置密码,默认123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置创建/修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());


        //设置创建人/修改人id,先随便设置一个

        employee.setCreateUser(BaseContext.getCurrentId());

        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        //将传入的password进行MD5加密，用来和数据库中的加密密码进行比对

        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }



}
