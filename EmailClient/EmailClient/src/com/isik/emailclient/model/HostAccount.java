package com.isik.emailclient.model;

//@author berk & sertac
public class HostAccount {
    
    private String emailAddress;
    private String password;
    private String protocol; // "imap" or "pop3"
    private String incomingHost; // e.g., imap.gmail.com
    private int incomingPort;    // e.g., 993
    
    // Outgoing Mail Settings (SMTP)
    private String outgoingHost; // e.g., smtp.gmail.com
    private int outgoingPort;    // e.g., 465 or 587

    public HostAccount(String emailAddress, String password, String protocol, 
                       String incomingHost, int incomingPort, 
                       String outgoingHost, int outgoingPort) {
        this.emailAddress = emailAddress;
        this.password = password;
        this.protocol = protocol;
        this.incomingHost = incomingHost;
        this.incomingPort = incomingPort;
        this.outgoingHost = outgoingHost;
        this.outgoingPort = outgoingPort;
    }

    // Getters and Setters
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getIncomingHost() { return incomingHost; }
    public void setIncomingHost(String incomingHost) { this.incomingHost = incomingHost; }

    public int getIncomingPort() { return incomingPort; }
    public void setIncomingPort(int incomingPort) { this.incomingPort = incomingPort; }

    public String getOutgoingHost() { return outgoingHost; }
    public void setOutgoingHost(String outgoingHost) { this.outgoingHost = outgoingHost; }

    public int getOutgoingPort() { return outgoingPort; }
    public void setOutgoingPort(int outgoingPort) { this.outgoingPort = outgoingPort; }
}
