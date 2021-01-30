package zhanweikai.com.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zhanweikai.com.annotation.RequiredPermission;
import zhanweikai.com.common.RestResult;
import zhanweikai.com.pojo.Employee;
import zhanweikai.com.service.EmployeeService;
import zhanweikai.com.vo.EmployeeInfo;
import zhanweikai.com.vo.EmployeeQuery;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@Api(tags = "管理员管理")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;


    @ApiOperation(value = "用户登录接口")
    @ApiImplicitParams({ @ApiImplicitParam(name = "username", value = "登录名"), @ApiImplicitParam(name = "password", value = "密码", required = true)})
    @PostMapping("/employee/login")
    public RestResult login(@RequestBody Map<String,String> loginInfo , HttpServletRequest http) throws UnsupportedEncodingException {
        //Map<String,String> loginInfo
  //      Employee employee = employeeService.selectEmployeeByName(username);
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
//        String u = new String(username.getBytes("utf-8"),"utf-8");
        Employee employee =  employeeService.selectEmployeeByNameAndPassword(username,password);
        if(employee == null){
            RestResult restResult = RestResult.error(400, "密码或用户名错误");
            return restResult;
        }else if("禁用".equals(employee.getType())){
            return RestResult.error(201,"账号已被禁用");
        }
        else{
            http.getSession().setAttribute("employee",employee);
            RestResult restResult = RestResult.success();
            restResult.put("employee",employee);
            restResult.put("message","返回成功");
            return  restResult;
        }
    }



    @ApiOperation(value = "员工查询")
    @ApiImplicitParam(name = "employeeQuery", value = "员工查询信息")
    @PostMapping("/api/employee/search")
    public RestResult search(@RequestBody Map<String,EmployeeQuery> employeeQueryMap){
        EmployeeQuery employeeQuery = employeeQueryMap.get("employeeQuery");

        return employeeService.searchByEmployeeQuery(employeeQuery);
    }


    @RequiredPermission("employee:add")
    @ApiOperation(value = "员工添加")
    @ApiImplicitParam(name = "employeeQuery", value = "员工添加信息")
    @PostMapping("/api/employee/save")
    public RestResult save(@RequestBody Map<String, EmployeeInfo> employeeInfoMap){
        EmployeeInfo employeeInfo = employeeInfoMap.get("employeeInfo");
      Integer result = employeeService.save(employeeInfo);
      if(result > 0){
          return  RestResult.success("插入成功");
      }
        return RestResult.error(201,"插入失败，可能用户名已经存在");
    }

    @RequiredPermission("employee:enable")
    @ApiOperation(value = "员工账号启用")
    @ApiImplicitParam(name = "longMap", value = "员工id")
    @PostMapping("/api/employee/enable")
    public RestResult enabled(@RequestBody Map<String,Long> longMap){
        Long id = longMap.get("id");
        int result = employeeService.updateTypeEnabled(id);
            if(result > 0){
                return  RestResult.success("已启用");
            }
        return RestResult.error(400,"启用失败");
    }

    @RequiredPermission("employee:disable")
    @ApiOperation(value = "员工账号禁用")
    @ApiImplicitParam(name = "longMap", value = "员工id")
    @PostMapping("/api/employee/disable")
    public RestResult disabled(@RequestBody Map<String,Long> longMap){
        Long id = longMap.get("id");
        Employee employee = employeeService.get(id);
        if("1".equals(employee.getRole())){
            return RestResult.error(400,"管理员账户不可禁用！");
        }
        int result = employeeService.updateTypeDisabled(id);
        if(result > 0){
            return  RestResult.success("已禁用");
        }
        return RestResult.error(400,"禁用失败");
    }



    @ApiOperation(value = "密码修改")
    @ApiImplicitParam(name = "longMap", value = "员工id")
    @PostMapping("/api/employee/updatePassword")
    public RestResult updatePassword(@RequestBody JSONObject jsonObject) {

        String password = (String) jsonObject.get("password");
        Integer id = jsonObject.getInteger("id");

        employeeService.reSetPassword(id,password);

        return RestResult.success("修改成功");
    }



}
