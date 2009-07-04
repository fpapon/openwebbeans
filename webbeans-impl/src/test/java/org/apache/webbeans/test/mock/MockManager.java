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
package org.apache.webbeans.test.mock;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.el.ELResolver;
import javax.enterprise.context.ScopeType;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observer;
import javax.enterprise.inject.TypeLiteral;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;

import org.apache.webbeans.component.AbstractBean;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.activity.ActivityManager;

public class MockManager implements BeanManager
{
    private BeanManagerImpl manager = null;

    private List<AbstractBean<?>> componentList = new ArrayList<AbstractBean<?>>();

    public MockManager()
    {
        this.manager = new BeanManagerImpl();
        ActivityManager.getInstance().setRootActivity(this.manager);
    }


    public void clear()
    {
        componentList.clear();        
        
        this.manager = new BeanManagerImpl();        
     
        ActivityManager.getInstance().setRootActivity(this.manager);        
    }

    public List<AbstractBean<?>> getComponents()
    {
        return componentList;
    }

    public AbstractBean<?> getComponent(int i)
    {
        return componentList.get(i);
    }

    public int getDeployedCompnents()
    {
        return manager.getBeans().size();
    }

    public BeanManager addBean(Bean<?> bean)
    {
        manager.addBean(bean);
        return this;
    }

    public BeanManager addContext(Context context)
    {
        return manager.addContext(context);
    }

    public BeanManager addDecorator(Decorator<?> decorator)
    {
        return manager.addDecorator(decorator);
    }

    public BeanManager addInterceptor(Interceptor<?> interceptor)
    {
        manager.addInterceptor(interceptor);
        return this;
    }

    public <T> BeanManager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
    {
        manager.addObserver(observer, eventType, bindings);
        return this;
    }

    public <T> BeanManager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
    {
        manager.addObserver(observer, eventType, bindings);
        return this;
    }

    public void fireEvent(Object event, Annotation... bindings)
    {
        manager.fireEvent(event, bindings);

    }

    public Context getContext(Class<? extends Annotation> scopeType)
    {
        return manager.getContext(scopeType);
    }

    public <T> T getInstanceToInject(InjectionPoint injectionPoint, CreationalContext<?> context)
    {
        return manager.<T>getInstanceToInject(injectionPoint, context); //X ugly <T> due to javac bug 6302954
    }
    
    public Object getInstanceToInject(InjectionPoint injectionPoint)
    {
       return manager.getInstanceToInject(injectionPoint);
        
    }    
    
    public <T> T getInstance(Bean<T> bean)
    {
        return manager.getInstance(bean);
    }

    public Object getInstanceByName(String name)
    {
        return manager.getInstanceByName(name);
    }

    public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes)
    {
        return manager.getInstanceByType(type, bindingTypes);
    }

    public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindingTypes)
    {
        return manager.getInstanceByType(type, bindingTypes);
    }

    public <T> BeanManager removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
    {
        return manager.removeObserver(observer, eventType, bindings);
    }

    public <T> BeanManager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
    {
        return manager.removeObserver(observer, eventType, bindings);
    }

    public Set<Bean<?>> resolveByName(String name)
    {
        return manager.resolveByName(name);
    }

    public Set<Bean<?>> resolveByType(Class<?> apiType, Annotation... bindingTypes)
    {
        return manager.resolveByType(apiType, bindingTypes);
    }

    public Set<Bean<?>> resolveByType(TypeLiteral<?> apiType, Annotation... bindingTypes)
    {
        return manager.resolveByType(apiType, bindingTypes);
    }

    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... bindingTypes)
    {
        return manager.resolveDecorators(types, bindingTypes);
    }

    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
    {
        return manager.resolveInterceptors(type, interceptorBindings);
    }

    public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
    {
        return manager.resolveObservers(event, bindings);
    }

    public BeanManager parse(InputStream xmlStream)
    {
        manager.parse(xmlStream);
        return manager;
    }

    public BeanManager createActivity()
    {
        return manager.createActivity();
    }

    public BeanManager setCurrent(Class<? extends Annotation> scopeType)
    {
        return manager.setCurrent(scopeType);
    }


    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type)
    {
        return this.manager.createAnnotatedType(type);
    }


    @Override
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual)
    {
        return this.manager.createCreationalContext(contextual);
    }


    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... bindings)
    {
        return this.manager.getBeans();
    }


    @Override
    public Set<Bean<?>> getBeans(String name)
    {
        return this.manager.getBeans(name);
    }


    @Override
    public ELResolver getELResolver()
    {
        return this.manager.getELResolver();
    }


    @Override
    public Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> ctx)
    {
        return this.manager.getInjectableReference(injectionPoint, ctx);
    }


    @Override
    public Set<Annotation> getInterceptorBindingTypeDefinition(Class<? extends Annotation> bindingType)
    {
        return this.manager.getInterceptorBindingTypeDefinition(bindingType);
    }


    @Override
    public <X> Bean<? extends X> getMostSpecializedBean(Bean<X> bean)
    {
        return this.manager.getMostSpecializedBean(bean);
    }


    @Override
    public Bean<?> getPassivationCapableBean(String id)
    {
        return this.manager.getPassivationCapableBean(id);
    }


    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx)
    {
        return this.manager.getReference(bean, beanType, ctx);
    }


    @Override
    public ScopeType getScopeDefinition(Class<? extends Annotation> scopeType)
    {
        return this.manager.getScopeDefinition(scopeType);
    }


    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype)
    {
        return this.manager.getStereotypeDefinition(stereotype);
    }


    @Override
    public boolean isBindingType(Class<? extends Annotation> annotationType)
    {
        return this.manager.isBindingType(annotationType);
    }


    @Override
    public boolean isInterceptorBindingType(Class<? extends Annotation> annotationType)
    {
        return this.manager.isInterceptorBindingType(annotationType);
    }


    @Override
    public boolean isScopeType(Class<? extends Annotation> annotationType)
    {
        return this.manager.isScopeType(annotationType);
    }


    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType)
    {
        return this.manager.isStereotype(annotationType);
    }


    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans)
    {
        return this.manager.resolve(beans);
    }


    @Override
    public void validate(InjectionPoint injectionPoint)
    {
        this.manager.validate(injectionPoint);
        
    }


    @Override
    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type)
    {
        return this.manager.createInjectionTarget(type);
    }


    @Override
    public <T> Set<ObserverMethod<?, T>> resolveObserverMethods(T event, Annotation... bindings)
    {
        return this.manager.resolveObserverMethods(event, bindings);
    }
}
