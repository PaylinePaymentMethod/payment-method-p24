package com.payline.payment.p24;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class FirstAutomationTest {

    //We should add @Test annotation that JUnit will run below method
    @Test
    //Start to write our test method. It should ends with "Test"
    public void chromeTest() {

        //Step 1- Driver Instantiation: Instantiate driver object as FirefoxDriver
        WebDriver driver = new ChromeDriver();

        //Step 2- Navigation: Open a website
        driver.navigate().to("https://www.thalesgroup.com/fr/global/propos-de-thales");

        //Step 3- Assertion: Check its title is correct
        //assertEquals method Parameters: Message, Expected Value, Actual Value
        Assert.assertEquals("Title check failed!", "A propos de Thales | Thales Group", driver.getTitle());

        //Step 4- Close Driver
        driver.close();

        //Step 5- Quit Driver
        driver.quit();
    }
}
