package com.lion328.xenonlauncher.minecraft.launcher.json.data.type;

import com.google.gson.*;
import com.lion328.xenonlauncher.minecraft.launcher.json.data.DependencyName;

import java.lang.reflect.Type;

public class DependencyNameTypeAdapter implements JsonSerializer<DependencyName>, JsonDeserializer<DependencyName> {

    @Override
    public JsonElement serialize(DependencyName dependencyName, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dependencyName.toString());
    }

    @Override
    public DependencyName deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return new DependencyName(jsonElement.getAsString());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException(e);
        }
    }
}
