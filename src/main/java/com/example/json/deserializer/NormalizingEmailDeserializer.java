package com.example.json.deserializer;

import java.io.IOException;
import java.text.Normalizer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class NormalizingEmailDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String s = p.getValueAsString();
        if (s == null) {
            return null;
        }
        
        return Normalizer.normalize(s.strip(), Normalizer.Form.NFKC)
                .toLowerCase();
    }

}
