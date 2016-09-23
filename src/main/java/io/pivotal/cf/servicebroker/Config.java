package io.pivotal.cf.servicebroker;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.pivotal.cf.servicebroker.broker.MarkLogicManageAPI;
import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("cloud")
public class Config {

    @Autowired
    private Environment env;

    //TODO move to brokeredservice?
    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion("2.10");
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    @Autowired
    RedisTemplate<String, ServiceInstance> instanceTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, ServiceInstance> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

    @Bean
    @Autowired
    RedisTemplate<String, ServiceBinding> bindingTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, ServiceBinding> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

    @Bean
    public MarkLogicManageAPI markLogicManageAPI() {
        return Feign
                .builder()
                .encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(env.getProperty("ML_USER"), env.getProperty("ML_PW")))
                .target(MarkLogicManageAPI.class,
                        "http://" + env.getProperty("ML_HOST") + ":" + env.getProperty("ML_PORT"));
    }
}