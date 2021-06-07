package servidorrd;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Cliente extends Thread{
    
    private Socket socket;
    private boolean ocupado;
    private OutputStream out;
    private DataOutputStream dos;
    private PrintStream send;
    private InputStream in;
    private DataInputStream dis;
    private BufferedReader receive;
    private boolean activo;
    //
    private ArrayList<Integer> orden;
    private static int rows;
    private static int cols;
    private static int chunks;
    private static int cantImg;
    private static int envioC;

    
    public Cliente(String nombre, Socket socket) throws IOException{
        super("Cliente de: "+nombre);
        this.socket= socket;
        this.ocupado= false;
        this.activo= true;
      
        
        this.out = socket.getOutputStream();
        this.dos = new DataOutputStream(out);
       
        this.in= this.socket.getInputStream();
        this.dis= new DataInputStream(in);
        envioC=1; 
    }// constructor
    
    public void run(){
        while(this.activo){
            try {
                dos.writeInt(1);
                
                int opc = dis.readInt();
                System.out.println(opc);
                if(opc==(-1)){
                     recibirImg();
                    // dos.writeInt(-1);
                }else if(opc==(-2)){
                      //dos.writeInt(-2);
                      procesar();
                }// else if
            } // while
            catch (IOException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }// run
    
     public void recibirImg() throws IOException{
         
      int con= 0;   
      ArrayList<Integer> listO= new ArrayList<>();
      ArrayList<Integer> listA= new ArrayList<>();
      int fin=100;
      while(con<fin){
       fin = dis.readInt();
       //System.out.println("Fin: "+fin);
       defineChunks(fin);
       
       int cImg= dis.readInt();
       cantImg= cImg;
          
       int orden = dis.readInt();
     //  System.out.println("Orden: "+orden);
       listO.add(orden);
       
       int orig = dis.readInt();
      // System.out.println("Origen: "+orig);
       listA.add(orig);
       
       int len= dis.readInt();
      // System.out.println("Image Size: " + len/1024 + "KB");
      
       //System.out.println("Len: "+len);
       byte[] data = new byte[len];
       dis.readFully(data);
       //System.out.println("RFULL: "+data);
      // dis.close();
       //in.close();

       InputStream ian = new ByteArrayInputStream(data);
       BufferedImage bImage = ImageIO.read(ian);
 
       File outputfile = new File("imgA"+ cantImg +"n" + orden +".jpg");
       // img1A.. imgContA
       ImageIO.write(bImage, "jpg", outputfile);
       con+=1;
         // System.out.println("CONT; "+con);
      }// while
     
      for(int i = 0; i < listO.size(); i++) {
          cambiarNombre("imgA"+ cantImg +"n" + +listO.get(i) +".jpg", "img"+ cantImg +"n" + +listA.get(i) +".jpg"); 
      }
      
      mergeImage();
      
    }// funcion
     
    public void procesar() throws IOException{
 
       randomNum();
       ordenEnvio();
       splitImage();
       enviarImg();
       cantImg+=1;
    }
      
     private static void splitImage() throws IOException { // DIVIDIR IMAGEN
       File file = new File("finalImg1.jpg"); 
       FileInputStream fis = new FileInputStream(file);
       BufferedImage image = ImageIO.read(fis);

        int chunkWidth = image.getWidth() / cols;
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
    ImageIO.write(imgs[i], "jpg", new File("img" + 1 +"n"+ i +".jpg"));
    }
    System.out.println("Mini images created");
          
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
        
        // una variable pos ayuda aqui
        //lista.get(pos)
    
    }
       
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
        
        
     
     public void enviarImg() throws IOException{
      
         BufferedImage img = null;

       for(int i=0; i<orden.size(); i++){
         
         dos.writeInt(orden.size());
     
         dos.writeInt(cantImg);
            
         dos.writeInt(orden.get(i)); // enviar cual es esta imagen
         
         dos.writeInt(i);
         
         img = ImageIO.read(new File("img" + cantImg +"n"+ i +".jpg"));
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         
         ImageIO.write(img, "jpg", baos);
         baos.flush();
         
         byte[] bytes = baos.toByteArray();
         baos.close();
         
         dos.writeInt(bytes.length);
         dos.write(bytes, 0, bytes.length);
         
       }// for
    
    }// enviarImg
    
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
    
    
    private static void mergeImage() throws IOException { // UNIR IMAGEN
 
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
    }
    
     public static void cambiarNombre(String nomA, String nomN){
      File file = new File(nomA);
      
      try {
       file.createNewFile();
      }catch(Exception e) {
        e.getStackTrace();
      }

      File newFile = new File(nomN);

      boolean value = file.renameTo(newFile);
    
    }// cambiarNombre
    
    
}// fin clase
