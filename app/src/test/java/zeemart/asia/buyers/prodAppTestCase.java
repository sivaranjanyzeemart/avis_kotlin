package zeemart.asia.buyers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;

public class prodAppTestCase {
    AndroidDriver driver;


    @Before()
    public void setUp() throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("deviceName", "98440b9f");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("appPackage", "zeemart.asia.buyers");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("platformVersion", "10");
        capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
        capabilities.setCapability("noReset", "true");
        capabilities.setCapability("appActivity", "zeemart.asia.buyers.activities.SplashActivity");
        driver = new AndroidDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

    }

    @Test
    public void loginSuccess() {
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_log_in')]")).click();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'email_edit')]")).click();//Enter Mobile No
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'email_edit')]")).sendKeys("tester@zeemart.asia");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'password_edit')]")).click();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'password_edit')]")).sendKeys("!123456Zm");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'login_btn')]")).click();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void loginFailure() {
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_log_in')]")).click();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'email_edit')]")).click();//Enter Mobile No
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'email_edit')]")).sendKeys("kovalan@zeemart.asia");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'password_edit')]")).click();//Enter Mobile No
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'password_edit')]")).sendKeys("!123456789Zm");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'login_btn')]")).click();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void createOrder() {
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_home_new_order')]")).click();
//        List<WebElement> list = driver.findElements(By.xpath("//android.widget.ListView[contains(@resource-id, 'lst_outlet_ids')]"));
//        list.get(0).click();
        WebDriverWait waitss = new WebDriverWait(driver, 8);
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'browse_btn_my_supplier')]")).click();
        List<WebElement> listSuppliers = driver.findElements(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'layout_supplier_list')]"));
        listSuppliers.get(0).click();

        List<WebElement> listProducts = driver.findElements(By.xpath("//android.widget.TextView[contains(@resource-id, 'txt_add_order')]"));
        listProducts.get(0).click();
        driver.findElement(By.xpath("//android.widget.ImageButton[contains(@resource-id, 'btn_inc_quantity')]")).click();
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_review_add_to_order')]")).click();
        List<WebElement> lstSelectDraft = driver.findElements(By.xpath("//android.widget.CheckBox[contains(@resource-id, 'check_box_select_draft')]"));
        lstSelectDraft.get(0).click();
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_review_cart_place_order')]")).click();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        isAlertPresent();
        mobileAlertHandle();
        WebDriverWait waits = new WebDriverWait(driver, 8);
    }


    @Test
    public void RepeatOrder() {
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_home_repeat_order')]")).click();
//        List<WebElement> list = driver.findElements(By.xpath("//android.widget.ListView[contains(@resource-id, 'lst_outlet_ids')]"));
//        list.get(0).click();
        WebDriverWait waitss = new WebDriverWait(driver, 8);
        List<WebElement> listSuppliers = driver.findElements(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_order_detail')]"));
        listSuppliers.get(0).click();
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btnReviewOrder')]")).click();
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_send_to_approver')]")).click();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        isAlertPresent();
        mobileAlertHandle();
        WebDriverWait waits = new WebDriverWait(driver, 8);
    }

    @Test
    public void createEssentialOrder() {
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_home_new_order')]")).click();
//        List<WebElement> list = driver.findElements(By.xpath("//android.widget.ListView[contains(@resource-id, 'lst_outlet_ids')]"));
//        list.get(0).click();

        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'browse_btn_essentials')]")).click();

        List<WebElement> listSuppliers = driver.findElements(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'layout_supplier_list')]"));
        listSuppliers.get(0).click();

        List<WebElement> listProducts = driver.findElements(By.xpath("//android.widget.TextView[contains(@resource-id, 'txt_add_order')]"));
        listProducts.get(0).click();
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_review_add_to_order')]")).click();
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_place_order_essentials')]")).click();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        isAlertPresent();
        mobileAlertHandle();
        WebDriverWait waits = new WebDriverWait(driver, 8);
    }

    @Test
    public void InvoiceFlow() {
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'invoices_btn_uploads')]")).click();
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'btn_new_invoice')]")).click();
        driver.findElement(By.xpath("//android.widget.ImageButton[contains(@resource-id, 'btn_capture_invoice')]")).click();
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_done_adjust')]")).click();
        driver.findElement(By.xpath("//android.widget.TextView[contains(@resource-id, 'txt_review_Images')]")).click();
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_upload')]")).click();
        WebDriverWait wait = new WebDriverWait(driver, 5);

    }

    @Test
    public void Inventory() {
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_inventory')]")).click();
        List<WebElement> list = driver.findElements(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_inventory_list_item_count')]"));
        list.get(0).click();
        driver.findElement(By.xpath("//android.widget.TextView[contains(@resource-id, 'txt_auto_fill_last_stock_count_text')]")).click();
        isAlertPresent();
        mobileAlertHandle();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'btn_save_stock_count')]")).click();
        WebDriverWait waitss = new WebDriverWait(driver, 5);
        isAlertPresent();
        mobileAlertHandle();
    }

    @Test
    public void changePassword() {
        driver.findElement(By.xpath("//android.widget.RelativeLayout[contains(@resource-id, 'lyt_change_password')]")).click();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'old_password_edit')]")).click();//Enter Mobile No
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'old_password_edit')]")).sendKeys("!123456Zm");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'new_password_edit')]")).click();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'new_password_edit')]")).sendKeys("!1234567Zm");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'confirm_password_edit')]")).click();
        driver.findElement(By.xpath("//android.widget.EditText[contains(@resource-id, 'confirm_password_edit')]")).sendKeys("!1234567Zm");
        driver.hideKeyboard();
        driver.findElement(By.xpath("//android.widget.Button[contains(@resource-id, 'change_password_btn')]")).click();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        isAlertPresent();
        mobileAlertHandle();
        WebDriverWait waits = new WebDriverWait(driver, 8);
    }

    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void mobileAlertHandle() {
        if (isAlertPresent()) {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        }
    }

    @After
    public void End() {
        driver.quit();
    }
}
