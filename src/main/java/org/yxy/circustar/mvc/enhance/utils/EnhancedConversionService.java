package org.yxy.circustar.mvc.enhance.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnhancedConversionService {

    public EnhancedConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    public ConversionService getConversionService() {
        return conversionService;
    }
    private ConversionService conversionService;

    public <T, S> S convert(T o, Class<S> clazz) throws IllegalAccessException, InstantiationException {
        if(o == null) {
            return null;
        }

        if(conversionService.canConvert(o.getClass(), clazz)) {
            return conversionService.convert(o, clazz);
        } else {
            S s = clazz.newInstance();
            BeanUtils.copyProperties(o, s);
            return s;
        }
    }

    public <T, S> List<S> convertList(List<T> list, Class<S> clazz) throws InstantiationException, IllegalAccessException {
        List<S> result = new ArrayList<>();
        for(T t : list) {
            result.add(convert(t, clazz));
        }

        return result;
    }

    public <T, S> List<S> convertCollection(Collection<T> list, Class<S> clazz) throws InstantiationException, IllegalAccessException {
        List<S> result = new ArrayList<>();
        for(T t : list) {
            result.add(convert(t, clazz));
        }

        return result;
    }
}
