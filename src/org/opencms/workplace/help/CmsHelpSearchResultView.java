/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/help/CmsHelpSearchResultView.java,v $
 * Date   : $Date: 2008/02/27 12:05:53 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.help;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Displays the result of a <code>{@link org.opencms.search.CmsSearch}</code>.<p>
 * 
 * Requires the following request parameters (see constructor): 
 * <ul>
 *  <li>
 *  index:<br>the String identifying the required search index. 
 *  <li>
 *  query:<br>the search query to run.
 * </ul>
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 6.0.0
 */
public class CmsHelpSearchResultView {

    /** The project that forces evaluation of all dynamic content. */
    protected CmsProject m_offlineProject;

    /** The project that allows static export. */
    protected CmsProject m_onlineProject;

    /** The flag that decides wethter links to search result point to their exported version or not. */
    private boolean m_exportLinks = false;

    /**
     * A small cache for the forms generated by <code>{@link #toPostParameters(String)}</code> during a request. <p>
     * 
     * Avoids duplicate forms.</p>  
     * 
     */
    private SortedMap m_formCache;

    /** The CmsJspActionElement to use. **/
    private CmsJspActionElement m_jsp;

    /** The url to a proprietary search url (different from m_jsp.getRequestContext().getUri). **/
    private String m_searchRessourceUrl;

    /**
     * Constructor with the action element to use. <p>
     * 
     * @param action the action element to use
     */
    public CmsHelpSearchResultView(CmsJspActionElement action) {

        m_jsp = action;
        this.m_formCache = new TreeMap();
        try {
            m_onlineProject = m_jsp.getCmsObject().readProject(CmsProject.ONLINE_PROJECT_ID);
            m_offlineProject = m_jsp.getRequestContext().currentProject();
        } catch (CmsException e) {
            // failed to get online project, at least avoid NPE
            m_onlineProject = m_offlineProject;
        }

    }

    /**
     * Constructor with arguments for construction of a <code>{@link CmsJspActionElement}</code>. <p>
     * 
     * @param pageContext the page context to use 
     * @param request the current request
     * @param response the current response
     */
    public CmsHelpSearchResultView(PageContext pageContext, HttpServletRequest request, HttpServletResponse response) {

        this(new CmsJspActionElement(pageContext, request, response));

    }

    /**
     * Returns the formatted search results.<p>
     * 
     * @param search the preconfigured search bean 
     * @return the formatted search results
     */
    public String displaySearchResult(CmsSearch search) {

        initSearch(search);
        StringBuffer result = new StringBuffer(800);
        CmsMessages messages = org.opencms.search.Messages.get().getBundle(m_jsp.getRequestContext().getLocale());

        result.append("<h1>\n");
        result.append(messages.key(org.opencms.search.Messages.GUI_HELP_SEARCH_RESULT_TITLE_0));
        result.append("\n</h1>\n");
        List searchResult;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(search.getQuery())) {
            search.setQuery("");
            searchResult = new ArrayList();
        } else {
            searchResult = search.getSearchResult();
        }

        HttpServletRequest request = m_jsp.getRequest();
        // get the action to perform from the request
        String action = request.getParameter("action");

        if ((action != null) && (searchResult == null)) {
            result.append("<p class=\"formerror\">\n");
            if (search.getLastException() != null) {

                result.append(messages.key(org.opencms.search.Messages.GUI_HELP_SEARCH_UNAVAILABLE_0));
                result.append("\n<!-- ").append(search.getLastException().toString());
                result.append(" //-->\n");
            } else {
                result.append(messages.key(org.opencms.search.Messages.GUI_HELP_SEARCH_NOMATCH_1, search.getQuery()));
                result.append("\n");
            }
            result.append("</p>\n");
        } else if ((action != null) && (searchResult.size() <= 0)) {
            result.append("<p class=\"formerror\">\n");
            result.append(messages.key(org.opencms.search.Messages.GUI_HELP_SEARCH_NOMATCH_1, search.getQuery()));
            result.append("\n");
            result.append("</p>\n");
        } else if ((action != null) && (searchResult.size() > 0)) {
            result.append("<p>\n");
            result.append(messages.key(org.opencms.search.Messages.GUI_HELP_SEARCH_RESULT_START_0));
            result.append("\n");
            result.append("</p>\n<p>\n");

            Iterator iterator = searchResult.iterator();

            try {
                if (m_exportLinks) {
                    m_jsp.getRequestContext().setCurrentProject(m_onlineProject);
                }

                while (iterator.hasNext()) {
                    CmsSearchResult entry = (CmsSearchResult)iterator.next();
                    result.append("\n<div class=\"searchResult\"><a class=\"navhelp\" href=\"");

                    result.append(m_jsp.link(new StringBuffer(
                        "/system/modules/org.opencms.workplace.help/jsptemplates/help_body.jsp?helpresource=").append(
                        m_jsp.getRequestContext().removeSiteRoot(entry.getPath())).append("&").append(
                        CmsLocaleManager.PARAMETER_LOCALE).append("=").append(m_jsp.getRequestContext().getLocale()).toString()));
                    result.append("\">\n");
                    result.append(entry.getField(CmsSearchField.FIELD_TITLE));
                    result.append("</a>");
                    result.append("&nbsp;(").append(entry.getScore()).append("&nbsp;%)\n");
                    result.append("<span class=\"searchExcerpt\">\n");
                    result.append(entry.getExcerpt()).append('\n');
                    result.append("</span>\n");
                    result.append("</div>\n");
                }
            } finally {
                m_jsp.getRequestContext().setCurrentProject(m_offlineProject);
            }

            result.append("</p>\n");

            // search page links below results
            if ((search.getPreviousUrl() != null) || (search.getNextUrl() != null)) {
                result.append("<p>");
                if (search.getPreviousUrl() != null) {

                    result.append("<a href=\"");
                    result.append(getSearchPageLink(m_jsp.link(new StringBuffer(search.getPreviousUrl()).append('&').append(
                        CmsLocaleManager.PARAMETER_LOCALE).append("=").append(m_jsp.getRequestContext().getLocale()).toString())));
                    result.append("\">");
                    result.append(messages.key(org.opencms.search.Messages.GUI_HELP_BUTTON_BACK_0));
                    result.append(" &lt;&lt;</a>&nbsp;&nbsp;\n");
                }
                Map pageLinks = search.getPageLinks();
                Iterator i = pageLinks.keySet().iterator();
                while (i.hasNext()) {
                    int pageNumber = ((Integer)i.next()).intValue();

                    result.append(" ");
                    if (pageNumber != search.getSearchPage()) {
                        result.append("<a href=\"").append(
                            getSearchPageLink(m_jsp.link(new StringBuffer(
                                (String)pageLinks.get(new Integer(pageNumber))).append('&').append(
                                CmsLocaleManager.PARAMETER_LOCALE).append("=").append(
                                m_jsp.getRequestContext().getLocale()).toString())));
                        result.append("\" target=\"_self\">").append(pageNumber).append("</a>\n");
                    } else {
                        result.append(pageNumber);
                    }
                }
                if (search.getNextUrl() != null) {
                    result.append("&nbsp;&nbsp;&nbsp;<a href=\"");
                    result.append(getSearchPageLink(new StringBuffer(m_jsp.link(search.getNextUrl())).append('&').append(
                        CmsLocaleManager.PARAMETER_LOCALE).append("=").append(m_jsp.getRequestContext().getLocale()).toString()));
                    result.append("\">&gt;&gt;");
                    result.append(messages.key(org.opencms.search.Messages.GUI_HELP_BUTTON_NEXT_0));
                    result.append("</a>\n");
                }
                result.append("</p>\n");
            }

        }

        // include the post forms for the page links: 
        Iterator values = m_formCache.values().iterator();
        while (values.hasNext()) {
            result.append(values.next());
        }
        return result.toString();

    }

    /**
     * Returns true if the links to search results shall point to exported content, false else. <p>
     * @return true if the links to search results shall point to exported content, false else
     */
    public boolean isExportLinks() {

        return m_exportLinks;
    }

    /**
     * Set wether the links to search results point to exported content or not. <p>
     * 
     * @param exportLinks The value that decides Set wether the links to search results point to exported content or not. 
     */
    public void setExportLinks(boolean exportLinks) {

        this.m_exportLinks = exportLinks;
    }

    /**
     * Set a proprietary resource uri for the search page. <p>
     * 
     * This is optional but allows to override the standard search result links 
     * (for next or previous pages) that point to 
     * <code>getJsp().getRequestContext().getUri()</code> whenever the search 
     * uri is element of some template and should not be linked directly.<p>
     * 
     * @param uri the proprietary resource uri for the search page
     */
    public void setSearchRessourceUrl(String uri) {

        this.m_searchRessourceUrl = uri;
    }

    /**
     * Returns the resource uri to the search page with respect to the 
     * optionally configured value <code>{@link #setSearchRessourceUrl(String)}</code> 
     * with the request parameters of the given argument.<p>
     * 
     * This is a workaround for Tomcat bug 35775 
     * (http://issues.apache.org/bugzilla/show_bug.cgi?id=35775). After it has been 
     * fixed the version 1.1 should be restored (or at least this codepath should be switched back.<p>
     * 
     * 
     * @param link the suggestion of the search result bean ( a previous, next or page number url)
     * @return the resource uri to the search page with respect to the 
     *         optionally configured value <code>{@link #setSearchRessourceUrl(String)}</code> 
     *         with the request parameters of the given argument
     */
    private String getSearchPageLink(String link) {

        if (m_searchRessourceUrl != null) {
            // for the form to generate we need params. 
            String pageParams = "";
            int paramIndex = link.indexOf('?');
            if (paramIndex > 0) {
                pageParams = link.substring(paramIndex);
            }
            String formurl = new StringBuffer(m_searchRessourceUrl).append(pageParams).toString();
            toPostParameters(formurl);

            link = new StringBuffer("javascript:document.forms['").append("form").append(m_formCache.size() - 1).append(
                "'].submit()").toString();
        }
        return link;
    }

    private void initSearch(CmsSearch search) {

        //  Collect the objects required to access the OpenCms VFS from the request
        CmsObject cmsObject = m_jsp.getCmsObject();

        ServletRequest request = m_jsp.getRequest();
        search.init(cmsObject);
        search.setField(new String[] {"title", "keywords", "description", "content"});
        search.setMatchesPerPage(5);
        search.setDisplayPages(7);
        search.setSearchRoot(new StringBuffer("/system/workplace/locales/").append(
            m_jsp.getRequestContext().getLocale()).append("/help/").toString());

        String query = request.getParameter("query");
        search.setQuery(query);
    }

    /**
     * Generates a html form (named form&lt;n&gt;) with parameters found in 
     * the given GET request string (appended params with "?value=param&value2=param2). 
     * &gt;n&lt; is the number of forms that already have been generated. 
     * This is a content-expensive bugfix for http://issues.apache.org/bugzilla/show_bug.cgi?id=35775 
     * and should be replaced with the 1.1 revision as soon that bug is fixed.<p>
     * 
     * The forms action will point to the given uri's path info part: All links in the page 
     * that includes this generated html and that are related to the get request should 
     * have "src='#'" and "onclick=documents.forms['&lt;getRequestUri&gt;'].submit()". <p>
     * 
     * The generated form lands in the internal <code>Map {@link #m_formCache}</code> as mapping 
     * from uris to Strings and has to be appended to the output at a valid position. <p> 
     * 
     * Warning: Does not work with multiple values mapped to one parameter ("key = value1,value2..").<p>
     * 
     * 
     * 
     * @param getRequestUri a request uri with optional query, will be used as name of the form too.
     */
    private void toPostParameters(String getRequestUri) {

        StringBuffer result;
        if (!m_formCache.containsKey(getRequestUri)) {

            result = new StringBuffer();
            int index = getRequestUri.indexOf('?');
            String query = "";
            String path = "";
            if (index > 0) {
                query = getRequestUri.substring(index + 1);
                path = getRequestUri.substring(0, index);
                result.append("\n<form method=\"post\" name=\"form").append(m_formCache.size()).append("\" action=\"");
                result.append(path).append("\">\n");

                // "key=value" pairs as tokens:
                StringTokenizer entryTokens = new StringTokenizer(query, "&", false);
                while (entryTokens.hasMoreTokens()) {
                    StringTokenizer keyValueToken = new StringTokenizer(entryTokens.nextToken(), "=", false);
                    if (keyValueToken.countTokens() != 2) {
                        continue;
                    }
                    // Undo the possible already performed url encoding for the given url 
                    String key = CmsEncoder.decode(keyValueToken.nextToken());
                    String value = CmsEncoder.decode(keyValueToken.nextToken());
                    result.append("  <input type=\"hidden\" name=\"");
                    result.append(key).append("\" value=\"");
                    result.append(value).append("\" />\n");
                }
                result.append("</form>\n");
                m_formCache.put(getRequestUri, result.toString());
            }
        }
    }
}