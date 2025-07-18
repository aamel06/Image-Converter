package org.example.imageconverter;

import javafx.scene.control.Alert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ConversionHandler {

    private String fileType;
    private java.awt.Color bgColour;

    public ConversionHandler(){

    }

    public void setColour(float r, float g, float b){
        bgColour = new Color(r,g,b);
        System.out.println(bgColour);
    }

    public void setTargetType(String ft){
        fileType = ft;
    }

    public String getTargetType(){
        return fileType;
    }

    public String getFileExtension(File filename){
        String fileAsString = filename.toString();
        if (fileAsString == null){
            return null;
        }
        int dotIndex = fileAsString.lastIndexOf(".");
        if(dotIndex >= 0){
            return fileAsString.substring(dotIndex);
        }
        return "";
    }

    public void startConversion(BufferedImage img, String originalType,File saveLocation){

        if (originalType.equals(".png") && ((fileType.equals(".jpg")) || (fileType.equals(".bmp")))){
            fromPNGConverter(img, saveLocation);
        }
        else{
            convert(img, saveLocation);
        }
    }

    public void fromPNGConverter(BufferedImage img, File saveLocation){
        BufferedImage newImage = new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        newImage.createGraphics().drawImage(img,0,0, bgColour,null);
        convert(newImage, saveLocation);


    }

    public void convert(BufferedImage newImage, File saveLocation){
        try{
            String fileExt = fileType.substring(1);
            boolean success = ImageIO.write(newImage, fileExt, saveLocation);
            if (!success) {
                System.out.println("ImageIO could not write image in format: " + fileType);
            }
            else{
                System.out.println("Image converted.");
                imageConvertedAlert();
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e);
        }
    }

    public void imageConvertedAlert(){
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("Image Converted!");
        a.showAndWait();
    }

}
