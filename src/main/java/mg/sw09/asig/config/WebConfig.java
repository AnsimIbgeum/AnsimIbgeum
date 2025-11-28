package mg.sw09.asig.config;

import mg.sw09.asig.filter.XssEscapeFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<XssEscapeFilter> getFilterRegistrationBean() {
        FilterRegistrationBean<XssEscapeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XssEscapeFilter());
        registrationBean.setOrder(1); // 가장 먼저 실행되도록 설정
        registrationBean.addUrlPatterns("/*"); // 모든 URL에 적용
        return registrationBean;
    }
}
