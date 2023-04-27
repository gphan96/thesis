import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NBLSolver {
   private int numVariables;
   private int numClauses;
   private List<Clause> instanceSAT;
   private HashMap<Integer, List<Double>> noisesByClause;
   private Random random;
   private String fileName;

   public NBLSolver(File file) {
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {
         fileName = file.getName();
         String line;
         int clause_index = 1;
         while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("c") || line.isEmpty()) {
               continue;
            } else if (line.startsWith("p cnf")) {
               String[] tokens = line.split("\\s+");
               numVariables = Integer.parseInt(tokens[2]);
               numClauses = Integer.parseInt(tokens[3]);
               instanceSAT = new ArrayList<>(numClauses);
            } else {
               String[] tokens = line.split("\\s+");
               List<Integer> clause = new ArrayList<>();
               for (int i = 0; i < tokens.length - 1; i++) {
                  if (!tokens[i].isEmpty()) {
                     int literal = Integer.parseInt(tokens[i]);
                     clause.add(literal);
                  }
               }
               if (clause.isEmpty()) {
                  break;
               }
               instanceSAT.add(new Clause(clause_index, clause));
               clause_index++;
            }
         }
      } catch (IOException e) {
         System.err.println("Failed to read file: " + file.getAbsolutePath());
         e.printStackTrace();
      }
   }

   //-- NOISE SOURCE --

   private double noiseSource() {
      return random.nextDouble() * 1 - 0.5;
   }

   //-- TAU --

   private double constructHyperspace() {
      noisesByClause = new HashMap<>(numClauses + 1, 1.0f);
      double result = 1.0;
      for (int i = 1; i <= numVariables; i++) {
         double product1 = 1.0;
         double product2 = 1.0;
         for (int j = 1; j <= numClauses; j++) {
            // Generate noise source for 2 literals of variable x in clause c
            double positive = noiseSource();
            double negative = noiseSource();
            // Group the noise source by clause index
            List<Double> tempList1 = noisesByClause.get(j);
            if (tempList1 == null) {
               tempList1 = new ArrayList<>();
               noisesByClause.put(j, tempList1);
            }
            tempList1.add(positive);
            tempList1.add(negative);
            
            product1 *= positive;
            product2 *= negative;
         }
         result *= (product1 + product2);
      }
      return result;
   }

   //-- SIGMA --

   private double constructInstanceNBL() {
      double result = 1.0;
      for (Clause clause : instanceSAT) {
         double sum = 0.0;
         for (int literal : clause.getLiterals()) {
            sum += cube(clause.getIndex(), literal);
         }
         result *= sum;
      }
      return result;
   }

   private double cube(int clause, int literal) {
      List<Double> tempList = noisesByClause.get(clause);
      double result = 1.0;
      // Noise sources of each clause are listed by the order:
      // [x1, -x1, x2, -x2, ...]
      for (int i = 0; i < numVariables; i++) {
         if (literal < 0 && i + 1 == Math.abs(literal)) {
            double noise = tempList.get(i * 2 + 1);
            result *= noise;
            continue;
         } else if (literal > 0 && i + 1 == literal) {
            double noise = tempList.get(i * 2);
            result *= noise;
            continue;
         }
         double positive = tempList.get(i * 2);
         double negative = tempList.get(i * 2 + 1);
         result *= (positive + negative);
      }
      return result;
   }

   //-- CHECKER --

   public boolean check() {
      boolean satifiability = true;
      double S_N = 0.0;
      List<Double> meanList = new ArrayList<>();

      int totalSample = 1000000;
      int step = 10;

      for (int i = 1; i <= totalSample; i++) {
         random = new Random();
         double tau = constructHyperspace();
         double sigma = constructInstanceNBL();
         S_N += (tau * sigma);
         double mean = S_N / i;
         if (i % step == 0) {
            meanList.add(mean);
         }
      }

      new Chart(meanList, fileName);
      return satifiability;
   }

}
