package uk.ac.ebi.ega.permissions;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;

@CucumberOptions(features = "src/it/resources/features/permissions-mgmt",
        plugin = {"json:target/cucumber.json", "pretty",
                "html:target/cucumber-reports"})
@RunWith(Cucumber.class)
@DirtiesContext
public class PermissionsMgmtIT {

}
