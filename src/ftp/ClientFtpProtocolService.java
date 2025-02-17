package ftp;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientFtpProtocolService implements Runnable {

    private Socket controlSocket;
    private BufferedReader controlReader;
    private PrintWriter controlWriter;
    private OutputStream log;
    private Thread controlThread;

    private CountDownLatch pasvLatch;

    private Socket dataSocket;

    private final Object dataChannelLock = new Object();
    private final AtomicBoolean dataChannelInUse = new AtomicBoolean(false);

    public ClientFtpProtocolService(OutputStream log) {
        this.log = log;
    }

    public void connectTo(String server, int port) throws IOException {
        controlSocket = new Socket(server, port);
        controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
        controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        controlThread = new Thread(this);
        controlThread.start();
    }

    @Override
    public void run() {
        String line;
        try {
            while ((line = controlReader.readLine()) != null) {
                log.write((line + "\n").getBytes());
                // Si se recibe el código 227, parseamos para obtener la dirección y puerto del canal de datos.
                if (line.startsWith("227")) {
                    InetSocketAddress dataAddress = parse227(line);
                    try {
                        dataSocket = new Socket(dataAddress.getHostName(), dataAddress.getPort());
                    } catch (IOException e) {
                        log.write(("Error al conectar el canal de datos: " + e.getMessage() + "\n").getBytes());
                    }
                    if (pasvLatch != null) {
                        pasvLatch.countDown();
                    }
                }
            }
        } catch (IOException e) {
            try {
                log.write(("Error en el canal de control: " + e.getMessage() + "\n").getBytes());
            } catch (IOException ex) {
                // Ignorar
            }
        }
    }

    private InetSocketAddress parse227(String response) throws IOException {
        Pattern pattern = Pattern.compile(".*\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\).*");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String ip = matcher.group(1) + "." + matcher.group(2) + "." +
                        matcher.group(3) + "." + matcher.group(4);
            int port = Integer.parseInt(matcher.group(5)) * 256 + Integer.parseInt(matcher.group(6));
            return new InetSocketAddress(ip, port);
        } else {
            throw new IOException("Respuesta PASV mal formateada: " + response);
        }
    }

    private void sendCommand(String command) throws IOException {
        controlWriter.println(command);
        log.write((command + "\n").getBytes());
    }

    public void authenticate(String user, String pass) throws IOException {
        sendCommand("USER " + user);
        sendCommand("PASS " + pass);
    }

    public void close() throws IOException {
        sendCommand("QUIT");
        if (controlSocket != null && !controlSocket.isClosed()) {
            controlSocket.close();
        }
    }

    public String sendQuit() throws IOException {
        String command = "QUIT";
        sendCommand(command);
        return command;
    }

    public String sendPwd() throws IOException {
        String command = "PWD";
        sendCommand(command);
        return command;
    }

    public String sendCwd(String down) throws IOException {
        String command = "CWD " + down;
        sendCommand(command);
        return command;
    }

    public String sendCdup() throws IOException {
        String command = "CDUP";
        sendCommand(command);
        return command;
    }

    public String sendPassv() throws IOException {
        // Bloquea hasta que el canal de datos esté libre.
        synchronized (dataChannelLock) {
            while (dataChannelInUse.get()) {
                try {
                    dataChannelLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            dataChannelInUse.set(true);
        }
        pasvLatch = new CountDownLatch(1);
        String command = "PASV";
        sendCommand(command);
        try {
            pasvLatch.await(); // Espera la respuesta 227 y la creación del dataSocket.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrumpido esperando respuesta PASV");
        }
        return command;
    }

    public String sendRetr(String remote, OutputStream out, boolean closeOutput) throws IOException {
        String command = "RETR " + remote;
        sendCommand(command);
        if (dataSocket == null) {
            throw new IOException("Canal de datos no iniciado. Llama a sendPassv() antes de RETR.");
        }
        ClientFtpDataService dataService = new ClientFtpDataService(
                dataSocket, out, closeOutput, dataChannelLock, dataChannelInUse);
        new Thread(dataService).start();
        // Reinicia el dataSocket para permitir futuras transferencias.
        dataSocket = null;
        return command;
    }

    public String sendList(OutputStream out, boolean closeOutput) throws IOException {
        String command = "LIST";
        sendCommand(command);
        if (dataSocket == null) {
            throw new IOException("Canal de datos no iniciado. Llama a sendPassv() antes de LIST.");
        }
        ClientFtpDataService dataService = new ClientFtpDataService(
                dataSocket, out, closeOutput, dataChannelLock, dataChannelInUse);
        new Thread(dataService).start();
        dataSocket = null;
        return command;
    }
}