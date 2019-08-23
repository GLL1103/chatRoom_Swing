package com.bittech.util;

import com.bittech.client.entity.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class commUtilTest {

    @Test
    public void loadProperties() {
        Properties pros = commUtil.loadProperties("db.properties");
        Assert.assertNotNull(pros);
    }

    @Test
    public void object2jsonTest() {
        User user = new User();
        user.setUserName("test");
        user.setPassword("111");
        user.setBrief("lalala");

        System.out.println(commUtil.object2json(user));
    }

    @Test
    public void json2Object() {

    }
}