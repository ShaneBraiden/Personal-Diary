import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DiaryGUI is the main class for building the Swing interface.
 * It interacts with DiaryManager to display data, separating UI from logic.
 */
public class DiaryGUI extends JFrame {

    private DiaryManager manager;

    // UI Components
    private JList<Entry> entryList;
    private DefaultListModel<Entry> listModel;
    private JTextArea contentArea;
    private JTextField dateField;
    private JButton saveButton;
    private JButton clearButton;

    public DiaryGUI(String password) throws Exception {
        // Initialize the logic manager
        manager = new DiaryManager(password);

        // Setup the main window
        setTitle("Personal Diary App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on screen

        // Build the layout
        initUI();
    }

    private void initUI() {
        // Create main panels
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // --- TOP PANEL (Date Input) ---
        topPanel.add(new JLabel("Date (yyyy-MM-dd): "));
        dateField = new JTextField(15);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        topPanel.add(dateField);

        // --- CENTER PANEL (List and Text Area) ---
        // 1. List on the left
        listModel = new DefaultListModel<>();
        refreshListModel();
        entryList = new JList<>(listModel);
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add listener to load entry when a date is clicked
        entryList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Entry selected = entryList.getSelectedValue();
                    if (selected != null) {
                        dateField.setText(selected.getDate());
                        contentArea.setText(selected.getContent());
                    }
                }
            }
        });

        JScrollPane listScrollPane = new JScrollPane(entryList);
        listScrollPane.setPreferredSize(new Dimension(150, 0));

        // 2. Text area on the right
        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane textScrollPane = new JScrollPane(contentArea);

        centerPanel.add(listScrollPane, BorderLayout.WEST);
        centerPanel.add(textScrollPane, BorderLayout.CENTER);

        // --- BOTTOM PANEL (Buttons) ---
        saveButton = new JButton("Save Entry");
        clearButton = new JButton("Clear / New");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveEntry();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        bottomPanel.add(saveButton);
        bottomPanel.add(clearButton);

        // Add panels to the main frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Invokes the manager to save the current text and date.
     */
    private void saveEntry() {
        String date = dateField.getText().trim();
        String content = contentArea.getText().trim();

        if (date.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date and Content cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        manager.addOrUpdateEntry(date, content);
        refreshListModel();
        JOptionPane.showMessageDialog(this, "Entry Saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears the current view for writing a new entry.
     */
    private void clearFields() {
        entryList.clearSelection();
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        contentArea.setText("");
        contentArea.requestFocus();
    }

    /**
     * Refreshes the JList to show the latest entries from the manager.
     */
    private void refreshListModel() {
        listModel.clear();
        for (Entry entry : manager.getEntries()) {
            listModel.addElement(entry);
        }
    }

    /**
     * Main method - application entry point.
     * Ensures GUI creation runs on the Event Dispatch Thread safely.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Prompt user for password using JPasswordField
                JPasswordField passwordField = new JPasswordField(10);
                int action = JOptionPane.showConfirmDialog(null, passwordField, 
                        "Enter Master Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
                if (action == JOptionPane.OK_OPTION) {
                    String password = new String(passwordField.getPassword());
                    if (password.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                    
                    try {
                        new DiaryGUI(password).setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Incorrect Password or Error unlocking diary.", 
                                "Decryption Failed", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                } else {
                    System.exit(0);
                }
            }
        });
    }
}
