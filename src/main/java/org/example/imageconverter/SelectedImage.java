package org.example.imageconverter;

import java.io.File;
import javafx.scene.image.Image;

public class SelectedImage {
    File imgFile;
    Image image;

    public void setImage(Image img){
        image = img;
    }

    public Image getImage(){
        return image;
    }

    public void setFileAddress(File f){
        imgFile = f;
    }

    public File getFileAddress(){
        return imgFile;
    }
}
