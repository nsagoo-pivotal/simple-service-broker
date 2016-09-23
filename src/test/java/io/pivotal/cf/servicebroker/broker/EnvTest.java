package io.pivotal.cf.servicebroker.broker;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.service.BindingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
@SpringApplicationConfiguration(classes = {Application.class})
public class EnvTest {

    @Autowired
    private Environment env;

    @Test
    public void testEnv() throws Exception {
        assertNotNull(env);
        String host = env.getProperty("ML_HOST");
        assertNotNull(host);
        assertEquals("someHost", host);
    }

    @Test
    public void testUUID() throws Exception {
        String s = UUID.randomUUID().toString();
        assertNotNull(s);
    }
}