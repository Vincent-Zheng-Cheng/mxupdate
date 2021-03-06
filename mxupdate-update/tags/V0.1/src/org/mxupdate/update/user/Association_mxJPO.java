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

package org.mxupdate.update.user;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrix.db.Context;
import matrix.util.MatrixException;

import org.mxupdate.update.AbstractAdminObject_mxJPO;
import org.mxupdate.update.util.InfoAnno_mxJPO;
import org.mxupdate.util.Mapping_mxJPO.AdminTypeDef;

import static org.mxupdate.update.util.StringUtil_mxJPO.convertTcl;
import static org.mxupdate.util.MqlUtil_mxJPO.execMql;

/**
 *
 * @author tmoxter
 * @version $Id$
 */
@InfoAnno_mxJPO(adminType = AdminTypeDef.Association)
public class Association_mxJPO
        extends AbstractAdminObject_mxJPO
{
    /**
     * Defines the serialize version unique identifier.
     */
    private static final long serialVersionUID = -3663847015076548873L;

    /**
     * Regular expression used to extract the version information from a &quot;
     * standard&quot; association print.
     *
     * @see #getMxFileDate(Context, String)
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<=  property version value )[0-9]++");

    /**
     * Stores the definition of this association instance.
     */
    private String definition = null;

    /**
     * Parses all association specific URLs.
     *
     * @param _url      URL to parse
     * @param _content  content of the URL to parse
     */
    @Override
    protected void parse(final String _url,
                         final String _content)
    {
        if ("/definition".equals(_url))  {
            this.definition = _content;

        } else  {
            super.parse(_url, _content);
        }
    }

    /**
     * Writes specific information about the cached associations to the given
     * writer instance.
     *
     * @param _out      writer instance
     */
    @Override
    protected void writeObject(final Writer _out)
            throws IOException
    {
        _out.append(" \\\n    ").append(isHidden() ? "hidden" : "!hidden")
            .append(" \\\n    definition \"").append(convertTcl(this.definition)).append("\"");
    }

    /**
     * The method overwrites the original method to append the MQL statements
     * in the <code>_preMQLCode</code> to reset this association:
     * <ul>
     * <li>reset description</li>
     * <li>set definition to current context user</li>
     * </ul>
     *
     * @param _context          context for this request
     * @param _preMQLCode       MQL statements which must be called before the
     *                          TCL code is executed
     * @param _postMQLCode      MQL statements which must be called after the
     *                          TCL code is executed
     * @param _preTCLCode       TCL code which is defined before the source
     *                          file is sourced
     * @param _tclVariables     map of all TCL variables where the key is the
     *                          name and the value is value of the TCL variable
     *                          (the value is automatically converted to TCL
     *                          syntax!)
     * @param _sourceFile       souce file with the TCL code to update
     */
    @Override
    protected void update(final Context _context,
                          final CharSequence _preMQLCode,
                          final CharSequence _postMQLCode,
                          final CharSequence _preTCLCode,
                          final Map<String,String> _tclVariables,
                          final File _sourceFile)
            throws Exception
    {
        // description and definition
        final StringBuilder preMQLCode = new StringBuilder()
                .append("mod ").append(this.getInfoAnno().adminType().getMxName())
                .append(" \"").append(this.getName()).append('\"')
                .append(" description \"\"")
                .append(" definition \"").append(_context.getUser()).append("\";\n");

        // append already existing pre MQL code
        preMQLCode.append(_preMQLCode);

        super.update(_context, preMQLCode, _postMQLCode, _preTCLCode, _tclVariables, _sourceFile);
    }

    /**
     * Returns the stored file date within Matrix for administration object
     * with given name. The original method is overwritten, because a select
     * statement of a &quot;print&quot; command does not work.
     *
     * @param _context      context for this request
     * @param _name         name of update object
     * @return modified date of given update object
     * @throws MatrixException if the MQL print failed
     * @see #VERSION_PATTERN
     */
    @Override
    public Date getMxFileDate(final Context _context,
                              final String _name)
            throws MatrixException
    {
        final String curVersion = execMql(_context, new StringBuilder()
                .append("print asso \"").append(_name).append("\""));
        final Matcher matcher = VERSION_PATTERN.matcher(curVersion);
        return (matcher.find())
               ? new Date(Long.parseLong(matcher.group()) * 1000)
               : null;
    }
}
