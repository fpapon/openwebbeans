/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package org.apache.webbeans.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Set;

import javax.context.ScopeType;
import javax.decorator.Decorator;
import javax.interceptor.Interceptor;

import org.apache.webbeans.component.ComponentImpl;
import org.apache.webbeans.component.ProducerComponentImpl;
import org.apache.webbeans.component.ProducerFieldComponent;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.container.ManagerImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.intercept.InterceptorUtil;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Configures Simple WebBeans Component.
 * <p>
 * Contains useful static methods for creating Simple WebBeans Components.
 * </p>
 * 
 * @version $Rev$ $Date$
 */
public final class SimpleWebBeansConfigurator
{
    /**
     * Private constructor.
     */
    private SimpleWebBeansConfigurator()
    {

    }

    /**
     * Checks the implementation class for checking conditions.
     * 
     * @param clazz implementation class
     * @throws WebBeansConfigurationException if any configuration exception occurs
     */
    public static void checkSimpleWebBeanCondition(Class<?> clazz) throws WebBeansConfigurationException
    {
        int modifier = clazz.getModifiers();

        if (AnnotationUtil.isAnnotationExistOnClass(clazz, Decorator.class) && AnnotationUtil.isAnnotationExistOnClass(clazz, Interceptor.class))
        {
            throw new WebBeansConfigurationException("Simple WebBean Component implementation class : " + clazz.getName() + " may not annotated with both @Interceptor and @Decorator annotation");
        }

        if (!AnnotationUtil.isAnnotationExistOnClass(clazz, Decorator.class) && !AnnotationUtil.isAnnotationExistOnClass(clazz, Interceptor.class))
        {
            InterceptorUtil.checkSimpleWebBeansInterceptorConditions(clazz);
        }

        if (ClassUtil.isInterface(modifier))
        {
            throw new WebBeansConfigurationException("Simple WebBean Component implementation class : " + clazz.getName() + " may not defined as interface");
        }
    }

    /**
     * Returns true if this class can be candidate for simple web bean, false otherwise.
     * 
     * @param clazz implementation class
     * @return true if this class can be candidate for simple web bean
     * @throws WebBeansConfigurationException if any configuration exception occurs
     */
    public static boolean isSimpleWebBean(Class<?> clazz) throws WebBeansConfigurationException
    {
        try
        {
            WebBeansUtil.isSimpleWebBeanClass(clazz);

        }
        catch (WebBeansConfigurationException e)
        {
            return false;
        }

        return true;
    }

    /**
     * Returns the newly created Simple WebBean Component.
     * 
     * @param clazz Simple WebBean Component implementation class
     * @return the newly created Simple WebBean Component
     * @throws WebBeansConfigurationException if any configuration exception occurs
     */
    public static <T> ComponentImpl<T> define(Class<T> clazz, WebBeansType type) throws WebBeansConfigurationException
    {
        ManagerImpl manager = ManagerImpl.getManager();

        checkSimpleWebBeanCondition(clazz);

        ComponentImpl<T> component = new ComponentImpl<T>(clazz, type);

        DefinitionUtil.defineSerializable(component);
        DefinitionUtil.defineStereoTypes(component, clazz.getDeclaredAnnotations());

        Class<? extends Annotation> deploymentType = DefinitionUtil.defineDeploymentType(component, clazz.getDeclaredAnnotations(), "There are more than one @DeploymentType annotation in Simple WebBean Component implementation class : " + component.getReturnType().getName());

        // Check if the deployment type is enabled.
        if (!WebBeansUtil.isDeploymentTypeEnabled(deploymentType))
        {
            return null;
        }

        Annotation[] clazzAnns = clazz.getDeclaredAnnotations();

        DefinitionUtil.defineApiTypes(component, clazz);
        DefinitionUtil.defineScopeType(component, clazzAnns, "Simple WebBean Component implementation class : " + clazz.getName() + " stereotypes must declare same @ScopeType annotations");
        WebBeansUtil.checkPassivationScope(component, component.getScopeType().getAnnotation(ScopeType.class));
        DefinitionUtil.defineBindingTypes(component, clazzAnns);
        DefinitionUtil.defineName(component, clazzAnns, WebBeansUtil.getSimpleWebBeanDefaultName(clazz.getSimpleName()));

        Constructor<T> constructor = WebBeansUtil.defineConstructor(clazz);
        component.setConstructor(constructor);
        DefinitionUtil.addConstructorInjectionPointMetaData(component, constructor);

        WebBeansUtil.checkSteroTypeRequirements(component, clazz.getDeclaredAnnotations(), "Simple WebBean Component implementation class : " + clazz.getName());

        Set<ProducerComponentImpl<?>> producerComponents = DefinitionUtil.defineProducerMethods(component);
        manager.getBeans().addAll(producerComponents);

        Set<ProducerFieldComponent<?>> producerFields = DefinitionUtil.defineProduerFields(component);
        manager.getBeans().addAll(producerFields);

        DefinitionUtil.defineDisposalMethods(component);
        DefinitionUtil.defineInjectedFields(component);
        DefinitionUtil.defineInjectedMethods(component);
        DefinitionUtil.defineObserverMethods(component, clazz);

        return component;
    }
}