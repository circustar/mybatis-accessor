package com.circustar.mvcenhance.enhance.service;

import com.circustar.mvcenhance.enhance.relation.EntityDtoServiceRelation;
import org.springframework.validation.BindingResult;

import java.util.Collection;
import java.util.Map;

public interface ICrudService {
    Collection<Object> updateByProviders(EntityDtoServiceRelation relationInfo, Object object, String[] updateNames, Map options, BindingResult bindingResult) throws Exception;
}
