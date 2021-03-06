/*
 * Copyright 2008-2009 The MxUpdate Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.mxupdate.update.datamodel;

import java.io.IOException;
import java.io.Writer;

import org.mxupdate.update.AbstractAdminObject_mxJPO;
import org.mxupdate.update.util.InfoAnno_mxJPO;
import org.mxupdate.util.Mapping_mxJPO.AdminTypeDef;

import static org.mxupdate.update.util.StringUtil_mxJPO.convertTcl;

/**
 *
 * @author tmoxter
 * @version $Id$
 */
@InfoAnno_mxJPO(adminType = AdminTypeDef.Format)
public class Format_mxJPO
        extends AbstractAdminObject_mxJPO
{
    /**
     * Defines the serialize version unique identifier.
     */
    private static final long serialVersionUID = -2981049394263810538L;

    /**
     * Reference to the edit program.
     */
    private String commandEdit = null;

    /**
     * Reference to the print program.
     */
    private String commandPrint = null;

    /**
     * Reference to the view program.
     */
    private String commandView = null;

    /**
     * File suffix of the format.
     */
    private String fileSuffix = null;

    /**
     * File type of the format.
     */
    private String fileType = null;

    /**
     * Version of the format.
     */
    private String version = null;

    /**
     * Parses all format specific URLs.
     *
     * @param _url      URL to parse
     * @param _content  content of the URL to parse
     */
    @Override
    protected void parse(final String _url,
                         final String _content)
    {
        if ("/editCommand".equals(_url))  {
            this.commandEdit = _content;
        } else if ("/printCommand".equals(_url))  {
            this.commandPrint = _content;
        } else if ("/viewCommand".equals(_url))  {
            this.commandView = _content;

        } else if ("/fileCreator".equals(_url))  {
            // to be ignored, because identically to fileType
        } else if ("/fileSuffix".equals(_url))  {
            this.fileSuffix = _content;
        } else if ("/fileType".equals(_url))  {
            this.fileType = _content;

        } else if ("/version".equals(_url))  {
            this.version = _content;

        } else  {
            super.parse(_url, _content);
        }
    }

    /**
     * Writes specific information about the cached format to the given
     * writer instance.
     *
     * @param _out      writer instance
     */
    @Override
    protected void writeObject(final Writer _out)
            throws IOException
    {
        _out.append(" \\\n    ").append(isHidden() ? "hidden" : "nothidden")
            .append(" \\\n    version \"").append((this.version != null) ? convertTcl(this.version) : "").append('\"')
            .append(" \\\n    suffix \"").append((this.fileSuffix != null) ? convertTcl(this.fileSuffix) : "").append('\"')
            .append(" \\\n    mime \"").append((this.fileType != null) ? convertTcl(this.fileType) : "").append('\"')
            .append(" \\\n    view \"").append((this.commandView != null) ? convertTcl(this.commandView) : "").append('\"')
            .append(" \\\n    edit \"").append((this.commandEdit != null) ? convertTcl(this.commandEdit) : "").append('\"')
            .append(" \\\n    print \"").append((this.commandPrint != null) ? convertTcl(this.commandPrint) : "").append('\"');
    }
}
