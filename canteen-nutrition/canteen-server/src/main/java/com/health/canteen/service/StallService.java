package com.health.canteen.service;

import com.health.canteen.result.PageResult;
import com.health.canteen.entity.Stall;
import com.health.canteen.dto.StallPageQueryDTO;

import java.util.List;

public interface StallService {

    PageResult<Stall> getStallPage(StallPageQueryDTO stallQueryParam);

    Stall getStallById(Integer id);

    void addStall(Stall stall);

    void updateStall(Stall stall);

    void updateStatus(Integer id, Integer status);

    void deleteStall(List<Integer> ids);

}
