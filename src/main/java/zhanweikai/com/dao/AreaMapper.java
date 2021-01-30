package zhanweikai.com.dao;

import org.apache.ibatis.annotations.Mapper;
import zhanweikai.com.pojo.Area;
import zhanweikai.com.pojo.Employee;
import zhanweikai.com.vo.AreaSaveDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface AreaMapper {
    Area selectByPrimaryKey(Long id);

    List<Area> selectAll();

    List<Area> selectByAreaQuery(Map map);

    Long count(Map map);

    void save(AreaSaveDTO area);

    Area selectByNumber(String number);
}
