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
package org.apache.webbeans.portable.events;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

/**
 * Default implementation of the {@link javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType}.
 *
 * @param <X> bean class info
 */
public class ProcessSyntheticAnnotatedTypeImpl<X> extends EventBase implements ProcessSyntheticAnnotatedType<X>
{

    private Extension source;

    /**Annotated Type*/
    private AnnotatedType<X> annotatedType = null;

    /**veto or not*/
    private boolean veto = false;

    /**
     * This field gets set to <code>true</code> when a custom AnnotatedType
     * got set in an Extension. In this case we must now take this modified
     * AnnotatedType for our further processing!
     */
    private boolean modifiedAnnotatedType = false;


    public ProcessSyntheticAnnotatedTypeImpl(AnnotatedType<X> annotatedType, Extension source)
    {
        this.annotatedType = annotatedType;
        this.source = source;
    }

    @Override
    public Extension getSource()
    {
        checkState();
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotatedType<X> getAnnotatedType()
    {
        checkState();
        return annotatedType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAnnotatedType(AnnotatedType<X> type)
    {
        checkState();
        annotatedType = type;
        modifiedAnnotatedType = true;
    }

    /**
     * Returns sets or not.
     *
     * @return set or not
     */
    public boolean isModifiedAnnotatedType()
    {
        return modifiedAnnotatedType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void veto()
    {
        checkState();
        veto = true;
    }

    //X TODO OWB-1182 CDI 2.0
    @Override
    public AnnotatedTypeConfigurator<X> configureAnnotatedType()
    {
        throw new UnsupportedOperationException("CDI 2.0 not yet imlemented");
    }

    /**
     * Returns veto status.
     *
     * @return veto status
     */
    public boolean isVeto()
    {
        return veto;
    }

}
