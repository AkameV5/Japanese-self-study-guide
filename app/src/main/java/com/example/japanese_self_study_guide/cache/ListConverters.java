package com.example.japanese_self_study_guide.cache;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ListConverters {

    private static final Gson GSON = new Gson();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    private static final Type INTEGER_LIST_TYPE = new TypeToken<List<Integer>>() {}.getType();

    @TypeConverter
    public static String fromStringList(List<String> value) {
        return value == null ? null : GSON.toJson(value, STRING_LIST_TYPE);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null || value.isEmpty()) return Collections.emptyList();
        return GSON.fromJson(value, STRING_LIST_TYPE);
    }

    @TypeConverter
    public static String fromIntegerList(List<Integer> value) {
        return value == null ? null : GSON.toJson(value, INTEGER_LIST_TYPE);
    }

    @TypeConverter
    public static List<Integer> toIntegerList(String value) {
        if (value == null || value.isEmpty()) return Collections.emptyList();
        return GSON.fromJson(value, INTEGER_LIST_TYPE);
    }
}
