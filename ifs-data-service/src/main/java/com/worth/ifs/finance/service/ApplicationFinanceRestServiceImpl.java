package com.worth.ifs.finance.service;

import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.finance.domain.ApplicationFinance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * ApplicationFinanceRestServiceImpl is a utility for CRUD operations on {@link ApplicationFinance}.
 * This class connects to the {@link com.worth.ifs.finance.controller.ApplicationFinanceController}
 * through a REST call.
 */
@Service
public class ApplicationFinanceRestServiceImpl extends BaseRestService implements ApplicationFinanceRestService {
    @Value("${ifs.data.service.rest.applicationfinance}")
    String applicationFinanceRestURL;

    public ApplicationFinance getApplicationFinance(Long applicationId, Long organisationId) {
        if(applicationId == null || organisationId == null){
            return null;
        }
        return restGet(applicationFinanceRestURL + "/findByApplicationOrganisation/" + applicationId + "/" + organisationId, ApplicationFinance.class);
    }

    public List<ApplicationFinance> getApplicationFinances(Long applicationId) {
        if(applicationId == null) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ApplicationFinance[]> responseEntity = restTemplate.getForEntity(dataRestServiceURL + applicationFinanceRestURL + "/findByApplication/" + applicationId, ApplicationFinance[].class);
        ApplicationFinance[] applicationFinances =  responseEntity.getBody();
        return Arrays.asList(applicationFinances);
    }

    public ApplicationFinance addApplicationFinanceForOrganisation(Long applicationId, Long organisationId) {
        if(applicationId == null || organisationId == null) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = dataRestServiceURL + applicationFinanceRestURL + "/add/" + applicationId + "/" + organisationId;

        ResponseEntity<ApplicationFinance> responseEntity = restTemplate.exchange(url, HttpMethod.POST, null, ApplicationFinance.class);
        return responseEntity.getBody();
    }
}
