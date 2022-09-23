package org.satel.bean;

import org.apache.camel.CamelContext;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Beans {

    @Autowired
    private CamelContext camelContext;

    @Bean
    public CxfEndpoint rabbitSenderService(CamelContext camelContext,
                                           @Value("${dzo.login}") String username,
                                           @Value("${dzo.password}") String password) {
        CxfEndpoint cxfEndpoint = new CxfEndpoint();
        cxfEndpoint.setCamelContext(camelContext);
        cxfEndpoint.setAddress("/Sender");
        cxfEndpoint.setWsdlURL("META-INF/wsdl/sender.wsdl");
        cxfEndpoint.setServiceClass(org.satel.RequestServices.class);

        //Если потребуется авторизация (только если делаем клиента)
//        cxfEndpoint.setUsername("username");
//        cxfEndpoint.setPassword("password");

        cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
        Map<String, Object> props = new HashMap<>();
        props.put("schema-validation-enabled", "true");
        cxfEndpoint.setProperties(props);

        return cxfEndpoint;
    }

    @Bean
    public CxfEndpoint dzoWebService(@Value("${dzo.url}") String url,
                                     @Value("${dzo.timeout}") long timeout,
                                     @Value("${dzo.login}") String username,
                                     @Value("${dzo.password}") String password) {
        CxfEndpoint cxfEndpoint = new CxfEndpoint();
        cxfEndpoint.setCamelContext(camelContext);
        cxfEndpoint.setAddress(url);
        cxfEndpoint.setWsdlURL("META-INF/wsdl/sender.wsdl");
        cxfEndpoint.setServiceClass(org.satel.RequestServices.class);
        //Если потребуется авторизация (только если делаем клиента)
//        cxfEndpoint.setUsername(username);
//        cxfEndpoint.setPassword(password);

        cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
        Map<String, Object> props = new HashMap<>();
        //props.put("schema-validation-enabled", "true");
        cxfEndpoint.setProperties(props);

        cxfEndpoint.setContinuationTimeout(timeout);

        return cxfEndpoint;
    }
}
