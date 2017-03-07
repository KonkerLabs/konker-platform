package com.konkerlabs.platform.registry.api.model.core;

import org.springframework.data.annotation.Transient;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface SerializableVO<T, R> {


    /**
     * Serialize from DB to VO
     * @param t
     * @return VO
     */
    R apply(T t);

    /**
     * Update DB object with VO values
     * @param t
     * @return DB
     */
    T patchDB(T t);

    /**
     * Serialize list of objects from DB to VO
     * @param t
     * @return List<R>
     */
    @Transient
    default List<R> apply(List<T> t) {
        return t.parallelStream()
                    .map(i -> apply(i))
                    .collect(Collectors.toList());

    }

}
