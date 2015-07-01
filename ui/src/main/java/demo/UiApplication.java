package demo;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@SpringBootApplication
@RestController
@EnableZuulProxy
@EnableRedisHttpSession
public class UiApplication {

  @RequestMapping("/resource")
  public Map<String,Object> home() {
    Map<String,Object> model = new HashMap<String,Object>();
    model.put("id", UUID.randomUUID().toString());
    model.put("content", "Hello World");
    return model;
  }
  @RequestMapping("/user")
  public Principal user(Principal user) {
    return user;
  }
  @RequestMapping("/token")
  @ResponseBody
  public Map<String,String> token(HttpSession session) {
    return Collections.singletonMap("token", session.getId());
  }

    public static void main(String[] args) {
        SpringApplication.run(UiApplication.class, args);
    }

    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http
          .httpBasic().and()
          .authorizeRequests()
            .antMatchers("/index.html", "/home.html", "/login.html", "/").permitAll().anyRequest()
            .authenticated().and()
          .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
          .csrf().csrfTokenRepository(csrfTokenRepository());
      }
      private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
      }
    }
}
