// VeriBlock NodeCore CLI
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package nodecore.cli;

import com.google.inject.Inject;
import nodecore.cli.contracts.Configuration;
import nodecore.cli.contracts.ProgramOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class DefaultConfiguration implements Configuration {
    private static final Logger _logger = LoggerFactory.getLogger(DefaultConfiguration.class);
    private Properties _properties = new Properties();
    private final Properties _defaultProperties = new Properties();
    private ProgramOptions _options;

    private String getPropertyOverrideOrDefault(final String name) {
        String value = _options.getProperty(name);
        if (value != null && value.length() > 0)
            return value;
        value = _properties.getProperty(name);
        if (value == null)
            return "";
        return value;
    }

    @Inject
    public DefaultConfiguration(ProgramOptions options) throws IOException {
        _options = options;
        loadDefaults();
        _properties = new Properties(_defaultProperties);

    }

    @Override
    public void clearProperties() {
        _properties.clear();
    }

    @Override
    public boolean isDebugEnabled() {
        String flag = getPropertyOverrideOrDefault("debug.enabled");
        return flag.equalsIgnoreCase("true");
    }


    @Override
    public void load() {
        try
        {
            try (InputStream stream = new FileInputStream(_options.getConfigPath())) {
                load(stream);
            }
        } catch (FileNotFoundException e) {
            _logger.info("Unable to load custom properties file: File '{}' does not exist. Using default properties.", _options.getConfigPath());
        } catch (IOException e) {
            _logger.info("Unable to load custom properties file. Using default properties.", e);
        }
    }

    @Override
    public void load(InputStream inputStream) {
        try {
            _properties.load(inputStream);
        } catch (Exception e) {
            _logger.error("Unhandled exception in DefaultConfiguration.load", e);
        }
    }

    private void loadDefaults() {
        try
        {

            try (InputStream stream = DefaultConfiguration.class
                    .getClassLoader()
                    .getResourceAsStream(Constants.DEFAULT_PROPERTIES)) {
                _defaultProperties.load(stream);
            }
        } catch (IOException e) {
            _logger.error("Unable to load default properties", e);
        }
    }

    @Override
    public void save() {
        try {
            File configFile = new File(_options.getConfigPath());
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            OutputStream stream = new FileOutputStream(configFile);
            save(stream);
            stream.close();
        } catch (IOException e) {
            _logger.warn("Unable to save custom properties file", e);
        }
    }

    @Override
    public void save(OutputStream outputStream) {
        try {
            _properties.store(outputStream, "NodeCore Configuration");
            outputStream.flush();
        } catch (Exception e) {
            _logger.error("Unhandled exception in DefaultConfiguration.save", e);
        }
    }

    @Override
    public String getPrivateKeyPath() {
        return getPropertyOverrideOrDefault(ConfigurationKeys.SECURITY_PRIVATE_KEY_PATH);
    }

    @Override
    public String getCertificateChainPath() {
        return getPropertyOverrideOrDefault(ConfigurationKeys.SECURITY_CERT_CHAIN_PATH);
    }

    private static class ConfigurationKeys {
        private static final String SECURITY_CERT_CHAIN_PATH = "rpc.security.cert.chain.path";
        private static final String SECURITY_PRIVATE_KEY_PATH = "rpc.security.private.key.path";
    }
}
