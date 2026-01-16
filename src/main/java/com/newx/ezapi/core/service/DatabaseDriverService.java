package com.newx.ezapi.core.service;

import com.newx.ezapi.core.entity.DatabaseDriver;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据库驱动服务接口
 */
public interface DatabaseDriverService {
    /**
     * 获取所有数据库驱动
     */
    List<DatabaseDriver> getAllDrivers();

    /**
     * 根据ID获取数据库驱动
     */
    DatabaseDriver getDriverById(Long id);

    /**
     * 添加数据库驱动
     */
    DatabaseDriver addDriver(DatabaseDriver driver, MultipartFile file);

    /**
     * 更新数据库驱动
     */
    DatabaseDriver updateDriver(DatabaseDriver driver, MultipartFile file);

    /**
     * 删除数据库驱动
     */
    boolean deleteDriver(Long id);

    /**
     * 启用/禁用数据库驱动
     */
    boolean toggleDriverStatus(Long id, boolean status);
}