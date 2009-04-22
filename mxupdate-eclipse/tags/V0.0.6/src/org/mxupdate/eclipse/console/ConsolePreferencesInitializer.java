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

package org.mxupdate.eclipse.console;

import org.mxupdate.eclipse.preferences.AbstractWorkspacePreferencesInitializer;

/**
 * Initialize all console specific preferences.
 *
 * @author Tim Moxter
 * @version $Id$
 */
public class ConsolePreferencesInitializer
        extends AbstractWorkspacePreferencesInitializer<ConsolePreference>
{
    /**
     * Calls
     * {@link AbstractWorkspacePreferencesInitializer#initializeDefaultPreferences(java.util.Collection)}
     * to initialize all console specific parameters defined in
     * {@link ConsolePreference}.
     */
    @Override
    public void initializeDefaultPreferences()
    {
        this.initializeDefaultPreferences(ConsolePreference.values());
    }
}
