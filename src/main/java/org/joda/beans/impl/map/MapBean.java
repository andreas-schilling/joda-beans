/*
 *  Copyright 2001-2010 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.impl.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.DynamicBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicProperty;

/**
 * Implementation of a fully dynamic {@code Bean} based on an exposed {@code Map}.
 * <p>
 * Properties are dynamic, and can be added and removed at will from the map.
 * The implementation is the {@code Bean}, {@code MetaBean} and data store combined.
 * 
 * @author Stephen Colebourne
 */
public class MapBean extends HashMap<String, Object> implements DynamicBean, MetaBean {
    // TODO: maybe it would be cleaner to create a separate short-lived meta-bean class

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    @Override
    public MetaBean metaBean() {
        return this;
    }

    @Override
    public boolean propertyExists(String name) {
        return metaPropertyExists(name);
    }

    @Override
    public Property<Object> property(String name) {
        return BasicProperty.of(this, metaProperty(name));
    }

    @Override
    public PropertyMap propertyMap() {
        return MapBeanPropertyMap.of(this);
    }

    @Override
    public void propertyDefine(String propertyName, Class<?> propertyType) {
        // no need to define
    }

    @Override
    public void propertyRemove(String propertyName) {
        remove(propertyName);
    }

    //-----------------------------------------------------------------------
    @Override
    public MapBean createBean() {
        return new MapBean();
    }

    @Override
    public PropertyMap createPropertyMap(Bean bean) {
        return MapBeanPropertyMap.of(beanType().cast(bean));
    }

    @Override
    public Class<MapBean> beanType() {
        return MapBean.class;
    }

    @Override
    public String name() {
        return MapBean.class.getName();
    }

    @Override
    public int metaPropertyCount() {
        return size();
    }

    @Override
    public boolean metaPropertyExists(String name) {
        return containsKey(name);
    }

    @Override
    public MetaProperty<Object> metaProperty(String name) {
        Object obj = get(name);
        if (obj == null) {
            throw new NoSuchElementException("Property not found: " + name);
        }
        return MapBeanMetaProperty.of(name);
    }

    @Override
    public Iterable<MetaProperty<Object>> metaPropertyIterable() {
        return new Iterable<MetaProperty<Object>>() {
            private final Iterator<String> it = keySet().iterator();
            @Override
            public Iterator<MetaProperty<Object>> iterator() {
                return new Iterator<MetaProperty<Object>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public MetaProperty<Object> next() {
                        return MapBeanMetaProperty.of(it.next());
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Unmodifiable");
                    }
                    
                };
            }
        };
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
        Map<String, MetaProperty<Object>> map = new HashMap<String, MetaProperty<Object>>();
        for (String name : keySet()) {
            map.put(name, MapBeanMetaProperty.of(name));
        }
        return map;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarises the bean.
     * <p>
     * The string contains the class name and properties.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + super.toString();
    }

}
