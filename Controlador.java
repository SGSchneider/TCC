/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semaforo;


/**
 *
 * @author Stefan Schneider
 */
public class Controlador {
    public static void main(String[] args) throws InterruptedException {
        //variáveis
        Camera cam;
        Sinalizador sinalizador;
        Detector detector;
        Comunicador comunicador;
        Monitorador monitorador;
        int indice, nvias;
        int[] viascs, fila;
        //inicializar variáveis
        indice=0;
        nvias=2;
        viascs=new int[1];
        viascs[0]=2;
        fila=new int[10];
        for(;indice<10;indice++){
            fila[indice]=0;
        }
        //carregar os módulos
        cam = new Camera();
        comunicador = new Comunicador(fila);
        detector = new Detector(cam,
                              //new int[]{50,1030/*Semaforo B*/},
                                new int[]{0,1080/*Semaforo A*/},
                              //new int[]{250,250/*Semaforo B*/},
                                new int[]{400,200/*Semaforo A*/},
                              //new int[]{300,0/*Semaforo B*/},
                                new int[]{450,200/*Semaforo A*/},
                              //new int[]{280,300/*Semaforo B*/},
                                new int[]{200,200/*Semaforo A*/},
                                nvias
                                        );
        sinalizador = new Sinalizador(detector);
        monitorador = new Monitorador(comunicador, cam,
                                    //1030,250,270,300/*SemaforoB*/
                                      0, 440, 200, 200/*SemaforoA*/);
        //iniciar repetição
        do{
            for(indice=0;indice<nvias;indice++){
                if(fila[0]==0){
                    if(detector.detectar(indice)){
                        sinalizador.Verde(indice+1);
                    }
                }else{
                    sinalizador.Verde(fila[0]);
                    comunicador.andaFila();
                    indice--;
                }
            }
        }while(true);
    }
}

