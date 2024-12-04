package projectworkspace;

import java.awt.BorderLayout;

import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenu extends JPanel {
    private final App app;

    public MainMenu(App app) {
        super(new BorderLayout());
        this.app = app;

        JLabel descLabel = createDescLabel();
        this.add(descLabel, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanel();

        this.add(buttonPanel, BorderLayout.CENTER);
    }

    private JLabel createDescLabel() {
        JLabel descLabel = new JLabel("Welcome to Regional Living Insights! Choose an option below to begin: ");
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        return descLabel;
    }

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
