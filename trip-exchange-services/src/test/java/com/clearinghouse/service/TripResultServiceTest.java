package com.clearinghouse.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TripResultServiceTest {

    private Object newInstanceWithoutConstructor(Class<?> cls) throws Exception {
        // Use ReflectionFactory to allocate instance without calling constructors
        // This is allowed in tests to exercise private methods without wiring dependencies
        sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
        Constructor<Object> objCons = Object.class.getDeclaredConstructor();
        Constructor<?> c = rf.newConstructorForSerialization(cls, objCons);
        c.setAccessible(true);
        return c.newInstance();
    }

    @Test
    public void testExtractHhMmSs_variousFormats() throws Exception {
        Object svc = newInstanceWithoutConstructor(TripResultService.class);
        Method m = TripResultService.class.getDeclaredMethod("extractHhMmSs", String.class);
        m.setAccessible(true);

        assertEquals("14:05:09", m.invoke(svc, "14:05:09"));
        assertEquals("02:05:00", m.invoke(svc, "2:05"));
        assertEquals("02:05:00", m.invoke(svc, "2:5"));
        assertEquals("02:05:03", m.invoke(svc, "2:5:3"));
        assertEquals("09:05:00", m.invoke(svc, "arrive at 9:05"));
        assertEquals("no time here", m.invoke(svc, "no time here"));
        assertNull(m.invoke(svc, (Object) null));
    }
}

