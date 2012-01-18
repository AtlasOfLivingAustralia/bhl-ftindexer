/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that uses reflection to map from a result set to a transfer object
 * 
 * @author baird
 *
 * @param <T>
 */
public class ReflectiveMapper<T> {
    
    private static Map<Class<?>, List<MappingDescriptor>> _mappingsCache = new HashMap<Class<?>, List<MappingDescriptor>>();

    public T map(ResultSet rs, T to) {

        try {
            if (to != null && rs != null && !rs.isAfterLast() && !rs.isBeforeFirst()) {
                
                List<MappingDescriptor> mappings = null;
                
                synchronized (_mappingsCache) {
                  if (!_mappingsCache.containsKey(to.getClass())) {
                      mappings = createMappings(to);
                      _mappingsCache.put(to.getClass(), mappings);                      
                  }
                }
                
                mappings = _mappingsCache.get(to.getClass());
                
                for (MappingDescriptor mapping : mappings) {
                    int colIndex = rs.findColumn(mapping.columnName);
                    Object value = rs.getObject(colIndex);
                    mapping.setter.invoke(to, value);
                }

            }
            return to;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<MappingDescriptor> createMappings(T to) {
        List<MappingDescriptor> mappings = new ArrayList<ReflectiveMapper.MappingDescriptor>();

        Class<?> c = to.getClass();
        Method[] methods = c.getDeclaredMethods();
        for (Method m : methods) {
            if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
                if (m.getName().startsWith("set")) {
                    MappingInfo mapInfo = m.getAnnotation(MappingInfo.class);
                    if (mapInfo != null) {
                        // LogService.log(this.getClass(), "Adding mapping for %s (%s => %s)", to.getClass().getName(), m.getName(), mapInfo.column());
                        mappings.add(new MappingDescriptor(mapInfo.column(), m, m.getReturnType()));
                    }
                }
            }
        }

        return mappings;
    }

    static class MappingDescriptor {

        public MappingDescriptor(String columnName, Method setter, Class<?> columnType) {
            this.columnName = columnName;
            this.setter = setter;
        }

        public String columnName;
        public Method setter;
        public Class<?> columnType;

    }
}
