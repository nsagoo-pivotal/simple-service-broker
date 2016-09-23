package io.pivotal.cf.servicebroker.model;

import io.pivotal.cf.servicebroker.Application;
import io.pivotal.cf.servicebroker.TestConfig;
import io.pivotal.cf.servicebroker.service.BindingService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@Ignore
public class ServiceBindingRepositoryTest {

    @Mock
    RedisTemplate<String, ServiceBinding> bindingTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Resource(name = "bindingTemplate")
    private HashOperations<String, String, ServiceBinding> repository;

    @Test
    public void instanceInsertedSuccessfully() throws Exception {
        ServiceBinding sib = TestConfig.getServiceInstanceBinding();

        repository.put(BindingService.OBJECT_ID, sib.getId(), sib);
        assertNotNull(repository.get(BindingService.OBJECT_ID, sib.getId()));

        repository.delete(BindingService.OBJECT_ID, sib.getId());
        assertNull(repository.get(BindingService.OBJECT_ID, sib.getId()));
    }
}