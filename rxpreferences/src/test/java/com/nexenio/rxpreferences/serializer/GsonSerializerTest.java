package com.nexenio.rxpreferences.serializer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GsonSerializerTest {

    private Serializer serializer;

    @Before
    public void setUp() {
        serializer = new GsonSerializer();
    }

    @Test
    public void serializeToString_boolean_emitsString() {
        serializer.serializeToString(true)
                .test()
                .assertValue("true")
                .assertComplete();
    }

    @Test
    public void serializeToString_object_emitsString() {
        serializer.serializeToString(new ExampleObject(1, "foo"))
                .test()
                .assertValue("{\"number\":1,\"text\":\"foo\"}")
                .assertComplete();
    }

    @Test
    public void deserializeFromString_booleanString_emitsValue() {
        serializer.deserializeFromString("true", Boolean.class)
                .test()
                .assertValue(true)
                .assertComplete();
    }

    @Test
    public void deserializeFromString_listString_emitsValue() {
        serializer.deserializeFromString("[1,2,3]", ExampleList.class)
                .test()
                .assertValue(integers -> integers.size() == 3)
                .assertComplete();
    }

    @Test
    public void deserializeFromString_objectString_emitsValue() {
        serializer.deserializeFromString("{\"number\":1,\"text\":\"foo\"}", ExampleObject.class)
                .test()
                .assertValue(object -> {
                    assertEquals(1, object.number);
                    assertEquals("foo", object.text);
                    return true;
                })
                .assertComplete();
    }

    @Test
    public void deserializeFromString_invalidString_emitsError() {
        serializer.deserializeFromString("foo", Integer.class)
                .test()
                .assertError(SerializerException.class);
    }

    private static class ExampleList extends ArrayList<Integer> {

    }

    private static class ExampleObject {

        protected int number;
        protected String text;

        public ExampleObject(int number, String text) {
            this.number = number;
            this.text = text;
        }

    }

}