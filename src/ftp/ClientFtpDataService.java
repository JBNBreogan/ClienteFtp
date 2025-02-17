package ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Clase que gestiona los datos de servicio ftp
 * @author Breogan Fernandez Tacon.
 */
public class ClientFtpDataService implements Runnable {

    private Socket dataSocket;
    private OutputStream out;
    private boolean closeOutput;
    private final Object dataChannelLock;
    private final AtomicBoolean dataChannelInUse;

    /**
     * Constructor parametrizado
     * @param dataSocket Socket de datos
     * @param out Stream de salida
     * @param closeOutput Comprobacion de cierre
     * @param dataChannelLock Objeto de bloqueo, ayuda a sincronizar los hilos
     * @param dataChannelInUse Indicador de uso del canal de datos
     */
    public ClientFtpDataService(Socket dataSocket, OutputStream out, boolean closeOutput,
                                Object dataChannelLock, AtomicBoolean dataChannelInUse) {
        this.dataSocket = dataSocket;
        this.out = out;
        this.closeOutput = closeOutput;
        this.dataChannelLock = dataChannelLock;
        this.dataChannelInUse = dataChannelInUse;
    }

    @Override
    public void run() {
        try  {
            InputStream in = dataSocket.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataSocket.close();
            } catch (IOException e) {
                // Ignorar excepciones al cerrar
            }
            if (closeOutput) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignorar excepciones al cerrar
                }
            }
            synchronized (dataChannelLock) {
                dataChannelInUse.set(false);
                dataChannelLock.notifyAll();
            }
        }
    }
}