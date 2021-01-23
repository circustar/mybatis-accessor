package com.circustar.mvcenhance.scanner;

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

    protected boolean matchClassName(String className) {
        return this.targetType.getName().equals(className);
    }

    @Nullable
    protected Boolean matchSuperClass(String superClassName) {
        return this.matchTargetType(superClassName);
    }

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
                Class<?> clazz = ClassUtils.forName(typeName, this.getClass().getClassLoader());
                return this.targetType.isAssignableFrom(clazz);
            } catch (Throwable var3) {
            }
            return false;
        }
    }
}
