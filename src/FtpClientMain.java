
import java.io.IOException;

import ftp.ClientFtpProtocolService;

/**
 * Clase que ejecuta toda la lógica para conectarse a un servidor ftp, y listar los archivos
 * @author Breogan Fernandez Tacon
 */
public class FtpClientMain {
    public static void main(String[] args) {
        try {
            ClientFtpProtocolService ftpClient = new ClientFtpProtocolService(System.out);
            ftpClient.connectTo("ftp.dlptest.com", 21);
            ftpClient.authenticate("dlpuser", "rNrKYTX9g7z3RgJRmxWuGHbeu");
            ftpClient.sendPwd();

            ftpClient.sendPassv();
            ftpClient.sendList(System.out, false);

            // prueba de descarga de un archivo
            /*
            ftpClient.sendPassv();
            ftpClient.sendRetr("(nombre del archivo)", new FileOutputStream("(nombre del archivo)"), true);
            */
            ftpClient.sendQuit();
            ftpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}