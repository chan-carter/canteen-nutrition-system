package com.health.canteen.mapper;

import com.health.canteen.entity.HealthProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthProfileMapper {

    HealthProfile getByUserId(Integer userId);

    /**
     * 一人一份，存在即更新（依赖 uk_profile_user 唯一键）
     */
    void saveOrUpdate(HealthProfile profile);
}
