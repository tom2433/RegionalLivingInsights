package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class CustomDataVisScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private ArrayList<ArrayList<String>> datasets;
    private JPanel actionPanel;
    private JButton beginDateBtn;
    private String selectedBeginMonth;
    private JButton viewResultsBtn;

    public CustomDataVisScreen(App app) {
        this.app = app;
        this.componentFactory = null;
        this.datasets = null;
    }

    public CustomDataVisScreen(App app, ArrayList<ArrayList<String>> datasets) {
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);
        this.datasets = datasets;

        JLabel descLabel = componentFactory.createDescLabel("Comparing all custom datasets");
        JLabel descLabel2 =  componentFactory.createDescLabel("Begin by selecting the begin date to pull data from. The end date to stop pulling data is Oct2024.");
        JLabel descLabel3 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs");
        JLabel dividerLabel = componentFactory.createDescLabel("------------------------------------");

        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);

        JButton resetBtn = createResetBtn();
        createBeginDateBtn();

        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel2);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel3);
        for (ArrayList<String> dataset : datasets) {
            JLabel setLabel = componentFactory.createDescLabel("Set " + (datasets.indexOf(dataset) + 1) + " ------------------------------");
            actionPanel.add(Box.createVerticalStrut(2));
            actionPanel.add(setLabel);
            actionPanel.add(Box.createVerticalStrut(2));
            for (String area : dataset) {
                JLabel areaLabel = componentFactory.createDescLabel(area);
                actionPanel.add(areaLabel);
            }
        }
        actionPanel.add(dividerLabel);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(beginDateBtn);

        contentPanel.add(actionPanel, BorderLayout.CENTER);

        JButton backBtn = componentFactory.createBackButton();
        this.add(backBtn, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void createBeginDateBtn() {
        beginDateBtn = new JButton("Select begin date");
        beginDateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        beginDateBtn.addActionListener(e -> createBeginDateDialog());
    }

    private void createBeginDateDialog() {
        String beginMonth = getBeginMonth();
        int beginMonthNum = DataReader.getNumFromMonth(beginMonth.substring(0, 3));
        int beginYearNum = Integer.parseInt(beginMonth.substring(3));
        ArrayList<String> monthsToChoose = new ArrayList<>();

        for (int i = beginYearNum; i <= 2020; i++) {
            for (int j = (i == beginYearNum ? beginMonthNum : 1); j <= 12; j++) {
                monthsToChoose.add(DataReader.getMonthFromNum(j) + Integer.toString(i));
            }
        }

        JDialog dialog = new JDialog(app.getFrame(), "Select a start month to begin pulling data");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store month options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (String month : monthsToChoose) {
            JButton monthBtn = new JButton(month);
            monthBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            monthBtn.addActionListener(e -> beginMonthSelected(month, dialog));
            panel.add(monthBtn);
            panel.add(Box.createVerticalStrut(10));
        }

        // wrap panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        // add scroll pane to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);

        // show dialog
        // center relative to parent frame
        dialog.setLocationRelativeTo(app.getFrame());
        dialog.setVisible(true);
    }

    private void beginMonthSelected(String month, JDialog dialog) {
        selectedBeginMonth = month;
        dialog.dispose();
        beginDateBtn.setText("Selected begin month: " + selectedBeginMonth);
        beginDateBtn.setEnabled(false);

        viewResultsBtn = new JButton("View Results");
        viewResultsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewResultsBtn.addActionListener(e -> viewResults());

        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(viewResultsBtn);
    }

    private void viewResults() {
        viewResultsBtn.setEnabled(false);

        JFreeChart populationBarChart = createPopulationBarChart();
        ChartPanel barChartPanel = new ChartPanel(populationBarChart);
        barChartPanel.setPreferredSize(new Dimension(300, 400));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(barChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        JFreeChart zhviLineChart = createZhviLineChart();
        ChartPanel lineChartPanel = new ChartPanel(zhviLineChart);
        lineChartPanel.setPreferredSize(new Dimension(300, 300));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(lineChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        componentFactory.createCalcPredictedBtn(actionPanel, lineChartPanel);
    }

    private JFreeChart createZhviLineChart() {
        XYSeriesCollection finalXYSeriesCollection = new XYSeriesCollection();

        for (ArrayList<String> dataset : datasets) {
            Map<Double, Double> datasetZhviData = app.getDataReader().getZhviDataFromSet(dataset, selectedBeginMonth);
            XYSeries datasetSeries = new XYSeries("Set " + (datasets.indexOf(dataset) + 1));

            for (Map.Entry<Double, Double> entry : datasetZhviData.entrySet()) {
                datasetSeries.add(entry.getKey(), entry.getValue());
            }
            finalXYSeriesCollection.addSeries(datasetSeries);
        }

        JFreeChart lineChart = ChartFactory.createXYLineChart(
            "Median Home Values For " + datasets.size() + " Custom Datasets",
            "Date (year)",
            "Median Home Value (USD)",
            finalXYSeriesCollection
        );

        return lineChart;
    }

    private JFreeChart createPopulationBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(
            app.getDataReader().getAvgStateDensity(),
            "Average US State",
            "Average Density"
        );

        // add value to dataset for each set
        for (ArrayList<String> set : datasets) {
            dataset.addValue(
                app.getDataReader().getDensityFromSet(set),
                "Custom Dataset",
                "Set " + (datasets.indexOf(set) + 1)
            );
        }

        JFreeChart barChart = ChartFactory.createBarChart(
            "Population Densities",
            "Area",
            "Population Density Per Square Kilometer",
            dataset
        );

        return barChart;
    }

    private String getBeginMonth() {
        ArrayList<Double> beginMonths = new ArrayList<>();
        for (ArrayList<String> dataset : datasets) {
            for (String area : dataset) {
                // if area is a state
                if (area.length() <= 2) {
                    beginMonths.add(DataReader.getDoubleFromMonth(app.getDataReader().getBeginDateFromState(area)));
                // else (area is a region)
                } else {
                    String region = area.substring(0, area.indexOf(',')).trim();
                    String state = area.substring(area.indexOf(',') + 2).trim();
                    beginMonths.add(DataReader.getDoubleFromMonth(app.getDataReader().getBeginDateFromRegion(state, region)));
                }
            }
        }

        Double beginMonth = Collections.max(beginMonths);
        return DataReader.getMonthFromDouble(beginMonth);
    }

    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshCustomDataVisScreen(datasets));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }
}
