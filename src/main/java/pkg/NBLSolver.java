package pkg;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.ui.TextAnchor;

public class NBLSolver {
   private int numVariables;
   private int numClauses;
   private List<Clause> instanceSAT;
   private HashMap<Integer, List<BigDecimal>> noisesByClause;
   private List<BigDecimal> meanList;
   private String fileName;
   private Utilities utils;

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

   private BigDecimal noiseSource() {
      Random random = new Random();
      return BigDecimal.valueOf(random.nextInt(2001) - 1000);
      // return utils.setPrecision(new BigDecimal(Math.random() * 2 - 1));
   }

   //-- TAU --

   private BigDecimal constructHyperspace() {
      noisesByClause = new HashMap<>(numClauses + 1, 1.0f);
      BigDecimal result = BigDecimal.ONE;
      for (int i = 1; i <= numVariables; i++) {
         BigDecimal product1 = BigDecimal.ONE;
         BigDecimal product2 = BigDecimal.ONE;
         for (int j = 1; j <= numClauses; j++) {
            // Generate noise source for 2 literals of variable x in clause c
            BigDecimal positive = noiseSource();
            BigDecimal negative = noiseSource();
            // Group the noise source by clause index
            List<BigDecimal> tempList1 = noisesByClause.get(j);
            if (tempList1 == null) {
               tempList1 = new ArrayList<>();
               noisesByClause.put(j, tempList1);
            }
            tempList1.add(positive);
            tempList1.add(negative);
            
            product1 = utils.setPrecision(product1).multiply(positive);
            product2 = utils.setPrecision(product2).multiply(negative);
         }
         BigDecimal sum = product1.add(product2);
         result = utils.setPrecision(result).multiply(sum);
      }
      return utils.setPrecision(result);
   }

   //-- SIGMA --

   private BigDecimal constructInstanceNBL() {
      BigDecimal result = BigDecimal.ONE;
      for (Clause clause : instanceSAT) {
         BigDecimal sum = BigDecimal.ZERO;
         for (int literal : clause.getLiterals()) {
            sum = sum.add(cube(clause.getIndex(), literal));
         }
         result = utils.setPrecision(result).multiply(sum);
      }
      return utils.setPrecision(result);
   }

   private BigDecimal cube(int clause, int literal) {
      List<BigDecimal> tempList = noisesByClause.get(clause);
      BigDecimal result = BigDecimal.ONE;
      // Noise sources of each clause are listed by the order:
      // [x1, -x1, x2, -x2, ...]
      for (int i = 0; i < numVariables; i++) {
         if (literal < 0 && i + 1 == Math.abs(literal)) {
            BigDecimal noise = tempList.get(i * 2 + 1);
            result = result.multiply(noise);
            continue;
         } else if (literal > 0 && i + 1 == literal) {
            BigDecimal noise = tempList.get(i * 2);
            result = result.multiply(noise);
            continue;
         }
         BigDecimal positive = tempList.get(i * 2);
         BigDecimal negative = tempList.get(i * 2 + 1);
         BigDecimal sum = positive.add(negative);
         result = utils.setPrecision(result).multiply(utils.setPrecision(sum));
      }
      return utils.setPrecision(result);
   }

   //-- CHECKER --

   public boolean check(int index) {
      utils = new Utilities();
      meanList = new ArrayList<>();
      boolean satifiability = true;
      BigDecimal sum = BigDecimal.ZERO;
      BigDecimal meanPre = BigDecimal.ZERO;

      int totalSample = 100000000;
      int minSample = 200000;

      for (int i = 1; i <= totalSample; i++) {
         BigDecimal tau = constructHyperspace();
         BigDecimal sigma = constructInstanceNBL();
         BigDecimal S_N = tau.multiply(sigma);
         // listS.add(S_N);
         BigDecimal meanCur = utils.setPrecision(sum.divide(BigDecimal.valueOf(i), RoundingMode.HALF_UP));

         // -- Print the extreme data
         if (S_N.abs().compareTo(meanCur.abs().multiply(BigDecimal.valueOf(1000000))) > 0 && i > 2) {
            System.out.println(meanCur);
            System.out.println(S_N);
            // for (int k = 1; k <= numClauses; k++) {
            //    System.out.println(noisesByClause.get(k));
            // }
            // continue; // ignore the extreme value
         }
         
         sum = sum.add(S_N);
         meanCur = utils.setPrecision(sum.divide(BigDecimal.valueOf(i), RoundingMode.HALF_UP));
         meanList.add(meanCur);
         
         
         // -- Stopping criterion

         if (i > minSample) {
            int threshold = 10;
            if (utils.checkStop(meanCur, meanPre, threshold)) {
                System.out.println("Stop at:    " + i);
                break;
            }
            meanPre = meanCur;
         }
      }
      

      // -- SAT criterion --

      int num = meanList.size();
      // BigDecimal lastMean = meanList.get(num - 1);

      List<BigDecimal> lastHalf = meanList.subList(num / 2, num);
      BigDecimal lastMax = Collections.max(lastHalf);
      BigDecimal lastMin = Collections.min(lastHalf);
      BigDecimal meanMax = utils.getMaxAbs(meanList);
      BigDecimal tolerance = meanMax.multiply(BigDecimal.valueOf(0.01));

      // -- check collision with second half
      if ((lastMax.abs().compareTo(tolerance) <= 0) || (lastMin.abs().compareTo(tolerance) <= 0) || (lastMin.compareTo(BigDecimal.valueOf(- tolerance.doubleValue())) <= 0 && tolerance.compareTo(lastMax) <= 0)) {
         satifiability = false;
      }

      // -- check collision with last value of S_N mean
      // if (lastMean.abs().compareTo(tolerance) <= 0) {
      //    satifiability = false;
      // }


      // -- Plot print --

      // Chart lineChart = new Chart(fileName, -0.0005, 0.002, 1);
      // lineChart.addSeries(meanList, 0, 1, "");

      // lineChart.addMarker("Max", lastMax.doubleValue(), Color.BLUE, TextAnchor.BOTTOM_RIGHT);
      // lineChart.addMarker("Min", lastMin.doubleValue(), Color.BLUE, TextAnchor.TOP_RIGHT);
      // lineChart.addMarker("Last", lastMean.doubleValue(), Color.BLUE, TextAnchor.BOTTOM_RIGHT);
      // lineChart.addMarker("Threshold", tolerance.doubleValue(), Color.BLACK, TextAnchor.BOTTOM_RIGHT);
      // lineChart.addMarker("Threshold", - tolerance.doubleValue(), Color.BLACK, TextAnchor.TOP_RIGHT);

      // lineChart.drawToFile(fileName + "/" + index);
      
      // System.out.println("Leading Zero:      " + leadingZero);
      // System.out.println("Last max:          " + lastMax);
      // System.out.println("Last min:          " + lastMin);
      // System.out.println("Tolerance:         " + tolerance);


      System.out.println("Satifiable?        " + satifiability);
      return satifiability;
   }
}
