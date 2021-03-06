/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.cache;

import org.gradle.CacheUsage;
import org.gradle.util.GFileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DefaultCacheFactory implements CacheFactory {
    private final Map<File, DefaultPersistentDirectoryCache> openCaches
            = new HashMap<File, DefaultPersistentDirectoryCache>();
    
    public PersistentCache open(File cacheDir, CacheUsage usage, Map<String, ?> properties) {
        File canonicalDir = GFileUtils.canonicalise(cacheDir);
        DefaultPersistentDirectoryCache cache = openCaches.get(canonicalDir);
        if (cache == null) {
            cache = new DefaultPersistentDirectoryCache(canonicalDir, usage, properties);
            openCaches.put(canonicalDir, cache);
        }
        else {
            if (!properties.equals(cache.getProperties())) {
                throw new UnsupportedOperationException(String.format(
                        "Cache '%s' is already open with different state.", cacheDir));
            }
        }
        return cache;
    }

    public void close(PersistentCache cache) {
        openCaches.values().remove(cache);
        ((DefaultPersistentDirectoryCache) cache).close();
    }
}
