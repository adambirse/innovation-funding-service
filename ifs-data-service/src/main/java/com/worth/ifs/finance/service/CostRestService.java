package com.worth.ifs.finance.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.finance.resource.cost.CostItem;

import java.util.List;

/**
 * Interface for CRUD operations on {@link Cost} related data.
 */
public interface CostRestService{
    RestResult<CostItem> add(Long applicationFinanceId, Long questionId, CostItem costItem);
    RestResult<List<CostItem>> getCosts(Long applicationFinanceId);
    RestResult<Void> update(CostItem costItem);
    RestResult<CostItem> findById(Long id);
    RestResult<Void> delete(Long costId);
}
