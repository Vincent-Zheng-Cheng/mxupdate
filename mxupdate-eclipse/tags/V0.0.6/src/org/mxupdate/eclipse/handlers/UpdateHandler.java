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

package org.mxupdate.eclipse.handlers;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.mxupdate.eclipse.Activator;

/**
 * Eclipse Handler called from the update command used to update selected TCL
 * update file.
 *
 * @author Tim Moxter
 * @version $Id$
 */
public class UpdateHandler
        extends AbstractFileHandler
{
    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     *
     * @param _files    set of files for which this handler is called
     * @see Activator#update(String)
     */
    @Override
    protected void execute(final List<IFile> _files)
    {
        for (final IFile file : _files)  {
            Activator.getDefault().update(file.getLocation().toString());
        }
    }
}
