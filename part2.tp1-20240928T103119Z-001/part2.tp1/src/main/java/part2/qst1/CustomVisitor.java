package part2.qst1;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CustomVisitor extends VoidVisitorAdapter<Void> {

    protected final List<String> methods = new ArrayList<>();
    protected final static Set<String> classesWithoutInheritance = new HashSet<>();
    protected final static List<String> classesWithInheritance = new ArrayList<>();

    private StringBuilder dotBuilder = new StringBuilder();  // For the AST
    private StringBuilder callGraphBuilder = new StringBuilder();  // For the Call Graph

    public CustomVisitor() {
        // Start the DOT representation of the AST
        dotBuilder.append("digraph AST {\n");

        // Start the DOT representation for the Call Graph
        callGraphBuilder.append("digraph CallGraph {\n");
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
        super.visit(cid, arg);

        // Increment the class count
        main_prt1.classCount++;

        // Get the class name
        String className = cid.getNameAsString();
        
        // Count the number of methods and fields (attributes) in this class
        int methodCount = cid.getMethods().size();
        int attributeCount = cid.getFields().size();

        // Store the information about methods and attributes
        main_prt1.classMethodCount.put(className, methodCount);
        main_prt1.classAttributeCount.put(className, attributeCount);
        main_prt1.methodCount += methodCount;
        main_prt1.totalAttributes += attributeCount;

        // Clear and populate the list of methods for this class
        methods.clear();
        cid.getMethods().forEach(method -> {
            methods.add(method.getNameAsString());
        });

        // Store the methods associated with this class
        main_prt1.classMethods.put(className, new ArrayList<>(methods));

        // Check for inheritance
        if (cid.getExtendedTypes().isEmpty()) {
            classesWithoutInheritance.add(className);
        } else {
            classesWithInheritance.add(className);
            cid.getExtendedTypes().forEach(superClass -> {
                String superClassName = superClass.getNameAsString();
                System.out.println("  - Inherits from: " + superClassName);

                // Add inheritance information to the DOT representation
                dotBuilder.append("\"").append(className).append("\" -> \"")
                          .append(superClassName).append("\" [label=\"inherits\"];\n");
            });
        }

        // Add class to the DOT graph
        dotBuilder.append("\"").append(className).append("\" [label=\"Class: ").append(className).append("\"];\n");

        // Visit methods within this class and add them to the DOT representation
        cid.getMethods().forEach(method -> {
            dotBuilder.append("\"").append(method.getNameAsString()).append("\" [label=\"Method: ").append(method.getNameAsString()).append("\"];\n");
            dotBuilder.append("\"").append(className).append("\" -> \"").append(method.getNameAsString()).append("\" [label=\"contains\"];\n");
        });

        // Visit fields within this class and add them to the DOT representation
        cid.getFields().forEach(field -> {
            dotBuilder.append("\"").append(field.getVariables()).append("\" [label=\"Field: ").append(field.getVariables()).append("\"];\n");
            dotBuilder.append("\"").append(className).append("\" -> \"").append(field.getVariables()).append("\" [label=\"contains\"];\n");
        });
    }

    @Override
    public void visit(MethodCallExpr methodCall, Void arg) {
        super.visit(methodCall, arg);

        // Get the enclosing method (the method that is making the call)
        String callerMethod = getEnclosingMethodName(methodCall);
        
        // Get the name of the called method
        String calledMethod = methodCall.getNameAsString();

        // Add the method call to the call graph
        callGraphBuilder.append("\"").append(callerMethod).append("\" -> \"")
                        .append(calledMethod).append("\" [label=\"calls\"];\n");
    }

    // Helper method to get the name of the enclosing method
    private String getEnclosingMethodName(MethodCallExpr methodCall) {
        // Traverse the ancestors of the method call to find the enclosing method declaration
        return methodCall.findAncestor(MethodDeclaration.class)
                .map(MethodDeclaration::getNameAsString)
                .orElse("UnknownMethod");
    }

    // Method to return the DOT representation for the AST as a String
    public String generateDot() {
        dotBuilder.append("}\n");  // Close the AST DOT graph
        return dotBuilder.toString();
    }

    // Method to return the DOT representation for the Call Graph as a String
    public String generateCallGraphDot() {
        callGraphBuilder.append("}\n");  // Close the Call Graph DOT graph
        return callGraphBuilder.toString();
    }

    public void printClasses() {
        // Print classes without inheritance
        System.out.println("Classes without inheritance:");
        classesWithoutInheritance.forEach(className -> System.out.println("Class: " + className));

        // Print classes with inheritance
        System.out.println("Classes with inheritance:");
        classesWithInheritance.forEach(className -> System.out.println("Class: " + className));
    }
}
