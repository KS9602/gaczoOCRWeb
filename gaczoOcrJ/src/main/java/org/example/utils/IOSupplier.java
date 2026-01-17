package org.example.utils;

import java.io.IOException;

@FunctionalInterface
interface IOSupplier<T> {
    T get() throws IOException;
}