package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class creates specific components like JLabels, JButtons, JPanels, etc.
 * It also creates graphs with predicted data for a user-specified number of
 * years and adds them to an actionPanel.
 *
 * @author Thomas England
 */
public class ComponentFactory {
    private final App app;
    private JButton calcPredictedBtn;
    private JDialog dialog;
    private JPanel actionPanel;
    private ChartPanel lineChartPanel;
    private int numYears;

    /**
     * Constructs a ComponentFactory connected to a specified App
     *
     * @param app an App object containing the App that this ComponentFactory
     *            belongs to.
     */
    public ComponentFactory(App app) {
        this.app = app;
        calcPredictedBtn = null;
        dialog = null;
        actionPanel = null;
        lineChartPanel = null;
        numYears = 0;
    }

    /**
     * Creates a centered JLabel containing a specified text
     *
     * @param text String containing text to put on the JLabel
     * @return a JLabel containing centered text
     */
    public JLabel createDescLabel(String text) {
        JLabel descLabel = new JLabel(text);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return descLabel;
    }

    /**
     * Creates a 'Back to Main Menu' button that brings the user back to the
     * main menu.
     *
     * @return a JButton containing 'Back to Main Menu' that brings user back to
     *         main menu.
     */
    public JButton createBackButton() {
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(app);
        backButton.setHorizontalAlignment(SwingConstants.CENTER);
        backButton.setVerticalAlignment(SwingConstants.CENTER);
        return backButton;
    }

    /**
     * Creates a content panel for a screen (BorderLayout JPanel with podding of
     * 20 px on each side from the edge of the window)
     *
     * @return a BorderLayout JPanel with 20 px padding on all sides
     */
    public JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(createEmptyBorder(20, 20, 20, 20));
        return contentPanel;
    }

    /**
     * Creates a button 'Calculate Predicted Data' that creates a line graph
     * with future data using trendline for a user specified number of years.
     *
     * @param actionPanel    the JPanel to add the button and graphs to
     * @param lineChartPanel the ChartPanel containing the line chart to read
     *                       data from to calculate trendline.
     */
    public void createCalcPredictedBtn(JPanel actionPanel, ChartPanel lineChartPanel) {
        // store actionPanel and lineChartPanel to edit/read from later
        this.actionPanel = actionPanel;
        this.lineChartPanel = lineChartPanel;
        calcPredictedBtn = new JButton("Calculate Predicted Data");
        calcPredictedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcPredictedBtn.addActionListener(e -> createPredictedDataDialog());

        // add to actionPanel and refresh actionPanel gui
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(calcPredictedBtn);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Activates when calcPredictedBtn is clicked - Prompts user for how many
     * years to predict trendline for and calls numYearsSelected() to create and
     * add graph to actionPanel.
     */
    private void createPredictedDataDialog() {
        // create dialog to prompt user for years
        dialog = new JDialog(app.getFrame(), "Select the number of years to predict");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // panel to hold buttons for num of years
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // create and add buttons to panel
        for (int i = 1; i <= 10; i++) {
            JButton numYearsBtn = new JButton();
            numYearsBtn.setText(i == 1 ? "1 year" : i + " years");
            numYearsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            numYearsBtn.addActionListener(e -> numYearsSelected(numYearsBtn));
            panel.add(numYearsBtn);
            panel.add(Box.createVerticalStrut(10));
        }

        // wrap panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        // add scroll pane to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);

        // show dialog and center relative to parent frame
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setVisible(true);
    }

    /**
     * Activates when user has selected the number of years to predict the
     * trendline for - retrieves all x and y values from old line graph,
     * calculates the future coordinates for the user-specified number of years,
     * creates full graph including past and future coordinates and adds graph
     * to actionPanel.
     *
     * @param numYearsBtn the JButton that the user clicked (contains num of
     *                    years to predict data for)
     */
    private void numYearsSelected(JButton numYearsBtn) {
        // retrieve num of years from numYearsBtn
        numYears = Integer.parseInt(numYearsBtn.getText().substring(0, 1));

        // check if numYears is 10 (else 10 would be recognized as 1 from substring)
        if (numYearsBtn.getText().substring(0, 2).equals("10")) {
            numYears = 10;
        }

        // close dialog and change text of numYearsBtn to user selection
        dialog.dispose();
        calcPredictedBtn.setText("Calculated for " + numYearsBtn.getText());

        // retrieve all XYSeries from lineChart in lineChartPanel
        JFreeChart lineChart = lineChartPanel.getChart();
        XYPlot plot = (XYPlot) lineChart.getPlot();
        XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();

        // create XYSeriesCollection to hold all new XYSeries (with trendline)
        XYSeriesCollection newDataset = new XYSeriesCollection();

        // double to hold max y value out of all XYSeries (to calculate divider line)
        double globalMaxValue = 0;

        // create new XYSeries with future data from trendline for each XYSeries in
        // original
        // dataset, add to XYSeriesCollection
        for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
            XYSeries originalSeries = dataset.getSeries(seriesIndex);
            XYSeries newSeries = new XYSeries(originalSeries.getKey());

            for (int i = 0; i < originalSeries.getItemCount(); i++) {
                newSeries.add(originalSeries.getX(i), originalSeries.getY(i));
            }

            // get calculated future coordinates from calcFutureCoords()
            calcFutureCoords(newSeries);
            newDataset.addSeries(newSeries);

            // set max y value if max y from series exceeds current max y value
            double maxValSeries = getMaxValFromSeries(newSeries);
            if (maxValSeries > globalMaxValue) {
                globalMaxValue = maxValSeries;
            }
        }

        // create divider to separate measured and calculated (y0 to yMax at x where
        // calculated
        // data begins)
        XYSeries divider = new XYSeries("Divider");
        divider.add(2025, 0);
        divider.add(2025, globalMaxValue);
        newDataset.addSeries(divider);

        // create new line chart with calculated data
        JFreeChart newLineChart = ChartFactory.createXYLineChart(
                "Median Home Values + " + numYears + (numYears == 1 ? " year" : " years"),
                "Date (Year)",
                "Median Home Value (USD)",
                newDataset);

        // add new line chart to actionPanel
        ChartPanel newLineChartPanel = new ChartPanel(newLineChart);
        newLineChartPanel.setPreferredSize(new Dimension(300, 300));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(newLineChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Retrieves the max Y value from an XYSeries for use in calculating divider
     * for line graph with calculated data.
     *
     * @param series the XYSeries to calculate max Y from
     * @return a double value containing the max Y from the specified XYSeries
     */
    private static double getMaxValFromSeries(XYSeries series) {
        double maxY = 0.0;

        // loop thru each y in XYSeries to find max
        for (int i = 0; i < series.getItemCount(); i++) {
            if ((double) series.getY(i) > maxY) {
                maxY = (double) series.getY(i);
            }
        }

        return maxY;
    }

    /**
     * Calculates a trendline from the specified XYSeries and adds XY
     * coordinates from the trendline starting at last x value and ending at the
     * user-specified number of years.
     *
     * @param series the XYSeries containing data to read from and add to
     */
    private void calcFutureCoords(XYSeries series) {
        // n = number of coordinates
        int n = series.getItemCount();

        // sum of all x vals, sum of all y, sum of product of all corresponding x and y,
        // sum of squares of all x values
        double sumX   = 0,
                sumY  = 0,
                sumXY = 0,
                sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX  += (double) series.getX(i);
            sumY  += (double) series.getY(i);
            sumXY += (double) series.getX(i) * (double) series.getY(i);
            sumX2 += (double) series.getX(i) * (double) series.getX(i);
        }

        // calculate slope (the 'm' in y = mx + b)
        double m = (n * sumXY - sumX * sumY) /
                   (n * sumX2 - sumX * sumX);
        // calculate y-axis intercept (the 'b' in y = mx + b)
        double b = (sumY - m * sumX) / n;

        // start calculating trendline at last x value
        double lastX = series.getX(n - 1).doubleValue();
        for (int i = 1; i <= numYears * 12; i++) {
            double x = lastX + (i / 12.0);
            series.add(x, m * x + b);
        }
    }
}
