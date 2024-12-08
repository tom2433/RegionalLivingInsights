package projectworkspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
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

/**
 * This class creates all components and handles all operations for the StateDataVisScreen. Allows
 * user to select a begin date to start pulling data from (end date is Oct2024), and displays a
 * bar chart comparing the 2 selected states' population density to the avg US state population
 * density and a line graph comparing the 2 selected states' median home values since the selected
 * begin date. Uses ComponentFactory to create a predicted data button which displays a graph of
 * predicted home values for the selected states.
 *
 * @author Thomas England
 */
public class StateDataVisScreen extends JPanel {
    private final App app;
    private ComponentFactory componentFactory;
    private String state1;
    private String state2;
    private JPanel actionPanel;
    private JButton beginDateBtn;
    private String selectedBeginMonth;
    private JButton viewResultsBtn;

    /**
     * Default constructor for the StateDataVisScreen. Constructs an empty StateDataVisScreen
     * object as a JPanel belonging to a specified App.
     *
     * @param app App object that this JPanel belongs to
     */
    public StateDataVisScreen(App app) {
        this.app = app;
    }

    /**
     * Workhorse constructor for the StateDataVisScreen. Constructs a StateDataVisScreen as a
     * JPanel with a BorderLayout containing all components the user initially sees on the
     * StateDataVisScreen.
     *
     * @param app App object that this JPanel belongs to
     * @param state1 String containing the first user-selected date to compare data
     * @param state2 String containing the second user-selected date to compare data
     */
    public StateDataVisScreen(App app, String state1, String state2) {
        // create as JPanel with BorderLayout
        super(new BorderLayout());
        this.app = app;
        this.componentFactory = new ComponentFactory(app);
        this.state1 = state1;
        this.state2 = state2;

        // create description labels and instructions
        JLabel descLabel = componentFactory.createDescLabel("Comparing " + state1 + " vs. " + state2);
        JLabel descLabel2 = componentFactory.createDescLabel("Begin by selecting the begin date to pull data from. The end date to stop pulling data is Oct2024.");
        JLabel descLabel3 = componentFactory.createDescLabel("If you make any accidental selections, click the button below to reset all inputs.");

        // create content JPanel to hold all contents of the screen (except next and back button)
        JPanel contentPanel = componentFactory.createContentPanel();
        contentPanel.add(descLabel, BorderLayout.NORTH);

        // create resetBtn to refresh screen and reset user input and create beginDateBtn to select begin date
        JButton resetBtn = createResetBtn();
        createBeginDateBtn();

        // create actionPanel as JPanel to hold all components in the main area including dynamic components
        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel2);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(descLabel3);
        actionPanel.add(Box.createVerticalStrut(2));
        actionPanel.add(resetBtn);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(beginDateBtn);

        // add actionPanel to contentPanel
        contentPanel.add(actionPanel, BorderLayout.CENTER);

        // create back button and add to parent JPanel
        JButton backBtn = componentFactory.createBackButton();
        this.add(backBtn, BorderLayout.NORTH);

        // wrap contentPanel in scrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // add scrollPane to parent JPanel (this)
        this.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Called from the constructor to create the resetBtn which refreshes the screen to reset all
     * user input.
     *
     * @return JButton which refreshes the screen to reset all user input
     */
    private JButton createResetBtn() {
        JButton resetBtn = new JButton("Reset all inputs");
        resetBtn.addActionListener(e -> app.refreshStateDataVisScreen(state1, state2));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return resetBtn;
    }

    /**
     * Called from the constructor to create the beginDateBtn which prompts the user for a begin
     * date to begin pulling data from (last date is Oct2024).
     */
    private void createBeginDateBtn() {
        beginDateBtn = new JButton("Select begin date");
        beginDateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        beginDateBtn.addActionListener(e -> createBeginDateDialog());
    }

    /**
     * Called when the user clicks the beginDateBtn to select a begin date. Displays a dialog
     * prompt for the user to select a begin date to begin pulling data.
     */
    private void createBeginDateDialog() {
        // retrieve first available begin month for both states' data
        String beginMonth = app.getDataReader().getBeginDateFromStates(state1, state2);
        int beginMonthNum = DataReader.getNumFromMonth(beginMonth.substring(0, 3));
        int beginYearNum = Integer.parseInt(beginMonth.substring(3));
        // ArrayList to store all available begin months as Strings
        ArrayList<String> monthsToChoose = new ArrayList<>();

        // fill ArrayList with available months
        for (int i = beginYearNum; i <= 2020; i++) {
            for (int j = (i == beginYearNum ? beginMonthNum : 1); j <= 12; j++) {
                monthsToChoose.add(DataReader.getMonthFromNum(j) + Integer.toString(i));
            }
        }

        // create dialog prompt
        JDialog dialog = new JDialog(app.getFrame(), "Select a start month to begin pulling data");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 300);

        // create panel to store month options
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add month button for each month to panel
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

    /**
     * Called when the user selects a begin month from the dialog prompt created in
     * createBeginDateDialog() after the user chooses to select a beginDate. Closes the dialog,
     * creates a 'view results' button, and adds button to actionPanel.
     *
     * @param month String containing the month that the user just selected
     * @param dialog JDialog referencing the dialog that the user just selected the month from
     *               (to close)
     */
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

    /**
     * Called when the user clicks the viewResultsBtn. Displays a bar chart containing avg US state
     * population density compared to the population densities of both the first user selected
     * state and the second. Displays a line comparing the median home values of both states since
     * the selected begin month. Uses ComponentFactory to create a 'calculate predicted data'
     * button which displays the same line graph but with predicted values from a trendline for a
     * user-specified number of years in the future.
     */
    private void viewResults() {
        // disable viewResultsBtn to prevent user from displaying more of the same graphs
        viewResultsBtn.setEnabled(false);
        // create population bar chart and add to actionPanel
        JFreeChart populationBarChart = createPopulationBarChart();
        ChartPanel barChartPanel = new ChartPanel(populationBarChart);
        barChartPanel.setPreferredSize(new Dimension(300, 400));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(barChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        // create zhvi line chart and add to actionPanel
        JFreeChart zhviLineChart = createZhviLineChart();
        ChartPanel lineChartPanel = new ChartPanel(zhviLineChart);
        lineChartPanel.setPreferredSize(new Dimension(300, 300));
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(lineChartPanel);
        actionPanel.revalidate();
        actionPanel.repaint();

        // create calculate predicted data button
        componentFactory.createCalcPredictedBtn(actionPanel, lineChartPanel);
    }

    /**
     * Called by the viewResults() method when the user clicks the button to view results. Creates
     * a line chart with two datasets - one for one user selected state and one for the other
     * containing median home values for each month since the user specified begin month.
     *
     * @return JFreeChart containing a line chart with median home value data from both states
     */
    private JFreeChart createZhviLineChart() {
        // HashMaps and XYSeries to store XY values for each state
        Map<Double, Double> state1zhviData = app.getDataReader().getZhviDataFromState(state1, selectedBeginMonth);
        Map<Double, Double> state2zhviData = app.getDataReader().getZhviDataFromState(state2, selectedBeginMonth);
        XYSeries state1Series = new XYSeries(state1);
        XYSeries state2Series = new XYSeries(state2);
        // XYSeriesCollection to store both XYSeries
        XYSeriesCollection dataset = new XYSeriesCollection();

        // fill first XYSeries with XY values returned from DataReader
        for (Map.Entry<Double, Double> entry : state1zhviData.entrySet()) {
            state1Series.add(entry.getKey(), entry.getValue());
        }

        // fill second XYSeries with XY values returned from DataReader
        for (Map.Entry<Double, Double> entry : state2zhviData.entrySet()) {
            state2Series.add(entry.getKey(), entry.getValue());
        }

        // add both XYSeries to XYSeriesCollection
        dataset.addSeries(state1Series);
        dataset.addSeries(state2Series);

        // create line chart with XYSeriesCollection and return
        JFreeChart lineChart = ChartFactory.createXYLineChart(
            "Median Home Values For " + state1 + " vs. " + state2,
            "Date (Year)",
            "Median Home Value (USD)",
            dataset
        );
        return lineChart;
    }

    /**
     * Called by the viewResults() method when the user clicks the button to view results. Creates
     * a bar chart with 3 values - one for the avg US state population density, and two more for
     * the population densities of the user selected states.
     *
     * @return JFreeChart containing a bar chart with population densities from the selected
     * states and the avg US state
     */
    private JFreeChart createPopulationBarChart() {
        // DefaultCategoryDataset to hold all three values from avg US state and both selected states
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(
            app.getDataReader().getAvgStateDensity(),
            "Average US State",
            "Average Density"
        );
        dataset.addValue(
            app.getDataReader().getStateDensity(state1),
            "Selected US State",
            state1
        );
        dataset.addValue(
            app.getDataReader().getStateDensity(state2),
            "Selected US State",
            state2
        );

        // create bar chart with DefaultCategoryDataset and return
        JFreeChart barChart = ChartFactory.createBarChart(
            "Population Density",
            "State",
            "Population Density Per Square Kilometer",
            dataset
        );
        return barChart;
    }
}
