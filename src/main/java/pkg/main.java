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
         for (int i = 1; i <= 1; i++) {
            List<BigDecimal> meanList;
            Utilities utils = new Utilities();
            Chart lineChart1 = new Chart("", 1,1,1);
            int step = 1;
            int leadingZero = 0;
            int delta = 0;
            for (File file : files) {
               FileOutputStream fos = new FileOutputStream("Logs/log_" + file.getName() + ".txt");
               PrintStream ps = new PrintStream(fos);
               System.setOut(ps);
               if (file.isFile() && file.getName().endsWith(".cnf")) {
                  //-- Execution --
                  long startTime1 = System.currentTimeMillis();
                  NBLSolver solver = new NBLSolver(file);
                  long startTime2 = System.currentTimeMillis();
                  meanList = solver.check();
                  long endTime = System.currentTimeMillis();

                  System.out.println("--------------");
                  BigDecimal lastMean = meanList.get(meanList.size() - 1);
                  int tempLeadingZero = utils.getLeadingZero(lastMean);
                  delta = tempLeadingZero - leadingZero;
                  leadingZero = (leadingZero < tempLeadingZero) ? tempLeadingZero : leadingZero;
                  lineChart1.addSeries(meanList, leadingZero, step, file.getName());

                  //-- Elapsed time --
                  long lTime = startTime2 - startTime1;
                  long pTime = endTime - startTime2;
                  long tTime = lTime + pTime;

                  System.out.println("Leading Zero :: " + tempLeadingZero);
                  System.out.println("------------Loading: " + lTime + " ms");
                  System.out.println("------------Processing: " + pTime + " ms");
                  System.out.println("------------Total: " + tTime + " ms");
               }
            }
            if (delta > 0) {
               lineChart1.modifySerieOne(delta);
            }

            lineChart1.addMarker(0, Color.BLACK);
            lineChart1.setTitle("Delta of Leading Zero: " + delta);
            lineChart1.drawToFile(delta);
            lineChart1.drawToSVG(delta);
         }
      }
   }
}
