package semaforo;

import boofcv.alg.background.BackgroundModelStationary;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ContourPacked;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.shapes.polygon.DetectPolygonBinaryGrayRefine;
import boofcv.factory.background.ConfigBackgroundBasic;
import boofcv.factory.background.ConfigBackgroundGmm;
//import boofcv.factory.background.ConfigBackgroundGmm;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.factory.shape.ConfigPolygonDetector;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.io.MediaManager;
import boofcv.io.UtilIO;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.gui.image.ImageGridPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import static boofcv.io.image.ConvertBufferedImage.convertTo;
import static boofcv.io.image.ConvertBufferedImage.extractBuffered;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import georegression.struct.shapes.Polygon2D_F64;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


/**
 *
 * @author Stefan Schneider
 */


final class Monitorador {
    int tamanhox, tamanhoy, xini, yini;
    // Tempo de abertura;
    int Xs, Vmax;
    //Ma = Tmuv + (Xs – Xmuv) /Vmax
    Comunicador comunicador;
    Camera cam;
    String filev, filec, topico, msg;
    ConfigBackgroundGmm configGmm;
    BackgroundModelStationary background;
    MediaManager media;
    SimpleImageSequence video;
    ImageType imageType;
    Monitorador(Comunicador cm, Camera c, int x, int tx, int y, int ty){
        tamanhox = tx;
        tamanhoy = ty;
        xini = x;
        yini = y;
        comunicador = cm;
        cam = c;
        Xs = 40;  //40m
        Vmax = 13;//13m/s ~= 50km/h
        msg = "2";
        filev = "D:\\Documentos\\TCC\\vidM.mp4";
        filec = "D:\\Documentos\\TCC\\imgM.png";
        topico = "SB/"/*Semáforo A*//*  "SA/"  /*Semáforo B*/;
        configGmm = new ConfigBackgroundGmm();
        configGmm.initialVariance = 700f;
        configGmm.decayCoefient = 3f;
        configGmm.learningPeriod = 15000.0f;
        imageType = ImageType.single(GrayF32.class);
        media = DefaultMediaManager.INSTANCE;
        background = FactoryBackgroundModel.stationaryGmm(configGmm, imageType);
        new Thread() {
            @Override
            public void run() {
            do{
                backSub();
            }while(true);
            }
        }.start();  
    }
    
    void backSub() {
        //Grava
        try {cam.capvid(0);} catch (InterruptedException ex) {}
        //Abre o arquivo de vídeo
        String fileName = UtilIO.pathExample(filev);
	    video =
            media.openVideo(fileName, background.getImageType());
        //converte para GrayU8
	    GrayU8 segmented =
            new GrayU8(video.getNextWidth(),video.getNextHeight());
        //recorta a área necessária
        GrayU8 area = segmented.subimage(xini,
                                        yini,
                                        (xini+tamanhox),
                                        (yini+tamanhoy));
	    BufferedImage visualized = new BufferedImage(area.width,
                                                    area.height,
                                                    BufferedImage.TYPE_INT_RGB);
	    while( video.hasNext() ) {
            ImageBase input = video.next();
            //atualiza o background
            background.updateBackground(input.subimage(xini,
                                                      yini,
                                                      (xini+tamanhox),
                                                      (yini+tamanhoy)),
                                                      area);
            //renderiza a imagem binária
            VisualizeBinaryData.renderBinary(area, false, visualized);
            //inverte as cores
            invert(visualized);
            //detecta se houve alteração
            if(detect()){
                //aplica a fórmula
                try {Thread.sleep((Xs/Vmax)*1000);} catch (InterruptedException e) {}
                //envia a mensagem para o outro semáforo
            comunicador.publicar(topico, msg.getBytes(), 1);
                return;
            }
	    }
    }
    
    public void invert(BufferedImage img) {
	BufferedImage image = img;
	// converte a imagem pra GrayF32
	GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
	GrayU8 binary = new GrayU8(input.width,input.height);               
	// Seleciona o Threshold
	double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);      
	// Aplica o Threshold
	ThresholdImageOps.threshold(input,binary,(float)threshold,true);
	//Filtra pequenas imperfeições
	GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
	filtered = BinaryImageOps.dilate8(filtered, 1, null);
        //Converte novamente para BufferedImage
        BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered, false, null);
        try {
            //Salva o Resultado
            ImageIO.write(visualFiltered, "PNG", new File(filec));
            try {Thread.sleep(5);} catch (InterruptedException e) {}
        } catch (IOException ex) {}
    }
    
    public boolean detect() {
		String image = filec;
		ConfigPolygonDetector config = new ConfigPolygonDetector(3,12);
                config.detector.contourToPoly.convex = false;
		DetectPolygonBinaryGrayRefine<GrayU8> detector = FactoryShapeDetector.polygon(config, GrayU8.class);
		if(processImages(image, detector)){
                return(true);
                }
                else{
                    return(false);
                }
	}

	private static boolean processImages(String file,DetectPolygonBinaryGrayRefine<GrayU8> detector)
	{
			BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(file));
			GrayU8 input = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
			GrayU8 binary = new GrayU8(input.width,input.height);
			int threshold = (int)GThresholdImageOps.computeOtsu(input, 0, 255);
			ThresholdImageOps.threshold(input, binary, threshold, true);
			detector.process(input, binary);
			List<Polygon2D_F64> found = detector.getPolygons(null,null);
                        List<ContourPacked> foundC = detector.getAllContours();
                        if(found.size()>0 || foundC.size()>0){
                            return(true);
                        }
                        else{
                            return(false);
                        }
		
	}
}


