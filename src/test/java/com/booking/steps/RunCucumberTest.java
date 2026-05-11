package com.booking.steps;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features",
        glue = "com.booking.steps",
        plugin = {"pretty", "html:target/report.html"},
        monochrome = true
)
public class RunCucumberTest {
}
