package com.mbio.dataproviders;

import com.mbio.utils.ExcelReader;
import org.testng.annotations.DataProvider;

/**
 * All data providers. Each one reads a sheet from src/test/resources/testdata/TestData.xlsx.
 * The test method runs once for every row in the sheet.
 */
public class TestDataProvider {

    @DataProvider(name = "navigationData")
    public Object[][] navigationData() {
        return ExcelReader.getSheetData("Navigation");
    }

    @DataProvider(name = "desktopViewports")
    public Object[][] desktopViewports() {
        return ExcelReader.getSheetData("DesktopViewports");
    }

    @DataProvider(name = "companyStats")
    public Object[][] companyStats() {
        return ExcelReader.getSheetData("CompanyStats");
    }

    @DataProvider(name = "companySections")
    public Object[][] companySections() {
        return ExcelReader.getSheetData("CompanySections");
    }

    @DataProvider(name = "pageStatus")
    public Object[][] pageStatus() {
        return ExcelReader.getSheetData("PageStatus");
    }

    @DataProvider(name = "banners")
    public Object[][] banners() {
        return ExcelReader.getSheetData("Banners");
    }
}
