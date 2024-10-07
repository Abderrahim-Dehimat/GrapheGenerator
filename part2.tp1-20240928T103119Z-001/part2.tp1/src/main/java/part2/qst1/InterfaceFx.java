package part2.qst1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class InterfaceFx extends Application {
    private main_prt1 engine;
    private File[] javaFiles;
    
    @Override
    public void start(Stage primaryStage) {
        engine = new main_prt1();
        
        
        // Project Path
        String projectDirectoryPath = "/home/kaouther/Téléchargements/visitorDesignPattern";
        engine.analyzeProject(projectDirectoryPath);
        
        javaFiles = engine.listJavaFiles(new File(projectDirectoryPath)).toArray(new File[0]);


        primaryStage.setTitle("AST Analysis");

        // Create BorderPane layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Create Top Section with buttons
        VBox buttonBox = new VBox(10); // Vertical box with spacing
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc;");

        // Create buttons for each case
        Button btn1 = new Button("Nombre de classes");
        Button btn2 = new Button("Nombre de méthodes");
        Button btn3 = new Button("Nombre total de lignes de code");
        Button btn4 = new Button("Nombre moyen de méthodes par classe");
        Button btn13 = new Button("Nombre total de packages de l’application");
        Button btn5 = new Button("Nombre moyen de lignes de code par méthode");
        Button btn6 = new Button("Nombre moyen d'attributs par classe");
        Button btn7 = new Button("Les 10% des classes qui possèdent le plus grand nombre de méthodes");
        Button btn8 = new Button("Les 10% des classes qui possèdent le plus grand nombre d’attributs");
        Button btn9 = new Button("Les classes qui font partie en même temps des deux catégories précédentes");
        Button btn10 = new Button("Les	classes	qui	possèdent plus	de X methodes");
        Button btn14 = new Button("Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par \r\n" + "classe)");
        Button btn15 = new Button("Le nombre maximal de paramètres par rapport à toutes les méthodes	de\r\n"+ "l’application)");
        Button btn11 = new Button("Generer AST");
        Button btn12 = new Button("Generer le Graphe d'appel");

        // Add buttons to the buttonBox
        buttonBox.getChildren().addAll(btn1, btn2, btn3, btn13, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn14, btn15, btn11, btn12);

        // Set button actions
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);
        outputArea.setStyle("-fx-font-size: 14px;");

        // Assign button actions to their respective functions
        btn1.setOnAction(e -> outputArea.setText("Nombre de classes dans l'application: " + main_prt1.classCount));
        btn2.setOnAction(e -> outputArea.setText("Nombre de méthodes dans l'application: " + main_prt1.methodCount));
        btn3.setOnAction(e -> outputArea.setText("Nombre total de lignes de code dans l'application: " + main_prt1.totalLinesOfCode));
        btn13.setOnAction(e -> {
            StringBuilder output = new StringBuilder("Nombre total de packages :\n");
            try {
                int totalPackages = main_prt1.countTotalPackages(javaFiles); // Appel direct de la méthode
                output.append(totalPackages).append("\n");
            } catch (IOException ex) {
                output.append("Erreur lors de l'analyse des fichiers.\n");
            }
            outputArea.setText(output.toString());
        });
        btn4.setOnAction(e -> outputArea.setText("Nombre moyen de méthodes par classe: " + (main_prt1.methodCount / (double) main_prt1.classCount)));
        btn5.setOnAction(e -> outputArea.setText("Nombre moyen de lignes de code par méthode: " + (main_prt1.totalLinesOfCode / (double) main_prt1.methodCount)));
        btn6.setOnAction(e -> outputArea.setText("Nombre moyen d'attributs par classe: " + (main_prt1.totalAttributes / (double) main_prt1.classCount)));
        btn7.setOnAction(e -> {
            StringBuilder output = new StringBuilder("Les	10%	des	classes	qui	possèdent	le	plus	grand	nombre	de	méthodes:\n");
            engine.displayTopClassesByMethodCount(output);
            outputArea.setText(output.toString());
        });
        btn8.setOnAction(e -> {
            StringBuilder output = new StringBuilder("Les 10% des classes	qui	possèdent le plus grand nombre d’attributs:\n");
            engine.displayTopClassesByAttributeCount(output);
            outputArea.setText(output.toString());
        });
        btn9.setOnAction(e -> {
            StringBuilder output = new StringBuilder("Les classes qui font partie en même temps des deux catégories précédentes.:\n");
            engine.displayClassesInBothTopCategories(output);
            outputArea.setText(output.toString());
        });
        btn10.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input");
            dialog.setHeaderText("Entrez la valeur de X:");
            dialog.setContentText("X:");
            dialog.showAndWait().ifPresent(x -> {
                try {
                    int intValue = Integer.parseInt(x);
                    StringBuilder output = new StringBuilder("Les	classes	qui	possèdent plus	de " + intValue + " méthodes:\n");
                    engine.displayClassesWithMoreThanXMethods(intValue, output);
                    outputArea.setText(output.toString());
                } catch (NumberFormatException ex) {
                    outputArea.setText("Veuillez entrer un numéro valide.\n");
                }
            });
        });

     // Button 11 Action: Generate and display the AST
        btn11.setOnAction(e -> {
            // Step 1: Generate the DOT representation and the AST image
            String dotRepresentation = engine.generateDotRepresentation();  // Assuming this method generates the DOT string for AST
            engine.generateDotFile(dotRepresentation, "project_ast");  // Generate the .dot file

            // Step 2: Convert the .dot to PNG using Graphviz (Ensure 'dot' is installed and available in PATH)
            try {
                Process process = new ProcessBuilder("dot", "-Tpng", "project_ast.dot", "-o", "project_ast_ast.png").start();
                process.waitFor();  // Wait for the conversion process to complete
            } catch (IOException | InterruptedException ex) {
                outputArea.setText("Error generating AST PNG: " + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            // Step 3: Set up the path to the generated PNG file
            File astImageFile = new File("project_ast_ast.png");

            if (astImageFile.exists()) {
                // Load the generated AST PNG file into an ImageView
                Image astImage = new Image(astImageFile.toURI().toString());
                ImageView imageView = new ImageView(astImage);

                // Set the size of the ImageView to fit the window
                imageView.setFitWidth(800);
                imageView.setFitHeight(800);
                imageView.setPreserveRatio(true); // Preserve aspect ratio

                // Display the ImageView in the center of the layout
                root.setCenter(imageView);

                // Show a message below the image
                outputArea.setText("AST generated and displayed.");
            } else {
                outputArea.setText("AST image not found. Please check if the PNG was generated correctly.");
            }
        });

        
        
     // Button 12 Action: Generate and display the Call Graph
        btn12.setOnAction(e -> {
            // Step 1: Generate the DOT representation for the Call Graph
            String callGraphDot = engine.generateCallGraphRepresentation();  // Assuming this method generates the DOT string for the Call Graph
            engine.generateDotFile(callGraphDot, "project_call_graph");  // Generate the .dot file for Call Graph

            // Step 2: Convert the .dot to PNG using Graphviz (Ensure 'dot' is installed and available in PATH)
            try {
                Process process = new ProcessBuilder("dot", "-Tpng", "project_call_graph.dot", "-o", "project_call_graph_call_graph.png").start();
                process.waitFor();  // Wait for the conversion process to complete
            } catch (IOException | InterruptedException ex) {
                outputArea.setText("Error generating Call Graph PNG: " + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            // Step 3: Set up the path to the generated PNG file for the Call Graph
            File callGraphImageFile = new File("project_call_graph_call_graph.png");

            if (callGraphImageFile.exists()) {
                // Load the generated Call Graph PNG file into an ImageView
                Image callGraphImage = new Image(callGraphImageFile.toURI().toString());
                ImageView imageView = new ImageView(callGraphImage);

                // Set the size of the ImageView to fit the window
                imageView.setFitWidth(800);
                imageView.setFitHeight(800);
                imageView.setPreserveRatio(true); // Preserve aspect ratio

                // Display the ImageView in the center of the layout
                root.setCenter(imageView);

                // Show a message below the image
                outputArea.setText("Call Graph generated and displayed.");
            } else {
                outputArea.setText("Call Graph image not found. Please check if the PNG was generated correctly.");
            }
        });

        
        btn14.setOnAction(e -> {
            StringBuilder output = new StringBuilder("Les 10% des méthodes ayant le plus de lignes de code :\n");
            try {
                List<String> topMethods = engine.topMethodsByLines2(javaFiles, 10); // Appel direct de la méthode
                for (String method : topMethods) {
                    output.append(method).append("\n");
                }
            } catch (IOException ex) {
                output.append("Erreur lors de l'analyse des fichiers.\n");
            }
            outputArea.setText(output.toString());
        });
        
        btn15.setOnAction(e -> {
            StringBuilder output = new StringBuilder("Le nombre maximal de paramètres par rapport à toutes les méthodes de lapplication: \n");
            try {
                int maxParams = engine.maxParameters(javaFiles);  // Appel direct de la méthode
                output.append(maxParams).append("\n");
            } catch (IOException ex) {
                output.append("Erreur lors de l'analyse des fichiers.\n");
            }
            outputArea.setText(output.toString());
        });




        // Add the button box and output area to the layout
        root.setLeft(buttonBox);
        root.setBottom(outputArea);

        // Set the scene with a larger window size
        Scene scene = new Scene(root, 1600, 1200); // Increase the scene size
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
