/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semaforo;
import com.pi4j.io.gpio.*;
/**
 *
 * @author Stefan Schneider
 */
class Sinalizador {
    static Detector detector;
    final GpioController gpioController;
    static GpioPinDigitalOutput ledVerd1;
    static GpioPinDigitalOutput ledVerm1;
    static GpioPinDigitalOutput ledAmar1;
    static GpioPinDigitalOutput ledVerd2;
    static GpioPinDigitalOutput ledVerm2;
    static GpioPinDigitalOutput ledAmar2;
Sinalizador(Detector d) throws InterruptedException{
        detector = d;
        //instanciamento do controlador GPIO
        gpioController = GpioFactory.getInstance();
        //config PINS
        ledVerm1 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03);
        ledAmar1 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02);
        ledVerd1 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_00);
        ledVerm2 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_04);
        ledAmar2 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_06);
        ledVerd2 = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_05);
        //teste acende
        ledVerd1.setState(PinState.HIGH);
        ledAmar1.setState(PinState.HIGH);
        ledVerd2.setState(PinState.HIGH);
        ledAmar2.setState(PinState.HIGH);
        Thread.sleep(3000);
        //começa
        ledVerm1.setState(PinState.HIGH);
        ledVerd1.setState(PinState.LOW);
        ledAmar1.setState(PinState.LOW);
        ledVerd2.setState(PinState.LOW);
        ledVerm2.setState(PinState.HIGH);
        ledAmar2.setState(PinState.LOW);
        Thread.sleep(2000);
    }
    static void Verde(int via) throws InterruptedException{
        int x;//verificaçãoapós abrir
        if(via==1){
            ledVerm1.setState(PinState.LOW);
            ledVerd1.setState(PinState.HIGH);
            Thread.sleep(10000);
            x=4;
            do{
                if(detector.detectar(via) && x>0){
                    Thread.sleep(5000);
                    x-=1;
                }
                else{
                    x=0;
                }
            }while(x>0);
            ledVerd1.setState(PinState.LOW);
            ledAmar1.setState(PinState.HIGH);
            Thread.sleep(5000);
            ledAmar1.setState(PinState.LOW);
            ledVerm1.setState(PinState.HIGH);
        }
        else{
            if(via==2){
                ledVerm2.setState(PinState.LOW);
                ledVerd2.setState(PinState.HIGH);
                Thread.sleep(10000);
                x=4;
                do{
                    if(detector.detectar(via) && x>0){
                        Thread.sleep(5000);
                        x-=1;
                    }
                    else{
                        x=0;
                    }
                }while(x>0);
                ledVerd2.setState(PinState.LOW);
                ledAmar2.setState(PinState.HIGH);
                Thread.sleep(5000);
                ledAmar2.setState(PinState.LOW);
                ledVerm2.setState(PinState.HIGH);
            }
        }
        Thread.sleep(2000);
    }  
}
