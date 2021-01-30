package zhanweikai.com.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zhanweikai.com.common.RestResult;
import zhanweikai.com.dao.AreaMapper;
import zhanweikai.com.dao.OrdersMapper;
import zhanweikai.com.dao.PeriodMapper;
import zhanweikai.com.pojo.*;
import zhanweikai.com.service.AreaService;
import zhanweikai.com.service.EmployeeService;
import zhanweikai.com.service.UserService;
import zhanweikai.com.vo.AreaSaveDTO;
import zhanweikai.com.vo.AreaSearchResultDTO;
import zhanweikai.com.vo.ListVo;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AreaServiceImpl implements AreaService {
    @Resource
    private AreaMapper areaMapper;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private PeriodMapper periodMapper;
    @Resource
    private UserService userService;
    @Resource
    private EmployeeService employeeService;

    public Area attach(Area area) {
        return areaMapper.selectByPrimaryKey(area.getAreaId());
    }

    @Override
    public Area getArea(Long areaId) {
        Area area = new Area();
        area.setAreaId(areaId);
        return area;
    }

    @Override
    public RestResult searchArea(String number, Integer page, Integer size) {
        Map map = new HashMap();
        map.put("number",number);
        List<Area> list =  PageHelper.startPage(page,size).doSelectPage(()->areaMapper.selectByAreaQuery(map));
        Long total = areaMapper.count(map);
        List<Area> areas = new ArrayList<>();
        for (Area area : list) {
                Area myArea = new Area();
                myArea.setAreaId(area.getAreaId());
                myArea.setNumber(area.getNumber());
                myArea.setRentalPrice(area.getRentalPrice());
                myArea.setStatus(area.getStatus());
                myArea.setType(area.getType());
                areas.add(myArea);
        }
        ListVo listVo = new ListVo();
        listVo.setTotal(total);
        listVo.setItems(areas);
//        listVo.setItems(list);
//        listVo.setTotal(total);
        return RestResult.success("查询成功",listVo);
    }

    @Override
    public RestResult saveArea(String number, String type) {
//        Area old = areaMapper.selectByNumber(number);
//        if(old != null) {
//            return RestResult.error("该场地已存在");
//        }

        AreaSaveDTO area = new AreaSaveDTO();
        area.setType(type);
        area.setNumber(number);
        if("small".equals(type)) {
            area.setRentalPrice(80d);
        }else if ("standard".equals(type)) {
            area.setRentalPrice(100d);
        }

        area.setEmployeeId(employeeService.getAccount().getId().toString());

        areaMapper.save(area);

        return RestResult.success("添加成功");
    }


    @Override
    public Area findById(Long id) {
        Area area = areaMapper.selectByPrimaryKey(id);
        return area;
    }

    /**
     * 查询场地状态是否为空场，  返回全部场地，场地为空的isSpare为true,场地不为空isSpare为false
     * @param periodId
     * @param playDay
     * @return
     */
    @Override
    public RestResult searchIsSpare(Long periodId, LocalDate playDay) {
        List<Area> areas =  areaMapper.selectAll();
        areas.forEach(i->i.setIsSpare(true));   //一般场地都为空

        Long period = getPeriodId(periodId);
        LocalDate playDay_ = getPlayDay(playDay);

        List<Orders> orders = ordersMapper.searchAreaByPeriodAndPlayDay(period,playDay_);
        for (Orders order : orders) {
            Area area = order.getAreaId();
            Long areaId = area.getAreaId();
            for (Area area_ : areas) {
                if (area_.getAreaId() == areaId) {   //该时间段内有订单对应的场地都应该设为false
                            //订单中有存在
                    area_.setIsSpare(false);  //设置为空场
                }
            }
        }
        List<AreaSearchResultDTO> areaResults = new ArrayList<>();

        setAreaResult(areaResults,areas,period,playDay_);
        return RestResult.success("返回成功",areaResults);
    }

    private void setAreaResult(List<AreaSearchResultDTO> areaResults, List<Area> areas,Long periodId,LocalDate playDay) {
        for (Area area:areas){
            AreaSearchResultDTO areaResult = new AreaSearchResultDTO();
            BeanUtils.copyProperties(area,areaResult);
            if(!area.getIsSpare()){
                //场地不为空时,根据场地和时间段查询出是谁订的场地。
                Orders order = ordersMapper.searchByAreaAndTime(area, periodId, playDay);
                if(order.getUserId().getUserId() != null){
                    //有可能是非会员包场的
                    User user = userService.attach(order.getUserId());
                    areaResult.setUserName(user.getUserName());
                }else {
                    areaResult.setPhone(order.getPhone());
                }
            }
            areaResults.add(areaResult);
        }

    }




    public Long getPeriodId(Long periodId){
        if(periodId == null){
            List<Long> periods = periodMapper.searchByNow();

            if(!periods.isEmpty()){
                return periods.get(0);
            }
        }

        return periodId;
    }

    public LocalDate getPlayDay(LocalDate playDay){
        if (playDay == null){
            return LocalDate.now();
        }
        return  playDay;
    }


}
