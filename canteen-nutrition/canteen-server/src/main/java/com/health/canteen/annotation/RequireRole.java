package com.health.canteen.annotation;

import java.lang.annotation.*;

/**
 * 角色权限校验：标注在Controller类或方法上，限定可访问的角色
 * 角色编码：1学生，2食堂商户，3管理员
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    int[] value();

}
