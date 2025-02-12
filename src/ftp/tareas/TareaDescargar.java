package ftp.tareas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ftp.GestorClienteFTP;

public class TareaDescargar extends TareaFTP{

    private String remoteFilePath;
    private String localFilePath;

    public TareaDescargar(GestorClienteFTP gestor, String remoteFilePath, String localFilePath) {
        super(gestor);
        this.remoteFilePath = remoteFilePath;
        this.localFilePath = localFilePath;
    }

    @Override
    public void run() {
        try (FileOutputStream fos = new FileOutputStream(new File(localFilePath))) {
            boolean success = gestor.getClient().retrieveFile(remoteFilePath, fos);
            System.out.println(success ? "Archivo descargado con éxito" : "Error al descargar el archivo");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
