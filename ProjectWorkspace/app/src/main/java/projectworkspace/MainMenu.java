package projectworkspace;

import java.awt.BorderLayout;

import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This class creates and handles all operations in the MainMenu screen. It allows the user to
 * select a screen to go to to either compare regions, compare states, or compare custom datasets.
 *
 * @author Thomas England
 */
public class MainMenu extends JPanel {
    private final App app;

    /**
     * Constructs a MainMenu screen as a JPanel belonging to a specified App.
     *
     * @param app App object referencing the App that this JPanel belongs to.
     */
    public MainMenu(App app) {
        super(new BorderLayout());
        this.app = app;

        JLabel descLabel = createDescLabel();
        this.add(descLabel, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanel();

        this.add(buttonPanel, BorderLayout.CENTER);
    }

    /**
     * Creates a JLabel containing welcome text.
     *
     * @return a JLabel containing welcome text.
     */
    private JLabel createDescLabel() {
        JLabel descLabel = new JLabel("Welcome to Regional Living Insights! Choose an option below to begin: ");
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        return descLabel;
    }

    /**
     * Creates a JPanel containing buttons that bring the user to their respective screens.
     *
     * @return A JPanel containing buttons that bring the user to their respective screens.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        String[] buttonLabels = {"Compare Regions", "Compare States", "Compare Custom Datasets with Regions and/or States"};
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(app);
            button.setAlignmentX(Box.CENTER_ALIGNMENT);
            buttonPanel.add(button);
            // add spacing between buttons
            buttonPanel.add(Box.createVerticalStrut(10));
        }

        buttonPanel.setBorder(createEmptyBorder(20, 50, 20, 50));

        return buttonPanel;
    }
}
