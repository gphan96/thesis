package pkg;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

public class main {
   
   public static void main(String[] args) throws IOException {
      File dir = new File("SATInstances");
      File[] files = dir.listFiles();
      if (files != null) {
         FileOutputStream fos = new FileOutputStream("Logs/log.txt");
         PrintStream ps = new PrintStream(fos);
         System.setOut(ps);
         for (int i = 1; i <= 1; i++) {
            System.out.println("---------- Execution " + i + " ----------");
            List<BigDecimal> meanList;
            Utilities utils = new Utilities();
            Chart lineChart = new Chart("", -5,5,1);
            int step = 1;
            int preLeadingZero = 0;
            long startTime = System.currentTimeMillis();
            for (File file : files) {
               if (file.isFile() && file.getName().endsWith(".cnf")) {
                  System.out.println("\nFile:       " + file.getName());
                  //-- Execution --
                  NBLSolver solver = new NBLSolver(file);
                  solver.check(i);
                  // meanList = solver.check();
                  // BigDecimal lastMean = meanList.get(meanList.size() - 1);
                  // int leadingZero = utils.getLeadingZero(lastMean);
                  // if (preLeadingZero != 0) {
                  //    lineChart.addSeries(meanList, preLeadingZero, step, file.getName());
                  // } else {
                  //    lineChart.addSeries(meanList, leadingZero, step, file.getName());
                  // }

                  // System.out.println("LZ:         " + leadingZero);
                  // System.out.println("Pre LZ:     " + preLeadingZero);
                  
                  // preLeadingZero = leadingZero;
               }
            }
            long endTime = System.currentTimeMillis();
            long pTime = endTime - startTime;
            System.out.println("\nProcessing: " + pTime + " ms");
            System.out.println("---------- End of " + i + " ----------\n\n");
            // if (delta > 0) {
            //    lineChart1.modifySerieOne(delta);
            // }

            // lineChart.addMarker(0, Color.BLACK);
            // lineChart.drawToFile("" + i);
            // lineChart1.drawToSVG(delta);
         }
      }
   }
}
