package blackCow.project;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//aws
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


@Configuration
@ComponentScan
public class AppConfig {


    @Bean
    public AmazonS3 amazonS3(){

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTH_1)
                .build();
    }


}

