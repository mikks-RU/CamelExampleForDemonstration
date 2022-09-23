package org.satel.bean;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.AMQConnection;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RabbitBeanGenerator implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        //Создаём ConnectionFactory для всех имеющихся параметров
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setInstanceSupplier(() -> createConnectionFactory());
        beanDefinitionRegistry.registerBeanDefinition("rabbitCURRENT", beanDefinition);
    }

    public ConnectionFactory createConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setHost(env.getProperty("rabbitmq.hostname"));
        connectionFactory.setPort(Integer.parseInt(env.getProperty("rabbitmq.port-number")));
        connectionFactory.setVirtualHost(env.getProperty("rabbitmq.vhost"));
        connectionFactory.setUsername(env.getProperty("rabbitmq.username"));
        connectionFactory.setPassword(env.getProperty("rabbitmq.password"));

        connectionFactory.setClientProperties(AMQConnection.defaultClientProperties());
        return connectionFactory;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
