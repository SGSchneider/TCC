/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semaforo;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 *
 * @author Stefan Schneider
 */
class Comunicador{
    int flag = 0;
    int[] fila;
    private MqttCallback mqttCB;
    private final String server, senha;
    private MqttClient client;
    private final MqttConnectOptions mqttOptions;
    private Ouvinte gestor;
    Comunicador(int[] f){
        fila = f;
        server = "tcp://192.168.137.1:8883";
        senha ="sma123";
        mqttOptions = new MqttConnectOptions();
        mqttOptions.setMaxInflight(200);
        mqttOptions.setConnectionTimeout(3);
        mqttOptions.setKeepAliveInterval(10);
        mqttOptions.setAutomaticReconnect(true);
        mqttOptions.setCleanSession(false);
        mqttOptions.setUserName("semaforoa");
        mqttOptions.setPassword(senha.toCharArray());
        gestor = new Ouvinte(this);
        iniciar();
        new Thread() {
            @Override
            public void run() {
            System.out.println("Done!");
            gestor.inicio();
            System.out.println("Sub Done!");
            }
        }.start();
    }   
                public IMqttToken subscribe(int qos, IMqttMessageListener gestorMQTT, String... topicos) {
                    if (client == null || topicos.length == 0) {
                        return null;
                    }
                    int tamanho = topicos.length;
                    int[] qoss = new int[tamanho];
                    IMqttMessageListener[] listners = new IMqttMessageListener[tamanho];

                    for (int i = 0; i < tamanho; i++) {
                        qoss[i] = qos;
                        listners[i] = gestorMQTT;
                    }
                    try {
                        return client.subscribeWithResponse(topicos, qoss, listners);
                    } catch (MqttException ex) {
                        System.out.println(String.format("Erro ao se inscrever no tópico", ex));
                        return null;
                    }
                }
                public void iniciar() {
                    int x=1;
                    do{
                        try {
                            System.out.println("Conectando no broker MQTT em " + server);
                            client = new MqttClient(
                                        server,
                                        String.format("cliente_java_%d", System.currentTimeMillis()),
                                        new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"))
                                    );
                            client.setCallback(mqttCB);
                            client.connect(mqttOptions);
                            x=0;
                        } catch (MqttException ex) {
                            System.out.println("Erro ao se conectar ao broker mqtt " + server + " - " + ex);
                            x=1;
                        }
                    }while(x>0);
                }

                public void publicar(String topic, byte[] payload, int qos) {
                    publicar(topic, payload, qos, false);
                }

                public synchronized void publicar(String topic, byte[] payload, int qos, boolean retained) {
                    try {
                        if (client.isConnected()) {
                            client.publish(topic, payload, qos, retained);
                            System.out.println(String.format("Tópico %s publicado. %dB", topic, payload.length));
                        } else {
                            System.out.println("Cliente desconectado, não foi possível publicar o tópico " + topic);
                        }
                    } catch (MqttException ex) {
                        System.out.println("Erro ao publicar " + topic + " - " + ex);
                    }
                }
                
                public void setMsg(String m){
                    int x;
                    switch(m){
                        case "1":
                            x=1;
                            break;
                        case "2":
                            x=2;
                            break;
                        case "3":
                            x=3;
                            break;
                        case "4":
                            x=4;
                            break;
                        default:
                            x=0;
                    }
                    do{
                        if(flag==0){
                            flag=1;
                            for(int i = 0; i<10 ; i++){
                                if(fila[i]==0){
                                    fila[i] = x;
                                    i=10;
                                    System.out.println("Fila:"+fila[0]+", "+fila[1]+", "+fila[2]+", "+fila[3]+", "+fila[4]+", "+fila[5]+", "+fila[6]+", "+fila[7]+", "+fila[8]+", "+fila[9]);
                                }else{
                                    if(fila[i]==x){
                                        i=10;
                                        System.out.println("Fila:"+fila[0]+", "+fila[1]+", "+fila[2]+", "+fila[3]+", "+fila[4]+", "+fila[5]+", "+fila[6]+", "+fila[7]+", "+fila[8]+", "+fila[9]);
                                    }
                                }
                            }
                            flag=10;
                        }
                    }while(flag!=10);
                    flag=0;
                }

                public void andaFila(){
                    do{
                        if(flag==0){
                            flag=1;
                            for(int i=1;i<10;i++){
                                fila[i-1]=fila[i];
                            }
                            flag=5;
                        }
                    }while(flag!=5);
                    flag=0;
                }
                
                    
}