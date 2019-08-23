package com.bittech.client.dao;
import java.sql.*;

import com.bittech.client.entity.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AccountDao extends BasedDao {
    public boolean userReg(User user) {
        //insert
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            String sql = "insert into user(username,password,brief) values(?,?,?)";
            statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            statement.setString(1,user.getUserName());
            statement.setString(2,DigestUtils.md5Hex(user.getPassword()));
            statement.setString(3,user.getBrief());
            int rows = statement.executeUpdate();
            if(rows == 1) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("用户注册失败");
            e.printStackTrace();
        }finally {
            closeResources(connection,statement);
        }
        return false;
    }

    public User userLogin(String userName,String password) {
        //查询
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //获取连接
            connection = getConnection();
            //sql脚本
            String sql = "select * from user where username = ? and password = ?";
            //创建命令
            statement = connection.prepareStatement(sql);
            statement.setString(1,userName);
            statement.setString(2,DigestUtils.md5Hex(password));
            //执行sql语句
            resultSet = statement.executeQuery();
            //结果处理
            if(resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUserName(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setBrief(resultSet.getString("brief"));
                return user;
            }
        } catch (SQLException e) {
            System.out.println("用户登录失败");
        } finally {
            closeResources(connection,statement,resultSet);
        }
        return null;
    }
}
