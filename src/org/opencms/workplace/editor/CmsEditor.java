/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditor.java,v $
 * Date   : $Date: 2004/05/03 11:47:39 $
 * Version: $Revision: 1.34 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editor;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;

/**
 * Provides basic methods for building the file editors of OpenCms.<p> 
 * 
 * The editor classes have to extend this class and implement action methods for common editor actions.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.34 $
 * 
 * @since 5.1.12
 */
public abstract class CmsEditor extends CmsDialog {

    /** Value for the action: change the body */
    public static final int ACTION_CHANGE_BODY = 124;

    /** Value for the action: exit */
    public static final int ACTION_EXIT = 122;

    /** Value for the action: show a preview */
    public static final int ACTION_PREVIEW = 126;
    
    /** Value for the action: save */
    public static final int ACTION_SAVE = 121;

    /** Value for the action: save and exit */
    public static final int ACTION_SAVEEXIT = 123;

    /** Value for the action: show the editor */
    public static final int ACTION_SHOW = 125;

    /** Value for the action: an error occured */
    public static final int ACTION_SHOW_ERRORMESSAGE = 127;
    
    /** Stores the VFS editor path */
    public static final String C_PATH_EDITORS = C_PATH_WORKPLACE + "editors/";

    /** Value for the action parameter: change the element */
    public static final String EDITOR_CHANGE_ELEMENT = "changeelement";

    /** Value for the action parameter: exit editor */
    public static final String EDITOR_EXIT = "exit";

    /** Value for the action parameter: show a preview */
    public static final String EDITOR_PREVIEW = "preview";
    
    /** Value for the action parameter: save content */
    public static final String EDITOR_SAVE = "save";

    /** Value for the action parameter: save and exit */
    public static final String EDITOR_SAVEEXIT = "saveexit";

    /** Value for the action parameter: show the editor */
    public static final String EDITOR_SHOW = "show";

    /** Value for the action parameter: an error occured */
    public static final String EDITOR_SHOW_ERRORMESSAGE = "error";
    
    /** Parameter name for the request parameter "backlink" */
    public static final String PARAM_BACKLINK = "backlink";
    
    /** Parameter name for the request parameter "content" */
    public static final String PARAM_CONTENT = "content";
    
    /** Parameter name for the request parameter "directedit" */
    public static final String PARAM_DIRECTEDIT = "directedit";
    
    /** Parameter name for the request parameter "editastext" */
    public static final String PARAM_EDITASTEXT = "editastext";
    
    /** Parameter name for the request parameter "editormode" */
    public static final String PARAM_EDITORMODE = "editormode";
    
    /** Parameter name for the request parameter "loaddefault" */
    public static final String PARAM_LOADDEFAULT = "loaddefault";
    
    /** Parameter name for the request parameter "tempfile" */
    public static final String PARAM_TEMPFILE = "tempfile";
    
    // some private members for parameter storage
    private String m_paramBackLink;
    private String m_paramContent;
    private String m_paramDirectedit;
    private String m_paramEditAsText;
    private String m_paramEditormode;
    private String m_paramLoadDefault;
    private String m_paramTempFile;
    
    /** Helper variable to store the uri to the editors pictures */
    private String m_picsUri = null;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditor(CmsJspActionElement jsp) {
        super(jsp);
    }
        
    /**
     * Performs the exit editor action.<p>
     * 
     * @throws CmsException if something goes wrong
     * @throws IOException if a redirection fails
     * @throws JspException if including an element fails
     */
    public abstract void actionExit() throws CmsException, IOException, JspException;
    
    /**
     * Performs the save content action.<p>
     * 
     * @throws CmsException if something goes wrong
     * @throws IOException if a redirection fails
     * @throws JspException if including an element fails
     */
    public abstract void actionSave() throws CmsException, IOException, JspException;
    
    /**
     * Generates a button for the OpenCms editor.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automattically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * @param useCustomImage if true, the button has to be placed in the editors "custom pics" folder
     * 
     * @return a button for the OpenCms editor
     */
    public String button(String href, String target, String image, String label, int type, boolean useCustomImage) {
        if (useCustomImage) {
            // search the picture in the "custom pics" folder
            return button(href, target, image, label, type, getPicsUri());
        } else {
            // search the picture in the common "buttons" folder
            return button(href, target, image, label, type);
        }
    }
    
    /**
     * Returns the editor action for a "cancel" button.<p>
     * 
     * This overwrites the cancel method of the CmsDialog class.<p>
     * 
     * Always use this value, do not write anything directly in the html page.<p>
     * 
     * @return the default action for a "cancel" button
     */
    public String buttonActionCancel() {
        String target = OpenCms.getLinkManager().substituteLink(getCms(), CmsWorkplaceAction.C_JSP_WORKPLACE_URI);
        return "onClick=\"top.location.href='" + target + "';\"";
    }
    
    /**
     * Returns the URI to the editor resource folder where button images and javascripts are located.<p>
     * 
     * @return the URI to the editor resource folder
     */
    public abstract String getEditorResourceUri();
    
    /**
     * Returns the back link when closing the editor.<p>
     * 
     * @return the back link
     */
    public String getParamBacklink() {
        if (m_paramBackLink == null) {
            m_paramBackLink = "";
        }
        return m_paramBackLink;
    }
    
    /**
     * Returns the content of the editor.<p>
     * @return the content of the editor
     */
    public String getParamContent() {
        if (m_paramContent == null) {
            m_paramContent = "";
        }
        return m_paramContent;
    }

    /**
     * Returns the direct edit flag parameter.<p>
     *  
     * @return the direct edit flag parameter
     */
    public String getParamDirectedit() {
        return m_paramDirectedit;
    }
    
    /**
     * Returns the edit as text parameter.<p>
     * 
     * @return the edit as text parameter
     */
    public String getParamEditastext() {
        return m_paramEditAsText;
    }
    
    /**
     * Returns the editor mode parameter.<p>
     *  
     * @return the editor mode parameter
     */
    public String getParamEditormode() {
        return m_paramEditormode;
    }
    
    /**
     * Returns the "loaddefault" parameter to determine if the default editor should be loaded.<p>
     * 
     * @return the "loaddefault" parameter
     */
    public String getParamLoaddefault() {
        return m_paramLoadDefault;
    }
    
    /**
     * Returns the name of the temporary file.<p>
     * 
     * @return the name of the temporary file
     */
    public String getParamTempfile() {
        return m_paramTempFile;
    }
    
    /**
     * Returns the path to the images used by this editor.<p>
     * 
     * @return the path to the images used by this editor
     */
    public String getPicsUri() {
        if (m_picsUri == null) {
            m_picsUri = getEditorResourceUri() + "pics/";
        }
        return m_picsUri;
    }
    
    /**
     * Determines if the online help is available in the currently selected user language.<p>
     * 
     * @return true if the online help is found, otherwise false
     */
    public boolean isHelpEnabled() {
        try {
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getLocale() + "/");
            return true;
        } catch (CmsException e) {
            // help folder is not available
            return false;         
        }
    }
    
    /**
     * Sets the back link when closing the editor.<p>
     * 
     * @param backLink the back link
     */
    public void setParamBacklink(String backLink) {
        m_paramBackLink = backLink;
    } 
    
    /**
     * Sets the content of the editor.<p>
     * 
     * @param content the content of the editor
     */
    public void setParamContent(String content) {
        if (content == null) {
            content = "";
        }
        m_paramContent = content;
    }

    /**
     * Sets the direct edit flag parameter.<p>
     * 
     * @param direct the direct edit flag parameter
     */
    public void setParamDirectedit(String direct) {
        m_paramDirectedit = direct;
    }
    
    /**
     * Sets the  edit as text parameter.<p>
     * 
     * @param editAsText "true" if the resource should be handled like a text file
     */
    public void setParamEditastext(String editAsText) {
        m_paramEditAsText = editAsText;
    }

    /**
     * Sets the editor mode parameter.<p>
     * 
     * @param mode the editor mode parameter
     */
    public void setParamEditormode(String mode) {
        m_paramEditormode = mode;
    }
    
    /**
     * Sets the "loaddefault" parameter to determine if the default editor should be loaded.<p>
     * 
     * @param loadDefault the "loaddefault" parameter
     */
    public void setParamLoaddefault(String loadDefault) {
        m_paramLoadDefault = loadDefault;
    }
    
    /**
     * Sets the name of the temporary file.<p>
     * 
     * @param fileName the name of the temporary file
     */
    public void setParamTempfile(String fileName) {
        m_paramTempFile = fileName;
    }
       
    /**
     * Writes the content of a temporary file back to the original file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void commitTempFile() throws CmsException {
        switchToTempProject();
        CmsFile tempFile = null;
        List properties = null;
        try {
            tempFile = getCms().readFile(getParamTempfile());
            properties = getCms().readPropertyObjects(getParamTempfile(), false);
        } finally {
            switchToCurrentProject();
        }
        // set current project
        CmsFile orgFile = getCms().readFile(getParamResource());
        orgFile.setContents(tempFile.getContents());
        getCms().writeFile(orgFile);
        getCms().writePropertyObjects(getParamResource(), properties);
    }
    
    /**
     * Creates a temporary file which is needed while working in an editor with preview option.<p>
     * 
     * @return the file name of the temporary file
     * @throws CmsException if something goes wrong
     */
    protected String createTempFile() throws CmsException {
        // read the selected file
        CmsResource file = getCms().readFileHeader(getParamResource());
        
        // Create the filename of the temporary file
        String temporaryFilename = CmsResource.getFolderPath(getCms().readAbsolutePath(file)) + I_CmsConstants.C_TEMP_PREFIX + file.getName();
        boolean ok = true;
        
        // switch to the temporary file project
        int tempProject = switchToTempProject();
        
        try {
            getCms().copyResource(getCms().readAbsolutePath(file), temporaryFilename, false, true, I_CmsConstants.C_COPY_AS_NEW);
        } catch (CmsException e) {
            if ((e.getType() == CmsException.C_FILE_EXISTS) || (e.getType() != CmsException.C_SQL_ERROR)) {
                try {
                    // try to re-use the old temporary file
                    getCms().changeLockedInProject(tempProject, temporaryFilename);
                    getCms().lockResource(temporaryFilename, true);
                } catch (Exception ex) {
                    ok = false;
                }
            } else {
                switchToCurrentProject();
                throw e;
            }
        }

        String extendedTempFile = temporaryFilename;
        int loop = 0;
        
        while (!ok) {
            // default temporary file could not be created, try other file names
            ok = true;
            extendedTempFile = temporaryFilename + loop;
    
            try {
                getCms().copyResource(getCms().readAbsolutePath(file), extendedTempFile);
            } catch (CmsException e) {
                if ((e.getType() != CmsException.C_FILE_EXISTS) && (e.getType() != CmsException.C_SQL_ERROR)) {
                    switchToCurrentProject();
                    // This was not a "file exists" exception. Very bad.
                    // We should not continue here since we may run into an endless loop.
                    throw e;
                }
                // temp file could not be created, try again
                loop++;
                ok = false;
            }
        }

        switchToCurrentProject();
        // We have found a temporary file!
        temporaryFilename = extendedTempFile;

        return temporaryFilename;
    }
    
    /**
     * Deletes a temporary file from the OpenCms VFS, needed when exiting an editor.<p> 
     */
    protected void deleteTempFile() {       
        try {
            // switch to the temporary file project
            switchToTempProject();
            // delete the temporary file
            getCms().deleteResource(getParamTempfile(), I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
            // switch back to the current project
            switchToCurrentProject();
        } catch (CmsException e) {
            // ignore this exception
        }
    }
    
    /**
     * Encodes the given content so that it can be transfered to the client.<p>
     * 
     * Content is encoded so that it is compatible with the JavaScript
     * "decodeURIComponent()" function.<p>
     * 
     * @param content the content to encode
     * @return the encoded content
     */
    protected String encodeContent(String content) {
        return CmsEncoder.escapeWBlanks(content, CmsEncoder.C_UTF8_ENCODING);
    }    
    
    /**
     * Decodes the given content the same way the client would do it.<p>
     * 
     * Content is decoded as if it was encoded using the JavaScript
     * "encodeURIComponent()" function.<p>
     * 
     * @param content the content to decode
     * @return the decoded content
     */    
    protected String decodeContent(String content) {
        return CmsEncoder.unescape(content, CmsEncoder.C_UTF8_ENCODING);
    }
    
    /**
     * Decodes an individual parameter value, ensuring the content is always decoded in UTF-8.<p>
     * 
     * For editors the content is always encoded using the 
     * JavaScript encodeURIComponent() method on the client,
     * which always encodes in UTF-8.<p> 
     * 
     * @param paramName the name of the parameter 
     * @param paramValue the unencoded value of the parameter
     * @return the encoded value of the parameter
     */
    protected String fillParamValuesDecode(String paramName, String paramValue) {
        if ((paramName != null) && (paramValue != null)) {
            if (PARAM_CONTENT.equals(paramName)) {                
                return CmsEncoder.decode(paramValue, CmsEncoder.C_UTF8_ENCODING);
            } else {
                return CmsEncoder.decode(paramValue, getCms().getRequestContext().getEncoding());
            }
        } else {
            return null;
        }
    }    
    
    /**
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    protected abstract void initContent();
    
    /**
     * Shows the common error page in case of an exception.<p>
     * 
     * @param editor initialized instance of the editor class
     * @param exception the current exception
     * @param key the suffix key for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @throws JspException if inclusion of the error page fails
     */
    protected void showErrorPage(Object editor, CmsException exception, String key) throws JspException {
        showErrorPage(editor, exception, key, C_FILE_DIALOG_SCREEN_ERROR);
    }
    
    /**
     * Shows the selected error page in case of an exception.<p>
     * 
     * @param editor initialized instance of the editor class
     * @param exception the current exception
     * @param key the suffix for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @param dialogUri the URI of the error dialog to use in the VFS
     * @throws JspException if inclusion of the error page fails
     */    
    protected void showErrorPage(Object editor, CmsException exception, String key, String dialogUri) throws JspException {
        // save initialized instance of the editor class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, editor);
        // reading of file contents failed, show error dialog
        setAction(ACTION_SHOW_ERRORMESSAGE);
        if (exception != null) {
            setParamErrorstack(exception.getStackTraceAsString());
        } else {
            setParamErrorstack(null);
        }

        setParamTitle(key("error.title.editor" + key));
        setParamMessage(key("error.message.editor" + key));
        String reasonSuggestion = key("error.reason.editor" + key) + "<br>\n" + key("error.suggestion.editor" + key) + "\n";
        setParamReasonSuggestion(reasonSuggestion);
        // log the error 
        if (exception != null) {
            String errorMessage = "Error while trying to " + key + " file " + getParamResource() + ": " + exception;
            if (OpenCms.getLog(editor).isErrorEnabled()) {
                OpenCms.getLog(editor).error(errorMessage, exception);
            }
        }
        // include the common error dialog
        getJsp().include(dialogUri);
    }
}