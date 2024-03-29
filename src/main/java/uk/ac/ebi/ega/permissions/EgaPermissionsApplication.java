package uk.ac.ebi.ega.permissions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EgaPermissionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EgaPermissionsApplication.class, args);
    }
}
