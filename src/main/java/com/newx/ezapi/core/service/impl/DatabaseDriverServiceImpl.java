package com.newx.ezapi.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.newx.ezapi.core.entity.DatabaseDriver;
import com.newx.ezapi.core.mapper.DatabaseDriverMapper;
import com.newx.ezapi.core.service.DatabaseDriverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DatabaseDriverServiceImpl extends ServiceImpl<DatabaseDriverMapper, DatabaseDriver> implements DatabaseDriverService {

    @Value("${file.upload.path:./uploads/drivers}")
    private String uploadPath;

    @Override
    public List<DatabaseDriver> getAllDrivers() {
        return this.list();
    }

    @Override
    public DatabaseDriver getDriverById(Long id) {
        return this.getById(id);
    }

    @Override
    public DatabaseDriver addDriver(DatabaseDriver driver, MultipartFile file) {
        // 设置时间戳
        Long currentTime = Instant.now().toEpochMilli();
        driver.setCreatedTime(currentTime);
        driver.setUpdatedTime(currentTime);
        driver.setIsActive(true); // 默认启用




        // 如果提供了文件，则处理文件上传
        if (file != null && !file.isEmpty()) {
            String fileName = storeDriverFile(file, driver.getDriverName());
            driver.setDriverFileName(fileName);
            driver.setDriverFilePath(uploadPath + "/" + fileName);
        }

        this.save(driver);
        return driver;
    }

    @Override
    public DatabaseDriver updateDriver(DatabaseDriver driver, MultipartFile file) {
        DatabaseDriver existingDriver = this.getById(driver.getId());
        if (existingDriver == null) {
            throw new RuntimeException("Driver not found with id: " + driver.getId());
        }

        // 更新基本信息
        existingDriver.setDriverName(driver.getDriverName());
        existingDriver.setDriverVersion(driver.getDriverVersion());
        existingDriver.setDriverDescription(driver.getDriverDescription());
        existingDriver.setDriverClassName(driver.getDriverClassName());
        existingDriver.setExampleJdbcUrl(driver.getExampleJdbcUrl());
        existingDriver.setSupportedDbTypes(driver.getSupportedDbTypes());
        existingDriver.setUpdatedTime(Instant.now().toEpochMilli());

        // 如果提供了新的文件，则更新文件
        if (file != null && !file.isEmpty()) {
            // 删除旧文件
            if (existingDriver.getDriverFilePath() != null) {
                try {
                    Path oldFilePath = Paths.get(existingDriver.getDriverFilePath());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    // 记录日志但不抛出异常，因为可能只是文件不存在
                    System.err.println("Failed to delete old driver file: " + e.getMessage());
                }
            }

            String fileName = storeDriverFile(file, driver.getDriverName());
            existingDriver.setDriverFileName(fileName);
            existingDriver.setDriverFilePath(uploadPath + "/" + fileName);
        }

        this.updateById(existingDriver);
        return existingDriver;
    }

    @Override
    public boolean deleteDriver(Long id) {
        DatabaseDriver driver = this.getById(id);
        if (driver == null) {
            return false;
        }

        // 删除驱动文件
        if (driver.getDriverFilePath() != null) {
            try {
                Path filePath = Paths.get(driver.getDriverFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete driver file: " + e.getMessage());
            }
        }

        return this.removeById(id);
    }

    @Override
    public boolean toggleDriverStatus(Long id, boolean status) {
        DatabaseDriver driver = this.getById(id);
        if (driver == null) {
            return false;
        }

        driver.setIsActive(status);
        driver.setUpdatedTime(Instant.now().toEpochMilli());
        return this.updateById(driver);
    }

    /**
     * 存储驱动文件
     */
    private String storeDriverFile(MultipartFile file, String driverName) {
        try {
            // 验证文件
            validateDriverFile(file);

            // 确保上传目录存在
            Path uploadDir = Paths.get(System.getProperty("user.home") + File.separator +".drivers");
            Files.createDirectories(uploadDir);

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String uniqueFileName = file.getOriginalFilename() + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadDir.resolve(uniqueFileName);

            // 将文件写入磁盘
            Files.write(filePath, file.getBytes());

            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store driver file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证驱动文件
     */
    private void validateDriverFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // 验证文件大小 (最大50MB)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds maximum limit of 50MB");
        }
        
        // 验证文件扩展名
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new RuntimeException("File name is invalid");
        }
        
        fileName = fileName.toLowerCase();
        if (!fileName.endsWith(".jar") && !fileName.endsWith(".zip") && !fileName.endsWith(".rar")) {
            throw new RuntimeException("Invalid file type. Only .jar, .zip, and .rar files are allowed.");
        }
    }
}