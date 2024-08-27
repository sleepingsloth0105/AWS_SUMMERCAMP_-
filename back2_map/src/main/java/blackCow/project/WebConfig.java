package blackCow.project;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 스프링 서버 전역적으로 CORS 설정
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowOrigins = {
            "http://ysu-006-final.s3-website.ap-south-1.amazonaws.com",
            "https://bread-cat13.github.io/ysu-blackCow-front/"
        };

        registry.addMapping("/map/buildings/**")  // 해당 경로에 대해 CORS 설정
                .allowedOrigins(allowOrigins)  // 수정 필요!
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 HTTP 메서드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true);  // 자격 증명 허용
    }
}