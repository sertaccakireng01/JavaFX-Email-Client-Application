package com.isik.emailclient.service;

import com.isik.emailclient.model.HostAccount;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

//@author berk
public class ConfigService {

    private static final String CONFIG_FILE = "email_config.properties";

    public void saveAccount(HostAccount account) throws Exception {
        Properties props = new Properties();
        
        props.setProperty("email", account.getEmailAddress());
        props.setProperty("password", account.getPassword());
        props.setProperty("protocol", account.getProtocol());
        
        props.setProperty("incomingHost", account.getIncomingHost());
        props.setProperty("incomingPort", String.valueOf(account.getIncomingPort()));
        
        props.setProperty("outgoingHost", account.getOutgoingHost());
        props.setProperty("outgoingPort", String.valueOf(account.getOutgoingPort()));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Email Client Configuration");
        }
    }

    public HostAccount loadAccount() throws Exception {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
        }

        return new HostAccount(
            props.getProperty("email"),
            props.getProperty("password"),
            props.getProperty("protocol"),
            props.getProperty("incomingHost"),
            Integer.parseInt(props.getProperty("incomingPort")),
            props.getProperty("outgoingHost"),
            Integer.parseInt(props.getProperty("outgoingPort"))
        );
    }
    
    
    public void deleteAccount() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
