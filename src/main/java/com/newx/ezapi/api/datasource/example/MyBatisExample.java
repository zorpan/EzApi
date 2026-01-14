package com.newx.ezapi.api.datasource.example;

import com.newx.ezapi.api.datasource.service.DataSourceManager;
import com.newx.ezapi.api.datasource.service.DatabaseQueryService;
import com.newx.ezapi.api.datasource.service.impl.DataSourceManagerImpl;
import com.newx.ezapi.api.datasource.service.impl.DatabaseQueryServiceImpl;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MyBatis风格SQL使用示例
 */
@Component
public class MyBatisExample {

    public static void main(String[] args) {
        // 这个示例演示如何使用MyBatis风格的SQL
        demonstrateMyBatisFeatures();
    }

    public static void demonstrateMyBatisFeatures() {
        // 创建数据源管理器和服务实例
        DataSourceManager dataSourceManager = new DataSourceManagerImpl();
        DatabaseQueryService databaseQueryService = new DatabaseQueryServiceImpl();
        
        // 这里只是示例，实际使用时会通过Spring自动注入
        
        System.out.println("=== MyBatis风格SQL示例 ===");
        
        // 示例1: 使用<if>标签进行条件查询
        String selectWithIf = "SELECT * FROM users " +
                             "<where> " +
                             "  <if test='id != null'> AND id = #{id} </if> " +
                             "  <if test='name != null'> AND name = #{name} </if> " +
                             "  <if test='email != null'> AND email LIKE CONCAT('%', #{email}, '%') </if> " +
                             "</where>";
        
        System.out.println("SQL模板: " + selectWithIf);
        
        // 参数1: 只传id
        Map<String, Object> params1 = new HashMap<>();
        params1.put("id", 1);
        System.out.println("参数1: " + params1);
        System.out.println("生成SQL: SELECT * FROM users WHERE id = '1'");
        
        // 参数2: 传id和name
        Map<String, Object> params2 = new HashMap<>();
        params2.put("id", 1);
        params2.put("name", "John");
        System.out.println("参数2: " + params2);
        System.out.println("生成SQL: SELECT * FROM users WHERE id = '1' AND name = 'John'");
        
        // 参数3: 传name和email
        Map<String, Object> params3 = new HashMap<>();
        params3.put("name", "John");
        params3.put("email", "john@example.com");
        System.out.println("参数3: " + params3);
        System.out.println("生成SQL: SELECT * FROM users WHERE name = 'John' AND email LIKE CONCAT('%', 'john@example.com', '%')");
        
        System.out.println("\n=== UPDATE语句示例 ===");
        
        // UPDATE语句使用<set>和<if>标签
        String updateWithSet = "UPDATE users " +
                              "<set> " +
                              "  <if test='name != null'>name = #{name}, </if> " +
                              "  <if test='email != null'>email = #{email}, </if> " +
                              "  <if test='age != null'>age = #{age} </if> " +
                              "</set> " +
                              "WHERE id = #{id}";
        
        System.out.println("UPDATE模板: " + updateWithSet);
        
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", 1);
        updateParams.put("name", "Updated Name");
        updateParams.put("email", "updated@example.com");
        // 注意：没有传递age，所以不会更新age字段
        System.out.println("UPDATE参数: " + updateParams);
        System.out.println("生成SQL: UPDATE users SET name = 'Updated Name', email = 'updated@example.com' WHERE id = '1'");
        
        System.out.println("\n=== DELETE语句示例 ===");
        
        // DELETE语句使用<where>和<if>标签
        String deleteWithWhere = "DELETE FROM users " +
                                "<where> " +
                                "  <if test='id != null'>id = #{id}</if> " +
                                "  <if test='name != null and id == null'>AND name = #{name}</if> " +
                                "  <if test='email != null and name == null and id == null'>AND email = #{email}</if> " +
                                "</where>";
        
        System.out.println("DELETE模板: " + deleteWithWhere);
        
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("email", "delete@example.com");
        System.out.println("DELETE参数: " + deleteParams);
        System.out.println("生成SQL: DELETE FROM users WHERE email = 'delete@example.com'");
        
        System.out.println("\n=== 复杂条件示例 ===");
        
        String complexQuery = "SELECT * FROM orders " +
                             "<where> " +
                             "  <if test='userId != null'>user_id = #{userId}</if> " +
                             "  <if test='status != null'>AND status = #{status}</if> " +
                             "  <if test='amountMin != null'>AND amount >= #{amountMin}</if> " +
                             "  <if test='amountMax != null'>AND amount <= #{amountMax}</if> " +
                             "  <if test='dateFrom != null'>AND created_date >= #{dateFrom}</if> " +
                             "  <if test='dateTo != null'>AND created_date &lt;= #{dateTo}</if> " +
                             "</where> " +
                             "ORDER BY created_date DESC";
        
        System.out.println("复杂查询模板: " + complexQuery);
        
        Map<String, Object> complexParams = new HashMap<>();
        complexParams.put("userId", 123);
        complexParams.put("status", "active");
        complexParams.put("amountMin", 100);
        System.out.println("复杂参数: " + complexParams);
        System.out.println("此查询将根据提供的参数动态构建WHERE子句");
    }
}