import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

class User {
    String email;
    String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

class Note {
    String id;
    String title;
    String content;
    String category;
    String userEmail;

    public Note(String id, String title, String content, String category, String userEmail) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return title + " (" + category + ")";
    }
}

public class NoteManagementApp extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0); // Black
    private static final Color PRIMARY_COLOR = new Color(255, 215, 0); // Gold
    private static final Color SECONDARY_COLOR = new Color(50, 50, 50); // Dark gray
    private static final Color TEXT_COLOR = new Color(255, 215, 0); // Gold

    private CardLayout cardLayout;
    private JPanel loginPanel, signUpPanel, notePanel, forgotPasswordPanel, resetPasswordPanel;
    private List<User> users;
    private DefaultListModel<Note> listModel;
    private JList<Note> noteList;
    private JTextField titleField;
    private JTextArea contentArea;
    private JComboBox<String> categoryCombo;
    private JTextField searchField;
    private JScrollPane listScrollPane;
    private String currentUserEmail;
    private List<Note> notes;

    public NoteManagementApp() {
        super("Note Management System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        users = new ArrayList<>();
        notes = new ArrayList<>();
        listModel = new DefaultListModel<>();
        
        loadData();
        setupUI();
    }

    private void setupUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cardLayout = new CardLayout();
        setLayout(cardLayout);

        createLoginPanel();
        createSignUpPanel();
        createNotePanel();
        createForgotPasswordPanel();
        createResetPasswordPanel();

        add(loginPanel, "login");
        add(signUpPanel, "signup");
        add(notePanel, "notes");
        add(forgotPasswordPanel, "forgotPassword");
        add(resetPasswordPanel, "resetPassword");

        cardLayout.show(getContentPane(), "login");
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField emailField = createStyledTextField(20);
        JPasswordField passwordField = createStyledPasswordField(20);

        JButton loginButton = createStyledButton("Login");
        JButton signUpButton = createStyledButton("Sign Up");
        JButton forgotPasswordButton = createStyledButton("Forgot Password");

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(createStyledLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        loginPanel.add(signUpButton, gbc);

        gbc.gridy = 4;
        loginPanel.add(forgotPasswordButton, gbc);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (email.isEmpty() || password.isEmpty()) {
                showError("Please enter both email and password");
                return;
            }

            if (authenticateUser(email, password)) {
                currentUserEmail = email;
                loadUserNotes();
                cardLayout.show(getContentPane(), "notes");
            } else {
                showError("Invalid email or password");
            }
        });

        signUpButton.addActionListener(e -> cardLayout.show(getContentPane(), "signup"));
        forgotPasswordButton.addActionListener(e -> cardLayout.show(getContentPane(), "forgotPassword"));
    }

    private void createSignUpPanel() {
        signUpPanel = new JPanel(new GridBagLayout());
        signUpPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField emailField = createStyledTextField(20);
        JPasswordField passwordField = createStyledPasswordField(20);
        JPasswordField confirmPasswordField = createStyledPasswordField(20);

        JButton signUpButton = createStyledButton("Sign Up");
        JButton backButton = createStyledButton("Back to Login");

        gbc.gridx = 0; gbc.gridy = 0;
        signUpPanel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        signUpPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        signUpPanel.add(createStyledLabel("Password:"), gbc);
        gbc.gridx = 1;
        signUpPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        signUpPanel.add(createStyledLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        signUpPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        signUpPanel.add(signUpButton, gbc);

        gbc.gridy = 4;
        signUpPanel.add(backButton, gbc);

        signUpButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("All fields are required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                return;
            }

            if (userExists(email)) {
                showError("Email already registered");
                return;
            }

            users.add(new User(email, password));
            saveUsers();
            showInfo("Registration successful");
            cardLayout.show(getContentPane(), "login");
        });

        backButton.addActionListener(e -> cardLayout.show(getContentPane(), "login"));
    }

    private void createNotePanel() {
        notePanel = new JPanel(new BorderLayout());
        notePanel.setBackground(BACKGROUND_COLOR);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        titleField = createStyledTextField(20);
        contentArea = createStyledTextArea(10, 30);
        categoryCombo = new JComboBox<>(new String[]{"Personal", "Work", "Study"});
        styleComboBox(categoryCombo);

        JButton addButton = createStyledButton("Save Note");
        JButton deleteButton = createStyledButton("Delete Note");
        JButton logoutButton = createStyledButton("Logout");

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchField = createStyledTextField(20);
        searchPanel.add(createStyledLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        noteList = new JList<>(listModel);
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleList(noteList);
        listScrollPane = new JScrollPane(noteList);
        listScrollPane.setPreferredSize(new Dimension(200, 0));

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(createStyledLabel("Title:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(createStyledLabel("Content:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(new JScrollPane(contentArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(createStyledLabel("Category:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(addButton, gbc);

        gbc.gridy = 4;
        inputPanel.add(deleteButton, gbc);

        gbc.gridy = 5;
        inputPanel.add(logoutButton, gbc);

        notePanel.add(searchPanel, BorderLayout.NORTH);
        notePanel.add(listScrollPane, BorderLayout.WEST);
        notePanel.add(inputPanel, BorderLayout.CENTER);

        addButton.addActionListener(e -> saveNote());
        deleteButton.addActionListener(e -> deleteNote());
        logoutButton.addActionListener(e -> logout());
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Note selectedNote = noteList.getSelectedValue();
                if (selectedNote != null) {
                    new NoteViewWindow(selectedNote);
                }
            }
        });
        searchField.getDocument().addDocumentListener(new SearchListener());
    }

    private void createForgotPasswordPanel() {
        forgotPasswordPanel = new JPanel(new GridBagLayout());
        forgotPasswordPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField emailField = createStyledTextField(20);
        JButton resetButton = createStyledButton("Send Reset Code");
        JButton backButton = createStyledButton("Back to Login");

        gbc.gridx = 0; gbc.gridy = 0;
        forgotPasswordPanel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        forgotPasswordPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        forgotPasswordPanel.add(resetButton, gbc);

        gbc.gridy = 2;
        forgotPasswordPanel.add(backButton, gbc);

        resetButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showError("Please enter your email");
                return;
            }
            if (userExists(email)) {
                String resetCode = generateResetCode();
                showInfo("Reset code: " + resetCode);
                cardLayout.show(getContentPane(), "resetPassword");
            } else {
                showError("Email not found");
            }
        });

        backButton.addActionListener(e -> cardLayout.show(getContentPane(), "login"));
    }

    private void createResetPasswordPanel() {
        resetPasswordPanel = new JPanel(new GridBagLayout());
        resetPasswordPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField codeField = createStyledTextField(20);
        JPasswordField passwordField = createStyledPasswordField(20);
        JPasswordField confirmPasswordField = createStyledPasswordField(20);

        JButton resetButton = createStyledButton("Reset Password");
        JButton backButton = createStyledButton("Back to Login");

        gbc.gridx = 0; gbc.gridy = 0;
        resetPasswordPanel.add(createStyledLabel("Reset Code:"), gbc);
        gbc.gridx = 1;
        resetPasswordPanel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        resetPasswordPanel.add(createStyledLabel("New Password:"), gbc);
        gbc.gridx = 1;
        resetPasswordPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        resetPasswordPanel.add(createStyledLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        resetPasswordPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        resetPasswordPanel.add(resetButton, gbc);

        gbc.gridy = 4;
        resetPasswordPanel.add(backButton, gbc);

        resetButton.addActionListener(e -> {
            // Implement password reset logic here
            showInfo("Password reset successful");
            cardLayout.show(getContentPane(), "login");
        });

        backButton.addActionListener(e -> cardLayout.show(getContentPane(), "login"));
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBackground(SECONDARY_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setBackground(SECONDARY_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setBackground(SECONDARY_COLOR);
        area.setForeground(TEXT_COLOR);
        area.setCaretColor(TEXT_COLOR);
        area.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 14));
        return area;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(PRIMARY_COLOR.darker(), 2));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(SECONDARY_COLOR);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    private void styleList(JList<?> list) {
        list.setBackground(SECONDARY_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setBorder(new LineBorder(PRIMARY_COLOR, 1));
        list.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean userExists(String email) {
        return users.stream().anyMatch(user -> user.email.equals(email));
    }

    private boolean authenticateUser(String email, String password) {
        return users.stream()
                .anyMatch(user -> user.email.equals(email) && user.password.equals(password));
    }

    private String generateResetCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void saveNote() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();

        if (title.isEmpty() || content.isEmpty()) {
            showError("Title and content are required");
            return;
        }

        Note selectedNote = noteList.getSelectedValue();
        if (selectedNote != null) {
            selectedNote.title = title;
            selectedNote.content = content;
            selectedNote.category = category;
        } else {
            String id = UUID.randomUUID().toString();
            Note newNote = new Note(id, title, content, category, currentUserEmail);
            notes.add(newNote);
            listModel.addElement(newNote);
        }

        saveNotes();
        loadUserNotes();
        clearFields();
    }

    private void deleteNote() {
        Note selectedNote = noteList.getSelectedValue();
        if (selectedNote != null) {
            notes.remove(selectedNote);
            saveNotes();
            loadUserNotes();
            clearFields();
        }
    }

    private void clearFields() {
        titleField.setText("");
        contentArea.setText("");
        categoryCombo.setSelectedIndex(0);
        noteList.clearSelection();
    }

    private void logout() {
        currentUserEmail = null;
        clearFields();
        listModel.clear();
        cardLayout.show(getContentPane(), "login");
    }

    private void loadUserNotes() {
        listModel.clear();
        notes.stream()
            .filter(note -> note.userEmail.equals(currentUserEmail))
            .forEach(listModel::addElement);
    }

    private void loadData() {
        loadUsers();
        loadNotes();
    }

    private void loadUsers() {
        users.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt"))) {
            for (User user : users) {
                writer.write(user.email + "," + user.password);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNotes() {
        notes.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("notes.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    notes.add(new Note(parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveNotes() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("notes.txt"))) {
            for (Note note : notes) {
                writer.write(String.join("|", note.id, note.title, note.content, note.category, note.userEmail));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SearchListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterNotes();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterNotes();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterNotes();
        }

        private void filterNotes() {
            String searchText = searchField.getText().toLowerCase();
            listModel.clear();
            notes.stream()
                .filter(note -> note.userEmail.equals(currentUserEmail))
                .filter(note -> note.title.toLowerCase().contains(searchText) ||
                               note.category.toLowerCase().contains(searchText))
                .forEach(listModel::addElement);
        }
    }

    private class NoteViewWindow extends JFrame {
        private static final long serialVersionUID = 1L;

        public NoteViewWindow(Note note) {
            super(note.title);
            setSize(600, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(BACKGROUND_COLOR);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel(note.title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(PRIMARY_COLOR);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);

            JTextArea contentArea = new JTextArea(note.content);
            contentArea.setFont(new Font("Arial", Font.PLAIN, 16));
            contentArea.setForeground(TEXT_COLOR);
            contentArea.setBackground(SECONDARY_COLOR);
            contentArea.setWrapStyleWord(true);
            contentArea.setLineWrap(true);
            contentArea.setEditable(false);
            contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
            scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = PRIMARY_COLOR;
                    this.trackColor = SECONDARY_COLOR;
                }
            });

            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            setContentPane(mainPanel);
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new NoteManagementApp().setVisible(true);
        });
    }
}