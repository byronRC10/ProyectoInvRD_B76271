package clienterd;

import java.awt.Graphics2D;
import javax.swing.*;  
import java.net.*; 
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Cliente  extends Observable implements Runnable{
    
    // atributos
    private static String inputFile;
    private static String filename;
    private int socketPortNumber;
    private OutputStream out;
    private DataOutputStream dos;
    private PrintStream send;
    private InputStream in;
    private DataInputStream dis;
    private BufferedReader receive;
    private Socket socket;
    private InetAddress address;
    private boolean conectado;
    private String mensaje;
    private int cont;
    private ArrayList<Integer> orden;
    private static int rows; 
    private static int cols;
    private static int chunks; 
    private static int cantImg;
    private static String nombrarA;
    
    public Cliente(int socketPortNumber, String ipServidor) throws UnknownHostException, IOException {

        this.socketPortNumber = socketPortNumber;
        this.conectado = true;
  
        this.address = InetAddress.getByName(ipServidor);

        this.socket = new Socket(address, this.socketPortNumber);

           
        this.out = socket.getOutputStream();
        this.dos = new DataOutputStream(out);
       
        this.in= this.socket.getInputStream();
        this.dis= new DataInputStream(in);
 
        
        this.mensaje= "Inicio cliente";
        this.cont= 0;
    } // constructor

    @Override
    public void run() {
       while (this.conectado) {
              
           try {
                int len = dis.readInt();
                System.out.println("Len: "+len);
                
                dos.writeInt(2);
              
            } // while
            catch (IOException ex) {
             
            }
          
      
        }// while
    }// run
    
    public void enviarImg() throws IOException{
       int len = dis.readInt();
       System.out.println("Len: "+len);
                
       dos.writeInt(-1);
    
        
       BufferedImage img = null;

     for(int i=0; i<orden.size(); i++){
         
         dos.writeInt(orden.size());
         
         dos.writeInt(cantImg);
         
         dos.writeInt(orden.get(i)); 
         
         dos.writeInt(i);
         
         System.out.println("Reading image from disk. ");
         img = ImageIO.read(new File("img" + cantImg +"n"+ i +".jpg"));
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         
         ImageIO.write(img, "jpg", baos);
         baos.flush();
         
         byte[] bytes = baos.toByteArray();
         baos.close();
         
         System.out.println("Sending image to server. ");
         
         dos.writeInt(bytes.length);
         dos.write(bytes, 0, bytes.length);
         
     }// for
    
    }// enviarImg
    
     public void procesar() throws IOException{
        randomNum();
        ordenEnvio();
        splitImage();
        enviarImg();
    }
     
    private void ordenEnvio(){
        orden = new ArrayList<>();
        
        while(orden.size()<chunks){
         int numR = (int) (Math.random()*((chunks-1)-0+1)+0); // (Maximo-Minimo+1)+Minimo
         int enc= 0;
          for(int i=0; i<orden.size(); i++){
             if(numR==orden.get(i)){
                 enc= 1;
             }// if
          }// for
         if(enc==0){
             orden.add(numR);
         }// if
         
        }// while
    
    }// ordenEnvio
    
     public static void randomNum(){
        int numR = (int) (Math.random()*(2-0+1)+0); // (Maximo-Minimo+1)+Minimo
        
        switch(numR){
            case 0:
                    rows = 2; 
                    cols = 2;
                    chunks = rows * cols;
                   break;
            case 1:
                    rows = 3; 
                    cols = 3;
                    chunks = rows * cols;
                   break;
            case 2:
                    rows = 4; 
                    cols = 4;
                    chunks = rows * cols;
                   break;
        }
    }// randomNum
    
    private static void splitImage() throws IOException { // DIVIDIR IMAGEN
       File file = new File(nombrarA); // crea archivo
       FileInputStream fis = new FileInputStream(file);
       BufferedImage image = ImageIO.read(fis); // leyendo la imagen

        int chunkWidth = image.getWidth() / cols; // determinar la dimension
        int chunkHeight = image.getHeight() / rows;
        int count = 0;
        BufferedImage imgs[] = new BufferedImage[chunks]; 
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }

     for (int i = 0; i < imgs.length; i++) {
      ImageIO.write(imgs[i], "jpg", new File("img" + (cantImg) +"n"+ i +".jpg"));
     }
    System.out.println("Mini images created");
          
    }
    
    public static void defineChunks(int num){
        int numR = num ; 
       
        switch(numR){
            case 4:
                    rows = 2; 
                    cols = 2;
                    chunks = 4;
                   break;
            case 9:
                    rows = 3; 
                    cols = 3;
                    chunks = 9;
                   break;
            case 16:
                    rows = 4; 
                    cols = 4;
                    chunks = 16;
                   break;
        }   
    }
    
    public void recibirImg() throws IOException{
       int len = dis.readInt();
                
       dos.writeInt(-2);
       
      int con= 0;   
      ArrayList<Integer> listO= new ArrayList<>();
      ArrayList<Integer> listA= new ArrayList<>();
      int fin=100;
      while(con<fin){
       fin = dis.readInt();
       defineChunks(fin);
          System.out.println(fin);
         
        int cImg= dis.readInt();
       cantImg= cImg;
          
       int orden = dis.readInt();
       listO.add(orden);
       
       int orig = dis.readInt();
       listA.add(orig);
       
       len= dis.readInt();
       System.out.println("Image Size: " + len/1024 + "KB");
      
       byte[] data = new byte[len];
       dis.readFully(data);
       System.out.println("RFULL: "+data);
      // dis.close();
       //in.close();

       InputStream ian = new ByteArrayInputStream(data);
       BufferedImage bImage = ImageIO.read(ian);
 
       File outputfile = new File("imgA"+ cantImg +"n" + orden +".jpg");
       // img1A.. imgContA
       ImageIO.write(bImage, "jpg", outputfile);
       con+=1;
          System.out.println("CONT; "+con);
      }// while
     
      for(int i = 0; i < listO.size(); i++) {
        cambiarNombre("imgA"+cantImg+"n" + +listO.get(i) +".jpg", "img"+ cantImg +"n" + +listA.get(i) +".jpg"); 
      }
      
      mergeImage();
   
   }// funcion
    
    private static void mergeImage() throws IOException { // UNIR IMAGEN
 
        System.out.println("ROW: "+rows);
        System.out.println("Cols_ "+cols);
        int chunkWidth, chunkHeight;
        int type;
 
        File[] imgFiles = new File[chunks];
        for (int i = 0; i < chunks; i++) {
            imgFiles[i] = new File("img"+ cantImg +"n" + i +".jpg");
        }
 
        BufferedImage[] buffImages = new BufferedImage[chunks];
        for (int i = 0; i < chunks; i++) {
            buffImages[i] = ImageIO.read(imgFiles[i]);
        }
        type = buffImages[0].getType();
        chunkWidth = buffImages[0].getWidth();
        chunkHeight = buffImages[0].getHeight();
 
        BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);
 
        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }
 
        ImageIO.write(finalImg, "jpeg", new File("finalImg"+ cantImg +".jpg"));
        InputStream is=(InputStream) ImageIO.createImageInputStream(finalImg);
 
         System.out.println ( "splicing has been completed!");
    }// mergeImage
    
    
      public static void cambiarNombre(String nomA, String nomN){

      File file = new File(nomA);
   
      try {
       file.createNewFile();
      }catch(Exception e) {
        e.getStackTrace();
      }

      // crear archivo nuevo
      File newFile = new File(nomN);

      // cambiar nombre archivo
      boolean value = file.renameTo(newFile);
    
    }// cambiarNombre

    public void setNombrarA(String nombrarA) {
        this.nombrarA = nombrarA;
    }

    public void setCantImg(int cantImg) {
        this.cantImg = cantImg;
    }
    
}// fin clase
    

