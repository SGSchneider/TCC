/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semaforo;

import boofcv.alg.background.BackgroundModelStationary;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.ContourPacked;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.shapes.polygon.DetectPolygonBinaryGrayRefine;
import boofcv.factory.background.ConfigBackgroundGmm;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.factory.shape.ConfigPolygonDetector;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ImageGridPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.MediaManager;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import static boofcv.io.image.ConvertBufferedImage.convertTo;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.image.UtilImageIO;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import georegression.struct.shapes.Polygon2D_F64;
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

class Detector { 
    static Camera cam;
    static int[] tamanhox, tamanhoy, xini, yini;
    static String filev, filec;
    static ConfigBackgroundGmm configGmm;
    static BackgroundModelStationary background[];
    static BufferedImage imagem;
    static MediaManager media;
    static ImageType imageType;
    static SimpleImageSequence video;

    Detector(Camera c, int[] x, int[] tx, int[] y, int[] ty, int nvia){
        background = new BackgroundModelStationary[nvia];
        configGmm = new ConfigBackgroundGmm();
        configGmm.initialVariance = 700f;
        configGmm.decayCoefient = 3f;
        configGmm.learningPeriod = 15000.0f;
        imageType = ImageType.single(GrayF32.class);
        media = DefaultMediaManager.INSTANCE;
        tamanhox = tx;
        tamanhoy = ty;
        xini =x;
        yini = y;
        filev = "D:\\Documentos\\TCC\\vidD.mp4";
        filec = "D:\\Documentos\\TCC\\imgD.png";
        cam = c;
        for(int i=0; i<nvia; i++){
        background[i] = FactoryBackgroundModel.stationaryGmm(configGmm, imageType);}
    }

    public static boolean backSub(int via) {
        new Thread() {
            @Override
            public void run() {
         try {
             //Grava
            cam.capvid(1);
        } catch (InterruptedException ex) {}
            //Abre o arquivo de vídeo
            String fileName = UtilIO.pathExample(filev);
            video = media.openVideo(fileName, background[via].getImageType());
            //converte para GrayU8
		    GrayU8 segmented = new GrayU8(video.getNextWidth(),video.getNextHeight());
		    //recorta a área necessária
		    GrayU8 area = segmented.subimage(xini[via],
                                             yini[via],
                                            (xini[via]+tamanhox[via]),
                                            (yini[via]+tamanhoy[via]));
		    BufferedImage visualized = new BufferedImage(area.width,
                                                        area.height,
                                                        BufferedImage.TYPE_INT_RGB);
			ImageBase input = video.next();
			//atualiza o background
			background[via].updateBackground(input.subimage(xini[via],
                                                            yini[via],
                                                            (xini[via]+tamanhox[via]),
                                                            (yini[via]+tamanhoy[via])),
                                                            area);
			//renderiza a imagem binária
			VisualizeBinaryData.renderBinary(area, false, visualized);
			//inverte as cores
            invert(visualized);
            }}.start();
            //detecta se houve alteração
            if(detect()){
                return(true);
            }else{return(false);}
	}
    
    public static void invert(BufferedImage img) {
		BufferedImage image = img;
		// converte a imagem pra GrayF32
		GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null,
                                                               GrayF32.class);
		GrayU8 binary = new GrayU8(input.width,input.height);               
		// Seleciona o Threshold
		double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);      
		// Aplica o Threshold
		ThresholdImageOps.threshold(input,binary,(float)threshold,true);
		//Filtra pequenas imperfeições
		GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
		filtered = BinaryImageOps.dilate8(filtered, 1, null);
        //Converte novamente para BufferedImage
        BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered,
                                                                        false,
                                                                        null);
        try {
            //Salva o Resultado
            ImageIO.write(visualFiltered, "PNG", new File(filec));
            try {Thread.sleep(5);} catch (InterruptedException e) {}
        } catch (IOException ex) {}
	}
    
    public static boolean detect() {
		String image = filec;
		ConfigPolygonDetector config = new ConfigPolygonDetector(3,12);
                config.detector.contourToPoly.convex = false;
		DetectPolygonBinaryGrayRefine<GrayU8> detector =
                FactoryShapeDetector.polygon(config, GrayU8.class);
		if(processImages(image, detector)){
                return(true);
                }
                else{
                    return(false);
                }
	}

	private static boolean processImages(String file,
                   DetectPolygonBinaryGrayRefine<GrayU8> detector)
	{
	    BufferedImage image =
                UtilImageIO.loadImage(UtilIO.pathExample(file));
		GrayU8 input =
                ConvertBufferedImage.convertFromSingle(image,
                                                        null,
                                                        GrayU8.class);
		GrayU8 binary =
                new GrayU8(input.width,input.height);
		int threshold =
                (int)GThresholdImageOps.computeOtsu(input, 0, 255);
		ThresholdImageOps.threshold(input, binary, threshold, true);
		detector.process(input, binary);
		List<Polygon2D_F64> found =
                detector.getPolygons(null,null);
        List<ContourPacked> foundC =
                detector.getAllContours();
        if(found.size()>0 || foundC.size()>0){
            return(true);
        }else{
            return(false);
        }
	}

    public static boolean detectar(int via){
        if(backSub(via)){
            return(true);
        }else{
            return(false);
        }
    }

}
