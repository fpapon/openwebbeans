/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Set;

import javax.context.ScopeType;
import javax.inject.AmbiguousDependencyException;
import javax.inject.UnsatisfiedDependencyException;
import javax.inject.manager.Bean;

import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansUtil;

public final class ResolutionUtil
{
    private ResolutionUtil()
    {

    }

    public static void resolveByTypeConditions(ParameterizedType type)
    {
        Asserts.assertNotNull(type, "type parameter can not be null");
        boolean result = ClassUtil.checkParametrizedType(type);

        if (!result)
        {
            throw new IllegalArgumentException("Parametrized type : " + type + " can not contain type variable or wildcard type arguments");
        }
    }

    public static void getInstanceByTypeConditions(Annotation[] bindingTypes)
    {
        AnnotationUtil.checkBindingTypeConditions(bindingTypes);
    }

    public static <T> void checkResolvedBeans(Set<Bean<T>> resolvedSet, Class<?> type, Annotation...bindingTypes)
    {
        if (resolvedSet.isEmpty())
        {
            StringBuffer message = new StringBuffer("Api type [" + type.getName() + "] is not found with the binding types [");
            
            int i = 0;
            for(Annotation annot : bindingTypes)
            {
                i++;
                
                message.append(annot);
                
                if(i != bindingTypes.length)
                {
                    message.append(",");   
                }
            }
            
            message.append("]");
            
            throw new UnsatisfiedDependencyException(message.toString());
        }

        if (resolvedSet.size() > 1)
        {
            throw new AmbiguousDependencyException("There is more than one api type with : " + type.getName());
        }

        Bean<T> bean = resolvedSet.iterator().next();
        WebBeansUtil.checkUnproxiableApiType(bean, bean.getScopeType().getAnnotation(ScopeType.class));

    }
}
