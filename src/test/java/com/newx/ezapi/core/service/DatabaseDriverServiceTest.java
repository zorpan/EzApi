package com.newx.ezapi.core.service;

import com.newx.ezapi.core.entity.DatabaseDriver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class DatabaseDriverServiceTest {

    @Resource
    private DatabaseDriverService databaseDriverService;

    @Test
    void testCrudOperations() throws IOException {
        // 创建一个模拟文件用于测试
        MultipartFile mockFile = new MockMultipartFile(
                "test-driver.jar",
                "test-driver.jar",
                "application/java-archive",
                "test content".getBytes()
        );

        // 测试添加驱动
        DatabaseDriver driver = new DatabaseDriver();
        driver.setDriverName("Test Driver");
        driver.setDriverVersion("1.0.0");
        driver.setDriverDescription("Test driver for unit testing");
        driver.setDriverClassName("com.test.Driver");
        driver.setExampleJdbcUrl("jdbc:test://localhost:5432/testdb");
        driver.setSupportedDbTypes("TEST");

        DatabaseDriver savedDriver = databaseDriverService.addDriver(driver, mockFile);
        assertNotNull(savedDriver.getId());
        assertEquals("Test Driver", savedDriver.getDriverName());

        // 测试获取所有驱动
        List<DatabaseDriver> allDrivers = databaseDriverService.getAllDrivers();
        assertTrue(allDrivers.size() > 0);

        // 测试根据ID获取驱动
        DatabaseDriver retrievedDriver = databaseDriverService.getDriverById(savedDriver.getId());
        assertNotNull(retrievedDriver);
        assertEquals(savedDriver.getId(), retrievedDriver.getId());

        // 测试更新驱动
        retrievedDriver.setDriverName("Updated Test Driver");
        DatabaseDriver updatedDriver = databaseDriverService.updateDriver(retrievedDriver, null);
        assertEquals("Updated Test Driver", updatedDriver.getDriverName());

        // 测试切换状态
        boolean statusChanged = databaseDriverService.toggleDriverStatus(updatedDriver.getId(), false);
        assertTrue(statusChanged);

        // 测试删除驱动
        boolean deleted = databaseDriverService.deleteDriver(updatedDriver.getId());
        assertTrue(deleted);
    }
}