/*
 * Sakuli - Testing and Monitoring-Tool for Websites and common UIs.
 *
 * Copyright 2013 - 2015 the original author or authors.
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
 */

package org.sakuli.datamodel.properties;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sakuli.exceptions.SakuliInitException;
import org.sakuli.exceptions.SakuliRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author tschneck Date: 09.05.14
 */
@Component
public class SakuliProperties extends AbstractProperties {

    public static final String SAKULI_PROPERTIES_FILE_APPENDER = File.separator + "sakuli.properties";
    public static final String SAKULI_DEFAULT_PROPERTIES_FILE_APPENDER = File.separator + "sakuli-default.properties";
    public static final String SAKULI_HOME_FOLDER = "sakuli.home.folder";
    public static final String SUPPRESS_RESUMED_EXCEPTIONS = "sakuli.exception.suppressResumedExceptions";
    public static final String JAVASCRIPT_ENGINE = "sakuli.javascript.engine";
    public static final String LOG_FOLDER = "sakuli.log.folder";
    public static final String LOG_MAX_AGE = "sakuli.log.maxAge";
    public static final String LOG_PATTERN = "sakuli.log.pattern";
    public static final String LOG_EXCEPTION_FORMAT = "sakuli.log.exception.format";
    public static final String LOG_LEVEL_SAKULI = "log.level.sakuli";
    public static final String LOG_LEVEL_SAHI = "log.level.sahi";
    public static final String LOG_LEVEL_SIKULI = "log.level.sikuli";
    public static final String LOG_LEVEL_SPRING = "log.level.spring";
    public static final String LOG_LEVEL_ROOT = "log.level.root";
    public static final String CONFIG_FOLDER_APPEDER = File.separator + "config";
    public static final String LIBS_FOLDER_APPEDER = File.separator + "libs";
    public static final String JS_LIB_FOLDER_APPEDER = LIBS_FOLDER_APPEDER + File.separator + "js";
    // have to be the common/libs older, so that {@link TextRecognizer} can add "tessdata" to the path!
    public static final String TESSDATA_LIB_FOLDER_APPEDER = LIBS_FOLDER_APPEDER;
    private static final boolean SUPPRESS_RESUMED_EXCEPTIONS_DEFAULT = false;
    private static final boolean JAVASCRIPT_ENGINE_DEFAULT = true;
    private static final int LOG_MAX_AGE_DEFAULT = 14;
    private static final String MAX_AGE_GREATER_ZERO = "Property '" + LOG_MAX_AGE + "' have to be greater then 0, please check your 'sakuli.properties' file!";

    @Value("${" + SAKULI_HOME_FOLDER + "}")
    private String sakuliHomeFolderPropertyValue;
    private Path sakuliHomeFolder;
    @Value("${" + SUPPRESS_RESUMED_EXCEPTIONS + ":" + SUPPRESS_RESUMED_EXCEPTIONS_DEFAULT + "}")
    private boolean suppressResumedExceptions;
    @Value("${" + JAVASCRIPT_ENGINE + ":" + JAVASCRIPT_ENGINE_DEFAULT + "}")
    private boolean loadJavaScriptEngine;
    @Value("${" + LOG_FOLDER + "}")
    private String logFolderPropertyValue;
    @Value("${" + LOG_MAX_AGE + ":" + LOG_MAX_AGE_DEFAULT + "}")
    private int logMaxAge;
    private Path logFolder;
    @Value("${" + LOG_PATTERN + "}")
    private String logPattern;
    @Value("${" + LOG_EXCEPTION_FORMAT + "}")
    private String logExceptionFormat;
    @Value("${" + LOG_LEVEL_SAKULI + ":}")
    private String logLevelSakuli;
    @Value("${" + LOG_LEVEL_SAHI + ":}")
    private String logLevelSahi;
    @Value("${" + LOG_LEVEL_SIKULI + ":}")
    private String logLevelSikuli;
    @Value("${" + LOG_LEVEL_SPRING + ":}")
    private String logLevelSpring;
    @Value("${" + LOG_LEVEL_ROOT + ":}")
    private String logLevelRoot;
    private Path configFolder;
    private Path jsLibFolder;
    private Path tessDataLibFolder;


    @PostConstruct
    public void initFolders() throws FileNotFoundException, SakuliInitException {
        sakuliHomeFolder = Paths.get(sakuliHomeFolderPropertyValue).normalize();
        checkFolders(sakuliHomeFolder);

        configFolder = Paths.get(sakuliHomeFolder + CONFIG_FOLDER_APPEDER);
        checkFolders(configFolder);

        jsLibFolder = Paths.get(sakuliHomeFolder + JS_LIB_FOLDER_APPEDER);
        checkFolders(jsLibFolder);

        tessDataLibFolder = Paths.get(sakuliHomeFolder + TESSDATA_LIB_FOLDER_APPEDER);
        checkFolders(tessDataLibFolder);

        //need not to be checked, will be generated by the logger if it is missing
        logFolder = Paths.get(logFolderPropertyValue).normalize();
        if (logMaxAge <= 0) {
            throw new SakuliInitException(MAX_AGE_GREATER_ZERO);
        }
    }

    public String getSakuliHomeFolderPropertyValue() {
        return sakuliHomeFolderPropertyValue;
    }

    public void setSakuliHomeFolderPropertyValue(String sakuliHomeFolderPropertyValue) {
        this.sakuliHomeFolderPropertyValue = sakuliHomeFolderPropertyValue;
    }

    public Path getSakuliHomeFolder() {
        return sakuliHomeFolder;
    }

    public void setSakuliHomeFolder(Path sakuliHomeFolder) {
        this.sakuliHomeFolder = sakuliHomeFolder;
    }

    public boolean isSuppressResumedExceptions() {
        return suppressResumedExceptions;
    }

    public void setSuppressResumedExceptions(boolean suppressResumedExceptions) {
        this.suppressResumedExceptions = suppressResumedExceptions;
    }

    public String getLogFolderPropertyValue() {
        return logFolderPropertyValue;
    }

    public void setLogFolderPropertyValue(String logFolderPropertyValue) {
        this.logFolderPropertyValue = logFolderPropertyValue;
    }

    public int getLogMaxAge() {
        return logMaxAge;
    }

    public void setLogMaxAge(int logMaxAge) {
        this.logMaxAge = logMaxAge;
    }

    public Path getLogFolder() {
        return logFolder;
    }

    public void setLogFolder(Path logFolder) {
        this.logFolder = logFolder;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public String[] getLogExceptionFormat() {
        if (StringUtils.hasText(logExceptionFormat) &&
                logExceptionFormat.startsWith("[") && logExceptionFormat.endsWith("]")) {
            try {
                return new JsonFactory().setCodec(new ObjectMapper()).createParser(logExceptionFormat).readValueAs(String[].class);
            } catch (IOException e) {
                throw new SakuliRuntimeException("Found invalid log exception format pattern", e);
            }
        } else if (StringUtils.hasText(logExceptionFormat)) {
            return new String[] { logExceptionFormat };
        } else {
            return new String[] {};
        }
    }

    public void setLogExceptionFormat(String logExceptionFormat) {
        this.logExceptionFormat = logExceptionFormat;
    }

    public Path getConfigFolder() {
        return configFolder;
    }

    public void setConfigFolder(Path configFolder) {
        this.configFolder = configFolder;
    }

    public Path getJsLibFolder() {
        return jsLibFolder;
    }

    public void setJsLibFolder(Path jsLibFolder) {
        this.jsLibFolder = jsLibFolder;
    }

    public Path getTessDataLibFolder() {
        return tessDataLibFolder;
    }

    public void setTessDataLibFolder(Path tessDataLibFolder) {
        this.tessDataLibFolder = tessDataLibFolder;
    }

    public String getLogLevelSakuli() {
        return logLevelSakuli;
    }

    public void setLogLevelSakuli(String logLevelSakuli) {
        this.logLevelSakuli = logLevelSakuli;
    }

    public String getLogLevelSahi() {
        return logLevelSahi;
    }

    public void setLogLevelSahi(String logLevelSahi) {
        this.logLevelSahi = logLevelSahi;
    }

    public String getLogLevelSpring() {
        return logLevelSpring;
    }

    public void setLogLevelSpring(String logLevelSpring) {
        this.logLevelSpring = logLevelSpring;
    }

    public String getLogLevelSikuli() {
        return logLevelSikuli;
    }

    public void setLogLevelSikuli(String logLevelSikuli) {
        this.logLevelSikuli = logLevelSikuli;
    }

    public String getLogLevelRoot() {
        return logLevelRoot;
    }

    public void setLogLevelRoot(String logLevelRoot) {
        this.logLevelRoot = logLevelRoot;
    }

    public boolean isLoadJavaScriptEngine() {
        return loadJavaScriptEngine;
    }

    public void setLoadJavaScriptEngine(boolean loadJavaScriptEngine) {
        this.loadJavaScriptEngine = loadJavaScriptEngine;
    }
}
