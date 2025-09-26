package com.necsws.websocketrpapoc;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties()
public class ConfigProperties {

    private String firefoxArguments;

    private Duration waitSeconds;

    public void setFirefoxArguments(String firefoxArguments) {
        this.firefoxArguments = firefoxArguments;
    }

    public void setWaitSeconds(Duration waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    public String getFirefoxArguments() {
        return firefoxArguments;
    }

    public Duration getWaitSeconds() {
        return waitSeconds;
    }
    
    @Value("${webdriver.gecko.driver.path}")
	private String geckoDriverPath;

	public String getGeckoDriverPath() {
	     return geckoDriverPath;
	 }	
	
	 @Value("${selenium.screenshots.enabled:false}")
	 private boolean enableScreenshots;
	
	public boolean getEnableScreenshots() {
	     return enableScreenshots;
	 }
	
	@Value("${selenium.screenshots.debug:false}")
	 private boolean enableScreenshotsDir;
	
	public boolean getEnableScreenshotsDir() {
	     return enableScreenshotsDir;
	 }
	
	@Value("${logging.level.root}")
	private String logLevel;

	public String getLogLevel() {
	     return logLevel;
	 }
	
	@Value("${screenshot.save.directory}")
    private String saveScreenShotPath;
	
	public String getScreenShotPath() {
	     return saveScreenShotPath;
	 }
}
