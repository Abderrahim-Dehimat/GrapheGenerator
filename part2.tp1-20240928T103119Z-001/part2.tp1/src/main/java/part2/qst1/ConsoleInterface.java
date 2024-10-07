package part2.qst1;

import java.util.Scanner;

public class ConsoleInterface {

    public static void main(String[] args) {
    	main_prt1 engine = new main_prt1();
        
        //  Project Path
        String projectDirectoryPath = "/home/kaouther/Téléchargements/visitorDesignPattern";
        engine.analyzeProject(projectDirectoryPath);

        try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
			    displayMenu();
			    int choice = scanner.nextInt();

			    switch (choice) {			    
			        case 1:
			            System.out.println("Nombre de classes dans l'application: " + main_prt1.classCount);
			            break;
			        case 2:
			            System.out.println("Nombre de méthodes dans l'application: " + main_prt1.methodCount);
			            break;
			        case 3:
			            System.out.println("Nombre total de lignes de code dans l'application: " + main_prt1.totalLinesOfCode);
			            break;
			        case 4:
			            System.out.println("Nombre moyen de méthodes par classe: " + (main_prt1.methodCount / (double) main_prt1.classCount));
			            break;
			        case 5:
			            System.out.println("Nombre moyen de lignes de code par méthode: " + (main_prt1.totalLinesOfCode / (double) main_prt1.methodCount));
			            break;
			        case 6:
			            System.out.println("Nombre moyen d'attributs par classe: " + (main_prt1.totalAttributes / (double) main_prt1.classCount));
			            break;
			        case 7:
			            engine.displayTopClassesByMethodCount(null);
			            break;
			        case 8:
			            engine.displayTopClassesByAttributeCount(null);
			            break;
			        case 9:
			            engine.displayClassesInBothTopCategories(null);
			            break;
			        case 10:
			            System.out.println("Entrez la valeur de X:");
			            int x = scanner.nextInt();
			            engine.displayClassesWithMoreThanXMethods(x, null);
			            break;
			        case 11:
			            // Generate AST
			            String dotRepresentation = engine.generateDotRepresentation();  // No arguments needed
			            engine.generateAstGraph(dotRepresentation, "project_ast");  // Pass the DOT and file name
			            break;


			        case 0:
			            System.out.println("Sortie du programme.");
			            System.exit(0);  // Exit the program
			            break;
			        default:
			            System.out.println("Option non valide. Veuillez réessayer.");
			    }
			}
		}
    }

    // Méthode pour afficher le menu des options
    private static void displayMenu() {
        System.out.println("\nMenu des options:");
        System.out.println("1. Afficher le nombre de classes");
        System.out.println("2. Afficher le nombre de méthodes");
        System.out.println("3. Afficher le nombre total de lignes de code");
        System.out.println("4. Afficher le nombre moyen de méthodes par classe");
        System.out.println("5. Afficher le nombre moyen de lignes de code par méthode");
        System.out.println("6. Afficher le nombre moyen d'attributs par classe");
        System.out.println("7. Afficher les 10 % des classes avec le plus grand nombre de méthodes");
        System.out.println("8. Afficher les 10 % des classes avec le plus grand nombre d'attributs");
        System.out.println("9. Afficher les classes présentes dans les deux catégories (méthodes et attributs)");
        System.out.println("10. Afficher les classes avec plus de X méthodes");
        System.out.println("11. Afficher AST de l'application");
        System.out.println("0. Quitter");
        System.out.print("Entrez votre choix: ");
    }
}
