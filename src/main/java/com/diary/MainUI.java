/*
 * =========================================================================
 *  Encrypted Personal Diary App
 *  ----------------------------
 *  DEPENDENCY SETUP:
 *  This project requires FlatLaf for the Modern UI.
 * 
 *  If you use Maven, add this to your pom.xml:
 *  <dependency>
 *      <groupId>com.formdev</groupId>
 *      <artifactId>flatlaf</artifactId>
 *      <version>3.4.1</version>
 *  </dependency>
 * 
 *  For direct download (JAR files): 
 *  Download "flatlaf-3.4.1.jar" from Maven Central and add it to your module path.
 * =========================================================================
 */
package com.diary;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;

/**
 * MainUI: The modern Swing interface that handles the login screen, 
 * two-pane layout, and user interactions.
 */
public class MainUI extends JFrame {

    private CryptoUtil cryptoUtil;
    private FileManager fileManager;
    private List<Entry> currentEntries;

    // UI Components
    private DefaultListModel<Entry> listModel;
    private JList<Entry> entryList;
    private JTextArea editorArea;
    private JButton saveButton;
    private JButton newButton;

    public MainUI() {
        setTitle("Encrypted Journal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Set modern font mapping before creating UI
        FlatRobotoFont.install();
        FlatLaf.setPreferredFontFamily(FlatRobotoFont.FAMILY);
        FlatLaf.setPreferredLightFontFamily(FlatRobotoFont.FAMILY_LIGHT);
        FlatLaf.setPreferredSemiboldFontFamily(FlatRobotoFont.FAMILY_SEMIBOLD);
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));

        initLoginScreen();
    }

    private void initLoginScreen() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel titleLabel = new JLabel("Secure Journal");
        titleLabel.setFont(new Font(FlatRobotoFont.FAMILY_SEMIBOLD, Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(titleLabel);

        formBox.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Enter your master password safely:");
        subtitleLabel.putClientProperty("FlatLaf.styleClass", "small");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(subtitleLabel);

        formBox.add(Box.createVerticalStrut(25));

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JComponent.roundRect", true);
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(passwordField);

        formBox.add(Box.createVerticalStrut(25));

        JButton loginBtn = new JButton("Unlock Diary");
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");
        loginBtn.setBackground(UIManager.getColor("Actions.Blue"));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font(FlatRobotoFont.FAMILY_SEMIBOLD, Font.PLAIN, 15));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        formBox.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String pwd = new String(passwordField.getPassword());
            if (pwd.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                this.cryptoUtil = new CryptoUtil(pwd);
                this.fileManager = new FileManager(this.cryptoUtil);
                
                // Attempt to load. If it fails with BadPaddingException, the password is wrong
                this.currentEntries = this.fileManager.loadEntries();
                
                remove(loginPanel); // Remove lock screen
                initMainUI();       // Transition to Main App
                revalidate();
                repaint();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Incorrect password or corrupted file.\n" + ex.getMessage(), "Decryption Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginPanel.add(formBox);
        add(loginPanel, BorderLayout.CENTER);
    }

    private void initMainUI() {
        setLayout(new BorderLayout());

        // Left Panel (List of entries)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(new EmptyBorder(15, 15, 15, 10));

        JLabel historyTitle = new JLabel("Entries History");
        historyTitle.setFont(new Font(FlatRobotoFont.FAMILY_SEMIBOLD, Font.BOLD, 18));
        historyTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        leftPanel.add(historyTitle, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        for (Entry entry : currentEntries) {
            listModel.addElement(entry);
        }

        entryList = new JList<>(listModel);
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entryList.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        entryList.setFixedCellHeight(35);
        
        JScrollPane listScroller = new JScrollPane(entryList);
        listScroller.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        leftPanel.add(listScroller, BorderLayout.CENTER);

        newButton = new JButton("+ New Entry");
        newButton.putClientProperty("JButton.buttonType", "roundRect");
        newButton.setMargin(new Insets(8, 0, 8, 0));
        leftPanel.add(newButton, BorderLayout.SOUTH);

        // Center Panel (Editor)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(15, 10, 15, 15));

        editorArea = new JTextArea();
        editorArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        editorArea.setLineWrap(true);
        editorArea.setWrapStyleWord(true);
        editorArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane editorScroller = new JScrollPane(editorArea);
        editorScroller.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        centerPanel.add(editorScroller, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        saveButton = new JButton("Encrypt & Save 🔒");
        saveButton.putClientProperty("JButton.buttonType", "roundRect");
        saveButton.setFont(new Font(FlatRobotoFont.FAMILY_SEMIBOLD, Font.PLAIN, 15));
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        bottomPanel.add(saveButton);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add split logic
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);

        // Actions
        newButton.addActionListener(e -> {
            entryList.clearSelection();
            editorArea.setText("");
            editorArea.requestFocus();
        });

        entryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Entry selected = entryList.getSelectedValue();
                if (selected != null) {
                    editorArea.setText(selected.getContent());
                }
            }
        });

        saveButton.addActionListener(e -> saveCurrentEditor());

        // Save aggressively just in case
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ensure latest edits on selected are saved before closing
                try {
                    fileManager.saveEntries(currentEntries);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void saveCurrentEditor() {
        String content = editorArea.getText().trim();
        if (content.isEmpty()) return;

        Entry selected = entryList.getSelectedValue();
        if (selected == null) {
            // New Entry
            Entry newEntry = new Entry(new Date(), content);
            currentEntries.add(newEntry);
            listModel.addElement(newEntry);
            entryList.setSelectedValue(newEntry, true);
        } else {
            // Update existing
            selected.setContent(content);
            selected.setDate(new Date()); // Update timestamp
            entryList.repaint();
        }

        try {
            fileManager.saveEntries(currentEntries);
            JOptionPane.showMessageDialog(this, "Saved & Encrypted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving to file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Run safely on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Setup FlatLaf Dark mode
                UIManager.setLookAndFeel(new FlatDarkLaf());
                FlatLaf.updateUI();
            } catch (Exception ex) {
                System.err.println("Failed to initialize FlatLaf");
            }

            MainUI app = new MainUI();
            app.setVisible(true);
        });
    }
}
