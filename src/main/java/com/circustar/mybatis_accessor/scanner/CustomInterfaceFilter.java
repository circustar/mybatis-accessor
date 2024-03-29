package com.circustar.mybatis_accessor.scanner;

import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

public class CustomInterfaceFilter extends AbstractTypeHierarchyTraversingFilter {
    private final Class<?> targetType;

    public CustomInterfaceFilter(Class<?> targetType) {
        super(true, true);
        this.targetType = targetType;
    }

    public final Class<?> getTargetType() {
        return this.targetType;
    }

    @Override
    protected boolean matchClassName(String className) {
        return this.targetType.getName().equals(className);
    }

    @Override
    @Nullable
    protected Boolean matchSuperClass(String superClassName) {
        return this.matchTargetType(superClassName);
    }

    @Override
    @Nullable
    protected Boolean matchInterface(String interfaceName) {
        return this.matchTargetType(interfaceName);
    }

    @Nullable
    protected Boolean matchTargetType(String typeName) {
        if (this.targetType.getName().equals(typeName)) {
            return true;
        } else if (Object.class.getName().equals(typeName)) {
            return false;
        } else {
            try {
                Class<?> clazz = ClassUtils.forName(typeName, Thread.currentThread().getContextClassLoader());
                return this.targetType.isAssignableFrom(clazz);
            } catch (ClassNotFoundException var3) {
                return false;
            }
        }
    }
}
