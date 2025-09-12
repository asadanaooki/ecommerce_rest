package com.example.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.bind.deserializer.TrimToNullDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JsonDeserialize(using = TrimToNullDeserializer.class)
public @interface TrimToNull {

}
