/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/A_CmsMessageBundle.java,v $
 * Date   : $Date: 2005/04/22 14:38:35 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.i18n;

import java.util.Locale;

/**
 * Convenience base class to access the localized messages of an OpenCms package.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.7.3
 */
public abstract class A_CmsMessageBundle implements I_CmsMessageBundle {

    /**
     * Returns an array of all messages bundles used by the OpenCms core.<p>
     * 
     * @return an array of all messages bundles used by the OpenCms core
     */
    public static I_CmsMessageBundle[] getOpenCmsMessageBundles() {

        return new I_CmsMessageBundle[] {
            org.opencms.configuration.Messages.get(),
            org.opencms.main.Messages.get(),
            org.opencms.scheduler.Messages.get(),
            org.opencms.validation.Messages.get(),
            org.opencms.util.Messages.get(),
            org.opencms.security.Messages.get(),
            org.opencms.threads.Messages.get(),
            org.opencms.flex.Messages.get()
        };
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#container(java.lang.String)
     */
    public CmsMessageContainer container(String key) {

        return container(key, null);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#container(java.lang.String, java.lang.Object)
     */
    public CmsMessageContainer container(String key, Object arg0) {

        return container(key, new Object[] {arg0});
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#container(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public CmsMessageContainer container(String key, Object arg0, Object arg1) {

        return container(key, new Object[] {arg0, arg1});
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#container(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public CmsMessageContainer container(String key, Object arg0, Object arg1, Object arg2) {

        return container(key, new Object[] {arg0, arg1, arg2});
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#container(java.lang.String, java.lang.Object[])
     */
    public CmsMessageContainer container(String message, Object[] args) {

        return new CmsMessageContainer(this, message, args);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#getBundle()
     */
    public CmsMessages getBundle() {

        Locale locale = CmsLocaleManager.getDefaultLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return getBundle(locale);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#getBundle(java.util.Locale)
     */
    public CmsMessages getBundle(Locale locale) {

        return new CmsMessages(getBundleName(), locale);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#key(java.util.Locale, java.lang.String, java.lang.Object[])
     */
    public String key(Locale locale, String key, Object[] args) {

        return getBundle(locale).key(key, args);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#key(java.lang.String)
     */
    public String key(String key) {

        return key(key, null);
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#key(java.lang.String, java.lang.Object)
     */
    public String key(String key, Object arg0) {

        return key(key, new Object[] {arg0});

    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#key(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public String key(String key, Object arg0, Object arg1) {

        return key(key, new Object[] {arg0, arg1});
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#key(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public String key(String key, Object arg0, Object arg1, Object arg2) {

        return key(key, new Object[] {arg0, arg1, arg2});
    }

    /**
     * @see org.opencms.i18n.I_CmsMessageBundle#key(java.lang.String, java.lang.Object[])
     */
    public String key(String key, Object[] args) {

        return getBundle().key(key, args);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append('[');
        result.append(this.getClass().getName());
        result.append(", bundle: ");
        result.append(getBundle());
        result.append(']');

        return result.toString();
    }
}
