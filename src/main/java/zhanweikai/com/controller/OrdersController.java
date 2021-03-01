package zhanweikai.com.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zhanweikai.com.common.RestResult;
import zhanweikai.com.dao.AreaMapper;
import zhanweikai.com.dao.OrdersMapper;
import zhanweikai.com.dao.PeriodMapper;
import zhanweikai.com.dao.UserMapper;
import zhanweikai.com.pojo.*;
import zhanweikai.com.service.OrdersService;
import zhanweikai.com.service.UserService;
import zhanweikai.com.vo.CreateOrdersDTO;
import zhanweikai.com.vo.EmployeeQuery;
import zhanweikai.com.vo.Pageable;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@RestController
@Api(tags = "订单管理")
public class OrdersController {
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private OrdersService ordersService;
    @Resource
    private AreaMapper areaMapper;
    @Resource
    private PeriodMapper periodMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;


    @ApiOperation(value = "测试")
    @ApiImplicitParam(name = "id", value = "测试id")
    @PostMapping("/api/orders/test")
    public RestResult search(){
        Orders orders = ordersMapper.selectByPrimaryKey(3);
        Double rentalPrice = orders.getAreaId().getRentalPrice();
        System.out.println(orders);
        return null;
    }



    @ApiOperation(value = "创建包场订单")
    @ApiImplicitParam(name = "id", value = "测试id")
    @PostMapping("/api/orders/save")
    public RestResult save(@RequestBody Map<String, CreateOrdersDTO> createOrdersInfo){
        CreateOrdersDTO orderInfo = createOrdersInfo.get("orderInfo");
        return ordersService.saveCharteredOrders(orderInfo);
    }


    @ApiOperation(value = "查询包场订单")
    @PostMapping("/api/orders/search")
    public RestResult search(@RequestBody Map<String, Pageable> pageableInfo){
        Pageable pageable = pageableInfo.get("pageable");
        return ordersService.search(pageable);
    }
    @PostMapping("/api/orders/del")
    public RestResult deleteOrder(@RequestBody JSONObject jsonObject) {
        System.out.println(jsonObject);
        Long areaId = null;
        Long periodId = null;
        List<Long> periods = periodMapper.searchByNow();
        if(!periods.isEmpty()) {
            periodId = periods.get(0);
        }

        LocalDate playDay = LocalDate.now();


        areaId = areaMapper.findByNumber((String)jsonObject.get("number"));
        if(jsonObject.get("periodId") != null) {
            periodId =  Long.parseLong(jsonObject.get("periodId").toString());
        }
        if(jsonObject.get("playDay") != null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            playDay = LocalDate.parse((String)jsonObject.get("playDay"));
        }
        Orders order = ordersMapper.selectOrder(areaId,periodId,playDay);

        if(LocalDate.now().isAfter(order.getPlayDay())) {
            return RestResult.error("您已过了可取消预约的时间，无法取消");
        }else if(LocalDate.now().isEqual(order.getPlayDay())) {
            Period period = order.getPeriodId();
            if(LocalTime.now().isAfter(period.getStartTime())) {
                return RestResult.error("您已过了可取消预约的时间，无法取消");
            }
        }

        if(order.getUserId() != null) {
            User user = userService.attach(order.getUserId());
            User newUser = new User();
            newUser.setUserId(user.getUserId());
            newUser.setBalance(user.getBalance()+order.getPaymentAmount());
            userMapper.updateByPrimaryKeySelective(newUser);
        }
        ordersMapper.delete(areaId,periodId,playDay);
        return RestResult.success("取消预约成功");
    }


    @ApiOperation(value = "统计查询")
    @ApiImplicitParam(name = "pageable", value = "分页信息")
    @PostMapping("/api/orders/searchStatistics")
    public RestResult searchStatistics(@RequestBody JSONObject jsonObject){
        LinkedHashMap dateTimeRange = (LinkedHashMap) jsonObject.get("dateTimeRange");
        String startTime = (String) dateTimeRange.get("start");
        String endTime = (String) dateTimeRange.get("end");

        if(startTime == null || endTime == null){
            LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime now = LocalDateTime.now();
            return ordersService.searchStatistics(start,now);
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTimeDate = LocalDateTime.parse(startTime, df);
        LocalDateTime endTimeDate = LocalDateTime.parse(endTime, df);
        return ordersService.searchStatistics(startTimeDate,endTimeDate);
    }













}
