package org.hibernate.omm.util;

import java.util.Collection;

public final class CollectionUtil {
    private CollectionUtil() {
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean hasMoreThanOneElement(Collection<?> collection) {
        return collection != null && collection.size() > 1;
    }
}