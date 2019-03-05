package com.renzobiz.simpletodo;

import java.util.Date;
import java.util.UUID;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static UUID fromStringId(String id) {
        return id == null ? null : UUID.fromString(id);
    }

    @TypeConverter
    public static String UUIDtoString(UUID id) {
        return id == null ? null : id.toString();
    }
}
