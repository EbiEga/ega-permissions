package uk.ac.ebi.ega.egapermissions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VersionController {

    public static final String version = "1.0.0";

    @GetMapping("/version")
    @ResponseBody
    public Version getVersion() {
        return new Version(version);
    }

}
