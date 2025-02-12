package ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class GestorClienteFTP {
    private FTPClient ftpClient = new FTPClient();

    public boolean connect(String server, int port, String user, String password) {
        try {
            ftpClient.connect(server, port);
            if (ftpClient.login(user, password)) {
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                System.out.println("Connected to " + server);
                return true;
            } else {
                System.out.println("Failed to connect.");
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                System.out.println("Disconnected.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public FTPClient getClient() {
        return ftpClient;
    }
}
