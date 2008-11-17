/*
 * Copyright 2008 The MxUpdate Team
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

package net.sourceforge.mxupdate.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

/**
 * @author tmoxter
 * @version $Id$
 */
public class Insert_mxJPO
{
    /**
     * File name extension of the JPO.
     */
    private static final String JPO_EXTENSION = "_" + "mxJPO";

    /**
     * File extension of a Java file.
     */
    private static final String JAVA_FILE_EXTENSION = ".java";

    /**
     * File name and file extension together.
     *
     * @see #evaluateFiles(Map, Map, File, File)
     */
    private static final String JPO_FILE_EXTENSION = JPO_EXTENSION + JAVA_FILE_EXTENSION;

    /**
     * Name of the JPO property holding the last modified date of the file.
     */
    private static final String PROP_FILEDATE = "file date";

    /**
     * Name of the JPO property holding the version.
     */
    private static final String PROP_VERSION = "version";

    /**
     * Name of the JPO property holding the installation date.
     */
    private static final String PROP_INSTALLED_DATE = "installed date";

    /**
     * Name of the JPO property holding the installer.
     *
     * @see #VALUE_INSTALLER
     */
    private static final String PROP_INSTALLER = "installer";

    /**
     * Value of the JPO property holding the installer.
     *
     * @see #PROP_INSTALLER
     */
    private static final String VALUE_INSTALLER = "The MxUpdate Team";

    /**
     * Name of the JPO property holding the author.
     *
     * @see #VALUE_AUTHOR
     */
    private static final String PROP_AUTHOR = "author";

    /**
     * Value of the JPO property holding the author.
     *
     * @see #PROP_AUTHOR
     */
    private static final String VALUE_AUTHOR = "The MxUpdate Team";

    /**
     * Name of the JPO property holding the original name.
     */
    private static final String PROP_ORIGINAL_NAME = "original name";

    /**
     * Name of the JPO property holding the application name.
     *
     * @see #VALUE_APPLICATION
     */
    private static final String PROP_APPLICATION = "application";

    /**
     * Value of the JPO property holding the application name.
     *
     * @see #PROP_APPLICATION
     */
    private static final String VALUE_APPLICATION = "MxUpdate";

    /**
     *
     */
    private static final Pattern PATTERN_PACKAGE = Pattern.compile("^package [A-Za-z0-9\\.]*;$");

    /**
     *
     */
    private static final Pattern PATTERN_IMPORT = Pattern.compile("(?<=^import[\\ \\t]?)[A-Za-z0-9\\.]*_"+"mxJPO(?=[\\ \\t]?;[\\ \\t]?$)");

    /**
     *
     */
    private static final Pattern classWithoutPckPat = Pattern.compile("(?<=\\.)[A-Za-z0-9]*_"+"mxJPO");

    /**
     *
     */
    private static final Pattern pattern = Pattern.compile("((?<=[ \\(\\)\\t\\r\\n@\\<])|(^))[A-Za-z0-9\\.]*\\_"+"mxJPO");

    /**
     * MQL command line to list the installed MxUpdate JPOs and the depending
     * last modified date of the installed file.
     *
     * @see #evaluteInstalledJPOs(Context)
     */
    private static final String CMD_LISTJPOS
            = "list prog \"MxUpdate,net.sourceforge.mxupdate.*\" "
                    + "select name property[" + PROP_FILEDATE + "].value "
                    + "dump \"\t\"";

    /**
     * Defines the date / time format used for the MxUpdate JPOs.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static  {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+00"));
    }

    /**
     * Defines the date format used for the installation date.
     */
    private static final SimpleDateFormat DATE_INSTALLED = new SimpleDateFormat("MM-dd-yyyy");
    static  {
        DATE_INSTALLED.setTimeZone(TimeZone.getTimeZone("GMT+00"));
    }

    /**
     *
     * @param _context  context for this request
     * @param _args
     * @throws Exception
     */
    public void mxMain(final Context _context,
                       final String[] _args)
            throws Exception
    {
        final File rootPath = new File(_args[0]);
        final String version = _args[1];


        // get installed JPOs
        final Map<String,Date> installedProgs = evaluteInstalledJPOs(_context);

        final Map<String,Map<String,ClassFile>> mapPckFiles = new TreeMap<String,Map<String,ClassFile>>();
        final Map<String,ClassFile> jpoMap = new TreeMap<String,ClassFile>();
        evaluateFiles(mapPckFiles, jpoMap, rootPath, new File(""));

        // delete obsolete JPOs
        for (final String progName : installedProgs.keySet())  {
            if (!jpoMap.containsKey(progName))  {
System.out.println("delete jpo '" + progName + "'");
                execMql(_context,
                        new StringBuilder()
                                .append("delete program '").append(progName).append('\''));
            }
        }

        // create new / update JPOs
        for (final Map.Entry<String,Map<String,ClassFile>> newPckFilesEntry : mapPckFiles.entrySet())  {
            // evaluate JPOs from current package
            final Map<String,String> class2Pck = new HashMap<String,String>();
            for (final ClassFile classFile : newPckFilesEntry.getValue().values())  {
                class2Pck.put(classFile.className, classFile.jpoName);
            }
            // install all JPOs from current package
            for (final ClassFile classFile : newPckFilesEntry.getValue().values())  {
                if (!installedProgs.containsKey(classFile.jpoName))  {
System.out.println("install jpo '" + classFile.jpoName + "'");
                    classFile.create(_context, class2Pck, version);
                } else  {
                    final Date mxDate = installedProgs.get(classFile.jpoName);
                    if ((mxDate == null) || !mxDate.equals(classFile.getLastModified()))  {
System.out.println("update jpo '" + classFile.jpoName + "'");
                        classFile.update(_context, class2Pck, version);
                    }
                }
            }
        }
    }

    /**
     * Executes given MQL command and returns the trimmed result of the MQL
     * execution.
     *
     * @param _context  context for this request
     * @param _cmd      MQL command to execute
     * @return trimmed result of the MQL execution
     * @throws MatrixException if MQL execution fails
     */
    protected String execMql(final Context _context,
                             final CharSequence _cmd)
            throws MatrixException
    {
        final MQLCommand mql = new MQLCommand();
        mql.executeCommand(_context, _cmd.toString());
        if ((mql.getError() != null) && !"".equals(mql.getError()))  {
            throw new MatrixException(mql.getError() + "\nMQL command was:\n" + _cmd);
        }
        return mql.getResult().trim();
    }


    /**
     * Searches for the installed JPOs and returns them including the
     * information about the last modified date of the installed file.
     *
     * @param _context  context for this request
     * @return map of already installed JPOs and the last modified date of the
     *         installed file
     * @throws MatrixException if the installed JPOs could not be evaluated
     * @see #CMD_LISTJPOS
     */
    protected Map<String,Date> evaluteInstalledJPOs(final Context _context)
            throws MatrixException
    {
        final String jpos = execMql(_context, CMD_LISTJPOS);
        final Map<String,Date> installedProgs = new TreeMap<String,Date>();
        for (final String oneJPO : jpos.split("\n"))  {
            final String[] oneJPOArr = oneJPO.split("\t");
            final String modDate = (oneJPOArr.length > 1) ? oneJPOArr[1] : "";
            Date mxDate;
            try  {
                mxDate = DATE_FORMAT.parse(modDate);
            } catch (final ParseException e)  {
                mxDate = null;
            }
            installedProgs.put(oneJPOArr[0], mxDate);
        }

        return installedProgs;
    }


    /**
     *
     * @param _mapPckFiles  map of packages and their JPOs
     * @param _jpoSet       map of all JPO programs
     * @param _rootPath     path of the root (where the JPO code is located)
     * @param _packagePath  path of the package depending on the root path
     */
    protected void evaluateFiles(final Map<String,Map<String,ClassFile>> _mapPckFiles,
                                 final Map<String,ClassFile> _jpoMap,
                                 final File _rootPath,
                                 final File _packagePath)
    {
        final File path = new File(_rootPath, _packagePath.toString());
        for (final File file : path.listFiles())  {
            if (file.isDirectory())  {
                evaluateFiles(_mapPckFiles, _jpoMap, _rootPath, new File(_packagePath, file.getName()));
            } else if (file.getName().endsWith(JPO_FILE_EXTENSION))  {
                final ClassFile classFile = new ClassFile(_packagePath, file);
                Map<String,ClassFile> pckFiles = _mapPckFiles.get(classFile.pckName);
                if (pckFiles == null)  {
                    pckFiles = new TreeMap<String,ClassFile>();
                    _mapPckFiles.put(classFile.pckName, pckFiles);
                }
                pckFiles.put(classFile.className, classFile);
                _jpoMap.put(classFile.jpoName, classFile);
            }
        }
    }


    private class ClassFile
    {
        final File jpoFile;

        final String pckName;

        final String className;

        final String jpoName;

        ClassFile(final File _packagePath,
                  final File _jpoFile)
        {
            this.jpoFile = _jpoFile;
            this.pckName = _packagePath.getPath()
                                       .replace(File.separatorChar, '.')
                                       .replaceAll("^\\.", "");
            this.className = _jpoFile.getName().replaceAll(JAVA_FILE_EXTENSION + "$", "");
            this.jpoName = "".equals(pckName)
                             ? this.className.replaceAll(JPO_EXTENSION + "$", "")
                             : pckName + "." + this.className.replaceAll(JPO_EXTENSION + "$", "");
        }

        /**
         * Returns the modified date of the JPO file.
         *
         * @return modified date of the JPO file
         * @see #jpoFile
         */
        public Date getLastModified()
        {
            return new Date(this.jpoFile.lastModified());
        }

        /**
         *
         * @param _context      context for this request
         * @param _class2Pck    used JPO name within the code and the related
         *                      class name
         * @param _version      application version
         * @throws IOException if the JPO file could not be read
         * @throws MatrixException if the JPO could not created
         */
        public void create(final Context _context,
                           final Map<String,String> _class2Pck,
                           final String _version)
                throws IOException, MatrixException
        {
            execMql(_context,
                    new StringBuilder()
                    .append("add program '").append(this.jpoName).append("' code \"")
                            .append(this.getCode(_class2Pck)).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_FILEDATE).append("\" value \"")
                            .append(Insert_mxJPO.DATE_FORMAT.format(getLastModified())).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_VERSION).append("\" value \"")
                            .append(_version).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_AUTHOR).append("\" value \"")
                            .append(Insert_mxJPO.VALUE_AUTHOR).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_INSTALLER).append("\" value \"")
                            .append(Insert_mxJPO.VALUE_INSTALLER).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_INSTALLED_DATE).append("\" value \"")
                            .append(Insert_mxJPO.DATE_INSTALLED.format(new Date())).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_APPLICATION).append("\" value \"")
                            .append(Insert_mxJPO.VALUE_APPLICATION).append("\" ")
                    .append("property \"").append(Insert_mxJPO.PROP_ORIGINAL_NAME).append("\" value \"")
                            .append(this.jpoName).append("\" "));
        }

        /**
         *
         * @param _context      context for this request
         * @param _class2Pck    used JPO name within the code and the related
         *                      class name
         * @param _version      application version
         * @throws IOException if the JPO file could not be read
         * @throws MatrixException if the JPO could not updated
         */
        public void update(final Context _context,
                           final Map<String,String> _class2Pck,
                           final String _version)
                throws IOException, MatrixException
        {
            execMql(_context,
                    new StringBuilder()
                    .append("mod program '").append(this.jpoName).append("' code \"")
                    .append(this.getCode(_class2Pck))
                    .append("\" add property \"").append(Insert_mxJPO.PROP_FILEDATE).append("\" value \"")
                            .append(Insert_mxJPO.DATE_FORMAT.format(getLastModified())).append('\"')
                    .append(" add property \"").append(Insert_mxJPO.PROP_VERSION).append("\" value \"")
                            .append(_version).append("\""));
        }

        /**
         *
         * @param _class2Pck    used JPO name within the code and the related
         *                      class name
         * @return code in the MQL syntax
         * @throws IOException if the file could not be read
         */
        private CharSequence getCode(final Map<String,String> _class2Pck)
                throws IOException
        {
            final StringBuilder code = new StringBuilder();

            final Map<String,String> class2Pck = new HashMap<String,String>(_class2Pck);

            final BufferedReader reader = new BufferedReader(new FileReader(this.jpoFile));
            try  {
                String line = reader.readLine();
                while (line != null)  {
                    final Matcher pckMatch = PATTERN_PACKAGE.matcher(line);
                    if (pckMatch.find())  {
                        code.append('\n');
                    } else  {
                        final Matcher impMatch = PATTERN_IMPORT.matcher(line);
                        if (impMatch.find())  {
                            final String impClass= impMatch.group();
                            // extract class name from imported name
                            final Matcher classWithoutPckMatch = classWithoutPckPat.matcher(impClass);
                            classWithoutPckMatch.find();
                            class2Pck.put(classWithoutPckMatch.group(), impClass.substring(0, impClass.length() - 6));
                        } else  {
                            final Matcher matcher = pattern.matcher(line);
                            int start = 0;
                            final StringBuilder newLine = new StringBuilder();
                            while (matcher.find())  {
                                if (className.equals(matcher.group()))  {
                                    newLine.append(line.substring(start, matcher.start()))
                                            .append("${"+"CLASSNAME"+"}");
                                    start = matcher.start() + matcher.group().length();
                                } else  {
                                    final String clazzName = class2Pck.containsKey(matcher.group())
                                                             ? class2Pck.get(matcher.group())
                                                             : matcher.group().substring(0, matcher.group().length() - 6);
                                    newLine.append(line.substring(start, matcher.start()))
                                           .append("${"+"CLASS:")
                                           .append(clazzName)
                                           .append("}");
                                    start = matcher.start() + matcher.group().length();
                                }
                            }
                            newLine.append(line.substring(start, line.length()));

                            code.append(newLine.toString().replaceAll("\\\\", "\\\\\\\\\\\\\\\\").replaceAll("\"", "\\\\\"")).append('\n');
                        }
                    }
                    line = reader.readLine();
                }
            } finally  {
                reader.close();
            }

            return code;
        }
    }
}