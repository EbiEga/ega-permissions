package uk.ac.ebi.ega.permissions;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@CucumberOptions(features = "src/it/resources/features/permissions-mgmt")
@RunWith(Cucumber.class)
public class PermissionsMgmtIT {

}
