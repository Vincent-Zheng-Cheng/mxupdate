/*
 * Copyright 2008-2011 The MxUpdate Team
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

package org.mxupdate.test.data.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import matrix.util.MatrixException;

import org.mxupdate.test.AbstractTest;
import org.mxupdate.test.ExportParser;
import org.mxupdate.test.data.AbstractAdminData;
import org.mxupdate.test.data.user.AbstractUserData;
import org.mxupdate.test.data.util.PropertyDef;
import org.mxupdate.update.util.StringUtil_mxJPO;
import org.testng.Assert;

/**
 * Used to define a policy, create them and test the result.
 *
 * @author The MxUpdate Team
 * @version $Id$
 */
public class PolicyData
    extends AbstractAdminData<PolicyData>
{
    /**
     * Within export the description must be defined.
     */
    private static final Map<String,String> REQUIRED_EXPORT_VALUES = new HashMap<String,String>();
    static  {
        PolicyData.REQUIRED_EXPORT_VALUES.put("description", "");
        PolicyData.REQUIRED_EXPORT_VALUES.put("defaultformat", "");
        PolicyData.REQUIRED_EXPORT_VALUES.put("sequence", "");
        PolicyData.REQUIRED_EXPORT_VALUES.put("store", "");
        PolicyData.REQUIRED_EXPORT_VALUES.put("hidden", "false");
    }

    /**
     * Are all types assigned?
     *
     * @see #setAllTypes(boolean)
     */
    private boolean allTypes = false;

    /**
     * All defined types for this policy.
     *
     * @see #appendTypes(TypeData...)
     * @see #getTypes()
     */
    private final Set<TypeData> types = new HashSet<TypeData>();

    /**
     * Are all formats assigned?
     *
     * @see #setAllFormats(boolean)
     */
    private boolean allFormats = false;

    /**
     * All defined formats for this policy.
     *
     * @see #appendFormats(TypeData...)
     * @see #getFormats()
     */
    private final Set<FormatData> formats = new HashSet<FormatData>();

    /**
     * All state flag used to define if an &quot;all state&quot; definition is
     * done.
     *
     * @see #create()
     * @see #setAllState(boolean)
     */
    private Boolean allState = false;

    /**
     * Access for all states.
     *
     * @see #allState
     * @see #create()
     * @see #getAllStateAccess()
     */
    private final Access<PolicyData> allStateAccess = new Access<PolicyData>(this);

    /**
     * All states for this policy.
     *
     * @see #create()
     * @see #addState(State)
     */
    private final List<State> states = new ArrayList<State>();

    /**
     * Initialize this policy data with given <code>_name</code>.
     *
     * @param _test     related test implementation (where this policy is
     *                  defined)
     * @param _name     name of the policy
     */
    public PolicyData(final AbstractTest _test,
                      final String _name)
    {
        super(_test, AbstractTest.CI.DM_POLICY, _name, PolicyData.REQUIRED_EXPORT_VALUES, null);
    }

    /**
     * Defines if all types are allowed for this policy.
     *
     * @param _allTypes     new value
     * @return this policy data instance
     * @see #allTypes
     */
    public PolicyData setAllTypes(final boolean _allTypes)
    {
        this.allTypes = _allTypes;
        return this;
    }

    /**
     * Appends given <code>_types</code> to the list of all {@link #types}.
     *
     * @param _types    type instances to append
     * @return this instance
     * @see #types
     */
    public PolicyData appendTypes(final TypeData... _types)
    {
        this.types.addAll(Arrays.asList(_types));
        return this;
    }

    /**
     * Returns all {@link #types} of this policy.
     *
     * @return all assigned types
     * @see #types
     */
    public Set<TypeData> getTypes()
    {
        return this.types;
    }

    /**
     * Defines if all types are allowed for this policy.
     *
     * @param _allFormats     new value
     * @return this policy data instance
     * @see #allFormats
     */
    public PolicyData setAllFormats(final boolean _allFormats)
    {
        this.allFormats = _allFormats;
        return this;
    }

    /**
     * Appends given <code>_formats</code> to the list of all {@link #formats}.
     *
     * @param _formats    format instances to append
     * @return this instance
     * @see #formats
     */
    public PolicyData appendFormats(final FormatData... _formats)
    {
        this.formats.addAll(Arrays.asList(_formats));
        return this;
    }

    /**
     * Returns all {@link #formats} of this policy.
     *
     * @return all assigned formats
     * @see #formats
     */
    public Set<FormatData> getFormats()
    {
        return this.formats;
    }

    /**
     * Defines whether the {@link #allState all state flag} must be set (or
     * not).
     *
     * @param _allState     new value
     * @return this policy data instance
     * @see #allState
     */
    public PolicyData setAllState(final Boolean _allState)
    {
        this.allState = _allState;
        return this;
    }

    /**
     * Returns the instance for all state access.
     *
     * @return all state access instance
     * @see #allStateAccess
     */
    public Access<PolicyData> getAllStateAccess()
    {
        return this.allStateAccess;
    }

    /**
     * Appends {@code _state} to this policy.
     *
     * @param _state        state to append
     * @return this policy instance
     * @see #states
     */
    public PolicyData addState(final State _state)
    {
        this.states.add(_state);
        return this;
    }

    /**
     * Prepares and returns the string of the CI file.
     *
     * @return string of the CI file
     */
    @Override()
    public String ciFile()
    {
        final StringBuilder strg = new StringBuilder();
        this.append4CIFileHeader(strg);

        strg.append("updatePolicy \"${NAME}\" {\n")
            .append("  hidden \"").append(this.getFlag("hidden") != null ? this.getFlag("hidden") : false).append("\"\n");

        // values
        for (final Map.Entry<String,String> entry : this.getValues().entrySet())  {
            strg.append(' ').append(entry.getKey()).append(" \"")
                .append(AbstractTest.convertTcl(entry.getValue()))
                .append('\"');
        }

        // type definition
        if (this.allTypes)  {
            strg.append(" type all\n");
        } else if (!this.types.isEmpty()) {
            final List<String> typeNames = new ArrayList<String>();
            for (final TypeData type : this.types)  {
                typeNames.add(type.getName());
            }
            strg.append(" type {").append(StringUtil_mxJPO.joinMql(' ', true, typeNames, null)).append("}\n");
        }

        // format definition
        if (this.allFormats)  {
            strg.append(" format all\n");
        } else if (!this.formats.isEmpty()) {
            final List<String> formatNames = new ArrayList<String>();
            for (final FormatData format : this.formats)  {
                formatNames.add(format.getName());
            }
            strg.append(" format {").append(StringUtil_mxJPO.joinMql(' ', true, formatNames, null)).append("}\n");
        }

        if ((this.allState != null) && this.allState)  {
            strg.append("  allstate {\n");
            this.allStateAccess.append4CIFile("", strg);
            strg.append("  }\n");
        }

        // append state information
        for (final PolicyData.State state : this.states)
        {
            state.append4CIFile(strg);
        }

        strg.append("}");

        // append properties
        for (final PropertyDef prop : this.getProperties())  {
            strg.append('\n').append(prop.getCITCLString(this.getCI()));
        }

        return strg.toString();
    }

    /**
     * Create the related policy in MX for this policy data instance.
     *
     * @return this policy data instance
     * @throws MatrixException if create failed
     */
    @Override()
    public PolicyData create()
        throws MatrixException
    {
        if (!this.isCreated())  {
            this.setCreated(true);

            this.createDependings();

            final StringBuilder cmd = new StringBuilder();
            cmd.append("escape add policy \"").append(AbstractTest.convertMql(this.getName())).append('\"');

            // assign types
            if (this.allTypes)  {
                cmd.append(" type all ");
            } else if (!this.types.isEmpty()) {
                final List<String> typeNames = new ArrayList<String>();
                for (final TypeData type : this.types)  {
                    typeNames.add(type.getName());
                    type.create();
                }
                cmd.append(" type ").append(StringUtil_mxJPO.joinMql(',', true, typeNames, null));
            }

            // assign formats
            if (this.allFormats)  {
                cmd.append(" format all ");
            } else if (!this.formats.isEmpty()) {
                final List<String> formatNames = new ArrayList<String>();
                for (final FormatData format : this.formats)  {
                    formatNames.add(format.getName());
                    format.create();
                }
                cmd.append(" format ").append(StringUtil_mxJPO.joinMql(',', true, formatNames, null));
            }

            if ((this.allState != null) && this.allState)  {
                cmd.append(" allstate ");
                this.allStateAccess.append4Create(cmd);
            }

            this.append4Create(cmd);

            // append state information
            for (final PolicyData.State state : this.states)
            {
                state.append4CIFile(cmd);
            }

            cmd.append(";\n")
                .append("escape add property ").append(this.getSymbolicName())
                .append(" on program eServiceSchemaVariableMapping.tcl")
                .append(" to policy \"").append(AbstractTest.convertMql(this.getName())).append("\"");

            this.getTest().mql(cmd);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * Creates assigned {@link #types}, {@link #formats} and all assigned users
     * of {@link #allStateAccess}.
     *
     * @see #formats
     * @see #types
     * @see #allStateAccess
     */
    @Override()
    public PolicyData createDependings()
        throws MatrixException
    {
        super.createDependings();

        // create assigned types
        for (final TypeData type : this.types)  {
            type.create();
        }
        // create assigned formats
        for (final FormatData format : this.formats)  {
            format.create();
        }
        // create assigned users
        for (final PolicyData.UserAccessFilter userAccess : this.allStateAccess.userAccessFilters)  {
            userAccess.user.create();
        }

        return this;
    }

    /**
     * Checks the export of this data piece if all values are correct defined.
     *
     * @param _exportParser     parsed export
     * @throws MatrixException if check failed
     */
    @Override()
    public void checkExport(final ExportParser _exportParser)
        throws MatrixException
    {
        this.checkExportProperties(_exportParser);

        // check symbolic name
        Assert.assertEquals(
                _exportParser.getSymbolicName(),
                this.getSymbolicName(),
                "check symbolic name");

        // check for all required values
        for (final String valueName : PolicyData.REQUIRED_EXPORT_VALUES.keySet())  {
            Assert.assertEquals(_exportParser.getLines("/updatePolicy/" + valueName + "/@value").size(),
                                1,
                                "required check that minimum and maximum one " + valueName + " is defined");
        }
        // check for defined values
        for (final Map.Entry<String,String> entry : this.getValues().entrySet())  {
            this.checkSingleValue(_exportParser,
                                  entry.getKey(),
                                  entry.getKey(),
                                  "\"" + AbstractTest.convertTcl(entry.getValue()) + "\"");
        }
        // check for hidden flag
        if ((this.getFlag("hidden") == null) || !this.getFlag("hidden"))  {
            this.checkSingleValue(_exportParser,
                                  "hidden flag (must be false)",
                                  "hidden",
                                   "\"false\"");
        } else  {
            this.checkSingleValue(_exportParser,
                                  "hidden flag (must be true)",
                                  "hidden",
                                  "\"true\"");
        }

        // check for types
        if (this.allTypes)  {
            this.checkSingleValue(_exportParser, "all types", "type", "all");
        } else if (!this.types.isEmpty()) {
            final Set<String> typeNames = new TreeSet<String>();
            for (final TypeData type : this.types)  {
                typeNames.add(type.getName());
            }
            this.checkSingleValue(_exportParser, "types", "type", "{" + StringUtil_mxJPO.joinMql(' ', true, typeNames, null) + "}");
        } else  {
            this.checkSingleValue(_exportParser, "types", "type", "{}");
        }

        // check for formats
        if (this.allFormats)  {
            this.checkSingleValue(_exportParser, "all formats", "format", "all");
        } else if (!this.formats.isEmpty()) {
            final Set<String> formatNames = new TreeSet<String>();
            for (final FormatData format : this.formats)  {
                formatNames.add(format.getName());
            }
            this.checkSingleValue(_exportParser, "formats", "format", "{" + StringUtil_mxJPO.joinMql(' ', true, formatNames, null) + "}");
        } else  {
            this.checkSingleValue(_exportParser, "formats", "format", "{}");
        }

        // check for all state flag
        if ((this.allState == null) || !this.allState)  {
            Assert.assertEquals(_exportParser.getLines("/updatePolicy/allstate/@value").size(),
                                0,
                                "check that not all state access is defined");
        } else  {
            Assert.assertEquals(
                    _exportParser.getLines("/updatePolicy/allstate/@value").size(),
                    1,
                    "check that all state access is defined");
            this.allStateAccess.checkExport("/updatePolicy/allstate", _exportParser);
        }

        // check all states
        for (final State state : this.states)
        {
            state.checkExport(_exportParser);
        }
    }

    /**
     * {@inheritDoc}
     * The original method is overwritten because for policies another path
     * exists for the values.
     *
     * @param _exportParser     parsed export
     * @param _kind             kind of the check
     * @param _tag              tag to check
     * @param _value            value to check (or <code>null</code> if value
     *                          is not defined)
     */
    @Override()
    public void checkSingleValue(final ExportParser _exportParser,
                                 final String _kind,
                                 final String _tag,
                                 final String _value)
    {
        if (_value != null)  {
            Assert.assertEquals(
                    _exportParser.getLines("/updatePolicy/" + _tag + "/@value").size(),
                    1,
                    "check " + _kind + " for '" + this.getName() + "' that " + _tag + " is defined");
            Assert.assertEquals(
                    _exportParser.getLines("/updatePolicy/" + _tag + "/@value").get(0),
                    _value,
                    "check " + _kind + " for '" + this.getName() + "' that " + _tag + " is " + _value);

        } else  {
            Assert.assertEquals(
                    _exportParser.getLines("/updatePolicy/" + _tag + "/@value").size(),
                    0,
                    "check " + _kind + " '" + this.getName() + "' that no " + _tag + " is defined");
        }
    }

    /**
     * Holds the double information about allowed access and filter.
     *
     * @param <AF>  class which is defined from this class
     */
    protected static class AccessFilterDouble<AF extends PolicyData.AccessFilterDouble<?>>
    {
        /**
         * Related access definitions.
         */
        private final Set<String> access = new TreeSet<String>();

        /**
         * Related filter expression.
         */
        private String filter;

        /**
         * Appends access.
         *
         * @param _access   access to append
         * @return this instance
         */
        @SuppressWarnings("unchecked")
        public AF addAccess(final String... _access)
        {
            this.access.addAll(Arrays.asList(_access));
            return (AF) this;
        }

        /**
         * Defines the filter.
         *
         * @param _filter   new filter
         * @return this instance
         */
        @SuppressWarnings("unchecked")
        public AF setFilter(final String _filter)
        {
            this.filter = _filter;
            return (AF) this;
        }

        /**
         * Prepares the TCL string which are used within the CI file.
         *
         * @return TCL string
         */
        protected String getCITCLString()
        {
            return "{" + StringUtil_mxJPO.joinTcl(' ', false, this.access, "none") +"}"
                + ((this.filter != null) ? " filter \"" + StringUtil_mxJPO.convertTcl(this.filter) + "\"" : "");
        }

        /**
         * Returns the MQL string used within create of the policy.
         *
         * @return MQL create string
         */
        protected String getMQLCreateString()
        {
            final StringBuilder ret = new StringBuilder();
            ret.append(StringUtil_mxJPO.joinMql(',', false, this.access, "none"));
            if (this.filter != null)  {
                ret.append(" filter \"").append(AbstractTest.convertTcl(this.filter)).append('\"');
            }
            return ret.toString();
        }
    }

    /**
     * Access definition for one user.
     */
    public static class UserAccessFilter
        extends PolicyData.AccessFilterDouble<PolicyData.UserAccessFilter>
    {
        /**
         * Related user of this user access filter.
         */
        private AbstractUserData<?> user;

        /**
         * Defines the {@link #user} of this user access filter.
         *
         * @param _user     referenced user
         * @return this instance
         */
        public UserAccessFilter setUser(final AbstractUserData<?> _user)
        {
            this.user = _user;
            return this;
        }

        /**
         * Prepares the TCL string which are used within the CI file.
         *
         * @return TCL string
         */
        @Override()
        protected String getCITCLString()
        {
            return "\"" + StringUtil_mxJPO.convertTcl(this.user.getName()) + "\" " + super.getCITCLString();
        }

        /**
         * Returns the MQL string used within create of the policy.
         *
         * @return MQL create string
         */
        @Override()
        protected String getMQLCreateString()
        {
            return "\"" + AbstractTest.convertMql(this.user.getName()) + "\" " +super.getMQLCreateString();
        }
    }

    /**
     * One access definition for all states or one state.
     *
     * @param <PARENT> parent class
     */
    public static class Access<PARENT>
    {
        /**
         * Set holding the complete owner access.
         */
        private final PolicyData.AccessFilterDouble<?> ownerAccess = new PolicyData.AccessFilterDouble<PolicyData.AccessFilterDouble<?>>();

        /**
         * Set holding the complete owner revoke.
         */
        private final PolicyData.AccessFilterDouble<?> ownerRevoke = new PolicyData.AccessFilterDouble<PolicyData.AccessFilterDouble<?>>();

        /**
         * Set holding the complete public access.
         */
        private final PolicyData.AccessFilterDouble<?> publicAccess = new PolicyData.AccessFilterDouble<PolicyData.AccessFilterDouble<?>>();

        /**
         * Set holding the complete public revoke.
         */
        private final PolicyData.AccessFilterDouble<?> publicRevoke = new PolicyData.AccessFilterDouble<PolicyData.AccessFilterDouble<?>>();

        /**
         * User access definitions.
         */
        private final Set<PolicyData.UserAccessFilter> userAccessFilters = new HashSet<PolicyData.UserAccessFilter>();

        /**
         * Parent object (needed to return related instance while defining the
         * access settings).
         */
        private final PARENT parent;

        /**
         * Initializes the {@link #parent} instance.
         *
         * @param _parent   parent instance
         */
        protected Access(final PARENT _parent)
        {
            this.parent = _parent;
        }

        /**
         * Appends owner access.
         *
         * @param _access   access to append
         * @return parent instance
         */
        public PARENT addOwnerAccess(final String... _access)
        {
            this.ownerAccess.addAccess(_access);
            return this.parent;
        }

        /**
         * Defines the filter for owner access.
         *
         * @param _ownerAccessFilter    filter for owner access
         * @return parent instance
         */
        public PARENT setOwnerAccessFilter(final String _ownerAccessFilter)
        {
            this.ownerAccess.setFilter(_ownerAccessFilter);
            return this.parent;
        }

        /**
         * Appends owner revoke.
         *
         * @param _access   access to append
         * @return parent instance
         */
        public PARENT addOwnerRevoke(final String... _access)
        {
            this.ownerRevoke.addAccess(_access);
            return this.parent;
        }

        /**
         * Defines the filter for owner revoke.
         *
         * @param _ownerRevokeFilter    filter for owner revoke
         * @return parent instance
         */
        public PARENT setOwnerRevokeFilter(final String _ownerRevokeFilter)
        {
            this.ownerRevoke.setFilter(_ownerRevokeFilter);
            return this.parent;
        }

        /**
         * Appends public access.
         *
         * @param _access   access to append
         * @return parent instance
         */
        public PARENT addPublicAccess(final String... _access)
        {
            this.publicAccess.addAccess(_access);
            return this.parent;
        }

        /**
         * Defines the filter for public access.
         *
         * @param _publicAccessFilter       filter for public access
         * @return parent instance
         */
        public PARENT setPublicAccessFilter(final String _publicAccessFilter)
        {
            this.publicAccess.setFilter(_publicAccessFilter);
            return this.parent;
        }

        /**
         * Appends public revoke.
         *
         * @param _access   access to append
         * @return parent instance
         */
        public PARENT addPublicRevoke(final String... _access)
        {
            this.publicRevoke.addAccess(_access);
            return this.parent;
        }

        /**
         * Defines the filter for public revoke.
         *
         * @param _publicRevokeFilter       filter for public revoke
         * @return parent instance
         */
        public PARENT setPublicRevokeFilter(final String _publicRevokeFilter)
        {
            this.publicRevoke.setFilter(_publicRevokeFilter);
            return this.parent;
        }

        /**
         * Appends a new user access statement.
         *
         * @param _userAccess   single user access to append
         * @return parent instance
         * @see #userAccess
         */
        public PARENT addUserAccess(final UserAccessFilter _userAccess)
        {
            this.userAccessFilters.add(_userAccess);
            return this.parent;
        }

        /**
         * Appends the TCL statements used within the CI file.
         *
         * @param _prefix   prefix with spaces for a line
         * @param _cmd      string builder where to append the TCL statements
         */
        protected void append4CIFile(final String _prefix,
                                     final StringBuilder _cmd)
        {
            _cmd.append(_prefix).append("    owner ").append(this.ownerAccess.getCITCLString()).append('\n')
                .append(_prefix).append("    revoke owner ").append(this.ownerRevoke.getCITCLString()).append('\n')
                .append(_prefix).append("    public ").append(this.publicAccess.getCITCLString()).append('\n')
                .append(_prefix).append("    revoke public ").append(this.publicRevoke.getCITCLString()).append('\n');
            for (final UserAccessFilter userAccess : this.userAccessFilters)  {
                _cmd.append(_prefix).append("    user ").append(userAccess.getCITCLString()).append('\n');
            }
        }

        /**
         * Appends the MQL statements to create the policy.
         *
         * @param _cmd  string builder where to append the MQL statements
         */
        protected void append4Create(final StringBuilder _cmd)
        {
            _cmd.append(" owner ").append(this.ownerAccess.getMQLCreateString())
                .append(" revoke owner ").append(this.ownerRevoke.getMQLCreateString())
                .append(" public ").append(this.publicAccess.getMQLCreateString())
                .append(" revoke public ").append(this.publicRevoke.getMQLCreateString());
            for (final UserAccessFilter userAccess : this.userAccessFilters)  {
                _cmd.append(" user ").append(userAccess.getMQLCreateString());
            }
        }

        /**
         * Checks the export.
         *
         * @param _urlPrefix        URL prefix
         * @param _exportParser     export parser
         * @throws MatrixException if information could not be fetched
         */
        public void checkExport(final String _urlPrefix,
                                final ExportParser _exportParser)
            throws MatrixException
        {
            Assert.assertEquals(
                    _exportParser.getLines(_urlPrefix + "/owner/@value").get(0),
                    this.ownerAccess.getCITCLString(),
                    "check that all state owner access is defined");
            Assert.assertEquals(
                    _exportParser.getLines(_urlPrefix + "/public/@value").get(0),
                    this.publicAccess.getCITCLString(),
                    "check that all state public access is defined");
            // owner / public revoke
            final List<String> revokes = _exportParser.getLines(_urlPrefix + "/revoke/@value");
            int count = 0;
            if (!this.ownerRevoke.access.isEmpty() || (this.ownerRevoke.filter != null))  {
                Assert.assertTrue(
                        revokes.contains("owner " + this.ownerRevoke.getCITCLString()),
                        "check that owner revoke is defined");
                count++;
            }
            if (!this.publicRevoke.access.isEmpty() || (this.publicRevoke.filter != null))  {
                Assert.assertTrue(
                        revokes.contains("public " + this.publicRevoke.getCITCLString()),
                        "check that public revoke is defined");
                count++;
            }
            Assert.assertEquals(
                    revokes.size(),
                    count,
                    "check that count of revokes is correct (have " + revokes + ")");
            // user access
            final Set<String> mustUsers = new HashSet<String>();
            for (final PolicyData.UserAccessFilter userAccessFilter : this.userAccessFilters)  {
                mustUsers.add(userAccessFilter.getCITCLString());
            }
            for (final String user : _exportParser.getLines(_urlPrefix + "/user/@value"))  {
                Assert.assertTrue(mustUsers.contains(user), "check that '" + user + "' is original defined");
                mustUsers.remove(user);
            }
            Assert.assertEquals(mustUsers.size(), 0, "check that all users are defined (have " + mustUsers + ")");
        }
    }

    /**
     * Represents one state of a policy.
     */
    public static class State
    {
        /** Name of the state. */
        private String name;

        /**
         * Access definition for this state.
         *
         * @see #append4CIFile(StringBuilder)
         * @see #getAccess()
         */
        private final Access<PolicyData.State> access = new Access<PolicyData.State>(this);

        /**
         * List of all signatures for this state.
         */
        private final List<PolicyData.Signature> signatures = new ArrayList<PolicyData.Signature>();

        /**
         * Defines the {@code _name} of the state.
         *
         * @param _name     name of the state
         * @return this state instance
         * @see #name
         */
        public PolicyData.State setName(final String _name)
        {
            this.name = _name;
            return this;
        }

        /**
         * Appends a signature.
         *
         * @param _signature    signature to append
         * @return this state instance
         * @see #signatures
         */
        public PolicyData.State addSignature(final PolicyData.Signature _signature)
        {
            this.signatures.add(_signature);
            return this;
        }

        /**
         * Appends the MQL statements to create the policy.
         *
         * @param _cmd  string builder where to append the MQL statements
         */
        protected void append4CIFile(final StringBuilder _cmd)
        {
            _cmd.append("    state \"").append(AbstractTest.convertTcl(this.name)).append("\" {\n");
            this.access.append4CIFile("  ", _cmd);
            for (final PolicyData.Signature signature : this.signatures)
            {
                signature.append4CIFile(_cmd);
            }
            _cmd.append("    }\n");
        }

        /**
         * Returns the instance for the access.
         *
         * @return access instance
         * @see #access
         */
        public Access<PolicyData.State> getAccess()
        {
            return this.access;
        }

        /**
         *
         * @param _exportParser     export parsed
         * @throws MatrixException if information could not be fetched
         */
        public void checkExport(final ExportParser _exportParser)
            throws MatrixException
        {
            this.access.checkExport("/updatePolicy/state", _exportParser);
            boolean found = false;
            final String value = "\"" + AbstractTest.convertTcl(this.name) + "\"";
            for (final ExportParser.Line line : _exportParser.getRootLines().get(0).getChildren())  {
                if ("state".equals(line.getTag()) && line.getValue().startsWith(value))  {
                    found = true;
                    for (final PolicyData.Signature signature : this.signatures)  {
                        signature.checkExport(line);
                    }
                }
            }
            Assert.assertTrue(found, "check that state '" + this.name + "' is found");
        }
    }

    /**
     * Signature for a state of a policy.
     */
    public static class Signature
    {
        /** Name of the signature. */
        private String name;

        /** Filter of the signature. */
        private String filter;

        /** Branch of the signature. */
        private String branch;

        /**
         * Defines the {@code _name} of the signature.
         *
         * @param _name     name of the signature
         * @return this signature instance
         * @see #name
         */
        public PolicyData.Signature setName(final String _name)
        {
            this.name = _name;
            return this;
        }

        /**
         * Defines the {@code _filter} of the signature.
         *
         * @param _filter     filter of the signature
         * @return this signature instance
         * @see #filter
         */
        public PolicyData.Signature setFilter(final String _filter)
        {
            this.filter = _filter;
            return this;
        }

        /**
         * Defines the {@code _branch} of the signature.
         *
         * @param _branch     branch of the signature
         * @return this signature instance
         * @see #branch
         */
        public PolicyData.Signature setBranch(final String _branch)
        {
            this.branch = _branch;
            return this;
        }

        /**
         * Appends the MQL statements to create the policy.
         *
         * @param _cmd  string builder where to append the MQL statements
         */
        protected void append4CIFile(final StringBuilder _cmd)
        {
            _cmd.append("        signature \"").append(AbstractTest.convertTcl(this.name)).append("\" {\n")
                .append("            branch \"").append(AbstractTest.convertTcl(this.branch)).append("\"\n")
                .append("            approve {}\n")
                .append("            ignore {}\n")
                .append("            reject {}\n")
                .append("            filter \"").append(AbstractTest.convertTcl(this.filter)).append("\"\n")
                .append("        }\n");
        }

        /**
         * Checks that the export is equal to the definition.
         *
         * @param _exportState  parsed line with exported state
         * @throws MatrixException if information could not be fetched
         */
        protected void checkExport(final ExportParser.Line _exportState)
        {
            boolean found = false;
            final String value = "\"" + AbstractTest.convertTcl(this.name) + "\"";
            for (final ExportParser.Line line : _exportState.getChildren())  {
                if ("signature".equals(line.getTag()) && line.getValue().startsWith(value))  {
                    found = true;
                    Assert.assertEquals(
                            line.evalSingleValue("branch"),
                            "\"" + AbstractTest.convertTcl(this.branch) + "\"",
                            "check for correct branch");
                    Assert.assertEquals(
                            line.evalSingleValue("approve"),
                            "{}",
                            "check for correct approve user");
                    Assert.assertEquals(
                            line.evalSingleValue("ignore"),
                            "{}",
                            "check for correct ignore user");
                    Assert.assertEquals(
                            line.evalSingleValue("reject"),
                            "{}",
                            "check for correct reject user");
                    Assert.assertEquals(
                            line.evalSingleValue("filter"),
                            "\"" + AbstractTest.convertTcl(this.filter) + "\"",
                            "check for correct filter");
                }
            }
            Assert.assertTrue(found, "check that signature " + this.name + " is found");
        }
    }
}
