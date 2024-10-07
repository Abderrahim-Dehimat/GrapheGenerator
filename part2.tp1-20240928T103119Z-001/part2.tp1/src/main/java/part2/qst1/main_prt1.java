package part2.qst1;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class main_prt1 {
    protected static int classCount = 0;  // Total number of classes
    protected static int methodCount = 0;  // Total number of methods
    protected static int totalLinesOfCode = 0;  // Total number of lines of code
    protected static int totalAttributes = 0;  // Total number of attributes
    protected static int packageCount = 0;  // Total number of packages (not yet implemented)

    protected static final Map<String, Integer> classMethodCount = new HashMap<>();  // Number of methods per class
    protected static final Map<String, Integer> classAttributeCount = new HashMap<>();  // Number of attributes per class
    protected static final Map<String, List<String>> classMethods = new HashMap<>();  // List of methods per class
    
    CustomVisitor visitor = new CustomVisitor();  // Instantiate the custom visitor for analyzing the project

    // Analyze the project directory
    public void analyzeProject(String projectDirectoryPath) {
        // Get all .java files
        List<File> javaFiles = listJavaFiles(new File(projectDirectoryPath));

        // Parse and visit each Java file
        for (File javaFile : javaFiles) {
            totalLinesOfCode += countLinesOfCode(javaFile);  // Count total lines of code

            try (FileInputStream in = new FileInputStream(javaFile)) {
                JavaParser parser = new JavaParser();
                ParseResult<CompilationUnit> parseResult = parser.parse(in);

                if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                    CompilationUnit cu = parseResult.getResult().get();
                    
                    // Use the visitor to visit the compilation unit (Java file)
                    cu.accept(visitor, null);  
                } else {
                    System.err.println("Parsing failed for file: " + javaFile.getPath());
                    parseResult.getProblems().forEach(problem ->
                        System.err.println("Problem: " + problem.getMessage())
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to get the DOT representation from CustomVisitor
    public String generateDotRepresentation() {
        String dotRepresentation = visitor.generateDot();// Use the visitor to get the accumulated DOT representation for the AST
        System.out.println("Generated DOT: " + dotRepresentation); //  for debugging
        return dotRepresentation;
    }

 // Method to generate a .dot file from a DOT representation
    public void generateDotFile(String dotRepresentation, String fileName) {
        if (dotRepresentation == null || dotRepresentation.trim().isEmpty()) {
            System.err.println("No DOT data to write!");
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("Invalid file name!");
            return;
        }

        try (PrintWriter out = new PrintWriter(fileName + ".dot")) {
            out.println(dotRepresentation);
            System.out.println(".dot file successfully generated: " + fileName + ".dot");
        } catch (IOException e) {
            System.err.println("Failed to generate the .dot file: " + e.getMessage());
            e.printStackTrace(); 
        }
    }


    // Method to get the DOT representation for the Call Graph from CustomVisitor
    public String generateCallGraphRepresentation() {
        return visitor.generateCallGraphDot();  // Use the visitor to get the accumulated DOT representation for the Call Graph
    }
 
   
    // Method to generate an image (PNG) from a DOT representation using Graphviz
    public void generateGraphImage(String dotRepresentation, String fileName) {
        try {
            // Create an instance of the Parser and read the DOT representation
            MutableGraph graph = new guru.nidi.graphviz.parse.Parser().read(dotRepresentation);

            // Render the graph as a PNG image
            Graphviz.fromGraph(graph)
                    .width(1000)  // Adjust the width for better clarity
                    .render(Format.PNG)
                    .toFile(new File(fileName + ".png"));

            System.out.println("Graph image generated for " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 // Method to generate both .dot and .png for AST
    public void generateAstFiles() {
        String astDotRepresentation = generateDotRepresentation();
        String astFileName = "ast_graph";

        // Generate the .dot file for AST
        generateDotFile(astDotRepresentation, astFileName);

        // Generate the .png file for AST
        generateGraphImage(astDotRepresentation, astFileName);
    }

    // Method to generate both .dot and .png for Call Graph
    public void generateCallGraphFiles() {
        String callGraphDotRepresentation = generateCallGraphRepresentation();
        String callGraphFileName = "call_graph";

        // Generate the .dot file for Call Graph
        generateDotFile(callGraphDotRepresentation, callGraphFileName);

        // Generate the .png file for Call Graph
        generateGraphImage(callGraphDotRepresentation, callGraphFileName);
    }

    // Display top classes by method count
    public void displayTopClassesByMethodCount(StringBuilder output) {
        int topCount = (int) Math.ceil(classMethodCount.size() * 0.1);
        List<Map.Entry<String, Integer>> sortedClasses = classMethodCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topCount)
                .collect(Collectors.toList());

        
        sortedClasses.forEach(entry -> output.append(entry.getKey() + ": " + entry.getValue() + " methodes\n"));
    }
    
    public static int countTotalPackages(File[] files) throws IOException {
        Set<String> packages = new HashSet<>();  // Utilisation d'un Set pour éviter les doublons
        Pattern packagePattern = Pattern.compile("^\\s*package\\s+([\\w\\.]+);");

        for (File file : files) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = packagePattern.matcher(line);
                if (matcher.find()) {
                    packages.add(matcher.group(1));  // Ajouter le nom du package au Set
                }
            }
            reader.close();
        }
        return packages.size();  // Retourner le nombre de packages uniques
    }


    public static List<String> topMethodsByLines2(File[] files, int topPercent) throws IOException {
        Pattern methodPattern = Pattern.compile("^\\s*(public|protected|private)\\s+[\\w<>\\[\\]]+\\s+[\\w]+\\(.*?\\)\\s*\\{");
        List<String> methods = new ArrayList<>();
        List<Integer> methodLines = new ArrayList<>();

        for (File file : files) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int braceCount = 0;
            int methodStart = 0;
            boolean inMethod = false;
            String currentMethod = null;

            while ((line = reader.readLine()) != null) {
                if (!inMethod && methodPattern.matcher(line).find()) {
                    currentMethod = line.trim();
                    methodStart = 0;
                    inMethod = true;
                }

                if (inMethod) {
                    methodStart++;
                    braceCount += line.contains("{") ? 1 : 0;
                    braceCount -= line.contains("}") ? 1 : 0;
                    
                    if (braceCount == 0 && currentMethod != null) {
                        methods.add(currentMethod + " (" + file.getName() + ")");
                        methodLines.add(methodStart);
                        inMethod = false;
                    }
                }
            }
            reader.close();
        }

        // Trier et sélectionner les 10 %
        List<String> topMethods = new ArrayList<>();
        int topCount = (int) Math.ceil(methods.size() * (topPercent / 100.0));
        topCount = topCount > 0 ? topCount : 1;  // Assurez-vous qu'il y a au moins 1 méthode sélectionnée
        for (int i = 0; i < topCount; i++) {
            int maxIndex = methodLines.indexOf(Collections.max(methodLines));
            topMethods.add(methods.get(maxIndex) + " - " + methodLines.get(maxIndex) + " lignes");
            methods.remove(maxIndex);
            methodLines.remove(maxIndex);
        }
        return topMethods;
    }

    public static int maxParameters(File[] files) throws IOException {
        Pattern paramPattern = Pattern.compile("\\((.*?)\\)");
        int maxParams = 0;

        for (File file : files) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = paramPattern.matcher(line);
                if (matcher.find()) {
                    String params = matcher.group(1).trim();
                    int paramCount = params.isEmpty() ? 0 : params.split(",").length;
                    if (paramCount > maxParams) {
                        maxParams = paramCount;
                    }
                }
            }
            reader.close();
        }

        return maxParams;
    }



    // Display top classes by attribute count
    public void displayTopClassesByAttributeCount(StringBuilder output) {
        int topCount = (int) Math.ceil(classAttributeCount.size() * 0.1);
        List<Map.Entry<String, Integer>> sortedClasses = classAttributeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topCount)
                .collect(Collectors.toList());

        
        sortedClasses.forEach(entry -> output.append(entry.getKey() + ": " + entry.getValue() + " attributs\n"));
    }

    // Display classes in both top categories
    public void displayClassesInBothTopCategories(StringBuilder output) {
        Set<String> topMethodClasses = classMethodCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit((int) Math.ceil(classMethodCount.size() * 0.1))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<String> topAttributeClasses = classAttributeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit((int) Math.ceil(classAttributeCount.size() * 0.1))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        topMethodClasses.retainAll(topAttributeClasses);
        topMethodClasses.forEach(className -> output.append(className + "\n"));
    }

    // Display classes with more than X methods
    public void displayClassesWithMoreThanXMethods(int x, StringBuilder output) {

        long count = classMethodCount.entrySet().stream()
                .filter(entry -> entry.getValue() > x)
                .peek(entry -> output.append(entry.getKey() + ": " + entry.getValue() + " methodes\n"))
                .count();

        if (count == 0) {
            output.append("Aucune classe de ce projet n'a plus de " + x + " methodes.\n");
        }
    }

    // Method to count lines of code in a file
    protected int countLinesOfCode(File file) {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Method to recursively list all .java files in a directory
    protected List<File> listJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        javaFiles.addAll(listJavaFiles(file)); // Recursive call for subdirectories
                    } else if (file.getName().endsWith(".java")) {
                        javaFiles.add(file); // Add .java files
                    }
                }
            }
        }
        return javaFiles;
    }
    
    public static List<String> topMethodsByLines(File[] files, int topPercent) throws IOException {
    	Pattern methodPattern = Pattern.compile("^\\s*(public|protected|private)\\s+[\\w<>\\[\\]]+\\s+[\\w]+\\(.*?\\)\\s*\\{");
        List<String> methods = new ArrayList<>();
        List<Integer> methodLines = new ArrayList<>();

        for (File file : files) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int braceCount = 0;
            int methodStart = 0;
            boolean inMethod = false;
            String currentMethod = null;

            while ((line = reader.readLine()) != null) {
                if (!inMethod && methodPattern.matcher(line).find()) {
                    currentMethod = line.trim();
                    methodStart = 0;
                    inMethod = true;
                }

                if (inMethod) {
                    methodStart++;
                    braceCount += line.contains("{") ? 1 : 0;
                    braceCount -= line.contains("}") ? 1 : 0;
                    
                    if (braceCount == 0 && currentMethod != null) {
                        methods.add(currentMethod + " (" + file.getName() + ")");
                        methodLines.add(methodStart);
                        inMethod = false;
                    }
                }
            }
            reader.close();
        }

        // Trier et sélectionner les 10 %
        List<String> topMethods = new ArrayList<>();
        int topCount = (int) Math.ceil(methods.size() * (topPercent / 100.0));
        topCount = topCount > 0 ? topCount : 1;  // Assurez-vous qu'il y a au moins 1 méthode sélectionnée
        for (int i = 0; i < topCount; i++) {
            int maxIndex = methodLines.indexOf(Collections.max(methodLines));
            topMethods.add(methods.get(maxIndex) + " - " + methodLines.get(maxIndex) + " lignes");
            methods.remove(maxIndex);
            methodLines.remove(maxIndex);
        }
        return topMethods;
    }
    
}
