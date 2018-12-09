package com.altona.db;

import java.util.Optional;

interface InternalValue<T> {

    Optional<T> getValueForInsert();

}
