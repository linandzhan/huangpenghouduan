package zhanweikai.com.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zhanweikai.com.common.RestResult;
import zhanweikai.com.dao.OrdersMapper;
import zhanweikai.com.pojo.Area;
import zhanweikai.com.pojo.Employee;
import zhanweikai.com.pojo.Orders;
import zhanweikai.com.vo.EmployeeQuery;

import javax.annotation.Resource;
import java.util.Map;
@RestController
@Api(tags = "订单管理")
public class OrdersController {
    @Resource
    private OrdersMapper ordersMapper;


    @ApiOperation(value = "测试")
    @ApiImplicitParam(name = "id", value = "测试id")
    @PostMapping("/api/orders/test")
    public RestResult search(){
        Orders orders = ordersMapper.selectByPrimaryKey(3);
        Double rentalPrice = orders.getAreaId().getRentalPrice();
        System.out.println(orders);
        return null;
    }














}