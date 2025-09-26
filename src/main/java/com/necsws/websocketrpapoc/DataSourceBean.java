package com.necsws.websocketrpapoc;




import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import java.io.FileInputStream;
import java.io.IOException;



@PropertySource("classpath:application.properties")
//@PropertySource("classpath:/Enc/enc.properties")
//@PropertySource("file:/d:/sandippatil/enc1.properties")
@Configuration
@PropertySource("file:${encryption.path}")
@Component
public class DataSourceBean {
	
	private static final Logger log = LoggerFactory.getLogger(RPAProcessor.class);

	@Value("${encryption.password}")
	String password;
	@Value("${encryption.username}")
	String username;
	@Value("${encryption.driver-class-name}")
	String driverclassname;
	@Value("${encryption.url}")
	String url;
	@Value("${encryption.passphrase:null}")
	String passphrase;
	@Value("${encryption.salt:null}")
	String decsalt;
	
	
	@Value("${encryption.path}")
	String encryptionFilePath;
	
    @ConfigurationProperties(prefix = "spring.datasource")    
    @Bean    
    @Primary    
    public DataSource getDataSource() throws Exception {
    	if(password.startsWith("AES{")) {
    		//already encrypted
    		password = password.substring(4,password.length()-1);    		
    		try {
    		//Decrypt Password
    		password = AesEncDecService.decrypt(password, passphrase,decsalt);    		
    		} catch (Exception e) {
    			log.error("Error During Decryption, please check passphrase/salt keys");
        		e.printStackTrace();
        	}    		
    	}else {
    		if(passphrase!="" && passphrase!=null && !passphrase.equals("null")) {
    			//Encrypt password and throw exception
    		  try {
    			String saltEnc = AesEncDecService.generateSalt();
    			password = AesEncDecService.encrypt(password, passphrase,saltEnc);
    			password="AES{"+password+"}";
    			log.error(password);
    			log.error(saltEnc);
    			throw new DWPProcessingException("Sorry Password is not encrpted");
    		} catch(DWPProcessingException e) {
    			log.error("Sorry Password is not encrpted,Please Review Configuration");
    		}
    		  catch (Exception e) {
    			  log.error("Error During Decryption, please check passphrase keys");
        		  e.printStackTrace();
        	}
    		}
    		else {
    			//go with non-encrpted password
    			password=password;
    		}
    	}
    	//log.info(password);
         DataSource build = DataSourceBuilder.create()
        		.url(url)
        		.username(username)
        		.password(password)
        		.driverClassName(driverclassname)
        		.build();   		
         return build;
    }
    

}
