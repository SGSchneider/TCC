/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semaforo;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author pi
 */
public class Ouvinte implements IMqttMessageListener {
    String mensagem;
    Comunicador com;
    Ouvinte(Comunicador c){
        com = c;
    }
    @Override
    public void messageArrived(String topico, MqttMessage mm) throws Exception {
                    mensagem = new String(mm.getPayload());
                    com.setMsg(mensagem);
                }
    public void inicio(){
        com.subscribe(1,this,"SA/");
    }
}
