package ftp.tareas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ftp.GestorClienteFTP;

public class TareaCargar extends TareaFTP{

    private String localFilePath;
    private String remoteFilePath;

    public TareaCargar(GestorClienteFTP gestor, String localFilePath, String remoteFilePath) {
        super(gestor);
        this.localFilePath = localFilePath;
        this.remoteFilePath = remoteFilePath;
    }

    @Override
    public void run() {
        try (FileInputStream fis = new FileInputStream(new File(localFilePath))) {
            boolean success = gestor.getClient().storeFile(remoteFilePath, fis);
            System.out.println(success ? "File uploaded successfully." : "File upload failed.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
