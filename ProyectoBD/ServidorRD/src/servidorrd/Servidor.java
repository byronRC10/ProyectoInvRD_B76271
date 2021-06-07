package servidorrd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor extends Thread{
    
    private int socketPortNumber;
    
    public Servidor(int socketPortNumber){
        super("Hilo Servidor");
        
        this.socketPortNumber=socketPortNumber;
    } // constructor

    public void run(){
        try {
            
            ServerSocket serverSocket=new ServerSocket(this.socketPortNumber);
  
            do{

                 Socket socket= serverSocket.accept();
                 
                 Cliente cliente= new Cliente("", socket);
                 cliente.start();

            }while(true);
        }catch(IOException e){
            System.out.println("Error E/S");
        }// catch
    } // run  
    
    public static void main(String  args[]) {
     Servidor s= new Servidor(4000);
     s.run();
    }
    
}// fin clase
