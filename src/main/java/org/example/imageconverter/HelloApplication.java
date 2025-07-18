package org.example.imageconverter;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class HelloApplication extends Application {

    private void updateConditionalControls(SelectedImage selImg, ConversionHandler ch, ColorPicker colourPicker, TextField pageSelector) {
        String originalType = ch.getFileExtension(selImg.getFileAddress());

        String fileType = "";
        if (ch.getTargetType() != null){
            fileType = ch.getTargetType();
        }

        boolean enableColourPicker = originalType.equals(".png") && (fileType.equals(".jpg") || fileType.equals(".bmp"));
        boolean enablePageSelector = originalType.equals(".pdf");

        colourPicker.setDisable(!enableColourPicker);
        pageSelector.setDisable(!enablePageSelector);
    }

    @Override
    public void start(Stage stage) {
        // Initialisation of stuff, classes, ui, etc
        String[] imageTypeList = {".jpg", ".png", ".webp", ".bmp", ".gif", ".pdf", ".JPG", ".PNG", ".WEBP", ".BMP", ".GIF", ".PDF"};
        SelectedImage selImg = new SelectedImage();
        ConversionHandler ch = new ConversionHandler();
        ImageView iv = new ImageView();
        Image placeholder = new Image("/Placeholder.png");
        Image placeholderActive = new Image("/PlaceholderActive.png");
        Image pdfImage = new Image("/pdfImage.png");
        Button addFile = new Button("Add File");
        ChoiceBox<String> imageType = new ChoiceBox<>(FXCollections.observableArrayList(".jpg",".png", ".webp", ".bmp", ".gif"));
        Button convert = new Button("Convert!");
        ColorPicker colourPicker = new ColorPicker();

        //Labels:
        Label title = new Label("Image");
        Label titleHighlighted = new Label("Converter");
        Label supportedTypes = new Label("Supported File Types: .png, .jpg, .jpeg, .webp, .gif, .bmp, .pdf");
        Label filepath = new Label("...");
        Label target = new Label("Target Format:");
        Label bgColour = new Label("Select Background Colour:");
        Label selPage = new Label("Select Page:");

        iv.setFitHeight(200);
        iv.setFitWidth(200);
        iv.setImage(placeholder);
        imageType.setDisable(true);
        imageType.setValue("...");
        colourPicker.setMaxWidth(50);
        TextField pageSelector = new TextField();
        pageSelector.setMaxWidth(40);
        colourPicker.setDisable(true);
        pageSelector.setDisable(true);
        convert.setDisable(true);

        title.getStyleClass().add("title");
        titleHighlighted.getStyleClass().addAll("title", "highlighted");
        supportedTypes.getStyleClass().add("extra");
        filepath.getStyleClass().addAll("highlighted", "extra");

        iv.setOnDragOver( event -> {
            iv.setImage(placeholderActive);
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasImage() || dragboard.hasFiles()){
                String draggedType = ch.getFileExtension(dragboard.getFiles().getFirst());
                boolean typeCheck = false;
                for (String type : imageTypeList){
                    if (draggedType.equals(type)){
                        typeCheck = true;
                        break;
                    }
                }
                if (typeCheck){
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
        });

        iv.setOnDragExited(_ ->{
            if (selImg.getImage() == null){iv.setImage(placeholder);}
            else{iv.setImage(selImg.getImage());}
        });

        iv.setOnDragDropped(event ->{
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasImage() || dragboard.hasFiles()){
                try{
                    String draggedType = ch.getFileExtension(dragboard.getFiles().getFirst());
                    if (draggedType.equals(".pdf")){
                        selImg.setImage(pdfImage);
                    }
                    else{
                        selImg.setImage(new Image(new FileInputStream(dragboard.getFiles().getFirst())));
                    }

                    filepath.setText(dragboard.getFiles().getFirst().toString());
                    imageType.setDisable(false);
                    selImg.setFileAddress(dragboard.getFiles().getFirst());
                    updateConditionalControls(selImg, ch, colourPicker, pageSelector);
                    iv.setImage(selImg.getImage());

                }
                catch (Exception e){
                    System.out.println("Error: " + e);
                }
            }
            else{
                iv.setImage(placeholder);
            }
        });

        // When the add file button is pressed, a new file chooser is created where the image is selected
        addFile.setOnAction(_ -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select the file you want to convert:");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.jpeg", "*.jpg", "*.JPG", "*.png", "*.PNG", "*.webp", "*.bmp", "*.gif", "*.pdf"));
            File imageFile = fc.showOpenDialog(stage);
            System.out.println(imageFile);

            // Once the image is selected, it is displayed in the image viewer. The selected image file address is updated and the convert button is enabled
            if (imageFile != null){
                selImg.setImage(new Image(imageFile.toURI().toString()));
                iv.setImage(selImg.getImage());
                filepath.setText(imageFile.toString());
                imageType.setDisable(false);
                selImg.setFileAddress(imageFile);
                String selectedType = ch.getFileExtension(selImg.getFileAddress());
                if (selectedType.equals(".pdf")){
                    iv.setImage(pdfImage);
                }

                updateConditionalControls(selImg, ch, colourPicker, pageSelector);

            }
        });

        // If the targeted image type changes, the listener updates the targeted file type in the conversion handler
        imageType.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> _, Number _, Number new_val) -> {
                    ch.setTargetType(imageTypeList[new_val.intValue()]);
                    updateConditionalControls(selImg, ch, colourPicker, pageSelector);

                    if (selImg.getImage() != null){
                        convert.setDisable(false);
                    }

                });

        // When the convert button is pressed a new file chooser is created to store the location of the target file thing
        convert.setOnAction(_ ->{
            try{
                int index;
                BufferedImage bufferedImage;
                if (ch.getFileExtension(selImg.getFileAddress()).equals(".pdf")) {
                    try {
                        PDDocument document = Loader.loadPDF(selImg.getFileAddress());
                        PDFRenderer renderer = new PDFRenderer(document);
                        String input = pageSelector.getText().trim();
                        index = Integer.parseInt(input);

                        if (index <= 0 || (document.getNumberOfPages() != 1 && index > document.getNumberOfPages() - 1)){
                            Alert a = new Alert(Alert.AlertType.WARNING);
                            a.setContentText("Please enter a number that is within the number of pages of the file.");
                            a.showAndWait();
                            return;
                        }

                        bufferedImage = renderer.renderImage(index - 1);
                    } catch (NumberFormatException e) {
                        Alert a = new Alert(Alert.AlertType.WARNING);
                        a.setContentText("Please enter a valid page number.");
                        a.showAndWait();
                        return;
                    }
                }
                else{
                    bufferedImage = ImageIO.read(selImg.getFileAddress());
                }
                FileChooser fc = new FileChooser(); //file chooser
                String imgType = ch.getTargetType();// string that stores what the images want to be converted to

                fc.setTitle("Select where you want the photo to be saved:");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(imgType, "*" + imgType));

                File saveLocation = fc.showSaveDialog(stage); //address of file

                if (saveLocation != null){
                    System.out.println(selImg.getFileAddress());
                    System.out.println("IMAGE CONVERTING...");
                    ch.startConversion(bufferedImage, saveLocation); // Begins conversion

                }
            }
            catch (Exception ioe){
                System.out.println("Error: " + ioe);
            }

        });

        colourPicker.setOnAction(_ -> {
            Color c = colourPicker.getValue();
            ch.setColour((float) c.getRed(),(float) c.getGreen(),(float) c.getBlue());
        });

        // ui stuff

        HBox mainTitle = new HBox();
        mainTitle.setSpacing(10);
        mainTitle.setAlignment(Pos.CENTER);
        mainTitle.getChildren().addAll(title, titleHighlighted);

        HBox options = new HBox();
        options.setSpacing(10);
        options.setAlignment(Pos.CENTER);
        options.getChildren().addAll(bgColour,colourPicker,selPage,pageSelector);

        HBox targetType = new HBox();
        targetType.setSpacing(10);
        targetType.setAlignment(Pos.CENTER);
        targetType.getChildren().addAll(target, imageType);

        VBox layout = new VBox(5);
        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                mainTitle,
                supportedTypes,
                addFile,
                iv,
                filepath,
                targetType,
                convert,
                options
                );


        Scene scene = new Scene(layout, 500, 500);

        var styleUrl = getClass().getResource("/style.css");
        if (styleUrl != null) {
            scene.getStylesheets().add(styleUrl.toExternalForm());
        } else {
            System.err.println("style.css not found.");
        }
        stage.setTitle("Image Converter");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }



    public static void main(String[] args) {
        launch();
    }
}
