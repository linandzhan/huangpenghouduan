package zhanweikai.com.service;

import zhanweikai.com.common.RestResult;
import zhanweikai.com.pojo.Employee;
import zhanweikai.com.pojo.Role;
import zhanweikai.com.vo.EmployeeDTO;
import zhanweikai.com.vo.EmployeeInfo;
import zhanweikai.com.vo.EmployeeQuery;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public interface EmployeeService {

    Employee selectEmployeeByName(String username);

    Employee selectEmployeeByPrimaryKey(Long id);

    Employee selectEmployeeByNameAndPassword(String username, String password);

    RestResult searchByEmployeeQuery(EmployeeQuery employeeQuery);

    Integer save(EmployeeInfo employeeInfo);

    int updateTypeEnabled(Long id);

    int updateTypeDisabled(Long id);

    Employee get(Long id);

    Set<String> findRoleByEmployee(Long id);

    Employee getAccount();

    void reSetPassword(Integer id, String password);

    void update(EmployeeDTO employeeDTO);
}
