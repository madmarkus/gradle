/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.util;

import org.gradle.api.UncheckedIOException;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Hans Dockter
 */
public class GUtil {
    public static <T extends Collection> T flatten(Object[] elements, T addTo, boolean flattenMaps) {
        return flatten(Arrays.asList(elements), addTo, flattenMaps);
    }

    public static <T extends Collection> T flatten(Object[] elements, T addTo) {
        return flatten(Arrays.asList(elements), addTo);
    }

    public static <T extends Collection> T flatten(Collection elements, T addTo) {
        return flatten(elements, addTo, true);
    }

    public static <T extends Collection> T flatten(Collection elements, T addTo, boolean flattenMaps) {
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof Collection) {
                flatten((Collection) element, addTo, flattenMaps);
            } else if ((element instanceof Map) && flattenMaps) {
                flatten(((Map) element).values(), addTo, flattenMaps);
            } else {
                addTo.add(element);
            }
        }
        return addTo;
    }

    public static List flatten(Collection elements, boolean flattenMaps) {
        return flatten(elements, new ArrayList(), flattenMaps);
    }

    public static List flatten(Collection elements) {
        return flatten(elements, new ArrayList());
    }

    public static String join(Collection self, String separator) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;

        if (separator == null) {
            separator = "";
        }

        for (Object value : self) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(value.toString());
        }
        return buffer.toString();
    }

    public static String join(Object[] self, String separator) {
        return join(Arrays.asList(self), separator);
    }

    public static List<String> prefix(String prefix, Collection<String> strings) {
        List<String> prefixed = new ArrayList<String>();
        for (String string : strings) {
            prefixed.add(prefix + string);
        }
        return prefixed;
    }

    public static boolean isTrue(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Collection) {
            return ((Collection) object).size() > 0;
        } else if (object instanceof String) {
            return ((String) object).length() > 0;
        }
        return true;
    }

    public static <T> T elvis(T object, T defaultValue) {
        return isTrue(object) ? object : defaultValue;
    }

    public static <T> Set<T> addSets(Iterable<? extends T>... sets) {
        HashSet<T> set = new HashSet<T>();
        addToCollection(set, sets);
        return set;
    }

    public static <T> List<T> addLists(Iterable<? extends T>... lists) {
        ArrayList<T> newList = new ArrayList<T>();
        addToCollection(newList, lists);
        return newList;
    }

    public static <T> Collection<T> addToCollection(Collection<T> dest, Iterable<? extends T>... srcs) {
        for (Iterable<? extends T> src : srcs) {
            for (T t : src) {
                dest.add(t);
            }
        }
        return dest;
    }

    public static Map addMaps(Map map1, Map map2) {
        HashMap map = new HashMap();
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }

    public static void addToMap(Map<String, String> dest, Properties src) {
        Enumeration<?> enumeration = src.propertyNames();
        while (enumeration.hasMoreElements()) {
            Object o = enumeration.nextElement();
            dest.put(o.toString(), src.getProperty(o.toString()));
        }
    }

    public static Properties loadProperties(File propertyFile) {
        try {
            return loadProperties(new FileInputStream(propertyFile));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Properties loadProperties(URL url) {
        try {
            return loadProperties(url.openStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Properties loadProperties(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }

    public static void saveProperties(Properties properties, File propertyFile) {
        try {
            FileOutputStream propertiesFileOutputStream = new FileOutputStream(propertyFile);
            properties.store(propertiesFileOutputStream, null);
            propertiesFileOutputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map map(Object... objects) {
        Map map = new HashMap();
        assert objects.length % 2 == 0;
        for (int i = 0; i < objects.length; i += 2) {
            map.put(objects[i], objects[i + 1]);
        }
        return map;
    }

    public static String toString(Iterable<String> names) {
        Formatter formatter = new Formatter();
        boolean first = true;
        for (String name : names) {
            if (first) {
                formatter.format("'%s'", name);
                first = false;
            } else {
                formatter.format(", '%s'", name);
            }
        }
        return formatter.toString();
    }

    /**
     * Converts an arbitrary string to a camel-case string which can be used in a Java identifier. Eg, with_underscores
     * -> withUnderscored
     */
    public static String toCamelCase(CharSequence string) {
        if (string == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Pattern.compile("[^\\w]+").matcher(string);
        int pos = 0;
        while (matcher.find()) {
            builder.append(StringUtils.capitalize(string.subSequence(pos, matcher.start()).toString()));
            pos = matcher.end();
        }
        builder.append(StringUtils.capitalize(string.subSequence(pos, string.length()).toString()));
        return builder.toString();
    }

    /**
     * Converts an arbitrary string to space-separated words. Eg, camelCase -> camel case, with_underscores -> with
     * underscores
     */
    public static String toWords(CharSequence string) {
        if (string == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        boolean inSeparator = false;
        for (; pos < string.length(); pos++) {
            char ch = string.charAt(pos);
            if (Character.isLowerCase(ch)) {
                if (inSeparator && builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(ch);
                inSeparator = false;
            } else if (Character.isUpperCase(ch)) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(Character.toLowerCase(ch));
                inSeparator = false;
            } else {
                inSeparator = true;
            }
        }

        return builder.toString();
    }

    public static byte[] serialize(Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return outputStream.toByteArray();
    }
}
