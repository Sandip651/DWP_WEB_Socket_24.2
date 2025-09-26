package com.necsws.websocketrpapoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;

public class RPAProcessor<TenancyDetails, ArrearsResponse> implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(RPAProcessor.class);

	private WebSocketSession session;
	private JsonNode currentJsonMessage;
	private WebDriver driver;
	private WebDriverWait waitingDriver;
	private ConfigProperties configProperties;

	private ODBCPackageCall odbcPackageCall;
	
	public RPAProcessor(WebSocketSession session, ConfigProperties configProperties, ODBCPackageCall odbcPackageCall) {
		this.session = session;
		this.configProperties = configProperties;
		this.odbcPackageCall = odbcPackageCall;
	}

	public void setCurrentJsonMessage(JsonNode currentJsonMessage) {
		this.currentJsonMessage = currentJsonMessage;
	}
	
	private Long defaultTimeout = (long) 300;
	
		
//	By locators
//	SignIn page
	By SignInPage = By.linkText("Sign in - Landlord portal.html");
	By eleUserName = By.id("id-userName");
	By elePassword = By.id("id-password");
	By submitButton = By.id("id-submit-button");

	By loginErrorSignInPage = By.id("validation-error");
	By loginErrorAccessCode = By.xpath("//div[@id='id-section-error-accessCode']");
	
//	Access code Page
	By accessCodePage = By.linkText("Enter your access code - Landlord portal.html");
	By codeText = By.id("id-accessCode");
	By errorAccessCodePage = By.linkText("Error - Enter your access code - Landlord portal.html");
	By newAccessCodeButton = By.xpath("//section[@id='expandableHelp2']//button");

//	ToDoList Page
	By toDoListPage = By.linkText("To-do list - Landlord portal.html");
	By customerName = By.xpath("(//a[@class='task-list__link'])[1]//div[@class='task-list__details']//span[1]");
	By listOfCustomers_BOTH = By.xpath("//li[@class='task-list__item']/a/div[@class='task-list__title']");
	By listOfCustomers_TENDETS = By.xpath("//a[contains(@href,'provide-tenancy-details')]//div[@class='task-list__title']");
	By listOfCustomers_HOUCOST = By.xpath("//a[contains(@href,'confirm-tenant-housing-costs')]//div[@class='task-list__title']");
	By toDoListLink = By.xpath("//a[contains(text(),'To-do list')]");
	By showMore_Button = By.id("id-show-more");
	
	//	Updates Page
	By updatePage = By.linkText("Updates - Landlord portal.html");

//	Tenancy Page1
	By tenancyPage1 = By.linkText("Confirm tenancy details - Page 1.html");
//	By tenantNameAndDOB = By
//			.xpath("//span[contains(text(),'Confirm tenan')]/parent::h1//following-sibling::form//div[@class='panel']//p[1]");
	By tenantNameAndDOB_HOUCOSTS = By.xpath("//h2[@id='tenant-name']");
//	By tenantNameAndDOBSecondTenant = By
//			.xpath("//span[contains(text(),'Confirm tenan')]/parent::h1//following-sibling::form//div[@class='panel']//p[2]");
	By tenancyAddress = By
			.xpath("//span[contains(text(),'Confirm tenancy details')]/parent::h1//following-sibling::form//div//p[last()-1]");
	By tenancyDate = By
			.xpath("//span[contains(text(),'Confirm tenancy details')]/parent::h1//following-sibling::form//div//p[last()]");
	By noOfParaInTenancyPage = By.xpath("//span[contains(text(),'Confirm tenan')]/parent::h1//following-sibling::form//div[@class='panel']//p");
	
//	By tenantNameAndDOB_HousingCost = By.xpath("//p[@id='tenant-name']");
	By tenancyAddress_HousingCost = By.xpath("//p[@id='tenant-address']");
	By tenantNameAndDOB_TENDETS = By
			.xpath("//span[contains(text(),'Confirm tenan')]/parent::h1//following-sibling::form//div[@class='panel']//h2");
	By tenantLiableToRent_Yes = By.id("isATenant-clickable-true");
	By tenantLiableToRent_No = By.id("isATenant-clickable-false");
	By tenantLiableTenantRef = By.id("id-rentReference");
	By acceptingOtherCharges_Yes = By.id("acceptingOtherCharges-clickable-true");
	By acceptingOtherCharges_No = By.id("acceptingOtherCharges-clickable-false");
	By accpOtherChargesTenantRef = By.id("id-rentReference2");

//	Tenancy Page2
	By tenancyPage2 = By.linkText("Confirm tenancy details - Tenancy details - To-do list - Page 2.html");
	By tempAccommodation_Yes = By.id("temporaryAccommodation-clickable-true");
	By tempAccommodation_No = By.id("temporaryAccommodation-clickable-false");
	By isAnyoneApartFromClaimant_Yes = By.id("jointTenancy-clickable-true");
	By isAnyoneApartFromClaimant_No = By.id("jointTenancy-clickable-false");
	By noOfTenantsIncludingClaimantText = By.id("id-tenants");
	By selectBedroom = By.id("id-numberOfBedrooms");
	By rentFreeWeeks_Yes = By.id("rentFreeWeeks-clickable-true");
	By rentFreeWeeks_No = By.id("rentFreeWeeks-clickable-false");
	By noOfWeeksTextInput = By.id("id-numberOfRentFreeWeeks");

//	Tenancy Page3
	By tenancyPage3 = By.linkText("Confirm tenancy details - Tenancy costs - Page 3.html");
	By rentForThePropertyText = By.id("id-rentAmount");
	By selectFrequencyForRent = By.id("id-rentPaymentFrequency");
	By isEligibleServiceCharge_Yes = By.id("serviceChargesExists-clickable-true");
	By isEligibleServiceCharge_No = By.id("serviceChargesExists-clickable-false");
	By eligibleServiceChargeText = By.id("id-eligibleServiceChargeAmount");
	By selectFrequencyForServiceCharge = By.id("id-eligibleServiceChargeFrequency");

//	Tenancy Page 2 of Housing Costs
	By doYouHaveServiceCharges_Yes = By.id("isServiceCharges-clickable-true");
	By doYouHaveServiceCharges_No = By.id("isServiceCharges-clickable-false");
	By serviceChargeText = By.id("id-serviceChargesAmount");
	By selectServiceChargePaymentFrequency = By.id("id-serviceChargesPaymentFrequency");
	By dateOfTheseCostEffectiveFrom = By.id("clickable-monthlyRentEffectiveDate");
	By dateOfTheseCostEffectiveWeekly = By.id("clickable-weeklyRentEffectiveDate");
	By dateOfTheseCostEffectiveFromOther = By.id("clickable-other");
	By dateOfTheseCostEffectiveFromOther_Day = By.id("id-effectiveDate.day");
	By dateOfTheseCostEffectiveFromOther_Month = By.id("id-effectiveDate.month");
	By dateOfTheseCostEffectiveFromOther_Year = By.id("id-effectiveDate.year");
	
//	Tenancy Page4
	By tenancyPage4 = By.linkText("Confirm tenancy details - Check and confirm details - Page 4.html");
	By tenancy4_heading = By.id("main-heading");
	By areTheseDetailsCorrect_Yes = By.id("correct-clickable-true");
	By areTheseDetailsCorrect_No = By.id("correct-clickable-false");
	
//	Housing Cost Page3
	By tenancy3_heading = By.xpath("//h2[@class='heading-medium']");

	String username = null;
	String password;
	String accessCode = null;
	String prru_id = null;
	String dwpConsecErrors;
	String dwpSubmitData;
	String dwpRequestType;
	String dwpNumberPrc;
	String dwpAraTenDets;
	String dwpAraHouCosts;
	String dwpNumOfMins;
	String dwpTimeOff;
	int l_errorCount = 0;
	int dwpConsecutiveErrors = 0;
	long dwpNumberOfMinutes;
	Integer dwpNumberOfRecordsToBeProcessed;
	String acaBalance;
	String nameAndDOB;
	String actualCustomerName;
	String DateOfBirth;
	String requiredDateFormat;
	String dob;
	String postCode;
	String actualDate;
	String nameAndDOBSecondTenant;
	String DateOfBirthSecondTenant;
	String dobSecondTenat;
	String tenantName;
	String secondTenantName;
	int countOfCustomer;
	String addressHousingCost;
	Date dateRentChargesChangeDt;
	Date dateServiceChargesChangedt;
	Date effectiveDate;
	String typeOfTenant;
	int a = 0;
	int y = 0;
	int x =0;
	String skipUpdate="N";
    int countOfCustomerSm = 0;
    int countOfCustomerOld = 0;
    int sameRec = 0;
    int zeroRec = 0;
	
	public void run() {
//		log.info("Running {}", session.getId());
		if (connectionIsValid()) {
			try {
				mainProcessing();
			} 
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			// Connection not valid, throw error back to client
			onError("Web Socket connection failed from spring boot application");
		}
	}

	private void mainProcessing() throws FileNotFoundException {
//		JsonNode loginDetails;
		JsonNode details;
		String loginUrl = "";	
		loginUrl = odbcPackageCall.getSystemParam("DWP_URL");
		log.info("login URL is :" + loginUrl);
		if (loginUrl == null)
		{
				// login url should not be null
	            onError("Please check system parameter of URL");
		}
//		log.info("defaultTimeOut"+defaultTimeout.toString());
		//Added try and catch exception to handle the closing of websocket and firefox instance
		try {
		startBrowser();
		try {
//			DWP : ENABLE FOR DWP
			goToSite(loginUrl);			
		}
		catch(Exception e) {
			e.printStackTrace();
			onError("Please check Login URL");
			throw new DwpWebSocketException("Login URL is not valid- Closing session");	
		}

		waitForTitleIs("Sign in - Landlord portal", 2);
//		String signPageURL = driver.getCurrentUrl();
//		log.info("The url of signIn page is : " +signPageURL);
		takeScreenshot("pre-signin");
		details = waitForMessage("LOGIN");
		if (details != null) {
			username = details.get("username").asText();
			password = details.get("password").asText();
			doSendKeysWithWait(eleUserName, username, 5);
			doSendKeysWithWait(elePassword, password, 5);
			takeScreenshot("sign-in page");
			doClickWithWait(submitButton, 5);
			untilPageLoadComplete(driver);

		}
		
		pageLoadWait(driver);
		int loginErrorCheck =0;
		while (driver.findElements(By.xpath("//span[contains(text(),'Enter your access code')]")).isEmpty()) {
			try {
				String errorMessage = getText(loginErrorSignInPage);
				log.info("Login Failed : " + errorMessage);
				onError(errorMessage);
				log.info("Login Failed ReLogin");
				takeScreenshot("pre-sign in");
				details = waitForMessage("RELOGIN");
				if (details != null) {
					username = details.get("username").asText();
					password = details.get("password").asText();
					doSendKeysWithWait(eleUserName, username, 5);
					doSendKeysWithWait(elePassword, password, 5);
					takeScreenshot("sign-in page");
					doClickWithWait(submitButton, 5);
					untilPageLoadComplete(driver);
				}
			} catch (Exception e) {
				loginErrorCheck++;
				if(loginErrorCheck>=5) {
					onError("Error on Login Page, Please Press Cancel Button");
					throw new DwpWebSocketException("Error on Login Page, Please Press Cancel Button");
//					closeBrowser();
//					endConnection();
//					break;
				}
//				e.printStackTrace();
			}
		}

		details = waitForMessage("2FA");
		if (details != null) {
			accessCode = details.get("code").asText();
			doSendKeysWithWait(codeText, accessCode, 5);
			takeScreenshot("token-page");
			doClickWithWait(submitButton, 5);
			untilPageLoadComplete(driver);
		}	
		
		pageLoadWait(driver);
		int accessCodeErrorCheck =0;
		while (driver.findElements(By.xpath("//a[contains(text(),'To-do list')]")).isEmpty()) {
			try {
				String errorMessage = getText(loginErrorAccessCode);
				log.info("Login Failed : " + errorMessage);
				onError(errorMessage);
				details = waitForMessage("RESEND2FA");
				if (details != null) {
					
					accessCode = details.get("code").asText();
					doSendKeysWithWait(codeText, accessCode, 5);
					takeScreenshot("tokenpage");
					doClickWithWait(submitButton, 5);
//					Thread.sleep(12000);
					untilPageLoadComplete(driver);
				}
			} catch (Exception e) {
				accessCodeErrorCheck++;
				if(accessCodeErrorCheck>=5) {
					onError("Error on Access code page, Please Press Cancel Button");
					throw new DwpWebSocketException("Error on Access code page, Please Press Cancel Button");
//					closeBrowser();
//					endConnection();
//					break;
				}
//				e.printStackTrace();
			}
		}
		
//	Code to check which page is opened
	String pageTitle = driver.getTitle();
	if (pageTitle.contentEquals("Updates - Landlord portal")) {
		doClickWithWait(toDoListLink, 10);
		untilPageLoadComplete(driver);
		
	}else if (pageTitle.contentEquals("To-do list - Landlord portal")) {
		doClickWithWait(toDoListLink, 10);
		untilPageLoadComplete(driver);
		
	}else {
		throw new DWPProcessingException("Some unexpected page is opened");
	}
		
		//	Current date
			LocalDate currentDate = LocalDate.now();
			log.info("Current Date: " + currentDate);
		
		// Create a LocalDate object for April 1st of the current year
			LocalDate aprilFirst = LocalDate.of(currentDate.getYear(), 4, 1);
			log.info("April 1st: " + aprilFirst);

//		Getting the current time before start of the execution
		Instant startTime = Instant.now();
		log.info("The startTime is :" + startTime);
		
		details = waitForMessage("TODOSUCCESS");
		try {
		if (details != null) {

			prru_id = details.get("prru_id").asText();
			dwpConsecErrors = details.get("dwpConsecErrors").asText();
			dwpSubmitData = details.get("dwpSubmitData").asText();
			dwpRequestType = details.get("dwpRequestType").asText();
			dwpNumberPrc = details.get("dwpNumberPrc").asText();
			dwpAraTenDets = details.get("dwpAraTenDets").asText();
			dwpAraHouCosts = details.get("dwpAraHouCosts").asText();
			dwpNumOfMins = details.get("dwpNumOfMins").asText();
			dwpTimeOff = details.get("dwpTimeOff").asText();
			log.info(dwpNumOfMins);
			if (dwpNumOfMins.trim() != "") {
				dwpNumberOfMinutes = Long.parseLong(dwpNumOfMins);
			}			
			if(!dwpNumberPrc.trim().isEmpty()) {
				dwpNumberOfRecordsToBeProcessed = Integer.parseInt(dwpNumberPrc);
			}else {
				dwpNumberOfRecordsToBeProcessed=null;
			}
			dwpConsecutiveErrors = Integer.parseInt(dwpConsecErrors);
			if (dwpConsecutiveErrors==99) {
			dwpConsecutiveErrors = 999;
			}
			log.info("The prru_id is: " + prru_id);
			log.info("No of Consecutive Errors to end run : " + dwpConsecErrors);
			log.info("DWP Submit Data is: " + dwpSubmitData);
			log.info("DWP Request Type is: " + dwpRequestType);
			log.info("Number of records to be processed : " + dwpNumberPrc);
			log.info("Arrears Action Code for Provide Tenancy details : " + dwpAraTenDets);
			log.info("Arrears Action Code for Confirm tenant housing costs : " + dwpAraHouCosts);
			log.info("DWP Number of Minutes is: " + dwpNumberOfMinutes);
			log.info("Cut off time for the DWP Process : " + dwpTimeOff);

			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", getElement(customerName));
			waitForTitleIs("To-do list - Landlord portal", 5);
			takeScreenshot("ToDoList page");
			
			int numOfRecordProcessed = 0;
			
			if(dwpRequestType.equalsIgnoreCase("BOTH")) {
				countOfCustomer = getElementsCount(listOfCustomers_BOTH);
			}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
				countOfCustomer = getElementsCount(listOfCustomers_TENDETS);
			}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
				countOfCustomer = getElementsCount(listOfCustomers_HOUCOST);
			}
			log.info("Count of customers are :" + countOfCustomer);
		
//				Checking the total number of records and number of records to be processed and "Show More" Button
//				if total number of records < then number of records to be processed, then check the presence of "Show More" button and click on it
					String continuousExecution = "N";
					if(dwpNumberOfRecordsToBeProcessed==null) {
						continuousExecution= "Y";
					dwpNumberOfRecordsToBeProcessed=countOfCustomer;
					}
					//To process 998 records
					while((countOfCustomer<dwpNumberOfRecordsToBeProcessed)|| ( continuousExecution=="Y" && countOfCustomer<999)){
					boolean showMoreBtn = driver.findElements(By.id("id-show-more")).size() >0;
					if(showMoreBtn)
					{   
						try
						{
						doClickWithWait(showMore_Button, 20);
						log.info("Clicked Show MOre Button in first loop");
						++x;
						pageLoadWait(driver);
						countOfCustomerOld = countOfCustomer;
						if(dwpRequestType.equalsIgnoreCase("BOTH")) {
							countOfCustomer = getElementsCount(listOfCustomers_BOTH);
						}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
							countOfCustomer = getElementsCount(listOfCustomers_TENDETS);
						}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
							countOfCustomer = getElementsCount(listOfCustomers_HOUCOST);
						}
						if (countOfCustomerOld==countOfCustomer) {
							++sameRec;
							if(sameRec==6) {
								break;
							}
						}
						else
						{
							sameRec=0;
						}										
						}
						catch(Exception e) {
							e.printStackTrace();
							log.info("Error while clicking show more button");
						}
						log.info("Count of customers are :" + countOfCustomer);
					}else {
						log.info("No Show More Button is present in the page");
						break;
					}
				}
				
				if(continuousExecution=="Y") {
					dwpNumberOfRecordsToBeProcessed=countOfCustomer;
				}
			
			try {
					for (int i = 1; i <= countOfCustomer; i++) {
						
						try {		
					 //FLOW :is the number of consecutive errors we have encountered = value of DWP_CONSCERRORS
						if(l_errorCount==dwpConsecutiveErrors) {
							SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
						    Date date = new Date();							
						    log.info("updateDWPEndDate");
						    log.info("l_errorCount" +l_errorCount );
						    log.info("dwpConsecutiveErrors" +dwpConsecutiveErrors );
							odbcPackageCall.updateDWPEndDate(Integer.parseInt(prru_id),"ERROR", date);
//							closeBrowser();
//							endConnection();
							break;
						}
//						Checking the successful completion of execution
						if(numOfRecordProcessed==dwpNumberOfRecordsToBeProcessed) {
//						if(i==dwpNumberOfRecordsToBeProcessed+1) {
//							odbcPackageCall.updateDWPStatus(Integer.parseInt(prru_id), "STOP_INIT");
//							SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
						    Date date1 = new Date();
						    log.info("updateDWPEndStatus");
							odbcPackageCall.updateDWPEndStatus(Integer.parseInt(prru_id),date1);
//							closeBrowser();
//							endConnection();
							break;
						}
						numOfRecordProcessed++;
						
						Date dateTime = new Date();
						SimpleDateFormat dateFormatTime = new SimpleDateFormat("HH:mm");
						dateFormatTime.format(dateTime);
						log.info(dateFormatTime.format(dateTime));
						
						//FLOW : is the current time >= the value of system parameter DWP_TIME_OFF
						if(dwpTimeOff.trim() != "") {
						if (dateFormatTime.parse(dateFormatTime.format(dateTime)).after(dateFormatTime.parse(dwpTimeOff)))
						{   
							//FLOW : Update statement function
							log.info("Time is greater than Office Hours");
							//onError("Time is greater than DWP Time Off 17:00");
						    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
						    Date date = new Date();
						    log.info("updateDWPEndStatus");
							odbcPackageCall.updateDWPEndStatus(Integer.parseInt(prru_id),date);
							log.info("Updated prru status is :" +prru_id);
//							closeBrowser();
//							endConnection();
							break;
						}
						}
						//FLOW : is the excuted minutes >= the value of dwpNumberOfMinutes
//						If the time of execution is equal or exceeds the dwpNumberOfMinutes then end the connection and stop the execution
//						Getting the current time after the execution
						Instant endTime = Instant.now();
						log.info("The endTime is :" + endTime);

//						Calculating the total time of execution
						long minutesOfExecution = Duration.between(startTime, endTime).toMinutes();
						log.info("the execution time :" + minutesOfExecution);
						log.info("the execution time :" + dwpNumberOfMinutes);
			
						if (dwpNumOfMins.trim() != "" && minutesOfExecution >= dwpNumberOfMinutes) {
							log.info("Number of minutes completed");
							log.info("updateDWPStatus");
							odbcPackageCall.updateDWPStatus(Integer.parseInt(prru_id), "STOP_INIT");
							skipUpdate="Y";
//							closeBrowser();
//							endConnection();
							break;
						}
						
						String processStatus = odbcPackageCall.getProcessStatus(Integer.parseInt(prru_id));
						// At this point should check the session is valid in DB
						//FLOW : Is the value of the process run status is ‘STOP_INIT’?
						if (processStatus.equals("STOP_INIT")) {
//							SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
						    Date date = new Date();
						    log.info("updateDWPEndDate");
							odbcPackageCall.updateDWPEndDate(Integer.parseInt(prru_id), "USER_ENDED",date);
							skipUpdate="Y";
//							closeBrowser();
//							endConnection();
							break;
						}
							
							//FLOW : Dose the ToDo List page is displayed.?
							boolean todoListPage = driver.findElements(By.xpath("//span[contains(text(),'To-do list')]/parent::h1")).size() >0;
							if(!todoListPage) {
								log.info("To-do List page is Not Displayed");
								driver.get("https://portal.universal-credit.service.gov.uk/tenancy-details-requests");
								untilPageLoadComplete(driver);
							}
							//FLOW : Does the page says: We'll take you back to where you were.?
							boolean welcomePage = driver.findElements(By.xpath("//h1[@data-tag-page-title='Welcome back']//following-sibling::div//p")).size() >0;
							if(welcomePage) {
								log.info("welcomePage Displayed");
								driver.get("https://portal.universal-credit.service.gov.uk/tenancy-details-requests");
//								Thread.sleep(3000);
								untilPageLoadComplete(driver);
							}
							zeroRec=0;
							for(int v=0;v<x;v++) {
									if(dwpRequestType.equalsIgnoreCase("BOTH")) {
										countOfCustomerSm = getElementsCount(listOfCustomers_BOTH);
									}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
										countOfCustomerSm = getElementsCount(listOfCustomers_TENDETS);
									}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
										countOfCustomerSm = getElementsCount(listOfCustomers_HOUCOST);
									}
									log.info("countOfCustomerSm bf "+countOfCustomerSm);									
									if(countOfCustomerSm==0) {
										++zeroRec;
										if (zeroRec>=5)
										{
											zeroRec=0;
											
											log.info("Fetching TODO List page again");
											takeScreenshotDir("Fetching TODO List"+"_"+i);
											doClickWithWait(toDoListLink, 10);
											untilPageLoadComplete(driver);										
											if(dwpRequestType.equalsIgnoreCase("BOTH")) {
												countOfCustomerSm = getElementsCount(listOfCustomers_BOTH);
											}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
												countOfCustomerSm = getElementsCount(listOfCustomers_TENDETS);
											}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
												countOfCustomerSm = getElementsCount(listOfCustomers_HOUCOST);
											}
											log.info("countOfCustomerSm af "+countOfCustomerSm);
										}
									}
									log.info("countOfCustomerSm"+countOfCustomerSm);
									log.info("Value of i"+i);
									if (countOfCustomerSm < i)
									{
										boolean showMoreBtn = driver.findElements(By.id("id-show-more")).size() >0;
										if(showMoreBtn)	
										{   
											doClickWithWait(showMore_Button, 30);
											log.info("Clicked Show MOre Button in second loop");
										}
									}
								}
							
//							Checking the tenancy type "Provide tenancy details" or "Confirm tenant's housing costs"
							if(dwpSubmitData.equalsIgnoreCase("N")) {
								if(dwpRequestType.equalsIgnoreCase("BOTH")) {
									try {
									typeOfTenant = driver.findElement(By.xpath("(//li[@class='task-list__item']/a)[" + i + "]/div[@class='task-list__title']")).getText();
									}
									catch(Exception e) {
										log.info("Error while typeOfTenant BOTH");
										log.info("updateDWPExtError"); 
										odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
										e.printStackTrace();
										takeScreenshotDir("typeOfTenant Error BOTH"+"_"+i);
										throw new DWPProcessingException("Error while typeOfTenant dwpSubmitData N");
									}									
								}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
									try {
									typeOfTenant = driver.findElement(By.xpath("(//a[contains(@href,'provide-tenancy-details')]//div[@class='task-list__title'])[" + i + "]")).getText();
									}
									catch(Exception e) {
										log.info("Error while typeOfTenant TENDETS");
										log.info("updateDWPExtError"); 
										odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
										e.printStackTrace();
										takeScreenshotDir("typeOfTenant Error TENDETS"+"_"+i);
//										typeOfTenant = driver.findElement(By.xpath("(//a[contains(@href,'provide-tenancy-details')]//div[@class='task-list__title'])[" + i + "]")).getText();
										throw new DWPProcessingException("Error while typeOfTenant dwpSubmitData N");
									}
								}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
									try {
									typeOfTenant = driver.findElement(By.xpath("(//a[contains(@href,'confirm-tenant-housing-costs')]//div[@class='task-list__title'])[" + i + "]")).getText();
									}
									catch(Exception e) {
										log.info("Error while typeOfTenant HOUCOST");
										log.info("updateDWPExtError"); 
										odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
										e.printStackTrace();
										takeScreenshotDir("typeOfTenant Error HOUCOST"+"_"+i);
										throw new DWPProcessingException("Error while typeOfTenant dwpSubmitData N");
									}
								}
								log.info("The type of tenant is:" +typeOfTenant);
							}
							else if(dwpSubmitData.equalsIgnoreCase("Y")) {
								y = i-a;
								try {
								if(dwpRequestType.equalsIgnoreCase("BOTH")) {
									typeOfTenant = driver.findElement(By.xpath("(//li[@class='task-list__item']/a)[" + y + "]/div[@class='task-list__title']")).getText();
								}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
									typeOfTenant = driver.findElement(By.xpath("(//a[contains(@href,'provide-tenancy-details')]//div[@class='task-list__title'])[" + y + "]")).getText();
								}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
									typeOfTenant = driver.findElement(By.xpath("(//a[contains(@href,'confirm-tenant-housing-costs')]//div[@class='task-list__title'])[" + y + "]")).getText();
								}
								log.info("The type of tenant is:" +typeOfTenant);
								}
								catch(Exception e) {
									log.info("updateDWPExtError"); 
									odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
									log.info("Error while typeOfTenant dwpSubmitData Y");
									e.printStackTrace();
									takeScreenshotDir("typeOfTenant Error "+"_"+i);
									throw new DWPProcessingException("Error while typeOfTenant dwpSubmitData Y");
								}
							}
							
//							When dwpSubmitData = Y, then the first record will be removed from the to-do-list and the second record will be in first
//							so each time we have to process the first record in the list.
							WebElement eleCustomer = null;
							if (dwpSubmitData.equalsIgnoreCase("Y")) {
								y = i-a;
								try {
								if(dwpRequestType.equalsIgnoreCase("BOTH")) {
									eleCustomer = driver.findElement(By.xpath("(//div[@class='task-list__details'])[" + y + "]//span[1]"));
								}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
									eleCustomer = driver.findElement(By.xpath("(//a[contains(@href,'provide-tenancy-details')]//div[@class='task-list__details'])[" + y + "]//span[1]"));
								}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
									eleCustomer = driver.findElement(By.xpath("(//a[contains(@href,'confirm-tenant-housing-costs')]//div[@class='task-list__details'])[" + y + "]//span[1]"));
								}
								//DOM issue
								String expectedCustomerName = eleCustomer.getText().trim();
								log.info("Expected customer name is :" + expectedCustomerName);
								takeScreenshot("ExpectedCustomerName Y"+"_"+i);
								}
								catch(Exception e) {
									log.info("Error while eleCustomer dwpSubmitData Y");
									log.info("updateDWPExtError"); 
									odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
									e.printStackTrace();
									takeScreenshotDir("eleCustomer Error dwpSubmitData Y"+"_"+i);
									throw new DWPProcessingException("Error while eleCustomer dwpSubmitData Y");
								}
								
						}
						
//							When dwpSubmitData = N, then the first record will be there in the list, and we have to process with the next consecutive record
//							in the list
							else if (dwpSubmitData.equalsIgnoreCase("N")) {
//								eleCustomer = driver.findElement(By.xpath("(//div[text()='Provide tenancy details'])[" + i + "]/parent::a//div//span[1]"));
//								eleCustomer = driver.findElement(By.xpath("(//div[@class='task-list__details'])[" + i + "]//span[1]"));
								try {
								if(dwpRequestType.equalsIgnoreCase("BOTH")) {
									eleCustomer = driver.findElement(By.xpath("(//div[@class='task-list__details'])[" + i + "]//span[1]"));
								}else if(dwpRequestType.equalsIgnoreCase("TENDETS")) {
									eleCustomer = driver.findElement(By.xpath("(//a[contains(@href,'provide-tenancy-details')]//div[@class='task-list__details'])[" + i + "]//span[1]"));
								}else if(dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
									eleCustomer = driver.findElement(By.xpath("(//a[contains(@href,'confirm-tenant-housing-costs')]//div[@class='task-list__details'])[" + i + "]//span[1]"));
								}
								//DOM issue
								String expectedCustomerName = eleCustomer.getText().trim();
								log.info("Expected customer name is :" + expectedCustomerName);
								takeScreenshot("ExpectedCustomerName N"+"_"+i);
								}
								catch(Exception e) {
									log.info("Error while eleCustomer dwpSubmitData N");
									log.info("updateDWPExtError"); 
									odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
									e.printStackTrace();
									takeScreenshotDir("eleCustomer Error dwpSubmitData N"+"_"+i);
									throw new DWPProcessingException("Error while eleCustomer dwpSubmitData N");
								}
								
							}
							
//							FLOW : DWP_REQUESTTYPE = 'BOTH' OR DWP_REQUESTTYPE = 'TENDETS' OR DWP_REQUESTTYPE = 'HOUCOSTS'
							/*
							 * is the vlaue of DWP_REQUESTTYPE = 'BOTH' and request type is 'Provide tenancy
							 * details' or 'Confirm tenant housing costs' OR the vlaue of DWP_REQUESTTYPE =
							 * 'TENDENTS' and request type is 'Provide tenancy details' OR the vlaue of
							 * DWP_REQUESTTYPE = 'HOUCOSTS' and request type is 'Confirm tenant housing
							 * costs'
							 */ 
							if(dwpRequestType.equalsIgnoreCase("BOTH") || dwpRequestType.equalsIgnoreCase("TENDETS") || dwpRequestType.equalsIgnoreCase("HOUCOSTS")) {
								try {
									//FLOW : Dose the ToDo List page is displayed.?
									boolean todoListPageCheck = driver.findElements(By.xpath("//span[contains(text(),'To-do list')]/parent::h1")).size() >0;
									if(todoListPageCheck) {
//										((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",eleCustomer);
										if(i==1 && dwpRequestType.equalsIgnoreCase("TENDETS")){
											String linkFirstCustomer = driver.findElement(By.xpath("(//a[contains(@href,'provide-tenancy-details')])[1]")).getAttribute("href");
											log.info("The href value of firstCustomer is :"+linkFirstCustomer);
											driver.get(linkFirstCustomer);
										}
										else if(i==1 && dwpRequestType.equalsIgnoreCase("HOUCOSTS")){
											String linkFirstCustomer = driver.findElement(By.xpath("(//a[contains(@href,'confirm-tenant-housing-costs')])[1]")).getAttribute("href");
											log.info("The href value of firstCustomer is :"+linkFirstCustomer);
											driver.get(linkFirstCustomer);
											}
										else if(i==1 && dwpRequestType.equalsIgnoreCase("BOTH")){
											String linkFirstCustomer = driver.findElement(By.xpath("(//a[@class='task-list__link'])[1]")).getAttribute("href");
											log.info("The href value of firstCustomer is :"+linkFirstCustomer);
											driver.get(linkFirstCustomer);
										}
										else {
											try {
												WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
												wait.until(ExpectedConditions.elementToBeClickable(eleCustomer));
												takeScreenshot("ExpectedCustomerName");
												log.info("Before eleCustomer click"+eleCustomer);
												eleCustomer.click();
												}
												catch(Exception e) {
													try {
														log.info("Error while eleCustomer eleCustomer.click - trying again to click");
														((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",eleCustomer);
														eleCustomer.click();
													}catch(Exception e1) {
														log.info("Error while eleCustomer eleCustomer.click");
														log.info("updateDWPExtError"); 
														odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
														e1.printStackTrace();
														takeScreenshotDir("Error eleCustomer.click");
														throw new DWPProcessingException("eleCustomer.click is not working as expected");	
													}
													}
											log.info("After eleCustomer click"+eleCustomer);
										}
										untilPageLoadComplete(driver);
									}	
								}catch(NoSuchElementException | ElementClickInterceptedException e){
									takeScreenshotDir("Error NoSuchElementException"+"_"+i);
//									l_errorCount++;
//									log.info("updateDWPTechError");
//									odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
                                    takeScreenshot("Exception Click eleCustomer"+"_"+i);
									log.info("Exception Click eleCustomer"+eleCustomer);
									log.info("updateDWPExtError"); 
									odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
									log.info("Element interaction error: " + e.getMessage());
									e.printStackTrace();
									throw new DWPProcessingException("The "+eleCustomer+" is not working as expected");
								}
								catch (TimeoutException e) {
									log.info("Timeout error: " + e.getMessage());
								}
												
							welcomePage = driver.findElements(By.xpath("//h1[@data-tag-page-title='Welcome back']//following-sibling::div//p")).size() >0;
							if(welcomePage) {
//								Unmatch
								log.info("updateDWPRecUnMatch");
								odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
								throw new DWPProcessingException("Error on Welcome back");						
							}
							
//     						waitForTitleIs("Confirm tenancy details - To-do list - Landlord portal", 5);
      						
							String tenanyPage1URL = driver.getCurrentUrl();
							log.info("The tenancy page1 url is :" + tenanyPage1URL);
							
//							Flow: Checking whether the record is of Provide tenancy details or Confirm tenant's housing cost
//							If the record is of Provide tenancy details then..
							
							if(typeOfTenant.contains("Provide tenancy details") && (dwpRequestType.equalsIgnoreCase("BOTH") || dwpRequestType.equalsIgnoreCase("TENDETS"))) {
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									getElement(tenantNameAndDOB_TENDETS));

							String doubleTenantName = getText(tenantNameAndDOB_TENDETS);
							log.info("The double tenant name is: "+doubleTenantName);
								
//							Checking the number of tenant for provide tenancy
							int countOfPara = getElementsCount(noOfParaInTenancyPage);
							
							nameAndDOB = getText(tenantNameAndDOB_TENDETS);
						    String[] tenantName_TENDETS = nameAndDOB.split("\\r?\\n|\\r");
						    log.info("The size of :"+tenantName_TENDETS.length);
							
							if(countOfPara==3 && tenantName_TENDETS.length==1){ 
							try {
//							Getting the name of the Customer
							nameAndDOB = getText(tenantNameAndDOB_TENDETS);
							log.info("The name and Dob is :" + nameAndDOB);
							
							String[] splitNameandDob = nameAndDOB.split("\\(");
							tenantName = splitNameandDob[0];
							log.info("Name is :" +tenantName);
//							Getting the required name of the tenant
							tenantName = getNameOfTenant(tenantName);
							
							String dateFromApplication = splitNameandDob[1].replace(")", "");
							log.info("Date From Application is :" +dateFromApplication);
							
							String[] dateFormatting = dateFromApplication.split("\\s+");
							DateOfBirth = dateFormatting[0] + "-" + dateFormatting[1] + "-" + dateFormatting[2];
							log.info("DOB is :" +DateOfBirth);
							
							dateFormatAsPerProcedureCall(DateOfBirth);
							dob = requiredDateFormat;
							log.info("Date of Birth :: " + dob);
								
							String address = getText(tenancyAddress);
							postCode = address.trim();
							log.info("The postCode is :" + postCode);

							String detailsDate = getText(tenancyDate);
							log.info("The detailsDate is :" + detailsDate);
							String[] partialDate = detailsDate.split("\\s+");
							String actualStartDate = partialDate[9] + "-" + partialDate[10] + "-" + partialDate[11];
							log.info("The actual start is:" + actualStartDate);

							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
									Date date = null;
									try {
										date = dateFormat.parse(actualStartDate);
									} catch (ParseException e) {
										e.printStackTrace();
									}
									log.info("Date from Applicatoin :- " + date);
//									Date aprilFirstDate = Date.from(aprilFirst.atStartOfDay(ZoneId.systemDefault()).toInstant());
//									if(date.after(aprilFirstDate) || date.equals(aprilFirstDate))
									//NHE-27841 UCRV financial year change correction 
									LocalDate dateToCompare = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
								        
								   // Convert java.util.Date to java.time.LocalDate									
									LocalDate dateCurrent = LocalDate.now();
							        int financialYearStart = dateCurrent.getMonthValue() >= 4 ? dateCurrent.getYear() : dateCurrent.getYear() - 1;
							        //Hardcoded to 7th April can be change later
								    LocalDate startOfFinancialYear = LocalDate.of(financialYearStart, 4, 7);
								    LocalDate endOfFinancialYear = LocalDate.of(financialYearStart + 1, 3, 31);
								  
								    if(!dateToCompare.isBefore(startOfFinancialYear) && !dateToCompare.isAfter(endOfFinancialYear)) 
									{
									dateFormatAsPerProcedureCall(actualStartDate);
									actualDate = requiredDateFormat;

									log.info("Rental Details Date of Customer :" + actualDate);
									takeScreenshot("Tenancy1 Details page"+"_"+i);
									log.info("---------------------**************--------------------");
									log.info("The name of customer is :" + tenantName);
									log.info("Date of Birth of customer is :: " + dob);
									log.info("The postal code is:" + postCode);
									log.info("The current date is:" + actualDate);
									log.info("The prru_id is:" + prru_id);
									log.info("---------------------**************--------------------");
									}
									else {
										log.info("updateDWPRecUnMatch");
										odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
										throw new DWPProcessingException("The claim date is in a previous financial year");
									}
								}
							catch(Exception e) {
//								Error while parsing the data
								throw new DWPProcessingException("Error on Parsing data of Single Tenant");
								}
							}
//							Two tenants
							else if(countOfPara==3 && tenantName_TENDETS.length==2) {
									try {
//										Getting the name of the Customer
										nameAndDOB = tenantName_TENDETS[0];
										log.info("The name and Dob is :" + nameAndDOB);
										
										String[] splitNameandDob = nameAndDOB.split("\\(");
										tenantName = splitNameandDob[0];
										log.info("Name is :" +tenantName);
//										Getting the required name of the tenant
										tenantName = getNameOfTenant(tenantName);
										
										String dateFromApplication = splitNameandDob[1].replace(")", "");
										log.info("Date From Application is :" +dateFromApplication);
										
										String[] dateFormatting = dateFromApplication.split("\\s+");
										DateOfBirth = dateFormatting[0] + "-" + dateFormatting[1] + "-" + dateFormatting[2];
										log.info("DOB is :" +DateOfBirth);
										
										dateFormatAsPerProcedureCall(DateOfBirth);
										dob = requiredDateFormat;
										log.info("Date of Birth :: " + dob);
										
										nameAndDOBSecondTenant = tenantName_TENDETS[1];
										log.info("The name and Dob is :" + nameAndDOBSecondTenant);
										
										String[] splitNameandDobSecondTenant = nameAndDOBSecondTenant.split("\\(");
										secondTenantName = splitNameandDobSecondTenant[0];
										log.info("Name is :" +secondTenantName);
//										Getting the required name of the tenant
										secondTenantName = getNameOfTenant(secondTenantName);
										
										String dateFromApplicationSecondTenant = splitNameandDobSecondTenant[1].replace(")", "");
										log.info("Date From Application is :" +dateFromApplicationSecondTenant);
										
										String[] dateFormattingSecondTenant = dateFromApplicationSecondTenant.split("\\s+");
										DateOfBirthSecondTenant = dateFormattingSecondTenant[0] + "-" + dateFormattingSecondTenant[1] + "-" + dateFormattingSecondTenant[2];
										log.info("DOB is :" +DateOfBirthSecondTenant);
										
										dateFormatAsPerProcedureCall(DateOfBirthSecondTenant);
										dobSecondTenat = requiredDateFormat;
										log.info("Date of Birth :: " + dobSecondTenat);
										
										String address = getText(tenancyAddress);
										postCode = address.trim();
										log.info("The postCode is :" + postCode);

										String detailsDate = getText(tenancyDate);
										log.info("The detailsDate is :" + detailsDate);
										String[] partialDate = detailsDate.split("\\s+");
										String actualStartDate = partialDate[9] + "-" + partialDate[10] + "-" + partialDate[11];
										log.info("The actual start is:" + actualStartDate);
										
										SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
											Date date = null;
											try {
												date = dateFormat.parse(actualStartDate);
											} catch (ParseException e) {
												e.printStackTrace();
											}
											log.info("Date from Applicatoin :- " + date);
//											Date aprilFirstDate = Date.from(aprilFirst.atStartOfDay(ZoneId.systemDefault()).toInstant());
//											if(date.after(aprilFirstDate) || date.equals(aprilFirstDate)) {
											//NHE-27841 UCRV financial year change correction 
											LocalDate dateToCompare = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
										        
										   // Convert java.util.Date to java.time.LocalDate	
											LocalDate dateCurrent = LocalDate.now();
									        int financialYearStart = dateCurrent.getMonthValue() >= 4 ? dateCurrent.getYear() : dateCurrent.getYear() - 1;
									      //Hardcoded to 7th April can be change later
										    LocalDate startOfFinancialYear = LocalDate.of(financialYearStart, 4, 7);
										    LocalDate endOfFinancialYear = LocalDate.of(financialYearStart + 1, 3, 31);
										  
										    if(!dateToCompare.isBefore(startOfFinancialYear) && !dateToCompare.isAfter(endOfFinancialYear)) 
										    {	
												dateFormatAsPerProcedureCall(actualStartDate);
												actualDate = requiredDateFormat;
												log.info("Rental Details Date of Customer :" + actualDate);
												takeScreenshot("Tenancy1 Details page"+"_"+i);

											log.info("---------------------**************--------------------");
											log.info("The name of customer is :" + tenantName);
											log.info("Date of Birth of customer is :: " + dob);
											log.info("The postal code is:" + postCode);
											log.info("The current date is:" + actualDate);
											log.info("The prru_id is:" + prru_id);
											
											log.info("The name of customer is :" + secondTenantName);
											log.info("Date of Birth of customer is :: " + dobSecondTenat);
											log.info("The postal code is:" + postCode);
											log.info("The current date is:" + actualDate);
											log.info("The prru_id is:" + prru_id);
											log.info("---------------------**************--------------------");

											}else {
												log.info("updateDWPRecUnMatch");
												odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
												throw new DWPProcessingException("The claim date is in a previous financial year");
											}
										}
									catch(Exception e) {
//										Error while parsing the data
										throw new DWPProcessingException("Error on Parsing data of Two Tenant");
									}
								} else {
//									UnMatch Data
									log.info("updateDWPRecUnMatch");
									odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
									throw new DWPProcessingException("More than two tenanat is there, Hence we will skip the reocord");
								}
							
//     ODBC Package call
//								@SuppressWarnings("unchecked")
								String racAccNo= null;
								String tenancyLiableForRentAtThisAddress = null ;
								String acceptingOtherChargesUseAndOccupation = null;								
								String tenantRefNo = null;								
								String propertyTemporaryAccommodation = null;								
								String isAnyoneTenAgreeApartFromClaimant = null;								
								String numberOfTenantIncludingClaimantValue = null;								
								String numberofBedrooms = null;								
								String frequencyOfRentAndCharge = null;								
								String frequencyOfServiceCharge = null;								
								String areThereAnyRentFreeWeek = null;								
								String noOfWeeksValue= null;								
								String rentOfPropertyExcludingServiceCharge= null;								
								String areThereEligibleServiceCharge= null;							
								String eligibleServiceChargeValue= null;								
								String dateRentChargesChange= null;	
								String dateRentChargesChangeNew= null;	
								String dateServiceChargesChange= null;
								String dateServiceChargesChangeNew= null;		;
								
								try {
									@SuppressWarnings("unchecked")
									List<String> tenancyDetails = odbcPackageCall
											.getTenancyDetails(tenantName, dob, postCode, actualDate, prru_id);
									log.info("Prru_status : " + tenancyDetails.get(0));
									log.info("Error_msg : " + tenancyDetails.get(1));									
									racAccNo = (String) tenancyDetails.get(2);
									log.info("Rac_accno : " + racAccNo);
									tenancyLiableForRentAtThisAddress = (String) tenancyDetails.get(3);
									log.info("****Is this person your tenant and liable for rent at this address : " + tenancyLiableForRentAtThisAddress);
									acceptingOtherChargesUseAndOccupation = (String) tenancyDetails.get(4);
									log.info("****Are you accepting other charges (use and occupation)? : " + acceptingOtherChargesUseAndOccupation);
									tenantRefNo = (String) tenancyDetails.get(5);
									log.info("****Tenant Reference number : " + tenantRefNo);
									propertyTemporaryAccommodation = (String) tenancyDetails.get(6);
									log.info("****Is this property temporary accommodation : " + propertyTemporaryAccommodation);
									isAnyoneTenAgreeApartFromClaimant = (String) tenancyDetails.get(7);
									log.info("****Is there anyone on the tenancy agreement apart from the claimant? : " + isAnyoneTenAgreeApartFromClaimant);
									numberOfTenantIncludingClaimantValue = (String) tenancyDetails.get(8);
									log.info("****How many tenants are there on the tenancy agreement including the claimant? : " + numberOfTenantIncludingClaimantValue);
									numberofBedrooms = (String) tenancyDetails.get(9);
									log.info("****How many bedrooms are there? : " + numberofBedrooms);
									frequencyOfRentAndCharge = (String) tenancyDetails.get(10);
									log.info("****Select how often you charge rent? frequency ? : " + frequencyOfRentAndCharge);
									frequencyOfServiceCharge = (String) tenancyDetails.get(11);
									log.info("****Select how often you charge service charges? frequency ? : " + frequencyOfServiceCharge);
									areThereAnyRentFreeWeek = (String) tenancyDetails.get(12);
									log.info("****Are there any rent-free weeks in the year? : " + areThereAnyRentFreeWeek);
									noOfWeeksValue = (String) tenancyDetails.get(13);
									log.info("****How many weeks are there? : " + noOfWeeksValue);
									rentOfPropertyExcludingServiceCharge = (String) tenancyDetails.get(14);
									log.info("****What is the rent for the property (excluding service charges)? : " + rentOfPropertyExcludingServiceCharge);
									areThereEligibleServiceCharge = (String) tenancyDetails.get(15);
									log.info("****Are there eligible service charges? : " + areThereEligibleServiceCharge);
									eligibleServiceChargeValue = (String) tenancyDetails.get(16);
									log.info("****How much are the eligible service charges for the property? : " + eligibleServiceChargeValue);
									SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
									SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									dateRentChargesChange = (String) tenancyDetails.get(17);
									if(dateRentChargesChange != null && !dateRentChargesChange.trim().isEmpty()) {
										dateRentChargesChangeDt = inputFormat.parse(dateRentChargesChange);
										dateRentChargesChangeNew = outputFormat.format(dateRentChargesChangeDt);									
										log.info("****Date the rent changed : " + dateRentChargesChangeNew);	
									}
									dateServiceChargesChange = (String) tenancyDetails.get(18);
									if(dateServiceChargesChange != null && !dateServiceChargesChange.trim().isEmpty()) {
										dateServiceChargesChangedt = inputFormat.parse(dateServiceChargesChange);
										dateServiceChargesChangeNew = outputFormat.format(dateServiceChargesChangedt);
										log.info("****Date the service charge changed new: " + dateServiceChargesChangeNew);
									}
									if ((tenancyLiableForRentAtThisAddress.equals("Y")) || (tenancyLiableForRentAtThisAddress.equals("N") && acceptingOtherChargesUseAndOccupation.equals("Y"))) {
											if((racAccNo.trim().isEmpty())||tenantRefNo.trim().isEmpty()
													||frequencyOfRentAndCharge.trim().isEmpty()
													||frequencyOfServiceCharge.trim().isEmpty()
													||rentOfPropertyExcludingServiceCharge.trim().isEmpty()
													||areThereEligibleServiceCharge.trim().isEmpty()){
												log.info("Update record unmatch since all answers are not populated");
												throw new DWPProcessingException("Unmatch due to config issue");									
											}
										}
									} 
								catch(IndexOutOfBoundsException e) {
								if(countOfPara==3 && tenantName_TENDETS.length==2) {
										log.info("Two Tenant Case");
									}
									else {
//										UnMatch Data
										log.info("updateDWPRecUnMatch");
										odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
										throw new DWPProcessingException("No Matching Tenant Information for Party");
									}
								}
								catch (Exception e) {
						            // Handle other exceptions
									//FLOW :Was there an error message from the procedure?
									log.info("updateDWPExtError");
									odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
									log.info("Exception: " + e.getMessage());
									throw new DWPProcessingException("Error on Procedure Call");
								}
								try {
									if((countOfPara==3 && tenantName_TENDETS.length==2) && (tenancyLiableForRentAtThisAddress.contentEquals("Y") || acceptingOtherChargesUseAndOccupation.contentEquals("Y"))){
										String racAccNoSecondTenant= null;
										String tenancyLiableForRentAtThisAddressSecondTenant = null ;
										String acceptingOtherChargesUseAndOccupationSecondTenant = null;								
										String tenantRefNoSecondTenant = null;	
										String propertyTemporaryAccommodationSecondTenant = null;	
										String isAnyoneTenAgreeApartFromClaimantSecondTenant = null;	
										String numberOfTenantIncludingClaimantValueSecondTenant = null;	
										
										@SuppressWarnings("unchecked")
										List<String> tenancyDetailsSecondTenant = odbcPackageCall
												.getTenancyDetails(secondTenantName, dobSecondTenat, postCode, actualDate, prru_id);
										log.info("Prru_status Second Tenant : " + tenancyDetailsSecondTenant.get(0));
										log.info("Error_msg Second Tenant : " + tenancyDetailsSecondTenant.get(1));									
										racAccNoSecondTenant = (String) tenancyDetailsSecondTenant.get(2);
										log.info("Rac_accno : " + racAccNoSecondTenant);
										tenancyLiableForRentAtThisAddressSecondTenant = (String) tenancyDetailsSecondTenant.get(3);
										log.info("****Is this person your tenant and liable for rent at this address : " + tenancyLiableForRentAtThisAddressSecondTenant);
										acceptingOtherChargesUseAndOccupationSecondTenant = (String) tenancyDetailsSecondTenant.get(4);
										log.info("****Are you accepting other charges (use and occupation)? : " + acceptingOtherChargesUseAndOccupationSecondTenant);
										tenantRefNoSecondTenant = (String) tenancyDetailsSecondTenant.get(5);
										log.info("****Tenant Reference number : " + tenantRefNoSecondTenant);
										propertyTemporaryAccommodationSecondTenant = (String) tenancyDetailsSecondTenant.get(6);
										log.info("****Is this property temporary accommodation : " + propertyTemporaryAccommodationSecondTenant);
										isAnyoneTenAgreeApartFromClaimantSecondTenant = (String) tenancyDetailsSecondTenant.get(7);
										log.info("****Is there anyone on the tenancy agreement apart from the claimant? : " + isAnyoneTenAgreeApartFromClaimantSecondTenant);
										numberOfTenantIncludingClaimantValueSecondTenant = (String) tenancyDetailsSecondTenant.get(8);
										log.info("****How many tenants are there on the tenancy agreement including the claimant? : " + numberOfTenantIncludingClaimantValueSecondTenant);
										if (tenantRefNo.contentEquals(tenantRefNoSecondTenant)) {
										//valid tenancy details
										try {
												if(Integer.parseInt(numberOfTenantIncludingClaimantValue) > 2){
													isAnyoneTenAgreeApartFromClaimant="Y";
												}else {
													isAnyoneTenAgreeApartFromClaimant="N";
												}	
											}catch(Exception e) {
													isAnyoneTenAgreeApartFromClaimant="N";
											}
											log.info("Proceed with the flow with output of first tenant");
										}
										else {
											//Unmatch data
//											log.info("updateDWPRecUnMatch");
//											odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
											throw new DWPProcessingException("No Matching Tenancy for Two Tenant");
										}
									}
									else if((countOfPara==3 && tenantName_TENDETS.length==2) && tenancyLiableForRentAtThisAddress.contentEquals("N") && acceptingOtherChargesUseAndOccupation.contentEquals("N")) {
										//Unmatch data
										throw new DWPProcessingException("No Matching Tenancy");
									}
								}
								catch(IndexOutOfBoundsException e) {
//										UnMatch Data
										log.info("updateDWPRecUnMatch");
										odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
										throw new DWPProcessingException("No Matching Tenant Information for Party for Two Tenant-I");
								}
								catch(Exception e) {
//										UnMatch Data
										log.info("updateDWPRecUnMatch");
										odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
										throw new DWPProcessingException("No Matching Tenant Information for Party for Two Tenant-E");
									}								

//									FLOW : Was the person matched - Is this a tenant - 'Y'
									if (tenancyLiableForRentAtThisAddress.equals("Y")) {
										log.info("Tenancy Match");
										((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
												getElement(tenantLiableToRent_Yes));
											takeScreenshot("TenantLiableToRent_Yes Field"+"_"+i);
											jsClick(tenantLiableToRent_Yes);
											waitForVisibilityOfElement(tenantLiableTenantRef, 10);
											doSendKeysWithWait(tenantLiableTenantRef, tenantRefNo, 10);
											pageLoadWait(driver);
											takeScreenshot("Tenant1 LiableforRentInput"+"_"+i);
									} 
//									FLOW : Was the person matched as having tenancy of use and occupation - Are you accepting other charges (use and occupation) -'Y'
									else if (tenancyLiableForRentAtThisAddress.equals("N")
											&& acceptingOtherChargesUseAndOccupation.equals("Y") ) {
										try {
										log.info("Tenancy Occupation Match");
										((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
												getElement(tenantLiableToRent_No));
										takeScreenshot("tenantLiableToRent_No Field"+"_"+i);
										jsClick(tenantLiableToRent_No);
										waitForVisibilityOfElement(acceptingOtherCharges_Yes, 5);
										takeScreenshot("Tenant1 LiableforRentInput"+"_"+i);
										jsClick(acceptingOtherCharges_Yes);
										doSendKeysWithWait(accpOtherChargesTenantRef, tenantRefNo, 5);
										pageLoadWait(driver);
										takeScreenshot("Tenant1 LiableforRentInputOther"+"_"+i);
										}
										catch (Exception e) {
											log.info("Tenancy Occupation Match failed"); 
											odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
											e.printStackTrace();
											throw new DWPProcessingException("DWPProcessingException : Tenancy Occupation Match");
										}	
									}
									else {
										//FLOW: update process_runs record set PRRU_NB_REC_UNMATCHED 
										//Record unmatched 
										log.info("updateDWPRecUnMatch");
										odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
										throw new DWPProcessingException("Error UnMatch in Tenacy Page 1");
									}
								
								takeScreenshot("Tenant1 Page after input"+"_"+i);
								doClickWithWait(submitButton, 5);
								pageLoadWait(driver);

								waitForTitleIs("Tenancy details - To-do list - Landlord portal", 5);
								takeScreenshot("tenant2 Page"+"_"+i);
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
										getElement(tempAccommodation_Yes));
								takeScreenshot("tenant2 Page before input"+"_"+i);
								log.info("We are in tenancy page 2");
								
//								FLOW : Is this a temporary accommodation
								if (propertyTemporaryAccommodation.equalsIgnoreCase("Y")) {
									jsClick(tempAccommodation_Yes);
								} else {
									jsClick(tempAccommodation_No);
								}
								
//								FLOW : Is this a joint tenancy
								if (isAnyoneTenAgreeApartFromClaimant.equalsIgnoreCase("Y")) {
									jsClick(isAnyoneApartFromClaimant_Yes);
									doSendKeysWithWait(noOfTenantsIncludingClaimantText,
											numberOfTenantIncludingClaimantValue, 5);
								} else {
									jsClick(isAnyoneApartFromClaimant_No);
								}
								waitForVisibilityOfElement(selectBedroom, 5);
								doSelectDropDownByValue(selectBedroom, numberofBedrooms);
								pageLoadWait(driver);

//								FLOW : Are there any rent free weeks
								if (areThereAnyRentFreeWeek.equalsIgnoreCase("Y")) {
									jsClick(rentFreeWeeks_Yes);
									doSendKeysWithWait(noOfWeeksTextInput, noOfWeeksValue, 5);
								} else {
									jsClick(rentFreeWeeks_No);
								}
								pageLoadWait(driver);
								takeScreenshot("tenant2 Page after input"+"_"+i);
								doClickWithWait(submitButton, 5);
								pageLoadWait(driver);
								
								waitForTitleIs("Tenancy costs - To-do list - Landlord portal", 5);
								takeScreenshot("tenant3 Page"+"_"+i);
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
										getElement(rentForThePropertyText));
								takeScreenshot("tenant3 Page before input"+"_"+i);
								log.info("We are in tenancy page 3");

								//doSendKeysWithWait(rentForThePropertyText, rentOfPropertyExcludingServiceCharge, 5);
								double rentOfPropertyExcludingServiceChargeNum = Double.parseDouble(rentOfPropertyExcludingServiceCharge);
								String rentOfPropertyExcludingServiceChargeFormat = String.format("%.2f", rentOfPropertyExcludingServiceChargeNum);
								doSendKeysWithWait(rentForThePropertyText, rentOfPropertyExcludingServiceChargeFormat, 5);
								frequencyOfRentAndCharge.trim();
								doSelectDropDownByVisibleText(selectFrequencyForRent, frequencyOfRentAndCharge);
								pageLoadWait(driver);

//								FLOW : Are there eligible service charges
								if (areThereEligibleServiceCharge.equalsIgnoreCase("Y")) {
									double eligibleServiceChargeValueNum = Double.parseDouble(eligibleServiceChargeValue);
									String eligibleServiceChargeValueFormat = String.format("%.2f", eligibleServiceChargeValueNum);
									jsClick(isEligibleServiceCharge_Yes);
									doSendKeysWithWait(eligibleServiceChargeText, eligibleServiceChargeValueFormat, 5);
									frequencyOfServiceCharge.trim();
									doSelectDropDownByVisibleText(selectFrequencyForServiceCharge,
											frequencyOfServiceCharge);
								} else {
									jsClick(isEligibleServiceCharge_No);
								}
								pageLoadWait(driver);
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
										getElement(rentForThePropertyText));
								takeScreenshot("tenant3 Page after input"+"_"+i);
								doClickWithWait(submitButton, 5);
								pageLoadWait(driver);

//								waitForTitleIs("Check and confirm details - To-do list - Landlord portal", 5);
								takeScreenshot("tenant4 Page"+"_"+i);
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
										getElement(tenancy4_heading));
								takeScreenshot("tenant4 Page before input"+"_"+i);
								log.info("Successfully entered all the details till tenancy page 3");
								log.info("We are in tenancy page 4");

//								FLOW : Is DWP_SUBMITDATA-'Y'?
								if (dwpSubmitData.equalsIgnoreCase("Y")) {
//									incrementing success records to pick next records in the list
									log.info("We are inside submitData - Y");
									++a;
//									jsClick(areTheseDetailsCorrect_Yes);
									takeScreenshot("tenant4 Page after input"+"_"+i);
									doClickWithWait(submitButton, 5);
									pageLoadWait(driver);
									acaBalance ="0";
									String p_rac_epo_code = null;
									String p_aca_effective_date = null;
									String p_aca_expiry_date = null;
									String p_aca_next_action_date = null;
									String p_rac_review_date = null;
									String p_nop_highlight_ind = null;
									String p_nop_ntt_code = null;
									String p_eac_exists = null;
									String p_anl_outcome_rsn = null;
									String p_ahra_refno = null;
									boolean p_commit = true;
									String p_aca_code = dwpAraTenDets;
									
//									FLOW : Create Arrears Action
									try {
										
										if(numberOfTenantIncludingClaimantValue == null || numberOfTenantIncludingClaimantValue.trim().isEmpty()) {
												numberOfTenantIncludingClaimantValue="1";
											}
										
									@SuppressWarnings("unchecked")
									List<ArrearsResponse> arrearsResponse = odbcPackageCall.createArrearsAction(racAccNo,
											acaBalance, 
											prru_id, 
											propertyTemporaryAccommodation, 
											dateRentChargesChangeNew, 
											rentOfPropertyExcludingServiceCharge, 
											dateServiceChargesChangeNew, 
											eligibleServiceChargeValue, 
											numberOfTenantIncludingClaimantValue, 
											frequencyOfRentAndCharge, 
											frequencyOfServiceCharge, 
											noOfWeeksValue, 
											numberofBedrooms,
											p_rac_epo_code,
											p_aca_effective_date,
											p_aca_expiry_date,
											p_aca_next_action_date,
											p_rac_review_date,
											p_nop_highlight_ind,
											p_nop_ntt_code,
											p_eac_exists,
											p_anl_outcome_rsn,
											p_ahra_refno,
											p_commit,
											p_aca_code);
									
										log.info("Temporary Accomodation : " + propertyTemporaryAccommodation);
										log.info("dateRentChargesChangeNew :" + dateRentChargesChangeNew);
										log.info("rentOfPropertyExcludingServiceCharge :" + rentOfPropertyExcludingServiceCharge);
										log.info("dateServiceChargesChangeNew : " + dateServiceChargesChangeNew);
										log.info("eligibleServiceChargeValue : " + eligibleServiceChargeValue);
										log.info("numberOfTenantIncludingClaimantValue : " + numberOfTenantIncludingClaimantValue);
										log.info("frequencyOfRentAndCharge : " + frequencyOfRentAndCharge);
										log.info("frequencyOfServiceCharge : " + frequencyOfServiceCharge);
										log.info("noOfWeeksValue : " + noOfWeeksValue);
										log.info("numberofBedrooms : " + numberofBedrooms);
										log.info("p_rac_epo_code : " + p_rac_epo_code);
										log.info("p_aca_effective_date : " + p_aca_effective_date);
										log.info("p_aca_expiry_date : " + p_aca_expiry_date);
										log.info("p_aca_next_action_date : " + p_aca_next_action_date);
										log.info("p_rac_review_date : " + p_rac_review_date);
										log.info("p_nop_highlight_ind : " + p_nop_highlight_ind);
										log.info("p_nop_ntt_code : " + p_nop_ntt_code);
										log.info("p_eac_exists : " + p_eac_exists);
										log.info("p_anl_outcome_rsn : " + p_anl_outcome_rsn);
										log.info("p_ahra_refno : " + p_ahra_refno);
										log.info("p_aca_code : " + p_aca_code);

										log.info("P_ERROR_FLAG : " + (String) arrearsResponse.get(0));
										log.info("P_ERROR_MSG : " + (String) arrearsResponse.get(1));
										log.info("P_VAR_WAR_MSG : " + (String) arrearsResponse.get(2));
									
									String errorFlag = (String) arrearsResponse.get(0);
									
									//FLOW : To check need to check error flag to success
										if (errorFlag.equals("Y")){
											//Success 
											//FLOW : update PRRU_NB_REC_SUCCESS
//											log.info("updateDWPTechError");
//											odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
											log.info("updateDWPExtError"); 
											odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
											}
									
									//FLOW : To check need to check error flag to success
									if (errorFlag.equals("N")){
									//Success 
									//FLOW : update PRRU_NB_REC_SUCCESS
									log.info("updateDWPRecSuccess");
									l_errorCount = 0;
									odbcPackageCall.updateDWPRecSuccess(Integer.parseInt(prru_id));
									}
									
									} catch (Exception e) {
							            // Handle other exceptions
										log.info("Exception: " + e.getMessage());

							        }	
										} else {
//									FLOW? : dwpSubmitData=N then it should continue with the next record and mark as success
									if(dwpSubmitData.equalsIgnoreCase("N")) {
										log.info("We are inside submitData - N");
//										FLOW : Check the process run status is "STOP_INIT"
//										doClickWithWait(areTheseDetailsCorrect_No);
										if (processStatus.equals("STOP_INIT")) {
//											SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
										    Date date2 = new Date();
										    log.info("updateDWPEndDate");
											odbcPackageCall.updateDWPEndDate(Integer.parseInt(prru_id), "USER_ENDED",date2);
											closeBrowser();
											endConnection();
											break;
										}
										//FLOW: DWP to check mark record as success or not
										log.info("updateDWPRecSuccess");
										l_errorCount = 0;
										odbcPackageCall.updateDWPRecSuccess(Integer.parseInt(prru_id));
									}
									
								}	
							}	
//							If the record is of Confirm tenant's housing costs then..
								else if(typeOfTenant.contains("Confirm tenant's housing cost") && (dwpRequestType.equalsIgnoreCase("BOTH") || dwpRequestType.equalsIgnoreCase("HOUCOSTS")))
								{							
									log.info("The code reached Housing cost page");
									((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
											getElement(tenantNameAndDOB_HOUCOSTS));
									String fullTenantName = getText(tenantNameAndDOB_HOUCOSTS);
									log.info("The full tenant name is: "+fullTenantName);
								
							        nameAndDOB = getText(tenantNameAndDOB_HOUCOSTS);
							        String[] tenantName_HOUCOSTS = nameAndDOB.split("\\r?\\n|\\r");
							        System.out.println("The size of :"+tenantName_HOUCOSTS.length);
									
//									Checking the number of tenant in Housing Cost							
									int countOfPara = getElementsCount(noOfParaInTenancyPage);
									
									if(countOfPara==1 && tenantName_HOUCOSTS.length==1){ 
										try{
//										Getting the name of the Customer
										nameAndDOB = getText(tenantNameAndDOB_HOUCOSTS);
										log.info("The name and Dob is :" + nameAndDOB);
										
										String[] splitNameandDob = nameAndDOB.split("\\(");
										tenantName = splitNameandDob[0];
										log.info("Name is :" +tenantName);
//										Getting the required name of the tenant
										tenantName = getNameOfTenant(tenantName);
										
										String dateFromApplication = splitNameandDob[1].replace(")", "");
										log.info("Date From Application is :" +dateFromApplication);
										
										String[] dateFormatting = dateFromApplication.split("\\s+");
										DateOfBirth = dateFormatting[0] + "-" + dateFormatting[1] + "-" + dateFormatting[2];
										log.info("DOB is :" +DateOfBirth);
										
										dateFormatAsPerProcedureCall(DateOfBirth);
										dob = requiredDateFormat;
										log.info("Date of Birth :: " + dob);
											
										String fullAddress = getText(tenancyAddress_HousingCost);
										addressHousingCost = fullAddress.trim();
										String[] splitAddress = addressHousingCost.split(",");
//										postCode = splitAddress[2].trim();
										postCode = splitAddress[splitAddress.length-1].trim();
										log.info("The postCode is :" + postCode);

										
//										This needs to be changed with system date
										SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
									    Date date = new Date();
									    actualDate = formatter.format(date);
									    takeScreenshot("Tenancy1 Details page_houcost"+"_"+i);
										
									log.info("---------------------**************--------------------");
									log.info("The name of customer is :" + tenantName);
									log.info("Date of Birth of customer is :: " + dob);
									log.info("The postal code is:" + postCode);
									log.info("The current date is:" + actualDate);
									log.info("The prru_id is:" + prru_id);
									log.info("---------------------**************--------------------");
									}
										catch(Exception e) {
//											Error while parsing the data
											throw new DWPProcessingException("Error on Parsing data of Single Tenant");
										}
									}						
//									Two tenants for Housing Cost
										else if(countOfPara==1 && tenantName_HOUCOSTS.length==2) {
											try {
//												Getting the name of the Customer
												nameAndDOB = tenantName_HOUCOSTS[0];
												log.info("The name and Dob is :" + nameAndDOB);
												
												String[] splitNameandDob = nameAndDOB.split("\\(");
												tenantName = splitNameandDob[0];
												log.info("Name is :" +tenantName);
//												Getting the required name of the tenant
												tenantName = getNameOfTenant(tenantName);;
												
												String dateFromApplication = splitNameandDob[1].replace(")", "");
												log.info("Date From Application is :" +dateFromApplication);
												
												String[] dateFormatting = dateFromApplication.split("\\s+");
												DateOfBirth = dateFormatting[0] + "-" + dateFormatting[1] + "-" + dateFormatting[2];
												log.info("DOB is :" +DateOfBirth);
												
												dateFormatAsPerProcedureCall(DateOfBirth);
												dob = requiredDateFormat;
												log.info("Date of Birth :: " + dob);
												
												nameAndDOBSecondTenant = tenantName_HOUCOSTS[1];
												log.info("The name and Dob is :" + nameAndDOBSecondTenant);
												
												String[] splitNameandDobSecondTenant = nameAndDOBSecondTenant.split("\\(");
												secondTenantName = splitNameandDobSecondTenant[0];
												log.info("Name is :" +secondTenantName);
//												Getting the required name of the tenant
												secondTenantName = getNameOfTenant(secondTenantName);;
												
												String dateFromApplicationSecondTenant = splitNameandDobSecondTenant[1].replace(")", "");
												log.info("Date From Application is :" +dateFromApplicationSecondTenant);
												
												String[] dateFormattingSecondTenant = dateFromApplicationSecondTenant.split("\\s+");
												DateOfBirthSecondTenant = dateFormattingSecondTenant[0] + "-" + dateFormattingSecondTenant[1] + "-" + dateFormattingSecondTenant[2];
												log.info("DOB is :" +DateOfBirthSecondTenant);
												
												dateFormatAsPerProcedureCall(DateOfBirthSecondTenant);
												dobSecondTenat = requiredDateFormat;
												log.info("Date of Birth :: " + dobSecondTenat);
												
												String fullAddress = getText(tenancyAddress_HousingCost);
												addressHousingCost = fullAddress.trim();
												String[] splitAddress = addressHousingCost.split(",");
//												postCode = splitAddress[2].trim();
												postCode = splitAddress[splitAddress.length-1].trim();
												log.info("The postCode is :" + postCode);
												
												SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
											    Date date = new Date();
											    actualDate = formatter.format(date);	
												takeScreenshot("Tenancy1 Details page_houcost"+"_"+i);

											log.info("---------------------**************--------------------");
											log.info("The name of customer is :" + tenantName);
											log.info("Date of Birth of customer is :: " + dob);
											log.info("The postal code is:" + postCode);
											log.info("The current date is:" + actualDate);
											log.info("The prru_id is:" + prru_id);
											
											log.info("The name of customer is :" + secondTenantName);
											log.info("Date of Birth of customer is :: " + dobSecondTenat);
											log.info("The postal code is:" + postCode);
											log.info("The current date is:" + actualDate);
											log.info("The prru_id is:" + prru_id);
											log.info("---------------------**************--------------------");
											}
											catch(Exception e) {
//												Error while parsing the data
												throw new DWPProcessingException("Error on Parsing data of Two Tenant");
											}
										} else {
//											UnMatch Data
											log.info("updateDWPRecUnMatch");
											odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
											throw new DWPProcessingException("More than two tenanat is there, Hence we will skip the reocord");
										}				
//						     ODBC Package call
//							@SuppressWarnings("unchecked")
							String racAccNo= null;
							String tenancyLiableForRentAtThisAddress = null ;
							String acceptingOtherChargesUseAndOccupation = null;								
							String tenantRefNo = null;								
							String propertyTemporaryAccommodation = null;								
							String isAnyoneTenAgreeApartFromClaimant = null;								
							String numberOfTenantIncludingClaimantValue = null;								
							String numberofBedrooms = null;								
							String frequencyOfRentAndCharge = null;								
							String frequencyOfServiceCharge = null;								
							String areThereAnyRentFreeWeek = null;								
							String noOfWeeksValue= null;								
							String rentOfPropertyExcludingServiceCharge= null;								
							String areThereEligibleServiceCharge= null;							
							String eligibleServiceChargeValue= null;								
							String dateRentChargesChange= null;	
							String dateRentChargesChangeNew= null;	
							String dateServiceChargesChange= null;
							String dateServiceChargesChangeNew= null;
							
							try {
								@SuppressWarnings("unchecked")
								List<String> tenancyDetails = odbcPackageCall
										.getTenancyDetails(tenantName, dob, postCode, actualDate, prru_id);
								log.info("Prru_status : " + tenancyDetails.get(0));
								log.info("Error_msg : " + tenancyDetails.get(1));									
								racAccNo = (String) tenancyDetails.get(2);
								log.info("Rac_accno : " + racAccNo);
								tenancyLiableForRentAtThisAddress = (String) tenancyDetails.get(3);
								log.info("****Is this person your tenant and liable for rent at this address : " + tenancyLiableForRentAtThisAddress);
								acceptingOtherChargesUseAndOccupation = (String) tenancyDetails.get(4);
								log.info("****Are you accepting other charges (use and occupation)? : " + acceptingOtherChargesUseAndOccupation);
								tenantRefNo = (String) tenancyDetails.get(5);
								log.info("****Tenant Reference number : " + tenantRefNo);
								propertyTemporaryAccommodation = (String) tenancyDetails.get(6);
								log.info("****Is this property temporary accommodation : " + propertyTemporaryAccommodation);
								isAnyoneTenAgreeApartFromClaimant = (String) tenancyDetails.get(7);
								log.info("****Is there anyone on the tenancy agreement apart from the claimant? : " + isAnyoneTenAgreeApartFromClaimant);
								numberOfTenantIncludingClaimantValue = (String) tenancyDetails.get(8);
								log.info("****How many tenants are there on the tenancy agreement including the claimant? : " + numberOfTenantIncludingClaimantValue);
								numberofBedrooms = (String) tenancyDetails.get(9);
								log.info("****How many bedrooms are there? : " + numberofBedrooms);
								frequencyOfRentAndCharge = (String) tenancyDetails.get(10);
								log.info("****Select how often you charge rent? frequency ? : " + frequencyOfRentAndCharge);
								frequencyOfServiceCharge = (String) tenancyDetails.get(11);
								log.info("****Select how often you charge service charges? frequency ? : " + frequencyOfServiceCharge);
								areThereAnyRentFreeWeek = (String) tenancyDetails.get(12);
								log.info("****Are there any rent-free weeks in the year? : " + areThereAnyRentFreeWeek);
								noOfWeeksValue = (String) tenancyDetails.get(13);
								log.info("****How many weeks are there? : " + noOfWeeksValue);
								rentOfPropertyExcludingServiceCharge = (String) tenancyDetails.get(14);
								log.info("****What is the rent for the property (excluding service charges)? : " + rentOfPropertyExcludingServiceCharge);
								areThereEligibleServiceCharge = (String) tenancyDetails.get(15);
								log.info("****Are there eligible service charges? : " + areThereEligibleServiceCharge);
								eligibleServiceChargeValue = (String) tenancyDetails.get(16);
								log.info("****How much are the eligible service charges for the property? : " + eligibleServiceChargeValue);
								SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								dateRentChargesChange = (String) tenancyDetails.get(17);
								if(dateRentChargesChange != null && !dateRentChargesChange.trim().isEmpty()) {
									dateRentChargesChangeDt = inputFormat.parse(dateRentChargesChange);
									dateRentChargesChangeNew = outputFormat.format(dateRentChargesChangeDt);									
									log.info("****Date the rent changed : " + dateRentChargesChangeNew);	
								}
								dateServiceChargesChange = (String) tenancyDetails.get(18);
								if(dateServiceChargesChange != null && !dateServiceChargesChange.trim().isEmpty()) {
									dateServiceChargesChangedt = inputFormat.parse(dateServiceChargesChange);
									dateServiceChargesChangeNew = outputFormat.format(dateServiceChargesChangedt);
									log.info("****Date the service charge changed new: " + dateServiceChargesChangeNew);
								}
								if (tenancyLiableForRentAtThisAddress.equals("Y")) {
									if((racAccNo.trim().isEmpty() ||frequencyOfRentAndCharge.trim().isEmpty()
											||frequencyOfServiceCharge.trim().isEmpty()
											||areThereEligibleServiceCharge.trim().isEmpty()
											||rentOfPropertyExcludingServiceCharge.trim().isEmpty()
											||((dateServiceChargesChange.trim().isEmpty()) && dateRentChargesChange.trim().isEmpty()))) {
										log.info("Update record unmatch since all answers are not populated");
										throw new DWPProcessingException("Unmatch due to config issue");								
									}
								}
							} 
							catch(IndexOutOfBoundsException e) {
//									UnMatch Data
									log.info("updateDWPRecUnMatch");
									odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
									throw new DWPProcessingException("No Matching Tenant Information for Party");
							}
							catch (Exception e) {
					            // Handle other exceptions
								//FLOW :Was there an error message from the procedure?
								log.info("updateDWPExtError");
								odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
								log.info("Exception: " + e.getMessage());
								throw new DWPProcessingException("Error on Procedure Call");
							}
							try {
								if((countOfPara==1 && tenantName_HOUCOSTS.length==2) && (tenancyLiableForRentAtThisAddress.contentEquals("Y"))){
									String racAccNoSecondTenant= null;
									String tenancyLiableForRentAtThisAddressSecondTenant = null ;
									String acceptingOtherChargesUseAndOccupationSecondTenant = null;								
									String tenantRefNoSecondTenant = null;	
									String propertyTemporaryAccommodationSecondTenant = null;	
									String isAnyoneTenAgreeApartFromClaimantSecondTenant = null;	
									String numberOfTenantIncludingClaimantValueSecondTenant = null;	
									
									@SuppressWarnings("unchecked")
									List<String> tenancyDetailsSecondTenant = odbcPackageCall
											.getTenancyDetails(secondTenantName, dobSecondTenat, postCode, actualDate, prru_id);
									log.info("Prru_status Second Tenant : " + tenancyDetailsSecondTenant.get(0));
									log.info("Error_msg Second Tenant : " + tenancyDetailsSecondTenant.get(1));									
									racAccNoSecondTenant = (String) tenancyDetailsSecondTenant.get(2);
									log.info("Rac_accno : " + racAccNoSecondTenant);
									tenancyLiableForRentAtThisAddressSecondTenant = (String) tenancyDetailsSecondTenant.get(3);
									log.info("****Is this person your tenant and liable for rent at this address : " + tenancyLiableForRentAtThisAddressSecondTenant);
									acceptingOtherChargesUseAndOccupationSecondTenant = (String) tenancyDetailsSecondTenant.get(4);
									log.info("****Are you accepting other charges (use and occupation)? : " + acceptingOtherChargesUseAndOccupationSecondTenant);
									tenantRefNoSecondTenant = (String) tenancyDetailsSecondTenant.get(5);
									log.info("****Tenant Reference number : " + tenantRefNoSecondTenant);
									propertyTemporaryAccommodationSecondTenant = (String) tenancyDetailsSecondTenant.get(6);
									log.info("****Is this property temporary accommodation : " + propertyTemporaryAccommodationSecondTenant);
									isAnyoneTenAgreeApartFromClaimantSecondTenant = (String) tenancyDetailsSecondTenant.get(7);
									log.info("****Is there anyone on the tenancy agreement apart from the claimant? : " + isAnyoneTenAgreeApartFromClaimantSecondTenant);
									numberOfTenantIncludingClaimantValueSecondTenant = (String) tenancyDetailsSecondTenant.get(8);
									log.info("****How many tenants are there on the tenancy agreement including the claimant? : " + numberOfTenantIncludingClaimantValueSecondTenant);
									if (tenantRefNo.contentEquals(tenantRefNoSecondTenant)) {
									//valid tenancy details
										try {
											if(Integer.parseInt(numberOfTenantIncludingClaimantValue) > 2){
												isAnyoneTenAgreeApartFromClaimant="Y";
											}else {
												isAnyoneTenAgreeApartFromClaimant="N";
											}	
										}catch(Exception e) {
												isAnyoneTenAgreeApartFromClaimant="N";
										}
										log.info("Proceed with the flow with output of first tenant");
									}
									else {
										//Unmatch data
//										log.info("updateDWPRecUnMatch");
//										odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
										throw new DWPProcessingException("No Matching Tenancy for Two Tenant");
									}
								}
								else if((countOfPara==1 && tenantName_HOUCOSTS.length==2) && tenancyLiableForRentAtThisAddress.contentEquals("N")) {
									//Unmatch data
									throw new DWPProcessingException("No Matching Tenancy");
								}
							}
							catch(IndexOutOfBoundsException e) {
//									UnMatch Data
									log.info("updateDWPRecUnMatch");
									odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
									throw new DWPProcessingException("No Matching Tenant Information for Party for Two Tenant-I");
							}
							catch(Exception e) {
//									UnMatch Data
									log.info("updateDWPRecUnMatch");
									odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
									throw new DWPProcessingException("No Matching Tenant Information for Party for Two Tenant-E");
								}
													
//								FLOW : Was the person matched - Is this a tenant - 'Y'
								if (tenancyLiableForRentAtThisAddress.equals("Y")) {
									log.info("Tenancy Match");
									((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
											getElement(tenantLiableToRent_Yes));
										jsClick(tenantLiableToRent_Yes);
										pageLoadWait(driver);
								} 
								else {
									//FLOW: update process_runs record set PRRU_NB_REC_UNMATCHED 
									//Record unmatched 
									log.info("updateDWPRecUnMatch");
									odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
									throw new DWPProcessingException("Error UnMatch in Tenacy Page 1");
								}
							takeScreenshot("Tenant1 Page after input_houcost"+"_"+i);
							doClickWithWait(submitButton, 5);
							pageLoadWait(driver);
							
//							waitForTitleIs("- Landlord portal", 5);
							String titleCheckForTenancy2 = driver.getTitle();
							log.info("The title of new tenancy page 2 of housing cost is :" +titleCheckForTenancy2);
							
							takeScreenshot("tenant2 Page_houcost"+"_"+i);
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
										getElement(rentForThePropertyText));
								takeScreenshot("tenant2 Page before input_houcost"+"_"+i);
								log.info("We are in tenancy page 2");
								frequencyOfRentAndCharge.trim();
								doSelectDropDownByVisibleText(selectFrequencyForRent, frequencyOfRentAndCharge);
								pageLoadWait(driver);
								double rentOfPropertyExcludingServiceChargeNum = Double.parseDouble(rentOfPropertyExcludingServiceCharge);
								String rentOfPropertyExcludingServiceChargeFormat = String.format("%.2f", rentOfPropertyExcludingServiceChargeNum);
								doSendKeysWithWait(rentForThePropertyText, rentOfPropertyExcludingServiceChargeFormat, 5);
								
//							FLOW : Are there eligible service charges
							if (areThereEligibleServiceCharge.equalsIgnoreCase("Y")) {
								double eligibleServiceChargeValueNum = Double.parseDouble(eligibleServiceChargeValue);
								String eligibleServiceChargeValueFormat = String.format("%.2f", eligibleServiceChargeValueNum);
								jsClick(doYouHaveServiceCharges_Yes);
								doSendKeysWithWait(serviceChargeText, eligibleServiceChargeValueFormat, 5);
								frequencyOfServiceCharge.trim();
								doSelectDropDownByVisibleText(selectServiceChargePaymentFrequency,
										frequencyOfServiceCharge);
							} else {
								jsClick(doYouHaveServiceCharges_No);
							}
							pageLoadWait(driver);
							takeScreenshot("tenant Page 2 after PartialInput_houcost"+"_"+i);
							
//							Selecting the date based on below conditions 
//							LocalDate currentDate = LocalDate.now();
//					        log.info("Current Date: " + currentDate);

					        // Create a LocalDate object for April 1st of the current year
//					        LocalDate aprilFirst = LocalDate.of(currentDate.getYear(), 4, 1);
//					        log.info("April 1st: " + aprilFirst);

					        // Compare the current date with April 1st
					        if (currentDate.isBefore(aprilFirst)) {
					            log.info("Today is before April 1st.");
					        } else if (currentDate.isAfter(aprilFirst)) {
					           log.info("Today is after April 1st.");
					        } else {
					            log.info("Today is April 1st.");
					        }
							
					      // Convert LocalDate to Date
					        Date localDateToDate = Date.from(aprilFirst.atStartOfDay(ZoneId.systemDefault()).toInstant());
					        //Calendar cal = getNthDayOfWeekOfMonth(2024,Calendar.APRIL,1,Calendar.MONDAY);
							Calendar cal = getNthDayOfWeekOfMonth(Year.now().getValue(),Calendar.APRIL,1,Calendar.MONDAY);
//						    log.info(cal.getTime());
					        
					        if(dateServiceChargesChangedt != null && !dateServiceChargesChangedt.equals("")) {
					        	effectiveDate = dateServiceChargesChangedt;
					        }
					        else {
					        	effectiveDate = dateRentChargesChangeDt;
					        }
					         if ((effectiveDate.after(localDateToDate)||effectiveDate.equals(localDateToDate)) && frequencyOfRentAndCharge.equalsIgnoreCase("monthly")){
									//Click first radio button'
									log.info("We are going to click 1st April");
									jsClick(dateOfTheseCostEffectiveFrom);
								}
								else if(frequencyOfRentAndCharge.equalsIgnoreCase("weekly")) {
									// Click second radio button
									log.info("We are going to click 7st April - First Monday of April");
									jsClick(dateOfTheseCostEffectiveWeekly);
								}
							     else {
							    	  log.info("updateDWPRecUnMatch");
									  odbcPackageCall.updateDWPRecUnMatch(Integer.parseInt(prru_id));
									  throw new DWPProcessingException("The date is not matching");
								//jsClick(dateOfTheseCostEffectiveFromOther);
								//String[] splitDateAndTime = dateServiceChargesChangeNew.split("\\s+");
								//String[] splitDate = splitDateAndTime[0].split("-");
								//doSendKeysWithWait(dateOfTheseCostEffectiveFromOther_Day, splitDate[2], 5);
								//doSendKeysWithWait(dateOfTheseCostEffectiveFromOther_Month, splitDate[1], 5);
								//doSendKeysWithWait(dateOfTheseCostEffectiveFromOther_Year, splitDate[0], 5);
							}
							
							takeScreenshot("tenant Page 2 after input_houcost"+"_"+i);
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									getElement(serviceChargeText));
							takeScreenshot("tenant Page 2 date input_houcost"+"_"+i);
							doClickWithWait(submitButton, 5);
							
//							Tenancy page 3 of housing cost
//							waitForTitleIs(" - Landlord portal", 5);
							String titleCheckForTenancy3 = driver.getTitle();
							log.info("The title of new tenancy page 3 of housing cost is :" +titleCheckForTenancy3);
							if(titleCheckForTenancy3.contains("Error")) {
								log.info("updateDWPExtError"); 
								odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
								throw new DWPProcessingException("Error on Tile, The correct page is not opened");
							}
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									getElement(tenancy3_heading));
							takeScreenshot("tenant Page 3 before input_houcost"+"_"+i);
							pageLoadWait(driver);
							log.info("Successfully entered all the details till tenancy page 2");
							log.info("We are in tenancy page 3");

//							FLOW : Is DWP_SUBMITDATA-'Y'?
							if (dwpSubmitData.equalsIgnoreCase("Y")) {
//								incrementing success records to pick next records in the list
								log.info("We are inside submitData - Y for Houcost");
								++a;
								doClickWithWait(submitButton, 5);
								pageLoadWait(driver);
								takeScreenshot("tenant3 Page after clicking submitBtn_houcost"+"_"+i);
								acaBalance ="0";
								String p_rac_epo_code = null;
								String p_aca_effective_date = null;
								String p_aca_expiry_date = null;
								String p_aca_next_action_date = null;
								String p_rac_review_date = null;
								String p_nop_highlight_ind = null;
								String p_nop_ntt_code = null;
								String p_eac_exists = null;
								String p_anl_outcome_rsn = null;
								String p_ahra_refno = null;
								boolean p_commit = true;
								String p_aca_code = dwpAraHouCosts;
								
//								FLOW : Create Arrears Action
								try {
									
									if(numberOfTenantIncludingClaimantValue == null || numberOfTenantIncludingClaimantValue.trim().isEmpty()) {
										numberOfTenantIncludingClaimantValue="1";
									}
									
								@SuppressWarnings("unchecked")
								List<ArrearsResponse> arrearsResponse = odbcPackageCall.createArrearsAction(racAccNo,
										acaBalance, 
										prru_id, 
										propertyTemporaryAccommodation, 
										dateRentChargesChangeNew, 
										rentOfPropertyExcludingServiceCharge, 
										dateServiceChargesChangeNew, 
										eligibleServiceChargeValue, 
										numberOfTenantIncludingClaimantValue, 
										frequencyOfRentAndCharge, 
										frequencyOfServiceCharge, 
										noOfWeeksValue, 
										numberofBedrooms,
										p_rac_epo_code,
										p_aca_effective_date,
										p_aca_expiry_date,
										p_aca_next_action_date,
										p_rac_review_date,
										p_nop_highlight_ind,
										p_nop_ntt_code,
										p_eac_exists,
										p_anl_outcome_rsn,
										p_ahra_refno,
										p_commit,
										p_aca_code);
								
								log.info("Temporary Accomodation : " + propertyTemporaryAccommodation);
								log.info("dateRentChargesChangeNew :" + dateRentChargesChangeNew);
								log.info("rentOfPropertyExcludingServiceCharge :" + rentOfPropertyExcludingServiceCharge);
								log.info("dateServiceChargesChangeNew : " + dateServiceChargesChangeNew);
								log.info("eligibleServiceChargeValue : " + eligibleServiceChargeValue);
								log.info("numberOfTenantIncludingClaimantValue : " + numberOfTenantIncludingClaimantValue);
								log.info("frequencyOfRentAndCharge : " + frequencyOfRentAndCharge);
								log.info("frequencyOfServiceCharge : " + frequencyOfServiceCharge);
								log.info("noOfWeeksValue : " + noOfWeeksValue);
								log.info("numberofBedrooms : " + numberofBedrooms);
								log.info("p_rac_epo_code : " + p_rac_epo_code);
								log.info("p_aca_effective_date : " + p_aca_effective_date);
								log.info("p_aca_expiry_date : " + p_aca_expiry_date);
								log.info("p_aca_next_action_date : " + p_aca_next_action_date);
								log.info("p_rac_review_date : " + p_rac_review_date);
								log.info("p_nop_highlight_ind : " + p_nop_highlight_ind);
								log.info("p_nop_ntt_code : " + p_nop_ntt_code);
								log.info("p_eac_exists : " + p_eac_exists);
								log.info("p_anl_outcome_rsn : " + p_anl_outcome_rsn);
								log.info("p_ahra_refno : " + p_ahra_refno);
								log.info("p_aca_code : " + p_aca_code);

								log.info("P_ERROR_FLAG : " + (String) arrearsResponse.get(0));
								log.info("P_ERROR_MSG : " + (String) arrearsResponse.get(1));
								log.info("P_VAR_WAR_MSG : " + (String) arrearsResponse.get(2));
								
								String errorFlag = (String) arrearsResponse.get(0);
								
								//FLOW : To check need to check error flag to success
								if (errorFlag.equals("Y")){
									//Success 
									//FLOW : update PRRU_NB_REC_SUCCESS
//									log.info("updateDWPTechError");
//									odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
									log.info("updateDWPExtError"); 
									odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
									}
								
								//FLOW : To check need to check error flag to success
								if (errorFlag.equals("N")){
								//Success 
								//FLOW : update PRRU_NB_REC_SUCCESS
								log.info("updateDWPRecSuccess");
								l_errorCount = 0;
								odbcPackageCall.updateDWPRecSuccess(Integer.parseInt(prru_id));
								}
								
								} catch (Exception e) {
						            // Handle other exceptions
									log.info("Exception: " + e.getMessage());

						        }	
									} else {
//								FLOW? : dwpSubmitData=N then it should continue with the next record and mark as success
								if(dwpSubmitData.equalsIgnoreCase("N")) {
									log.info("We are inside submitData - N for houcost");
//									FLOW : Check the process run status is "STOP_INIT"
									if (processStatus.equals("STOP_INIT")) {
//										SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
									    Date date2 = new Date();
									    log.info("updateDWPEndDate");
										odbcPackageCall.updateDWPEndDate(Integer.parseInt(prru_id), "USER_ENDED",date2);
										closeBrowser();
										endConnection();
										break;
									}
									//FLOW: DWP to check mark record as success or not
									log.info("updateDWPRecSuccess");
									l_errorCount = 0;
									odbcPackageCall.updateDWPRecSuccess(Integer.parseInt(prru_id));
								}
							}
						}
					}
				}
							catch(DWPProcessingException e) {
								// Handle the specific exception){
							l_errorCount++;
							log.info("DWPProcessingException"+"  "+e.getMessage());
						}
					}
//					This is for num of execution mentioned by parameter					
					//FLOW : Successful completion
//					log.info("updateDWPEndStatus");
//					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
//			    Date date = new Date();
//					odbcPackageCall.updateDWPEndStatus(Integer.parseInt(prru_id), date);
//					closeBrowser();
//				endConnection();			
			}
				catch (ParseException e) {
					e.printStackTrace();
				}		
		}
		//		This is for continuous execution
//			//FLOW : Successful completion
			log.info("updateDWPEndStatus");
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		    Date date = new Date();
			odbcPackageCall.updateDWPEndStatus(Integer.parseInt(prru_id), date);
//			endConnection();
//			closeBrowser();		
	}
	catch(NoSuchElementException e){
			log.info("We are in No Such Element Exception block");
			e.printStackTrace();
			l_errorCount ++;
			if(l_errorCount==dwpConsecutiveErrors) {
//				update query
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
			    Date date = new Date();
			    log.info("updateDWPEndDate");
				odbcPackageCall.updateDWPEndDate(Integer.parseInt(prru_id),"ERROR", date);
				log.info("Updated prru status is :" +prru_id);
				e.printStackTrace();
			}
		}
		
		finally {
			log.info("In Finally Block"+"_"+skipUpdate);
			if(skipUpdate=="N") {
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			    Date date = new Date();
			    log.info("updateDWPEndStatus");
				odbcPackageCall.updateDWPEndStatus(Integer.parseInt(prru_id),date);
			}
			try {
				closeBrowser();
				endConnection();
			}catch(IllegalStateException e) {
				log.info("Websocket closed -e");
			}
		}
		}
		//Handles exception during the process
		catch (DwpWebSocketException e) {
			e.printStackTrace();
			log.info("DwpWebSocketException..Closing processes");
			try {
						closeBrowser();
						endConnection();
					}catch(IllegalStateException e1) {
						log.info("Websocket closed -e1");
					}			
		}
		catch (Exception e) {
			e.printStackTrace();
			log.info("DWP Exception..Closing processes");
			try {
						closeBrowser();
						endConnection();
					}catch(IllegalStateException e2) {
						log.info("Websocket closed -e2");
					}
				}
		}

	private JsonNode waitForMessage(String type) {
		try {
			this.currentJsonMessage = null;
			session.sendMessage(new TextMessage(String.format("{\"type\": \"%s\"}", type)));
			log.info("Waiting for message of type {}", type);			
		
			synchronized (this) {
				while (this.currentJsonMessage == null) {
					wait();
				}
			}
			
			while(getMessageType().equals("RESENDCODE")){
				log.info("click on I need a new access code");
				//FLOW : Logic to click I need a new access code
				doClickWithWait(newAccessCodeButton, 5);
				this.currentJsonMessage = null;			
						
			synchronized (this) {
				while (this.currentJsonMessage == null) {
					wait();
				}
			}
			}
			String receivedType = getMessageType();
			if (receivedType.equals("CANCEL")) {
				log.info("User Initiated Cancel");
				this.currentJsonMessage=null;
				throw new DwpWebSocketException("User has cancelled the Operation");	    	
		    	}
			else if (!receivedType.equals(type)) {
				onError(String.format("Expecting %s but received %s", type, receivedType));
			} else {
				log.info("Valid message received");
			}
		} catch (IOException | InterruptedException ex) {
			log.warn("Exception sending message ", ex);
		}
		return this.currentJsonMessage;
	}

	private boolean connectionIsValid() {
		String initialMessageType = getMessageType();
		if (initialMessageType.equals("START")) {
			String dbSession = currentJsonMessage.get("session").asText();
			String dbLogin = currentJsonMessage.get("user").asText();
			String isValidSession = odbcPackageCall.isValidSession(dbSession,dbLogin);
			// At this point should check the session is valid in DB
			if (isValidSession.equals("Y")) {
				log.info("Session is valid ");
				return true;
			} else {
				// Do not send an error back to the client just drop out
				onError(String.format("Session is Invalid "));
				endConnection();
			}
		} else {
			onError(String.format("Expecting initial message of START but received %s", initialMessageType));
			endConnection();
		}
		return false;
	}

	private void startBrowser() {
		try {
			FirefoxProfile profile = new FirefoxProfile();
			FirefoxOptions options = new FirefoxOptions();
//			profile.setPreference("intl.accept_languages", "en_GB");
			options.setProfile(profile);
			options.addArguments(configProperties.getFirefoxArguments());
			log.info("Starting firefox with {}", configProperties.getFirefoxArguments());
			log.info(configProperties.getGeckoDriverPath());
            System.setProperty("webdriver.gecko.driver",configProperties.getGeckoDriverPath());
			driver = new FirefoxDriver(options);
			waitingDriver = new WebDriverWait(driver, configProperties.getWaitSeconds());
			// Hack to clear any login cookies
			driver.manage().deleteAllCookies();
		}	catch(Exception e){
	    	onError(String.format("webdriver.gecko.driver is having problem"));
//			closeBrowser();
	    	endConnection();
	    	e.printStackTrace();
    		
    	}
		
	}

	private void closeBrowser() {
		driver.quit();
	}

	private void goToSite(String url) {
		driver.get(url);
	}

	private void endConnection() {
		log.info("Ending session");
		try {
			session.sendMessage(new TextMessage("{\"type\": \"END\"}"));
			session.close();
		} catch (IOException ex) {
			log.error("Failed to close session ", ex);
		}
		// Remove this instance from the processor map
		Repository.processorMapBySession().remove(session.getId());
	}

	private void onError(String errorMessage) {
		log.warn(errorMessage);
		try {
			session.sendMessage(
					new TextMessage(String.format("{\"type\": \"ERROR\", \"detail\": \"%s\"}", errorMessage)));
		} catch (IOException ex) {
			log.warn("Exception sending message ", ex);
		}
	}

	public void takeScreenshot(String name) {
		try {
			log.info("Getting Screenshot");
			log.info("Screenshot Enabled"+configProperties.getEnableScreenshots());
			if(configProperties.getEnableScreenshots()) {
			new File(configProperties.getScreenShotPath()+"/screenshots/").mkdirs();
			File target = new File(String.format(configProperties.getScreenShotPath()+"/screenshots/%s.png", name.replaceAll("\\W", "-")+ "_"+prru_id));
			log.info("Taking screenshot at {}", target.getAbsolutePath());
 
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			FileCopyUtils.copy(scrFile, target);
		}
	}catch (IOException ex) {
			log.info("Failed to take screen shot ", ex);
			log.warn("Warnin : Failed to take screen shot ", ex);
		}
	}
	
	public void takeScreenshotDir(String name) {
		try {
			log.info("Getting Screenshot");
			log.info("Screenshot Enabled"+configProperties.getEnableScreenshotsDir());
			if(configProperties.getEnableScreenshotsDir()) {
			new File(configProperties.getScreenShotPath()+"/screenshots/").mkdirs();
			File target = new File(String.format(configProperties.getScreenShotPath()+"/screenshots/%s.png", name.replaceAll("\\W", "-")+ "_"+prru_id));
			log.info("Taking screenshot at {}", target.getAbsolutePath());
 
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			FileCopyUtils.copy(scrFile, target);
		}
	}catch (IOException ex) {
			log.info("Failed to take screen shot ", ex);
			log.warn("Warnin : Failed to take screen shot ", ex);
		}
	}

	private String getMessageType() {
		return this.currentJsonMessage.get("type").asText();
	}
	
	public String waitForTitleIs(String title, int timeOut) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOut));

		try {
			if (wait.until(ExpectedConditions.titleIs(title))) {
				return driver.getTitle();
			}
		} catch (TimeoutException e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
			if(prru_id != null && !prru_id.trim().isEmpty()) {
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			}
			log.info(title + " title value is not present....");
			//onError(title + " correct page is not loaded....");			
			//e.printStackTrace();
			throw new DWPProcessingException("Error on Title");
		}
		return null;

	}
	
//	Selenium functions
	public WebElement getElement(By locator) {
		try {
			return driver.findElement(locator);
		}catch(Exception e){
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
			if(prru_id != null && !prru_id.trim().isEmpty()) {
//				log.info("updateDWPTechError");
//				odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
				log.info("updateDWPExtError"); 
				odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			}
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public void doSendKeys(By locator, String value) {
		try {
			getElement(locator).clear();
			getElement(locator).sendKeys(value);
		}catch(Exception e){
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public String getText(By locator) {
		try {
			return getElement(locator).getText();
		}catch(Exception e){
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
			if(prru_id != null && !prru_id.trim().isEmpty()) {
//				log.info("updateDWPTechError");
//				odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
				log.info("updateDWPExtError"); 
				odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			}
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public WebElement waitForVisibilityOfElement(By locator, int timeOut) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOut));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}
	
	public WebElement waitForClickableOfElement(By locator, int timeOut) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOut));
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
		
	}

	public void doClickWithWait(By locator, int timeOut) {
			try {
			waitForClickableOfElement(locator, timeOut).click();
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
			if(prru_id != null && !prru_id.trim().isEmpty()) {
//				log.info("updateDWPTechError");
//				odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
				log.info("updateDWPExtError"); 
				odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
				takeScreenshotDir("Error while clicking on "+locator +"_"+prru_id);
			}
			e.printStackTrace();			
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}
	
	public void jsClick(By locator) {
		try {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", getElement(locator));
		}catch(Exception e) {
			//		FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public void doSendKeysWithWait(By locator, String value, int timeOut) {
		try {
			getElement(locator).clear();
			waitForVisibilityOfElement(locator, timeOut).sendKeys(value);
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
			if(prru_id != null && !prru_id.trim().isEmpty()) {
//				log.info("updateDWPTechError");
//				odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
				log.info("updateDWPExtError"); 
				odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			}
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}


	private Select createSelect(By locator) {
		try {
			Select select = new Select(getElement(locator));
			return select;
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public void doSelectDropDownByIndex(By locator, int index) {
		try {
			createSelect(locator).selectByIndex(index);
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public void doSelectDropDownByVisibleText(By locator, String visibleText) {
		try {
			createSelect(locator).selectByVisibleText(visibleText);
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public void doSelectDropDownByValue(By locator, String value) {
		try {
			createSelect(locator).selectByValue(value);
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public int getElementsCount(By locator) {
		try {
			return getElements(locator).size();
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			log.info("updateDWPExtError"); 
			odbcPackageCall.updateDWPExtError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}

	public List<WebElement> getElements(By locator) {
		try {
			return driver.findElements(locator);
		}catch(Exception e) {
//			FLOW : update PRRU_NB_REC_TECH_ERROR process_runs record
//			log.info("updateDWPTechError");
//			odbcPackageCall.updateDWPTechError(Integer.parseInt(prru_id));
			e.printStackTrace();
			throw new DWPProcessingException("The "+locator+" is not working as expected");
		}
	}
	
//	Wait Concepts
	public void untilElementClickable(WebDriver driver, By locator) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));
			wait.until(ExpectedConditions.elementToBeClickable(locator));
		}catch (Exception e) {
			log.info(e.getMessage());
		}
	}
	
	public void untilJqueryIsDone(WebDriver driver) {
		until(driver, (d) -> {
			Boolean isJqueryCallDone = (Boolean) ((JavascriptExecutor) driver).executeScript("return (window.jQuery != null) && (jQuery.active === 0);");
			if (!isJqueryCallDone)
				log.info("JQuery call is in Progress");
			return isJqueryCallDone;
		}, defaultTimeout);
	}
	
	private void until(WebDriver driver, Function<WebDriver, Boolean> waitCondition, Long timeoutInSeconds) {
		WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(50));
		try {
			webDriverWait.until(waitCondition);
		} catch (Exception e) {
		}
	}
	
	public void untilPageLoadComplete(WebDriver driver) {
		until(driver, (d) -> {
			Boolean isPageLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript("return document.readyState")
					.equals("complete");
			if (!isPageLoaded)
				log.info("Document is loading");
			return isPageLoaded;
		}, defaultTimeout);
	}
	
	public void pageLoadWait(WebDriver driver) {
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
	}
	
	
//	Functions to derive the desired input parameter for procedure call
	
	//converting string format '26-March-1984' to required date format to process the procedure call
	public String dateFormatAsPerProcedureCall(String dateFromApplication) {
		//	First convert the string to date
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = null;
		try {
			date = dateFormat.parse(dateFromApplication);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		log.info("Date from Applicatoin :- " + date);

		// Set the desired date format required to process the procedure call
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		requiredDateFormat = dateFormat2.format(date);
		log.info("Required Date Format :-" + requiredDateFormat);
		return requiredDateFormat;
	}
	
//	Getting the name as per required format (First Name and Last Name)
	public String getNameOfTenant(String tenantName) {
		//	Get the name of tenant
		if(tenantName.contains(" and ")) {
			String[] splitNames = tenantName.split(" and ");
			String firstName = splitNames[0].trim();
			String secondName = splitNames[1].trim();
			log.info("The first person name is: "+firstName);
			log.info("The second person name is: "+secondName);

			String[] nameOne = firstName.split("\\s+");
			int sizeOfArrayNameOne = nameOne.length;
			
			actualCustomerName = nameOne[0] + " " + nameOne[sizeOfArrayNameOne-1];
			log.info("The customer name is: " + actualCustomerName);
		}
		else {
			String[] splitName = tenantName.split("\\s+");
			int sizeOfArray = splitName.length;
			
			actualCustomerName = splitName[0] + " " + splitName[sizeOfArray-1];
			log.info("The customer name is: " + actualCustomerName);
		}
		return actualCustomerName;
	}
	
//	To get the first of any month
	public Calendar getNthDayOfWeekOfMonth(int year, int month, int n, int dayOfWeek) {
	    Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE,0);
	    cal.set(Calendar.SECOND,0);
	    cal.set(Calendar.YEAR,year);
	    cal.set(Calendar.MONTH,month);
	    cal.set(Calendar.DAY_OF_MONTH,1);

	    int dayDiff = dayOfWeek-cal.get(Calendar.DAY_OF_WEEK);
	    if (dayDiff<0) {
	        dayDiff+=7;
	    }
	    dayDiff+=7*(n-1);

	    cal.add(Calendar.DATE, dayDiff); 
	    return cal;
	}

}
