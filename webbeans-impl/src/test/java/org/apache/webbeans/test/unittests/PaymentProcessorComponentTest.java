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
package org.apache.webbeans.test.unittests;

import java.util.List;

import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractBean;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.CheckWithMoneyPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.apache.webbeans.test.servlet.TestContext;
import org.junit.Before;
import org.junit.Test;

public class PaymentProcessorComponentTest extends TestContext
{
    BeanManager container = null;

    public PaymentProcessorComponentTest()
    {
        super(PaymentProcessorComponentTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testTypedComponent() throws Throwable
    {
        clear();

        defineSimpleWebBean(CheckWithCheckPayment.class);
        defineSimpleWebBean(CheckWithMoneyPayment.class);
        defineSimpleWebBean(PaymentProcessorComponent.class);

        List<AbstractBean<?>> comps = getComponents();

        ContextFactory.initRequestContext(null);

        Assert.assertEquals(3, comps.size());

        getManager().getInstance(comps.get(0));
        getManager().getInstance(comps.get(1));

        Object object = getManager().getInstance(comps.get(2));
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof PaymentProcessorComponent);

        PaymentProcessorComponent uc = (PaymentProcessorComponent) object;
        IPayment p = uc.getPaymentCheck();

        Assert.assertTrue(p instanceof CheckWithCheckPayment);
        Assert.assertEquals("CHECK", p.pay());

        p = uc.getPaymentMoney();

        Assert.assertTrue(p instanceof CheckWithMoneyPayment);

        Assert.assertEquals("MONEY", p.pay());

        ContextFactory.destroyRequestContext(null);
    }

}
