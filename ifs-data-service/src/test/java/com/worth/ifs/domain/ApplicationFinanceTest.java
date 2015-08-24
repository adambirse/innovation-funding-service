package com.worth.ifs.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApplicationFinanceTest {
    ApplicationFinance applicationFinance;

    Long id;
    Organisation organisation;
    Application application;

    @Before
    public void setUp() throws Exception {
        id=0L;
        organisation = new Organisation(1L, "Worth Internet Systems");
        application = new Application();
        applicationFinance = new ApplicationFinance(id, application, organisation);
    }

    @Test
    public void applicationFinanceShouldReturnCorrectAttributeValues() throws Exception {
        Assert.assertEquals(applicationFinance.getId(), id);
    }
}