package com.newx.ezapi.core.controller;

import com.newx.ezapi.common.result.Result;
import com.newx.ezapi.core.entity.DatabaseDriver;
import com.newx.ezapi.core.service.DatabaseDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据库驱动管理控制器
 */
@RestController
@RequestMapping("/api/database-driver")
public class DatabaseDriverController {

    @Autowired
    private DatabaseDriverService databaseDriverService;

    /**
     * 获取所有数据库驱动
     */
    @GetMapping("/list")
    public Result<List<DatabaseDriver>> getAllDrivers() {
        List<DatabaseDriver> drivers = databaseDriverService.getAllDrivers();
        return Result.success(drivers);
    }

    /**
     * 根据ID获取数据库驱动
     */
    @GetMapping("/{id}")
    public Result<DatabaseDriver> getDriverById(@PathVariable Long id) {
        DatabaseDriver driver = databaseDriverService.getDriverById(id);
        if (driver != null) {
            return Result.success(driver);
        } else {
            return Result.error("Driver not found");
        }
    }

    /**
     * 添加数据库驱动
     */
    @PostMapping
    public Result<DatabaseDriver> addDriver(
            @RequestParam("driverName") String driverName,
            @RequestParam(value = "driverVersion", required = false) String driverVersion,
            @RequestParam(value = "driverDescription", required = false) String driverDescription,
            @RequestParam(value = "driverClassName", required = false) String driverClassName,
            @RequestParam(value = "exampleJdbcUrl", required = false) String exampleJdbcUrl,
            @RequestParam(value = "supportedDbTypes", required = false) String supportedDbTypes,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        DatabaseDriver driver = new DatabaseDriver();
        driver.setDriverName(driverName);
        driver.setDriverVersion(driverVersion);
        driver.setDriverDescription(driverDescription);
        driver.setDriverClassName(driverClassName);
        driver.setExampleJdbcUrl(exampleJdbcUrl);
        driver.setSupportedDbTypes(supportedDbTypes);

        try {
            DatabaseDriver savedDriver = databaseDriverService.addDriver(driver, file);
            return Result.success(savedDriver);
        } catch (Exception e) {
            return Result.error("Failed to add driver: " + e.getMessage());
        }
    }

    /**
     * 更新数据库驱动
     */
    @PutMapping("/{id}")
    public Result<DatabaseDriver> updateDriver(
            @PathVariable Long id,
            @RequestParam(value = "driverName", required = false) String driverName,
            @RequestParam(value = "driverVersion", required = false) String driverVersion,
            @RequestParam(value = "driverDescription", required = false) String driverDescription,
            @RequestParam(value = "driverClassName", required = false) String driverClassName,
            @RequestParam(value = "exampleJdbcUrl", required = false) String exampleJdbcUrl,
            @RequestParam(value = "supportedDbTypes", required = false) String supportedDbTypes,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        DatabaseDriver driver = databaseDriverService.getDriverById(id);
        if (driver == null) {
            return Result.error("Driver not found");
        }

        // 更新字段
        if (driverName != null) driver.setDriverName(driverName);
        if (driverVersion != null) driver.setDriverVersion(driverVersion);
        if (driverDescription != null) driver.setDriverDescription(driverDescription);
        if (driverClassName != null) driver.setDriverClassName(driverClassName);
        if (exampleJdbcUrl != null) driver.setExampleJdbcUrl(exampleJdbcUrl);
        if (supportedDbTypes != null) driver.setSupportedDbTypes(supportedDbTypes);
        if (isActive != null) driver.setIsActive(isActive);

        try {
            DatabaseDriver updatedDriver = databaseDriverService.updateDriver(driver, file);
            return Result.success(updatedDriver);
        } catch (Exception e) {
            return Result.error("Failed to update driver: " + e.getMessage());
        }
    }

    /**
     * 删除数据库驱动
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDriver(@PathVariable Long id) {
        boolean deleted = databaseDriverService.deleteDriver(id);
        if (deleted) {
            return Result.success("Driver deleted successfully");
        } else {
            return Result.error("Failed to delete driver");
        }
    }

    /**
     * 启用/禁用数据库驱动
     */
    @PutMapping("/{id}/toggle-status")
    public Result<String> toggleDriverStatus(@PathVariable Long id, @RequestParam boolean status) {
        boolean result = databaseDriverService.toggleDriverStatus(id, status);
        if (result) {
            return Result.success("Driver status updated successfully");
        } else {
            return Result.error("Failed to update driver status");
        }
    }
}