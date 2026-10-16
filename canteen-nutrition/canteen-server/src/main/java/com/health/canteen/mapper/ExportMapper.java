package com.health.canteen.mapper;

import com.health.canteen.export.DishExportVO;
import com.health.canteen.export.OrderExportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExportMapper {

    List<DishExportVO> listDishes(@Param("merchantId") Integer merchantId);

    List<OrderExportVO> listOrders(@Param("merchantId") Integer merchantId);
}
