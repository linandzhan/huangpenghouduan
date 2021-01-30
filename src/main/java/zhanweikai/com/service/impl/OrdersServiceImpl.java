package zhanweikai.com.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zhanweikai.com.common.RestResult;
import zhanweikai.com.dao.AreaMapper;
import zhanweikai.com.dao.OrdersMapper;
import zhanweikai.com.dao.UserMapper;
import zhanweikai.com.pojo.Area;
import zhanweikai.com.pojo.Employee;
import zhanweikai.com.pojo.Orders;
import zhanweikai.com.pojo.User;
import zhanweikai.com.service.*;
import zhanweikai.com.vo.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdersServiceImpl implements OrdersService {
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private AreaMapper areaMapper;
    @Resource
    private AreaService areaService;
    @Resource
    private PeriodService periodService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private EmployeeService employeeService;


    @Override
    public RestResult saveCharteredOrders(CreateOrdersDTO orderInfo) {

        Area area = new Area();
        area.setAreaId(orderInfo.getAreaId());
        Area attachArea = areaService.attach(area);
        orderInfo.setAreaAmont(attachArea.getRentalPrice());




        User user = userMapper.selectByPhone(orderInfo.getPhone());
        orderInfo.setPaymentAmount(orderInfo.getAreaAmont());
        if(user != null){
            orderInfo.setUserId(user.getUserId());
            if (User.LEVEL_LOW.equals(user.getLevel())){
                orderInfo.setPaymentAmount(orderInfo.getAreaAmont() * 0.9);
            }else if(User.LEVEL_MIDDLE.equals(user.getLevel())){
                orderInfo.setPaymentAmount(orderInfo.getAreaAmont() * 0.8);
            }else if(User.LEVEL_MIDDLE.equals(user.getLevel())){
                orderInfo.setPaymentAmount(orderInfo.getAreaAmont() * 0.7);
            }
            User attachUser = userService.attach(user);
            if(attachUser.getBalance()-orderInfo.getPaymentAmount() < 0) {
                return RestResult.error("该客户的余额不足以扣此次包场的费用");
            }
            attachUser.setBalance(attachUser.getBalance() - orderInfo.getPaymentAmount());
            userMapper.updateByPrimaryKeySelective(attachUser);  //扣除相应的余额
        }


        orderInfo.setCreateTime(LocalDateTime.now());



        orderInfo.setEmployeeId(employeeService.getAccount().getId());

       int i =  ordersMapper.save(orderInfo);
       if(i > 0){
           return RestResult.success("包场成功",orderInfo.getPaymentAmount());
       }else {
           return RestResult.error(403,"包场失败");
       }
    }

    @Override
    public RestResult search(Pageable pageable) {
      //  List<Employee> list =  PageHelper.startPage(employeeQuery.getPage(),employeeQuery.getPageSize()).doSelectPage(()->employeeMapper.selectByEmployeeQuery(employeeQuery));
       List<Orders> list = PageHelper.startPage(pageable.getPage(),pageable.getSize()).doSelectPage(()->ordersMapper.selectOrders());
        Long total = ordersMapper.countOrders();

        List<OrdersSearchDTO> ordersDto = new ArrayList<>();
        for(Orders order : list){
            OrdersSearchDTO orderDTO = new OrdersSearchDTO();
            BeanUtils.copyProperties(order,orderDTO);

//            String dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            orderDTO.setEmployeeName(order.getEmployeeId().getUsername());
            orderDTO.setPeriodTime(order.getPeriodId().getPeriodTime());
            orderDTO.setAreaName(order.getAreaId().getNumber());
            ordersDto.add(orderDTO);
        }
        ListVo listVo = new ListVo();
        listVo.setTotal(total);
        listVo.setItems(ordersDto);

        return RestResult.success("查询成功",listVo);
    }

    @Override
    public RestResult searchStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        Double increaseIncome;

        Long ordersQuantity = ordersMapper.countOrders();
        Double allIncome = ordersMapper.countAmount();

        Long increaseOrdersQuantity = ordersMapper.countOrdersByTime(startTime,endTime);

        increaseIncome =  ordersMapper.countAmountByTime(startTime,endTime);
        if(increaseIncome == null) {
            increaseIncome = 0D;
        }
        if(allIncome == null) {
            allIncome = 0D;
        }
       statisticsDTO.setAllIncome(allIncome);
       statisticsDTO.setAllOrdersQuantity(ordersQuantity);
       statisticsDTO.setIncreaseOrdersQuantity(increaseOrdersQuantity);
       statisticsDTO.setIncreaseIncome(increaseIncome);
        return RestResult.success(statisticsDTO);
    }


}
