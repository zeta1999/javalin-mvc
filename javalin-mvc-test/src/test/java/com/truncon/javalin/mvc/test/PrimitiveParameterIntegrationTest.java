package com.truncon.javalin.mvc.test;

import com.truncon.javalin.mvc.test.controllers.PrimitiveParameterController;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static com.truncon.javalin.mvc.test.QueryUtils.getStringResponse;
import static com.truncon.javalin.mvc.test.RouteBuilder.buildRoute;

public final class PrimitiveParameterIntegrationTest {
    @Test
    public void testBoolean() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Boolean.toString(Boolean.TRUE);
            String route = buildRoute(PrimitiveParameterController.BOOLEAN_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testInteger() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Integer.toString(Integer.MAX_VALUE);
            String route = buildRoute(PrimitiveParameterController.INTEGER_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testDouble() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Double.toString(Double.MAX_VALUE);
            String route = buildRoute(PrimitiveParameterController.DOUBLE_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testString() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String route = buildRoute(PrimitiveParameterController.STRING_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", "Hello")
            }));
            String response = getStringResponse(route);
            Assert.assertEquals("Hello", response);
        }
    }

    @Test
    public void testByte() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Byte.toString(Byte.MAX_VALUE);
            String route = buildRoute(PrimitiveParameterController.BYTE_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testShort() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Short.toString(Short.MAX_VALUE);
            String route = buildRoute(PrimitiveParameterController.SHORT_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testFloat() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Float.toString(Float.MAX_VALUE);
            String route = buildRoute(PrimitiveParameterController.FLOAT_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testChar() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = "a";
            String route = buildRoute(PrimitiveParameterController.CHAR_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }

    @Test
    public void testLong() throws Exception {
        try (AppHost app = AppHost.startNew()) {
            String value = Long.toString(Long.MAX_VALUE);
            String route = buildRoute(PrimitiveParameterController.LONG_ROUTE, MapUtils.putAll(new HashMap<>(), new Pair[] {
                    Pair.of("value", value)
            }));
            String response = getStringResponse(route);
            Assert.assertEquals(value, response);
        }
    }
}