/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.decorator;

import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.OwbParametrizedTypeImpl;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.ClassUtil;

import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/**
 * Defines decorators. It wraps the bean instance related
 * with decorator class. Actually, each decorator is an instance
 * of the {@link Bean}.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> decorator type info
 *
 * @deprecated replaced by DecoratorBean
 */
public class WebBeansDecoratorRemove<T> extends InjectionTargetBean<T> implements Decorator<T>
{
    /** Decorator class */
    private Class<?> clazz;

    /** Decorates api types */
    private Set<Type> decoratedTypes = new HashSet<Type>();

    /** Delegate field class type */
    protected Type delegateType;
    
    /** The type of this decorator */
    protected Type decoratorGenericType;

    /** Delegate field bindings */
    protected Set<Annotation> delegateBindings = new HashSet<Annotation>();
    
    protected Field delegateField;

    /** Wrapped bean*/
    private InjectionTargetBean<T> wrappedBean;
    
    /**Custom Decorator*/
    private Decorator<T> customDecorator = null;

    private final Set<String> ignoredDecoratorInterfaces;
    
    /**
     * Creates a new decorator bean instance with the given wrapped bean and custom decorator bean.
     * @param wrappedBean wrapped bean instance
     * @param customDecorator custom decorator
     */
    public WebBeansDecoratorRemove(InjectionTargetBean<T> wrappedBean, Decorator<T> customDecorator)
    {
        super(wrappedBean.getWebBeansContext(),
              WebBeansType.DECORATOR,
              wrappedBean.getAnnotatedType(),
              wrappedBean.getTypes(),
              wrappedBean.getQualifiers(),
              Dependent.class,
              wrappedBean.getReturnType(),
              wrappedBean.getStereotypes());
        this.wrappedBean = wrappedBean;
        this.customDecorator = customDecorator;
        ignoredDecoratorInterfaces = getIgnoredDecoratorInterfaces(wrappedBean);
        initDelegate();
    }

    /**
     * Creates a new decorator bean instance with the given wrapped bean.
     * @param wrappedBean wrapped bean instance
     */
    public WebBeansDecoratorRemove(InjectionTargetBean<T> wrappedBean)
    {
        super(wrappedBean.getWebBeansContext(),
              WebBeansType.DECORATOR,
              wrappedBean.getAnnotatedType(),
              wrappedBean.getTypes(),
              wrappedBean.getQualifiers(),
              Dependent.class,
              wrappedBean.getReturnType(),
              wrappedBean.getStereotypes());
        
        this.wrappedBean = wrappedBean;
        clazz = wrappedBean.getReturnType();
        ignoredDecoratorInterfaces = getIgnoredDecoratorInterfaces(wrappedBean);

        init();
    }

    private static <T> Set<String> getIgnoredDecoratorInterfaces(InjectionTargetBean<T> wrappedBean)
    {
        Set<String> result = new HashSet<String>(wrappedBean.getWebBeansContext().getOpenWebBeansConfiguration().getIgnoredInterfaces());
        result.add(Serializable.class.getName());
        return result;
    }

    @Override
    public InjectionTarget<T> getInjectionTarget()
    {
        //X TODO review
        return wrappedBean.getInjectionTarget();
    }

    protected void init()
    {
        Class<?> beanClass = getBeanClass();
        decoratedTypes = new HashSet<Type>(this.getTypes());
        
        /* determine a safe Type for for a later BeanManager.getReference(...) */
        if (ClassUtil.isDefinitionContainsTypeVariables(beanClass)) 
        { 
              OwbParametrizedTypeImpl pt = new OwbParametrizedTypeImpl(beanClass.getDeclaringClass(),beanClass);
              TypeVariable<?>[] tvs = beanClass.getTypeParameters();
              for(TypeVariable<?> tv : tvs)
              {
                  pt.addTypeArgument(tv);
              }
              decoratedTypes.remove(pt);
              setDecoratorGenericType(pt);
        }
        else 
        {               
            decoratedTypes.remove(beanClass);
            setDecoratorGenericType(beanClass);
        }

        /* drop any non-interface bean types */
        Type superClass = beanClass.getGenericSuperclass();
        while (superClass != Object.class) 
        { 
            decoratedTypes.remove(superClass);
            superClass = superClass.getClass().getGenericSuperclass();
        }
        decoratedTypes.remove(Object.class);
        decoratedTypes.remove(java.io.Serializable.class); /* 8.1 */

        
        for (Iterator<Type> i = decoratedTypes.iterator(); i.hasNext(); )
        {
            Type t = i.next();
            if (t instanceof Class<?> && ignoredDecoratorInterfaces.contains(((Class) t).getName()))
            {
                i.remove();
            }
        }

        initDelegate();
    }

    protected void initDelegate()
    {
        Set<InjectionPoint> injectionPoints = getInjectionPoints();
        boolean found = false;
        InjectionPoint ipFound = null;
        for(InjectionPoint ip : injectionPoints)
        {
            if(ip.getAnnotated().isAnnotationPresent(Delegate.class))
            {
                if(!found)
                {
                    found = true;
                    ipFound = ip;                    
                }
                else
                {
                    throw new WebBeansConfigurationException("Decorators must have a one @Delegate injection point. " +
                            "But the decorator bean : " + toString() + " has more than one");
                }
            }            
        }
        
        
        if(ipFound == null)
        {
            throw new WebBeansConfigurationException("Decorators must have a one @Delegate injection point." +
                    "But the decorator bean : " + toString() + " has none");
        }
        
        if(!(ipFound.getMember() instanceof Constructor))
        {
            AnnotatedElement element = (AnnotatedElement)ipFound.getMember();
            if(!element.isAnnotationPresent(Inject.class))
            {
                String message = "Error in decorator : "+ toString() + ". The delegate injection point must be an injected field, " +
                        "initializer method parameter or bean constructor method parameter.";

                throw new WebBeansConfigurationException(message);
            }                
        }
        
        initDelegateInternal(ipFound);
        
    }
    
    @Override
    public boolean isPassivationCapable()
    {
        return wrappedBean.isPassivationCapable();
    }

    private void initDelegateInternal(InjectionPoint ip)
    {
        if(customDecorator != null)
        {
            delegateType = customDecorator.getDelegateType();
            delegateBindings = customDecorator.getDelegateQualifiers();
        }
        else
        {
            delegateType = ip.getType();
            delegateBindings = ip.getQualifiers();
        }
                
        if(ip.getMember() instanceof Field)
        {
            delegateField = (Field)ip.getMember();
        }
        else
        {
            Field[] fields = ClassUtil.getFieldsWithType(wrappedBean.getWebBeansContext(), getReturnType(), delegateType);
            if(fields.length == 0)
            {
                throw new WebBeansConfigurationException("Delegate injection field is not found for decorator : " + toString());
            }
            
            if(fields.length > 1)
            {
                throw new WebBeansConfigurationException("More than one delegate injection field is found for decorator : " + toString());
            }

            delegateField = fields[0];
        }
        
        Type fieldType = delegateField.getGenericType();

        for (Type decType : getDecoratedTypes())
        {
            if (!(ClassUtil.getClass(decType)).isAssignableFrom(ClassUtil.getClass(fieldType)))
            {
                throw new WebBeansConfigurationException("Decorator : " + toString() + " delegate attribute must implement all of the decorator decorated types" + 
                        ", but decorator type " + decType + " is not assignable from delegate type of " + fieldType);
            }
            else
            {
                if(ClassUtil.isParametrizedType(decType) && ClassUtil.isParametrizedType(fieldType))
                {                    
                    if(!fieldType.equals(decType))
                    {
                        throw new WebBeansConfigurationException("Decorator : " + toString() + " generic delegate attribute must be same with decorated type : " + decType);
                    }
                }
            }
        }
    }
    
    public Set<Annotation> getDelegateQualifiers()
    {
        if(customDecorator != null)
        {
            return customDecorator.getDelegateQualifiers();
        }
        
        return delegateBindings;
    }

    public Type getDelegateType()
    {
        if(customDecorator != null)
        {
            return customDecorator.getDelegateType();
        }        
        
        return delegateType;
    }

    public void setDelegate(Object instance, Object delegate)
    {
        if (!delegateField.isAccessible())
        {
            getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(delegateField, true);
        }

        try
        {
            delegateField.set(instance, delegate);

        }
        catch (IllegalArgumentException e)
        {
            getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0007, instance.getClass().getName()), e);
            throw new WebBeansException(e);

        }
        catch (IllegalAccessException e)
        {
            getLogger().log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0015, delegateField.getName(), instance.getClass().getName()), e);
        }

    }

    
    @SuppressWarnings("unchecked")    
    protected  T createInstance(CreationalContext<T> creationalContext)
    {
        if(customDecorator != null)
        {
            return customDecorator.create(creationalContext);
        }

        WebBeansContext webBeansContext = wrappedBean.getWebBeansContext();
        Context context = webBeansContext.getBeanManagerImpl().getContext(getScope());
        Object actualInstance = context.get((Bean<Object>) wrappedBean, (CreationalContext<Object>)creationalContext);
        T proxy = (T) webBeansContext.getProxyFactoryRemove().createDependentScopedBeanProxyRemove(wrappedBean, actualInstance, creationalContext);
        
        return proxy;        
    }

    public Type getDecoratorGenericType() 
    {
        return decoratorGenericType;
    }

    public void setDecoratorGenericType(Type decoratorGenericType) 
    {
        this.decoratorGenericType = decoratorGenericType;
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        if(customDecorator != null)
        {
            return customDecorator.getQualifiers();
        }
        
        return wrappedBean.getQualifiers();
    }

    @Override
    public String getName()
    {
        if(customDecorator != null)
        {
            return customDecorator.getName();
        }
        
        return wrappedBean.getName();
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        if(customDecorator != null)
        {
            return customDecorator.getScope();
        }
        
        return wrappedBean.getScope();
    }

    
    public Set<Type> getTypes()
    {
        if(customDecorator != null)
        {
            return customDecorator.getTypes();
        }
        
        return wrappedBean.getTypes();
    }

    @Override
    public boolean isNullable()
    {
        if(customDecorator != null)
        {
            return customDecorator.isNullable();
        }
        
        return wrappedBean.isNullable();
    }

    public Set<InjectionPoint> getInjectionPoints()
    {
        if(customDecorator != null)
        {
            return customDecorator.getInjectionPoints();
        }
        
        return wrappedBean.getInjectionPoints();
    }


    @Override
    public Class<?> getBeanClass()
    {
        if(customDecorator != null)
        {
            return customDecorator.getBeanClass();
        }
        
        return wrappedBean.getBeanClass();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        if(customDecorator != null)
        {
            return customDecorator.getStereotypes();
        }

        return wrappedBean.getStereotypes();
    }

    public Set<Type> getDecoratedTypes()
    {
        if(customDecorator != null)
        {
            return customDecorator.getDecoratedTypes();
        }

        return decoratedTypes;
    }

    @Override
    public boolean isAlternative()
    {
        if(customDecorator != null)
        {
            return customDecorator.isAlternative();
        }

        return wrappedBean.isAlternative();
    }

    @Override
    public void validatePassivationDependencies()
    {
        wrappedBean.validatePassivationDependencies();
    }

}