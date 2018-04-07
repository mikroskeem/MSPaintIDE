package com.uddernetworks.mspaint.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class Test extends Application implements Initializable {

    @FXML
    private TextField inputName;
    @FXML
    private JFXTextField highlightedImage;
    @FXML
    private JFXTextField cacheFile;
    @FXML
    private JFXTextField classOutput;
    @FXML
    private JFXTextField compiledJarOutput;
    @FXML
    private JFXTextField libraryFile;
    @FXML
    private JFXTextField otherFiles;
    @FXML
    private JFXTextField letterDirectory;
    @FXML
    private JFXTextField compilerOutputValue;
    @FXML
    private JFXTextField programOutputValue;

    @FXML
    private JFXButton changeInputImage;
    @FXML
    private JFXButton changeHighlightImage;
    @FXML
    private JFXButton changeCacheFile;
    @FXML
    private JFXButton changeClassOutput;
    @FXML
    private JFXButton changeCompiledJar;
    @FXML
    private JFXButton changeLibraries;
    @FXML
    private JFXButton changeOtherFiles;
    @FXML
    private JFXButton changeLetterDir;
    @FXML
    private JFXButton compilerOutput;
    @FXML
    private JFXButton programOutput;

    @FXML
    private JFXProgressBar progress;

    @FXML
    private Label statusText;

    private Main main;
    private Stage primaryStage;

    private FileFilter imageFilter = new FileNameExtensionFilter("Image files", "png");
    private FileFilter txtFilter = new FileNameExtensionFilter("Text document", "txt");
    private FileFilter jarFilter = new FileNameExtensionFilter("JAR Archive", "jar");

    public Test() throws IOException, URISyntaxException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        System.out.println("WTFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

        this.main = new Main();
        main.start(this);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.out.println("1111111111111111");

        System.out.println("222222222222222");

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        registerThings();
    }

    public void setStatusText(String text) {
        statusText.setText(text);
    }

    public void registerThings() throws IOException {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Test.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add("style.css");

        primaryStage.setScene(scene);

        primaryStage.setTitle("MS Paint IDE");
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("ms-paint-logo.png")));

        primaryStage.show();

        TextArea node = (TextArea) scene.lookup("#output");
        node.setText("Text here");

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            builder.append("Line #" + i).append("\n");
        }

        node.setText(builder.toString());
    }

    @FXML
    private void changePathButton(ActionEvent event) {

    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputName.setText(main.getInputImage());
        highlightedImage.setText(main.getHighlightedFile());
        cacheFile.setText(main.getObjectFile());
        compiledJarOutput.setText(main.getJarFile());
        libraryFile.setText(main.getLibraryFile());
        otherFiles.setText(main.getOtherFiles());
        classOutput.setText(main.getClassOutput());
        letterDirectory.setText(main.getLetterDirectory());
        compilerOutputValue.setText(main.getCompilerOutput());
        programOutputValue.setText(main.getAppOutput());

        changeInputImage.setOnAction(event -> {
            File selected = main.getInputImage().isEmpty() ? main.getCurrentJar() : new File(main.getInputImage());
            FileDirectoryChooser.openFileChoser(selected, imageFilter, JFileChooser.FILES_AND_DIRECTORIES, file -> {
                inputName.setText(file.getAbsolutePath());
                main.setInputImage(file);
            });
        });

        changeHighlightImage.setOnAction(event -> {
            File selected = main.getHighlightedFile().isEmpty() ? main.getCurrentJar() : new File(main.getHighlightedFile());
            FileDirectoryChooser.openFileChoser(selected, null, JFileChooser.DIRECTORIES_ONLY, file -> {
                highlightedImage.setText(file.getAbsolutePath());
                main.setHighlightedFile(file);
            });
        });

        changeCacheFile.setOnAction(event -> {
            File selected = main.getObjectFile().isEmpty() ? main.getCurrentJar() : new File(main.getObjectFile());
            FileDirectoryChooser.openFileChoser(selected, null, JFileChooser.DIRECTORIES_ONLY, file -> {
                cacheFile.setText(file.getAbsolutePath());
                main.setObjectFile(file);
            });
        });

        changeClassOutput.setOnAction(event -> {
            File selected = main.getClassOutput().isEmpty() ? main.getCurrentJar() : new File(main.getClassOutput());
            FileDirectoryChooser.openFileChoser(selected, null, JFileChooser.DIRECTORIES_ONLY, file -> {
                classOutput.setText(file.getAbsolutePath());
                main.setClassOutput(file);
            });
        });

        changeCompiledJar.setOnAction(event -> {
                    File selected = main.getJarFile().isEmpty() ? main.getCurrentJar() : new File(main.getJarFile());
                    FileDirectoryChooser.openFileChoser(selected, jarFilter, JFileChooser.FILES_ONLY, file -> {
                        compiledJarOutput.setText(file.getAbsolutePath());
                        main.setJarFile(file);
                    });
                });

        changeLibraries.setOnAction(event -> {
            File selected = main.getLibraryFile().isEmpty() ? main.getCurrentJar() : new File(main.getLibraryFile());
            FileDirectoryChooser.openFileChoser(selected, null, JFileChooser.FILES_AND_DIRECTORIES, file -> {
                libraryFile.setText(file.getAbsolutePath());
                main.setLibraryFile(file);
            });
        });

        changeOtherFiles.setOnAction(event -> {
            File selected = main.getOtherFiles().isEmpty() ? main.getCurrentJar() : new File(main.getOtherFiles());
            FileDirectoryChooser.openFileChoser(selected, null, JFileChooser.FILES_AND_DIRECTORIES, file -> {
                otherFiles.setText(file.getAbsolutePath());
                main.setOtherFiles(file);
            });
        });

        changeLetterDir.setOnAction(event -> {
            File selected = main.getLetterDirectory().isEmpty() ? main.getCurrentJar() : new File(main.getLetterDirectory());
            FileDirectoryChooser.openFileChoser(selected, null, JFileChooser.DIRECTORIES_ONLY, file -> {
                letterDirectory.setText(file.getAbsolutePath());
                main.setLetterDirectory(file);
            });
        });

        compilerOutput.setOnAction(event -> {
            File selected = main.getCompilerOutput().isEmpty() ? main.getCurrentJar() : new File(main.getCompilerOutput());
            FileDirectoryChooser.openFileChoser(selected, imageFilter, JFileChooser.FILES_ONLY, file -> {
                compilerOutputValue.setText(file.getAbsolutePath());
                main.setCompilerOutput(file);
            });
        });

        programOutput.setOnAction(event -> {
            File selected = main.getAppOutput().isEmpty() ? main.getCurrentJar() : new File(main.getAppOutput());
            FileDirectoryChooser.openFileChoser(selected, imageFilter, JFileChooser.FILES_ONLY, file -> {
                programOutputValue.setText(file.getAbsolutePath());
                main.setAppOutput(file);
            });
        });
    }
}
