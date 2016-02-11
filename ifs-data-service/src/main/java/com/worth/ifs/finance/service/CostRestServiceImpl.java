package com.worth.ifs.finance.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.resource.cost.CostItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.costItemListType;

/**
 * CostRestServiceImpl is a utility for CRUD operations on {@link Cost}.
 * This class connects to the {@link com.worth.ifs.finance.controller.CostController}
 * through a REST call.
 */
@Service
public class CostRestServiceImpl extends BaseRestService implements CostRestService {

    @Value("${ifs.data.service.rest.cost}")
    String costRestURL;

    @Override
    public RestResult<CostItem> add(Long applicationFinanceId, Long questionId, CostItem costItem) {
        return postWithRestResult(costRestURL + "/add/" + applicationFinanceId + "/" + questionId, costItem, CostItem.class);
    }

    @Override
    public RestResult<List<CostItem>> getCosts(Long applicationFinanceId) {
        return getWithRestResult(costRestURL + "/get/" + applicationFinanceId, costItemListType());
    }

    @Override
    public RestResult<Void> update(CostItem costItem) {
        return putWithRestResult(costRestURL + "/update/" + costItem.getId(), costItem, Void.class);
    }

    @Override
    public RestResult<Void> delete(Long costId) {
        return deleteWithRestResult(costRestURL + "/delete/" + costId, Void.class);
    }

    @Override
    public RestResult<CostItem> findById(Long id) {
        return getWithRestResult(costRestURL + "/findById/" + id, CostItem.class);
    }
}
