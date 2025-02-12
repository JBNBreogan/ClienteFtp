package ftp.tareas;

import ftp.GestorClienteFTP;

/**
 * Clase padre de las tareas que realiza el cliente FTP
 * @author Breogan
 */
public abstract class TareaFTP implements Runnable{
    
    protected GestorClienteFTP gestor;


    public TareaFTP(GestorClienteFTP gestor){
        this.gestor = gestor;
    }

    @Override
    public abstract void run();
    
}
