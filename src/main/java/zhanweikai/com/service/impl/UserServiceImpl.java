package zhanweikai.com.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import zhanweikai.com.common.RestResult;
import zhanweikai.com.dao.UserMapper;
import zhanweikai.com.pojo.Employee;
import zhanweikai.com.pojo.User;
import zhanweikai.com.service.UserService;
import zhanweikai.com.vo.CulculateDTO;
import zhanweikai.com.vo.ListVo;
import zhanweikai.com.vo.UserQueryDTO;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public User selectById(Long id) {
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    @Override
    public RestResult searchByPhoneAndName(UserQueryDTO userQueryDTO) {
        //List<Employee> list =  PageHelper.startPage(employeeQuery.getPage(),employeeQuery.getPageSize()).doSelectPage(()->employeeMapper.selectByEmployeeQuery(employeeQuery));
        List<User> users = PageHelper.startPage(userQueryDTO.getPage(),userQueryDTO.getSize()).doSelectPage(()->userMapper.searchByPhoneAndName(userQueryDTO));

        Long total = userMapper.countByPhoneAndName(userQueryDTO);
        ListVo listVo = new ListVo();
        listVo.setItems(users);
        listVo.setTotal(total);

        return RestResult.success("查询成功",listVo);
    }

    @Override
    public int updateTypeEnabled(Long id) {
        User user = new User();
        user.setUserId(id);
        user.setStatus("0");
        int i = userMapper.updateByPrimaryKeySelective(user);
        return i;
    }

    @Override
    public int updateTypeDisabled(Long id) {
        User user = new User();
        user.setUserId(id);
        user.setStatus("1");
        int i = userMapper.updateByPrimaryKeySelective(user);
        return i;
    }

    @Override
    public RestResult save(User user) {
        user.setStatus("0"); //默认为启用

   //     user.setBalance(0D);
        int i = userMapper.insertSelective(user);

        if(i > 0){
            return RestResult.success("添加成功");
        }else {
            return RestResult.error("添加失败");
        }
    }

    @Override
    public RestResult culculateReduce(Long userId, Double reduceBalance) {
        CulculateDTO culculateDTO = new CulculateDTO();
        User user = userMapper.selectByPrimaryKey(userId);

        String level = user.getLevel();
        if(User.LEVEL_HIGH.equals(level)){
            culculateDTO.setDiscount("7折");
            double result = reduceBalance * 0.7;
            culculateDTO.setActualBalance(result);
        }else if(User.LEVEL_MIDDLE.equals(level)){
            double result = reduceBalance * 0.8;
            culculateDTO.setActualBalance(result);
            culculateDTO.setDiscount("8折");
        }else if(User.LEVEL_LOW.equals(level)){
            double result = reduceBalance * 0.9;
            culculateDTO.setActualBalance(result);
            culculateDTO.setDiscount("9折");
        }else {
            culculateDTO.setActualBalance(reduceBalance);
            culculateDTO.setDiscount("无折扣");
        }


        return RestResult.success("计算成功",culculateDTO);
    }



    @Override
    public RestResult reduceBalance(Long id, Double actualBalance) {
        User user = userMapper.selectBalanceByPrimaryKey(id);
        if(user.getBalance() < actualBalance){
            return RestResult.error(400,"余额不足以扣费");
        }else if("1".equals(user.getStatus())){
            return RestResult.error(400,"该用户已被禁用");
        }
        else{
            user.setStatus(null);
            user.setBalance(user.getBalance() - actualBalance);
            int i = userMapper.updateByPrimaryKeySelective(user);
            if(i > 0){
                return RestResult.success("扣费成功");
            }else {
                return RestResult.error(400,"扣费失败");
            }

        }



    }

    @Override
    public RestResult rechargeBalance(Long id, Double rechargeMoney) {
        User user = userMapper.selectBalanceByPrimaryKey(id);
        if("1".equals(user.getStatus())){
            return RestResult.error(400,"该用户已被禁用");
        }else if(rechargeMoney < 0){
            return RestResult.error(400,"输入金额不对");
        }else {
            user.setStatus(null);
            user.setBalance(user.getBalance() + rechargeMoney);
            isLevelUpdate(user,rechargeMoney);
            int i = userMapper.updateByPrimaryKeySelective(user);
            if(i > 0){
                return RestResult.success("充值成功");
            }else {
                return RestResult.error(400,"充值失败");
            }
        }
    }

    private void isLevelUpdate(User user, Double rechargeMoney) {
        User attachUser = attach(user);
        if(User.LEVEL_NO_LEVEL.equals(attachUser.getLevel())){
            if(rechargeMoney >= 100 && rechargeMoney < 200){
                user.setLevel(User.LEVEL_LOW);
            }else if(rechargeMoney >= 200 && rechargeMoney < 400){
                user.setLevel(User.LEVEL_MIDDLE);
            }else if(rechargeMoney >= 400){
                user.setLevel(User.LEVEL_HIGH);
            }
        }

        if(User.LEVEL_LOW.equals(attachUser.getLevel())){
           if(rechargeMoney >= 200 && rechargeMoney < 400){
                user.setLevel(User.LEVEL_MIDDLE);
            }else if(rechargeMoney >= 400){
                user.setLevel(User.LEVEL_HIGH);
            }
        }

        if(User.LEVEL_MIDDLE.equals(attachUser.getLevel())){
            if(rechargeMoney >= 400){
                user.setLevel(User.LEVEL_HIGH);
            }
        }
    }

    @Override
    public User attach(User user) {
        return userMapper.selectByPrimaryKey(user.getUserId());
    }

    @Override
    public RestResult searchUserType(String phone) {
        User user = userMapper.selectByPhone(phone);
        if(user != null){
            return RestResult.success("have-level",user);
        }else {
            return RestResult.success("no-level");
        }

    }

    @Override
    public RestResult getTypeByBalance(Double balance) {
        //活动规则：100到200初级  200到400中级  400以上高级
        User user = new User();
        if( balance >= 100 && balance <200) {
            user.setLevel(User.LEVEL_LOW);
        }else if(balance >= 200 && balance <400) {
            user.setLevel(User.LEVEL_MIDDLE);
        }else if(balance >= 400) {
            user.setLevel(User.LEVEL_HIGH);
        }else {
            user.setLevel("no-level");
        }
        return RestResult.success(user);
    }
}
