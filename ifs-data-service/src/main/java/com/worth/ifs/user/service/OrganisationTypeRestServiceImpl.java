package com.worth.ifs.user.service;

import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Arrays.asList;

@Service
public class OrganisationTypeRestServiceImpl extends BaseRestService implements OrganisationTypeRestService {
    @Value("${ifs.data.service.rest.organisationtype}")
    private String restUrl;


    @Override
    public OrganisationTypeResource findOne(Long id) {
        return restGet(restUrl + "/" + id, OrganisationTypeResource.class);
    }

    @Override
    public List<OrganisationTypeResource> getAll() {
        return asList(restGet(restUrl + "/getAll", OrganisationTypeResource[].class));
    }
}