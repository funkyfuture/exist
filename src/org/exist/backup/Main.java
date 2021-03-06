/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2011 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id$
 */
package org.exist.backup;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.exist.util.SystemExitCodes;
import org.xml.sax.SAXException;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

import org.exist.util.ConfigurationHelper;
import org.exist.xmldb.DatabaseInstanceManager;
import org.exist.xmldb.XmldbURI;

import java.io.IOException;
import java.io.InputStream;

import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.xml.parsers.ParserConfigurationException;

import org.exist.backup.restore.listener.ConsoleRestoreListener;
import org.exist.backup.restore.listener.GuiRestoreListener;
import org.exist.backup.restore.listener.RestoreListener;
import org.exist.client.ClientFrame;
import se.softhouse.jargo.*;

import static org.exist.util.ArgumentUtil.getBool;
import static org.exist.util.ArgumentUtil.getOpt;
import static se.softhouse.jargo.Arguments.*;

/**
 * Main.java
 *
 * @author Wolfgang Meier
 */
public class Main {

    /* general arguments */
    private static final Argument<?> helpArg = helpArgument("-h", "--help");
    private static final Argument<Boolean> guiArg = optionArgument("-U", "--gui")
            .description("start in GUI mode")
            .defaultValue(false)
            .build();
    private static final Argument<Boolean> quietArg = optionArgument("-q", "--quiet")
            .description("be quiet. Just print errors.")
            .defaultValue(false)
            .build();
    private static final Argument<Map<String, String>> optionArg = stringArgument("-o", "--option")
            .description("specify extra options: property=value. For available properties see client.properties.")
            .asKeyValuesWithKeyParser(StringParsers.stringParser())
            .build();


    /* user/pass arguments */
    private static final Argument<String> userArg = stringArgument("-u", "--user")
            .description("set user.")
            .required()
            .build();
    private static final Argument<String> passwordArg = stringArgument("-p", "--password")
            .description("set the password for connecting to the database.")
            .required()
            .build();
    private static final Argument<String> dbaPasswordArg = stringArgument("-P", "--dba-password")
            .description("if the backup specifies a different password for the admin user, use this option to specify the new password. Otherwise you will get a permission denied")
            .build();


    /* backup arguments */
    private static final Argument<String> backupCollectionArg = stringArgument("-b", "--backup")
            .description("backup the specified collection.")
            .required()
            .build();
    private static final Argument<File> backupOutputDirArg = fileArgument("-d", "--dir")
            .description("specify the directory to use for backups.")
            .build();


    /* restore arguments */
    private static final Argument<File> restoreArg = fileArgument("-r", "--restore")
            .description("read the specified __contents__.xml file and restore the resources described there.")
            .build();
    private static final Argument<Boolean> rebuildExpathRepoArg = optionArgument("-R", "--rebuild")
            .description("rebuild the EXpath app repository after restore.")
            .defaultValue(false)
            .build();

    private static Properties loadProperties() {
        // read properties
        final Path propFile = ConfigurationHelper.lookup("backup.properties");
        final Properties properties = new Properties();
        try {
            if (Files.isReadable(propFile)) {
                try(final InputStream pin = Files.newInputStream(propFile)) {
                    properties.load(pin);
                }
            } else {
                try(final InputStream pin = Main.class.getResourceAsStream("backup.properties")) {
                    properties.load(pin);
                }
            }
        } catch (final IOException e) {
            System.err.println("WARN - Unable to load properties from: " + propFile.toAbsolutePath().toString());
        }
        return properties;
    }

    /**
     * Constructor for Main.
     *
     * @param arguments parsed command line arguments
     */
    public static void process(final ParsedArguments arguments) {
        final Properties properties = loadProperties();
        final Preferences preferences = Preferences.userNodeForPackage(Main.class);

        final boolean guiMode = getBool(arguments, guiArg);
        final boolean quiet = getBool(arguments, quietArg);
        Optional.ofNullable(arguments.get(optionArg)).ifPresent(options -> options.forEach(properties::setProperty));

        properties.setProperty("user", arguments.get(userArg));
        final String optionPass = arguments.get(passwordArg);
        properties.setProperty("password", optionPass);
        final Optional<String> optionDbaPass = getOpt(arguments, dbaPasswordArg);

        final Optional<String> backupCollection = getOpt(arguments, backupCollectionArg);
        getOpt(arguments, backupOutputDirArg).ifPresent(backupOutputDir -> properties.setProperty("backup-dir", backupOutputDir.getAbsolutePath()));

        final Optional<Path> restorePath = getOpt(arguments, restoreArg).map(File::toPath);
        final boolean rebuildRepo = getBool(arguments, rebuildExpathRepoArg);

        // initialize driver
        Database database;

        try {
            final Class<?> cl = Class.forName(properties.getProperty("driver", "org.exist.xmldb.DatabaseImpl"));
            database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");

            if (properties.containsKey("configuration")) {
                database.setProperty("configuration", properties.getProperty("configuration"));
            }
            DatabaseManager.registerDatabase(database);
        } catch (final ClassNotFoundException | InstantiationException | XMLDBException | IllegalAccessException e) {
            reportError(e);
            return;
        }

        // process
        if (backupCollection.isPresent()) {
            String collection = backupCollection.get();
            if (collection.isEmpty()) {
                if (guiMode) {
                    final CreateBackupDialog dialog = new CreateBackupDialog(properties.getProperty("uri", "xmldb:exist://"), properties.getProperty("user", "admin"), properties.getProperty("password", ""), Paths.get(preferences.get("directory.backup", System.getProperty("user.dir"))));

                    if (JOptionPane.showOptionDialog(null, dialog, "Create Backup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION) {
                        collection = dialog.getCollection();
                        properties.setProperty("backup-dir", dialog.getBackupTarget());
                    }
                } else {
                    collection = XmldbURI.ROOT_COLLECTION;
                }
            }

            if (!collection.isEmpty()) {
                try {
                    final Backup backup = new Backup(
                            properties.getProperty("user", "admin"),
                            properties.getProperty("password", ""),
                            Paths.get(properties.getProperty("backup-dir", "backup")),
                            XmldbURI.xmldbUriFor(properties.getProperty("uri", "xmldb:exist://") + collection),
                            properties
                    );
                    backup.backup(guiMode, null);
                } catch (final Exception e) {
                    reportError(e);
                }
            }
        }

        if (restorePath.isPresent()) {
            Path path = restorePath.get();
            if (!Files.exists(path) && guiMode) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (chooser.showDialog(null, "Select backup file for restore") == JFileChooser.APPROVE_OPTION) {
                    path = chooser.getSelectedFile().toPath();
                }
            }

            if (Files.exists(path)) {
                final String username = properties.getProperty("user", "admin");
                final String uri = properties.getProperty("uri", "xmldb:exist://");

                try {
                    if (guiMode) {
                        restoreWithGui(username, optionPass, optionDbaPass, path, uri);
                    } else {
                        restoreWithoutGui(username, optionPass, optionDbaPass, path, uri, rebuildRepo, quiet);
                    }
                } catch (final Exception e) {
                    reportError(e);
                }
            }
        }

        try {
            String uri = properties.getProperty("uri", XmldbURI.EMBEDDED_SERVER_URI_PREFIX);
            if (!(uri.contains(XmldbURI.ROOT_COLLECTION) || uri.endsWith(XmldbURI.ROOT_COLLECTION))) {
                uri += XmldbURI.ROOT_COLLECTION;
            }

            final Collection root = DatabaseManager.getCollection(uri, properties.getProperty("user", "admin"), optionDbaPass.orElse(optionPass));
            shutdown(root);
        } catch (final Exception e) {
            reportError(e);
        }
        System.exit(SystemExitCodes.OK_EXIT_CODE);
    }

    private static void restoreWithoutGui(final String username, final String password, final Optional<String> dbaPassword, final Path f,
                                          final String uri, final boolean rebuildRepo, boolean quiet) {

        final RestoreListener listener = new ConsoleRestoreListener(quiet);
        final Restore restore = new Restore();

        try {
            restore.restore(listener, username, password, dbaPassword.orElse(null), f, uri);
        } catch (final IOException | URISyntaxException | ParserConfigurationException | XMLDBException | SAXException ioe) {
            listener.error(ioe.getMessage());
        }

        if (listener.hasProblems()) {
            System.err.println(listener.warningsAndErrorsAsString());
        }
        if (rebuildRepo) {
            System.out.println("Rebuilding application repository ...");
            System.out.println("URI: " + uri);
            try {
                String rootURI = uri;
                if (!(rootURI.contains(XmldbURI.ROOT_COLLECTION) || rootURI.endsWith(XmldbURI.ROOT_COLLECTION))) {
                    rootURI += XmldbURI.ROOT_COLLECTION;
                }
                final Collection root = DatabaseManager.getCollection(rootURI, username, dbaPassword.orElse(password));
                if (root != null) {
                    ClientFrame.repairRepository(root);
                    System.out.println("Application repository rebuilt successfully.");
                } else {
                    System.err.println("Failed to retrieve root collection: " + uri);
                }
            } catch (XMLDBException e) {
                reportError(e);
                System.err.println("Rebuilding application repository failed!");
            }
        } else {
            System.out.println("\nIf you restored collections inside /db/apps, you may want\n" +
                    "to rebuild the application repository. To do so, run the following query\n" +
                    "as admin:\n\n" +
                    "import module namespace repair=\"http://exist-db.org/xquery/repo/repair\"\n" +
                    "at \"resource:org/exist/xquery/modules/expathrepo/repair.xql\";\n" +
                    "repair:clean-all(),\n" +
                    "repair:repair()\n");
        }
    }

    private static void restoreWithGui(final String username, final String password, final Optional<String> dbaPassword, final Path f, final String uri) {

        final GuiRestoreListener listener = new GuiRestoreListener();

        final Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                final Restore restore = new Restore();

                try {
                    restore.restore(listener, username, password, dbaPassword.orElse(null), f, uri);

                    listener.hideDialog();

                    if (JOptionPane.showConfirmDialog(null, "Would you like to rebuild the application repository?\nThis is only necessary if application packages were restored.", "Rebuild App Repository?",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        System.out.println("Rebuilding application repository ...");
                        try {
                            String rootURI = uri;
                            if (!(rootURI.contains(XmldbURI.ROOT_COLLECTION) || rootURI.endsWith(XmldbURI.ROOT_COLLECTION))) {
                                rootURI += XmldbURI.ROOT_COLLECTION;
                            }
                            final Collection root = DatabaseManager.getCollection(rootURI, username, dbaPassword.orElse(password));
                            ClientFrame.repairRepository(root);
                            System.out.println("Application repository rebuilt successfully.");
                        } catch (XMLDBException e) {
                            reportError(e);
                            System.err.println("Rebuilding application repository failed!");
                        }
                    }
                } catch (final Exception e) {
                    ClientFrame.showErrorMessage(e.getMessage(), null); //$NON-NLS-1$
                } finally {
                    if (listener.hasProblems()) {
                        ClientFrame.showErrorMessage(listener.warningsAndErrorsAsString(), null);
                    }
                }

                return null;
            }
        };

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<Void> future = executor.submit(callable);

        while (!future.isDone() && !future.isCancelled()) {
            try {
                future.get(100, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException ie) {

            } catch (final ExecutionException ee) {
                break;
            } catch (final TimeoutException te) {

            }
        }
    }


    private static void reportError(final Throwable e) {
        e.printStackTrace();

        if (e.getCause() != null) {
            System.err.println("caused by ");
            e.getCause().printStackTrace();
        }

        System.exit(SystemExitCodes.CATCH_ALL_GENERAL_ERROR_EXIT_CODE);
    }

    private static void shutdown(final Collection root) {
        try {
            final DatabaseInstanceManager mgr = (DatabaseInstanceManager) root.getService("DatabaseInstanceManager", "1.0");

            if (mgr == null) {
                System.err.println("service is not available");
            } else if (mgr.isLocalInstance()) {
                System.out.println("shutting down database...");
                mgr.shutdown();
            }
        } catch (final XMLDBException e) {
            System.err.println("database shutdown failed: ");
            e.printStackTrace();
        }
    }


    public static void main(final String[] args) {
        try {
            final ParsedArguments arguments = CommandLineParser
                    .withArguments(userArg, passwordArg, dbaPasswordArg)
                    .andArguments(backupCollectionArg, backupOutputDirArg)
                    .andArguments(restoreArg, rebuildExpathRepoArg)
                    .andArguments(helpArg, guiArg, quietArg, optionArg)
                    .parse(args);

            process(arguments);
        } catch(final ArgumentException e) {
            System.out.println(e.getMessageAndUsage());
            System.exit(SystemExitCodes.INVALID_ARGUMENT_EXIT_CODE);

        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(SystemExitCodes.CATCH_ALL_GENERAL_ERROR_EXIT_CODE);
        }
    }
}
