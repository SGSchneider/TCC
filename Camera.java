/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semaforo;

import java.io.IOException;





/**
 *
 * @author Stefan Schneider
 */

public final class Camera {
    int flag =0;
    Camera(){
    }
    
    public void capvid(int opt) throws InterruptedException{
        do{
            if(flag==0){
                flag = 1;
              try {
              if(opt == 0){
/*Semaforo A*/      Runtime.getRuntime().exec
                          ("raspivid -t 500 -w 1280 -h 720 -o /home/pi/vidM.h264");
/*Semaforo B*///    Runtime.getRuntime().exec
              //          ("raspivid -t 500 -w 1280 -h 1080 -o /home/pi/vidM.h264");
                    Thread.sleep(1000);
                    Runtime.getRuntime().exec
                            ("MP4Box -add /home/pi/vid.h264 /home/pi/vidM.mp4");
                    
                    Thread.sleep(1000);}
              else{
/*Semaforo A*/      Runtime.getRuntime().exec
                          ("raspivid -t 500 -w 1280 -h 720 -o /home/pi/vidD.h264");
/*Semaforo B*///    Runtime.getRuntime().exec
              //          ("raspivid -t 500 -w 1280 -h 1080 -o /home/pi/vidM.h264");
                    Thread.sleep(1000);
                    Runtime.getRuntime().exec
                            ("MP4Box -add /home/pi/vid.h264 /home/pi/vidD.mp4");
                    Thread.sleep(1000);}
              }catch (IOException ex) {}
                flag = 2;
            }
        }while(flag!=2);
    }
    
    
    public void capimg(int opt) throws InterruptedException{
        do{
            if(flag==0){
                flag = 3;
                try {
                    if(opt ==0){
                        Runtime.getRuntime().exec
                                ("raspistill -t 100 -w 1280 -h 720 -o /home/pi/imgM.png");
                        Thread.sleep(500);
                    }else{
                        Runtime.getRuntime().exec
                                ("raspistill -t 100 -w 1280 -h 720 -o /home/pi/imgD.png");
                        Thread.sleep(500);
                    }
                } catch (IOException ex) {}
                flag = 4;
            }
        }while(flag!=4);
    }
}



