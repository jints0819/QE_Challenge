import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.ComparisonFailure;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;


public class Shop_Tax {

    WebDriver driver;

    static final int inventoryCount = 1;
    static final double defaultSaleTax = 0.05;
    static final int price_zebra = 13;
    static final int price_lion = 20;
    static final int price_elephant = 35;
    static final int price_giraffe = 17;

    Select drpState;
    Map<String,Double> stateTaxMap;
    int states;

    List<WebElement> list;
    List<String> stateList = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    @Before
    public void init() {
        stateTaxMap = new HashMap<>();
        stateTaxMap.put( "CA", 0.08 );
        stateTaxMap.put( "NY", 0.06 );
        stateTaxMap.put( "MN", 0.00 );

        //go to web page
        driver = new FirefoxDriver();
        driver.get("https://jungle-socks.herokuapp.com/");

        //select dropdown list
        this.drpState = new Select(driver.findElement(By.name("state")));
        this.list=drpState.getOptions();
        list.remove(drpState.getFirstSelectedOption());
        this.states = this.list.size();

        // get the values of state list
        for ( int i = 0; i < this.list.size(); i++) {
            this.stateList.add( this.list.get(i).getAttribute("value") );
        }
    }


    @Test
    public void countTotal() {

       for(int i = 0 ; i < this.stateList.size(); i++) {

            // get drpSate
           this.drpState = new Select(driver.findElement(By.name("state")));

            // select state and get taxRate
            String st = this.stateList.get(i);
            double taxRate = stateTaxMap.getOrDefault(st, defaultSaleTax);
            System.out.println("state: " + st + ", tax: " + taxRate);
            drpState.selectByValue(st);

           try {
            // fill in the Quantity
            WebElement zebraTextbox = driver.findElement(By.id("line_item_quantity_zebra"));
            zebraTextbox.clear();
            zebraTextbox.sendKeys(String.valueOf(inventoryCount));

            WebElement lionTextbox = driver.findElement(By.id("line_item_quantity_lion"));
            lionTextbox.clear();
            lionTextbox.sendKeys(String.valueOf(inventoryCount));

            WebElement elephantTextbox = driver.findElement(By.id("line_item_quantity_elephant"));
            elephantTextbox.clear();
            elephantTextbox.sendKeys(String.valueOf(inventoryCount));

            WebElement giraffeTextbox = driver.findElement(By.id("line_item_quantity_giraffe"));
            giraffeTextbox.clear();
            giraffeTextbox.sendKeys(String.valueOf(inventoryCount));


            // Click Checkout Button
            WebElement checkoutButton = driver.findElement(By.name("commit"));
            checkoutButton.click();

            //verify the subtotal
            double subtotal = inventoryCount * price_zebra + inventoryCount * price_lion + inventoryCount * price_elephant + inventoryCount * price_giraffe;
            String subtotal_price = driver.findElement(By.id("subtotal")).getText();
            Assert.assertEquals(subtotal_price, '$' + String.format("%.2f", subtotal));

            //verify the tax
            double sale_tax = subtotal * taxRate;
            String tax_price = driver.findElement(By.id("taxes")).getText();
            Assert.assertEquals(tax_price, '$' + String.format("%.2f", sale_tax));

            //verify the total
            double total = subtotal + sale_tax;
            String total_price = driver.findElement(By.id("total")).getText();
            Assert.assertEquals(total_price, '$' + String.format("%.2f", total));

        }catch (ComparisonFailure exception){
            String err = st + " "+ exception.getMessage();
            errors.add(err);
            driver.navigate().to("https://jungle-socks.herokuapp.com/");
            continue;
        }

           driver.navigate().to("https://jungle-socks.herokuapp.com/");
        }
    }
    @After
    public void tearDown(){
        try {
            if (!errors.isEmpty()) {
                throw new AssertionError(errors);
            }
        }
        finally {
            errors.clear();
            driver.quit();
        }
    }
}
