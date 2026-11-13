import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class Main extends JFrame {

    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<Contact> recycleBin = new ArrayList<>();
    private Contact deletedContact = null;

    private JTextField name, phone, email, address;
    private JTextField alternatePhone, category, dob, anniversary;
    private JTextField relationship, tags, search;
    private JTextArea notes;

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private boolean darkMode = false;
    private String selectedPhotoPath = "";

    private static final String DATA_FILE = "contacts_data.dat";
    private static final String BACKUP_FILE = "contacts_backup.dat";
    private static final String PASSWORD_FILE = "app_password.txt";

    public Main() {
        if (!login()) {
            System.exit(0);
        }

        setTitle("Contact Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
        loadContacts();
        refreshTable();
        showReminders();
    }

    // ---------------- LOGIN ----------------

    private boolean login() {
    File file = new File("app_password.txt");

    if (!file.exists()) {
        JPasswordField newPasswordField = new JPasswordField();

        int option = JOptionPane.showConfirmDialog(
            null,
            newPasswordField,
            "Create your application password",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (option != JOptionPane.OK_OPTION) {
            return false;
        }

        String password = new String(newPasswordField.getPassword());

        if (password.trim().isEmpty()) {
            return false;
        }

        saveEncryptedPassword(password);
        return file.exists();
    }

    JPasswordField passwordField = new JPasswordField();

    int result = JOptionPane.showConfirmDialog(
        null,
        passwordField,
        "Enter Application Password",
        JOptionPane.OK_CANCEL_OPTION
    );

    if (result != JOptionPane.OK_OPTION) {
        return false;
    }

    String enteredPassword =
        new String(passwordField.getPassword());

    if (verifyEncryptedPassword(enteredPassword)) {
        return true;
    }

    // Support migration from the old plain-text format
    try {
        String saved = new String(
            Files.readAllBytes(file.toPath())
        ).trim();

        if (!saved.startsWith("PBKDF2")
                && saved.equals(enteredPassword)) {
            saveEncryptedPassword(enteredPassword);
            return true;
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(
            null, "Could not read password."
        );
        return false;
    }

    JOptionPane.showMessageDialog(
        null, "Incorrect password."
    );
    return false;
}
    // ---------------- USER INTERFACE ----------------

    private void createUI() {

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel heading = new JLabel(
            "CONTACT MANAGEMENT SYSTEM",
            JLabel.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 25));
        main.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 4, 8, 8));
        fields.setBorder(
            BorderFactory.createTitledBorder("Contact Details")
        );

        name = new JTextField();
        phone = new JTextField();
        email = new JTextField();
        address = new JTextField();
        alternatePhone = new JTextField();
        category = new JTextField("General");
        dob = new JTextField();
        anniversary = new JTextField();
        relationship = new JTextField();
        tags = new JTextField();
        notes = new JTextArea(2, 15);

        addField(fields, "Name:", name);
        addField(fields, "Phone:", phone);
        addField(fields, "Email:", email);
        addField(fields, "Address:", address);
        addField(fields, "Alternate Phone:", alternatePhone);
        addField(fields, "Category:", category);
        addField(fields, "Birthday (YYYY-MM-DD):", dob);
        addField(fields, "Anniversary:", anniversary);
        addField(fields, "Relationship:", relationship);
        addField(fields, "Tags:", tags);
        addField(fields, "Notes:", new JScrollPane(notes));

        JPanel buttons = new JPanel(new FlowLayout());

        addButton(buttons, "Add", e -> addContact());
        addButton(buttons, "Update", e -> updateContact());
        addButton(buttons, "Delete", e -> deleteContact());
        addButton(buttons, "Undo Delete", e -> undoDelete());
        addButton(buttons, "Clear", e -> clearFields());
        addButton(buttons, "Favorite", e -> toggleFavorite());
        addButton(buttons, "History", e -> showHistory());
        addButton(buttons, "Add Phone", e -> addExtraPhone());
        addButton(buttons, "Email", e -> openEmail());
        addButton(buttons, "Call", e -> openPhone());
        addButton(buttons, "Dashboard", e -> dashboard());
        addButton(buttons, "Recycle Bin", e -> showRecycleBin());
        addButton(buttons, "Import CSV", e -> importCSV());
        addButton(buttons, "Export CSV", e -> exportCSV());
        addButton(buttons, "PDF Export", e -> exportPDF());
        addButton(buttons, "Excel Export", e -> exportExcel());
        addButton(buttons, "Backup", e -> backup());
        addButton(buttons, "Restore Backup", e -> restoreBackup());
        addButton(buttons, "Print", e -> printContacts());
        addButton(buttons, "Dark Mode", e -> toggleTheme());
        addButton(buttons, "Sort A-Z", e -> sortContacts());
        addButton(buttons, "Duplicates", e -> findDuplicates());
        addButton(buttons, "Merge Duplicates", e -> mergeDuplicates());
        addButton(buttons, "Change Password", e -> changePassword());
        addButton(buttons, "Birthday Reminder", e -> showReminders());
        addButton(buttons, "Choose Photo", e -> choosePhoto());
        addButton(buttons, "View Photo", e -> viewPhoto());
        addButton(buttons, "Assign Group", e -> assignGroup());
        addButton(buttons, "View Groups", e -> viewGroups());
        addButton(buttons, "Filter Group", e -> filterGroup());
        addButton(buttons, "Show All", e -> showAllContacts());
        addButton(buttons, "Advanced Search", e -> advancedSearch());
        addButton(buttons, "Export TXT", e -> exportContactsToText());
        addButton(buttons, "Import TXT", e -> importContactsFromText());
        addButton(buttons, "Export JSON", e -> exportContactsToJSON());
        addButton(buttons, "Export vCard", e -> exportContactVCard());
        addButton(buttons, "Copy Contact", e -> copyContactDetails());
        addButton(buttons, "Print Contact", e -> printIndividualContact());
        addButton(buttons, "Block / Unblock", e -> toggleContactBlock());
        addButton(buttons, "Category Statistics", e -> showCategoryStatistics());
        addButton(buttons, "View Blocked", e -> viewBlockedContacts());
        addButton(buttons, "Select All", e -> selectAllContacts());
        addButton(buttons, "Deselect All", e -> deselectAllContacts());
        addButton(buttons, "Export Selected", e -> exportSelectedContacts());
        addButton(buttons, "Sort by Phone", e -> sortContactsByPhone());
        addButton(buttons, "Sort by Email", e -> sortContactsByEmail());
        addButton(buttons, "View Favorites", e -> viewFavoriteContacts());
        addButton(buttons, "Copy Phone Number", e -> copyPhoneNumber());
        addButton(buttons, "Copy Email", e -> copyEmailAddress());
        addButton(buttons, "Recent Contacts", e -> showRecentContacts());
        addButton(buttons, "Recently Updated", e -> showRecentlyUpdatedContacts());
        addButton(buttons, "Favorite Count", e -> showFavoriteCount());
        addButton(buttons, "Missing Email", e -> findContactsWithoutEmail());
        addButton(buttons, "Missing Address", e -> findContactsWithoutAddress());
        addButton(buttons, "Import JSON", e -> importContactsFromJSON());
        addButton(buttons, "Bulk Delete", e -> bulkDeleteContacts());
        addButton(buttons, "Export Selected CSV",e -> exportSelectedContactsToCSV());
        addButton(buttons, "Bulk Update Category",e -> bulkUpdateCategory());
        addButton(buttons, "Bulk Add Tags",e -> bulkAddTags());
        addButton(buttons, "Bulk Remove Tags", e -> bulkRemoveTags());
        addButton(buttons, "Clear All Filters", e -> clearAllFilters());
        addButton(buttons, "Total Contacts", e -> showTotalContacts());
        addButton(buttons, "Category Count", e -> showCategoryCount());
        addButton(buttons, "Search Company", e -> searchByCompany());
        addButton(buttons, "Search Initial", e -> searchByInitial());
        addButton(buttons, "Search Phone Prefix", e -> searchByPhonePrefix());
        addButton(buttons, "Search Email Domain", e -> searchByEmailDomain());
        addButton(buttons, "Alternate Phone Contacts", e -> showAlternatePhoneContacts());
        addButton(buttons, "Birthday Contacts", e -> showBirthdayContacts());
        addButton(buttons, "Anniversary Contacts", e -> showAnniversaryContacts());
        addButton(buttons, "Emergency Contacts", e -> showEmergencyContacts());
        addButton(buttons, "Set Emergency Contact", e -> toggleEmergencyContact());
        addButton(buttons, "Private Contact", e -> togglePrivateContact());
        addButton(buttons, "View Private Contacts", e -> viewPrivateContacts());
        addButton(buttons, "Search by Tag", e -> searchByTag());
        addButton(buttons, "Search Relationship", e -> searchByRelationship());
        addButton(buttons, "Search Department", e -> searchByDepartment());
        addButton(buttons, "Contacts with Website", e -> showWebsiteContacts());
        addButton(buttons, "Search Nickname", e -> searchByNickname());
        addButton(buttons, "Search Job Title", e -> searchByJobTitle());
        addButton(buttons, "Created Today", e -> showContactsCreatedToday());
        addButton(buttons, "Updated Today", e -> showContactsUpdatedToday());
        addButton(buttons, "Sort Z-A", e -> sortContactsZA());
        addButton(buttons, "Oldest Contacts", e -> sortOldestContacts());
        addButton(buttons, "Newest Contacts", e -> sortNewestContacts());
        addButton(buttons, "Export XML", e -> exportContactsToXML());
        addButton(buttons, "Export HTML", e -> exportContactsToHTML());
        addButton(buttons, "Export Markdown", e -> exportContactsToMarkdown());
        addButton(buttons, "Import XML", e -> importContactsFromXML());
        addButton(buttons, "Validate All Contacts", e -> validateAllContacts());
        addButton(buttons, "Find Invalid Emails", e -> findInvalidEmails());
        addButton(buttons, "Find Invalid Phones", e -> findInvalidPhones());
        addButton(buttons, "Database Report", e -> generateDatabaseReport());

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(fields, BorderLayout.CENTER);
        top.add(new JScrollPane(buttons), BorderLayout.SOUTH);

        search = new JTextField(25);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(search);

        String[] columns = {
            "ID", "Name", "Phone", "Email",
            "Category", "Favorite"
        };

        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        displaySelectedContact();

    }
});

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(searchPanel, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        setContentPane(main);

        search.addActionListener(e -> searchContacts());

        search.getDocument().addDocumentListener(
    new javax.swing.event.DocumentListener() {

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            searchContacts();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            searchContacts();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            searchContacts();
        }
    }
);

}  // Close createUI()

    private void addField(
        JPanel panel, String label, Component component
    ) {
        panel.add(new JLabel(label));
        panel.add(component);
    }

    private void addButton(
        JPanel panel,
        String title,
        java.awt.event.ActionListener action
    ) {
        JButton button = new JButton(title);
        button.addActionListener(action);
        panel.add(button);
    }

    // ---------------- CONTACT OPERATIONS ----------------

    private boolean validateFields() {

        if (name.getText().trim().isEmpty()
            || phone.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                this, "Name and phone are required."
            );
            return false;
        }

        if (!phone.getText().trim().matches("[0-9+() -]{7,20}")) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid phone number."
            );
            return false;
        }

        if (!email.getText().trim().isEmpty()
            && !email.getText().trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )) {

            JOptionPane.showMessageDialog(
                this, "Enter a valid email."
            );
            return false;
        }

        return true;
    }

    private String generateID() {
    int max = 0;

    for (Contact c : contacts) {
        try {
            String id = c.getId();

            if (id != null && id.startsWith("C")) {
                int number = Integer.parseInt(id.substring(1));
                max = Math.max(max, number);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    return String.format("C%04d", max + 1);
}

    private void addContact() {

        if (!validateFields()) return;

      for (Contact c : contacts) {
            if (c.getPhone().equals(phone.getText().trim())) {
                JOptionPane.showMessageDialog(
                    this, "Duplicate phone number."
                );
                return;
            }
        }

        // Continue your addContact() code here
        Contact newContact = createContact(generateID(), false);
        newContact.addHistory("Contact created");

        contacts.add(newContact);

        saveContacts();
        refreshTable();
        clearFields();

        JOptionPane.showMessageDialog(
            this, "Contact added successfully."
        );
        // ---- END ----
    } // End addContact()


private Contact createContact(String id, boolean favorite) {
    return new Contact(
        id,
        name.getText().trim(),
        phone.getText().trim(),
        email.getText().trim(),
        address.getText().trim(),
        alternatePhone.getText().trim(),
        category.getText().trim(),
        notes.getText().trim(),
        dob.getText().trim(),
        anniversary.getText().trim(),
        "",
        favorite,
        relationship.getText().trim(),
        tags.getText().trim(),
        selectedPhotoPath
    );
}
    private void updateContact() {

        int row = table.getSelectedRow();

        if (row == -1 || !validateFields()) {
            if (row == -1) {
                JOptionPane.showMessageDialog(
                    this, "Select a contact first."
                );
            }
            return;
        }

        int index = table.convertRowIndexToModel(row);
        String id = model.getValueAt(index, 0).toString();

        for (Contact c : contacts) {
            if (c.getPhone().equals(phone.getText().trim())
                && !c.getId().equals(id)) {
                JOptionPane.showMessageDialog(
                    this, "Phone number already exists."
                );
                return;
            }
        }

        for (int i = 0; i < contacts.size(); i++) {
            Contact old = contacts.get(i);

            if (old.getId().equals(id)) {
                Contact updated = createContact(
                    old.getId(), old.isFavorite()
                );
                // Preserve existing contact details during update
updated.setCompany(old.getCompany());
updated.setJobTitle(old.getJobTitle());
updated.setNickname(old.getNickname());
updated.setWebsite(old.getWebsite());
updated.setAlternateAddress(old.getAlternateAddress());
updated.setDepartment(old.getDepartment());
updated.setDescription(old.getDescription());

updated.setBlocked(old.isBlocked());
updated.setEmergencyContact(old.isEmergencyContact());
updated.setPrivateContact(old.isPrivateContact());

updated.setAdditionalEmails(
    old.getAdditionalEmails() == null
        ? new ArrayList<>()
        : new ArrayList<>(old.getAdditionalEmails()));

updated.setSocialMediaProfiles(
    old.getSocialMediaProfiles() == null
        ? new ArrayList<>()
        : new ArrayList<>(old.getSocialMediaProfiles()));

                updated.setPhotoPath(old.getPhotoPath());
                updated.setAdditionalPhones(old.getAdditionalPhones());
                updated.setInteractionHistory(
                    old.getInteractionHistory()
                );
                updated.setCreatedDate(old.getCreatedDate());
                updated.addHistory("Contact updated");

                contacts.set(i, updated);
                break;
            }
        }

        saveContacts();
        refreshTable();
        clearFields();
    }

    private void deleteContact() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Select a contact."
            );
            return;
        }

        int index = table.convertRowIndexToModel(row);
        String id = model.getValueAt(index, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
            this, "Move contact to recycle bin?",
            "Delete", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        for (Contact c : contacts) {
            if (c.getId().equals(id)) {
                deletedContact = c;
                recycleBin.add(c);
                break;
            }
        }

        for (int i = 0; i < contacts.size(); i++) {
    if (contacts.get(i).getId().equals(id)) {
        contacts.remove(i);
        break;
    }
}

        saveContacts();
        refreshTable();
        clearFields();
    }

    private void undoDelete() {
        if (deletedContact == null) {
            JOptionPane.showMessageDialog(
                this, "Nothing to undo."
            );
            return;
        }

        contacts.add(deletedContact);
        recycleBin.remove(deletedContact);
        deletedContact = null;

        saveContacts();
        refreshTable();
    }

    private void showRecycleBin() {
        StringBuilder result = new StringBuilder();

        for (Contact c : recycleBin) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }

        JOptionPane.showMessageDialog(
            this,
            result.length() == 0 ? "Recycle bin is empty." : result
        );
    }

    private Contact selectedContact() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Select a contact first."
            );
            return null;
        }

        String id = model.getValueAt(
            table.convertRowIndexToModel(row), 0
        ).toString();

        for (Contact c : contacts) {
            if (c.getId().equals(id)) return c;
        }

        return null;
    }

    private void displaySelectedContact() {

        Contact c = selectedContactWithoutMessage();
        if (c == null) return;
        selectedPhotoPath = c.getPhotoPath();

        name.setText(c.getName());
        phone.setText(c.getPhone());
        email.setText(c.getEmail());
        address.setText(c.getAddress());
        alternatePhone.setText(c.getAlternatePhone());
        category.setText(c.getCategory());
        dob.setText(c.getDob());
        anniversary.setText(c.getAnniversary());
        relationship.setText(c.getRelationship());
        tags.setText(c.getTags());
        notes.setText(c.getNotes());

        c.markAsViewed();
        saveContacts();
    }

    private Contact selectedContactWithoutMessage() {
        int row = table.getSelectedRow();
        if (row == -1) return null;

        String id = model.getValueAt(
            table.convertRowIndexToModel(row), 0
        ).toString();

        for (Contact c : contacts) {
            if (c.getId().equals(id)) return c;
        }

        return null;
    }

    private void clearFields() {
        name.setText("");
        phone.setText("");
        email.setText("");
        address.setText("");
        alternatePhone.setText("");
        category.setText("General");
        dob.setText("");
        anniversary.setText("");
        relationship.setText("");
        tags.setText("");
        notes.setText("");
        table.clearSelection();
    }

    private void refreshTable() {

        model.setRowCount(0);

        for (Contact c : contacts) {
            model.addRow(new Object[] {
                c.getId(),
                c.getName(),
                c.getPhone(),
                c.getEmail(),
                c.getCategory(),
                c.isFavorite() ? "Yes" : "No"
            });
        }

        setTitle(
            "Contact Management System | Total Contacts: "
            + contacts.size()
        );
    }

    private void searchContacts() {
        String value = search.getText().trim();

        if (value.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                RowFilter.regexFilter(
                    "(?i)" +
                    java.util.regex.Pattern.quote(value)
                )
            );
        }
    }

    private void sortContacts() {
        Collections.sort(
            contacts,
            (a, b) -> a.getName().compareToIgnoreCase(b.getName())
        );

        refreshTable();
        saveContacts();
    }

    // ---------------- MULTIPLE PHONE NUMBERS ----------------

    private void addExtraPhone() {
        Contact c = selectedContact();
        if (c == null) return;

        String number = JOptionPane.showInputDialog(
            this, "Enter additional phone number:"
        );

        if (number != null && !number.trim().isEmpty()) {
            c.addPhoneNumber(number);
            c.addHistory("Additional phone number added");
            saveContacts();

            JOptionPane.showMessageDialog(
                this, "Additional number saved."
            );
        }
    }

    // ---------------- HISTORY ----------------

    private void showHistory() {
        Contact c = selectedContact();
        if (c == null) return;

       String history = String.join("\n", c.getInteractionHistory());
        JOptionPane.showMessageDialog(
            this,
            history.isEmpty() ? "No history available." : history,
            "Contact History",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ---------------- EMAIL AND PHONE ----------------

    private void openEmail() {
        Contact c = selectedContact();
        if (c == null) return;

        try {
            if (!Desktop.isDesktopSupported()) {
                throw new Exception("Desktop actions not supported.");
            }

            Desktop.getDesktop().mail(
                new URI("mailto:" + c.getEmail())
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Could not open email application: "
                + e.getMessage()
            );
        }
    }

    private void openPhone() {
        Contact c = selectedContact();
        if (c == null) return;

        JOptionPane.showMessageDialog(
            this,
            "Phone number: " + c.getPhone()
            + "\nUse a supported calling application to place a call."
        );
    }

    // ---------------- DASHBOARD ----------------

    private void dashboard() {
        int favorites = 0;

        for (Contact c : contacts) {
            if (c.isFavorite()) favorites++;
        }

        JOptionPane.showMessageDialog(
            this,
            "CONTACT DASHBOARD\n\n"
            + "Total Contacts: " + contacts.size()
            + "\nFavorite Contacts: " + favorites
            + "\nOther Contacts: " + (contacts.size() - favorites),
            "Dashboard",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ---------------- DUPLICATE CONTACTS ----------------

    private void findDuplicates() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < contacts.size(); i++) {
            for (int j = i + 1; j < contacts.size(); j++) {
                Contact a = contacts.get(i);
                Contact b = contacts.get(j);

                if (a.getPhone().equals(b.getPhone())
                    || (!a.getEmail().isEmpty()
                    && a.getEmail().equalsIgnoreCase(b.getEmail()))) {

                    result.append(a.getName())
                          .append(" and ")
                          .append(b.getName())
                          .append("\n");
                }
            }
        }

        JOptionPane.showMessageDialog(
            this,
            result.length() == 0
                ? "No duplicates found."
                : result.toString()
        );
    }

    private void mergeDuplicates() {

    int mergedCount = 0;

    for (int i = 0; i < contacts.size(); i++) {

        Contact a = contacts.get(i);

        for (int j = i + 1; j < contacts.size();) {

            Contact b = contacts.get(j);

            String phoneA = a.getPhone() == null ? "" : a.getPhone().trim();
            String phoneB = b.getPhone() == null ? "" : b.getPhone().trim();

            String emailA = a.getEmail() == null ? "" : a.getEmail().trim();
            String emailB = b.getEmail() == null ? "" : b.getEmail().trim();

            boolean samePhone = !phoneA.isEmpty()
                    && phoneA.equalsIgnoreCase(phoneB);

            boolean sameEmail = !emailA.isEmpty()
                    && emailA.equalsIgnoreCase(emailB);

            if (samePhone || sameEmail) {

                // Merge additional phones, emails, profiles and history
                a.mergeListsFrom(b);

                // Preserve the duplicate's primary phone
                if (!phoneB.isEmpty() && !samePhone) {
                    a.addPhoneNumber(phoneB);
                }

                // Preserve missing contact details
                if ((a.getCompany() == null || a.getCompany().isEmpty())
                        && b.getCompany() != null) {
                    a.setCompany(b.getCompany());
                }

                if ((a.getJobTitle() == null || a.getJobTitle().isEmpty())
                        && b.getJobTitle() != null) {
                    a.setJobTitle(b.getJobTitle());
                }

                if ((a.getNickname() == null || a.getNickname().isEmpty())
                        && b.getNickname() != null) {
                    a.setNickname(b.getNickname());
                }

                if ((a.getWebsite() == null || a.getWebsite().isEmpty())
                        && b.getWebsite() != null) {
                    a.setWebsite(b.getWebsite());
                }

                if ((a.getDepartment() == null || a.getDepartment().isEmpty())
                        && b.getDepartment() != null) {
                    a.setDepartment(b.getDepartment());
                }

                if ((a.getDescription() == null || a.getDescription().isEmpty())
                        && b.getDescription() != null) {
                    a.setDescription(b.getDescription());
                }

                // Preserve important status flags
                a.setBlocked(a.isBlocked() || b.isBlocked());
                a.setEmergencyContact(
                        a.isEmergencyContact() || b.isEmergencyContact());
                a.setPrivateContact(
                        a.isPrivateContact() || b.isPrivateContact());

                a.addHistory("Merged duplicate contact: " + b.getName());

                contacts.remove(j);
                mergedCount++;

            } else {
                j++;
            }
        }
    }

    saveContacts();

    JOptionPane.showMessageDialog(this,
            mergedCount + " duplicate contact(s) merged successfully.");
}

    // ---------------- CSV IMPORT AND EXPORT ----------------

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("contacts.csv"));

        if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter out = new PrintWriter(
            new FileWriter(chooser.getSelectedFile())
        )) {
            out.println("ID,Name,Phone,Email,Address,Category");

            for (Contact c : contacts) {
                out.println(
                    csv(c.getId()) + "," +
                    csv(c.getName()) + "," +
                    csv(c.getPhone()) + "," +
                    csv(c.getEmail()) + "," +
                    csv(c.getAddress()) + "," +
                    csv(c.getCategory())
                );
            }

            JOptionPane.showMessageDialog(
                this, "CSV exported successfully."
            );

        } catch (IOException e) {
            showError(e);
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void importCSV() {
    JFileChooser chooser = new JFileChooser();

    if (chooser.showOpenDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    int count = 0;
    int skipped = 0;

    try (BufferedReader reader = new BufferedReader(
            new FileReader(chooser.getSelectedFile()))) {

        String line;
        boolean first = true;

        while ((line = reader.readLine()) != null) {

            if (first) {
                first = false;
                continue;
            }
            String[] values = parseCSVLine(line);

            if (values.length < 4) {
                skipped++;
                continue;
            }

            String importedName = cleanCSV(values[1]);
            String importedPhone = cleanCSV(values[2]);
            String importedEmail = cleanCSV(values[3]);

            if (importedName.isEmpty() || importedPhone.isEmpty()) {
                skipped++;
                continue;
            }

            // Allow duplicate phone numbers during CSV import
            Contact c = new Contact(
                    generateID(),
                    importedName,
                    importedPhone,
                    importedEmail,
                    values.length > 4 ? cleanCSV(values[4]) : "",
                    "",
                    values.length > 5 ? cleanCSV(values[5]) : "General",
                    "", "", "", "", false
            );

            contacts.add(c);
            count++;
        }

        saveContacts();
        refreshTable();

        JOptionPane.showMessageDialog(
                this,
                count + " contacts imported.\n"
                + skipped + " invalid rows skipped.\n\n"
                + "Use Merge Duplicates to combine duplicates."
        );

    } catch (IOException e) {
        showError(e);
    }
}
private String[] parseCSVLine(String line) {
        ArrayList<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }
private String cleanCSV(String value) {
    if (value == null) {
        return "";
    }

    value = value.trim();

    if (value.startsWith("\"") && value.endsWith("\"")
            && value.length() >= 2) {

        value = value.substring(1, value.length() - 1)
                .replace("\"\"", "\"");
    }

    return value;
}
    // ---------------- PDF AND EXCEL EXPORT ----------------

    private void exportPDF() {
        JOptionPane.showMessageDialog(
            this,
            "PDF export requires a PDF library such as PDFBox or OpenPDF. "
            + "It is not included in this standard-library version."
        );
    }

    private void exportExcel() {
        JOptionPane.showMessageDialog(
            this,
            "Excel-compatible CSV export is available. "
            + "Use Export CSV and open the file in Microsoft Excel."
        );
    }

    // ---------------- PRINT ----------------

    private void printContacts() {
        try {
            table.print();
        } catch (PrinterException e) {
            showError(e);
        }
    }

    // ---------------- DARK MODE ----------------

    private void toggleTheme() {
        darkMode = !darkMode;

        Color background = darkMode
            ? new Color(45, 45, 45) : Color.WHITE;

        Color foreground = darkMode
            ? Color.WHITE : Color.BLACK;

        changeColors(getContentPane(), background, foreground);
        repaint();
    }

    private void changeColors(
        Component component, Color background, Color foreground
    ) {
        component.setBackground(background);
        component.setForeground(foreground);

        if (component instanceof Container) {
            for (Component child :
                ((Container) component).getComponents()) {
                changeColors(child, background, foreground);
            }
        }
    }

    // ---------------- BIRTHDAY REMINDERS ----------------

    private void showReminders() {
        StringBuilder reminders = new StringBuilder();

        LocalDate today = LocalDate.now();

        for (Contact c : contacts) {
            try {
               if (c.getDob() != null && !c.getDob().isEmpty()) {
    LocalDate birthday =
        LocalDate.parse(c.getDob());
                    if (birthday.getMonthValue() == today.getMonthValue()
                        && birthday.getDayOfMonth() == today.getDayOfMonth()) {
                        reminders.append("Birthday: ")
                            .append(c.getName()).append("\n");
                    }
                }
            } catch (Exception ignored) {}
        }

        if (reminders.length() > 0) {
            JOptionPane.showMessageDialog(
                this, reminders.toString(),
                "Birthday Reminder",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // ---------------- BACKUP AND RESTORE ----------------

    private void backup() {
        saveContacts();

        try {
            Files.copy(
                new File(DATA_FILE).toPath(),
                new File(BACKUP_FILE).toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );

            JOptionPane.showMessageDialog(
                this, "Backup created successfully."
            );

        } catch (IOException e) {
            showError(e);
        }
    }

    private void restoreBackup() {
        File backup = new File(BACKUP_FILE);

        if (!backup.exists()) {
            JOptionPane.showMessageDialog(
                this, "No backup found."
            );
            return;
        }

        try {
            Files.copy(
                backup.toPath(),
                new File(DATA_FILE).toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );

            loadContacts();
            refreshTable();

            JOptionPane.showMessageDialog(
                this, "Backup restored."
            );

        } catch (IOException e) {
            showError(e);
        }
    }

    // ---------------- SAVE AND LOAD ----------------

    private void saveContacts() {
        try (ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream(DATA_FILE)
        )) {
            out.writeObject(contacts);

        } catch (IOException e) {
            showError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadContacts() {
        File file = new File(DATA_FILE);

        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(
            new FileInputStream(file)
        )) {
            Object data = in.readObject();

            if (data instanceof ArrayList<?>) {
                contacts = (ArrayList<Contact>) data;
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(
            this, "Error: " + e.getMessage()
        );
    }
    private void choosePhoto() {

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose Contact Photo");

    chooser.setFileFilter(
        new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif"
        )
    );

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        selectedPhotoPath =
            chooser.getSelectedFile().getAbsolutePath();

        JOptionPane.showMessageDialog(
            this, "Contact photo selected successfully."
        );
    }
}

private void viewPhoto() {
    Contact c = selectedContact();

    if (c == null) return;

    String path = c.getPhotoPath();

    if (path == null || path.trim().isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "No photo available for this contact."
        );
        return;
    }

    File imageFile = new File(path);

    if (!imageFile.exists()) {
        JOptionPane.showMessageDialog(
            this, "Photo file not found."
        );
        return;
    }

    ImageIcon icon = new ImageIcon(path);

    Image image = icon.getImage().getScaledInstance(
        250, 250, Image.SCALE_SMOOTH
    );

    JLabel imageLabel = new JLabel(
        new ImageIcon(image)
    );

    JOptionPane.showMessageDialog(
        this,
        imageLabel,
        "Contact Photo",
        JOptionPane.PLAIN_MESSAGE
    );
}
private void assignGroup() {
    Contact c = selectedContact();
    if (c == null) return;

    String group = JOptionPane.showInputDialog(
        this, "Enter group name (Family, Friends, College, Work):",
        c.getCategory()
    );

    if (group == null || group.trim().isEmpty()) return;

    c.setCategory(group.trim());
    c.addHistory("Group changed to " + group.trim());

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this, "Contact assigned to " + group.trim()
    );
}

private void viewGroups() {
    java.util.Map<String, Integer> groups =
        new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Contact c : contacts) {
        String group = c.getCategory();

        if (group == null || group.trim().isEmpty()) {
            group = "Uncategorized";
        }

        groups.put(group, groups.getOrDefault(group, 0) + 1);
    }

    StringBuilder result = new StringBuilder("CONTACT GROUPS\n\n");

    for (String group : groups.keySet()) {
        result.append(group)
              .append(" : ")
              .append(groups.get(group))
              .append(" contacts\n");
    }

    JOptionPane.showMessageDialog(
        this,
        groups.isEmpty() ? "No groups available." : result.toString()
    );
}

private void filterGroup() {
    java.util.Set<String> groups = new java.util.TreeSet<>(
        String.CASE_INSENSITIVE_ORDER
    );

    for (Contact c : contacts) {
        if (c.getCategory() != null &&
            !c.getCategory().trim().isEmpty()) {
            groups.add(c.getCategory().trim());
        }
    }

    if (groups.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No groups available.");
        return;
    }

    String group = (String) JOptionPane.showInputDialog(
        this,
        "Choose a group:",
        "Filter Contacts",
        JOptionPane.QUESTION_MESSAGE,
        null,
        groups.toArray(),
        groups.iterator().next()
    );

    if (group == null) return;

    sorter.setRowFilter(
        RowFilter.regexFilter(
            "(?i)^" + java.util.regex.Pattern.quote(group) + "$",
            4
        )
    );
}

private void showAllContacts() {
    sorter.setRowFilter(null);
    search.setText("");
    refreshTable();

    JOptionPane.showMessageDialog(
        this, "All contacts displayed."
    );
}
private void advancedSearch() {

    JTextField searchName = new JTextField(15);
    JTextField searchPhone = new JTextField(15);
    JTextField searchEmail = new JTextField(15);
    JTextField searchCategory = new JTextField(15);

    JCheckBox favoritesOnly = new JCheckBox("Favorites only");

    JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

    panel.add(new JLabel("Name:"));
    panel.add(searchName);

    panel.add(new JLabel("Phone:"));
    panel.add(searchPhone);

    panel.add(new JLabel("Email:"));
    panel.add(searchEmail);

    panel.add(new JLabel("Category:"));
    panel.add(searchCategory);

    panel.add(favoritesOnly);

    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        "Advanced Contact Search",
        JOptionPane.OK_CANCEL_OPTION
    );

    if (result != JOptionPane.OK_OPTION) return;

    java.util.List<RowFilter<DefaultTableModel, Integer>> filters =
        new ArrayList<>();

    if (!searchName.getText().trim().isEmpty()) {
        filters.add(RowFilter.regexFilter(
            "(?i)" + java.util.regex.Pattern.quote(
                searchName.getText().trim()
            ), 1
        ));
    }

    if (!searchPhone.getText().trim().isEmpty()) {
        filters.add(RowFilter.regexFilter(
            "(?i)" + java.util.regex.Pattern.quote(
                searchPhone.getText().trim()
            ), 2
        ));
    }

    if (!searchEmail.getText().trim().isEmpty()) {
        filters.add(RowFilter.regexFilter(
            "(?i)" + java.util.regex.Pattern.quote(
                searchEmail.getText().trim()
            ), 3
        ));
    }

    if (!searchCategory.getText().trim().isEmpty()) {
        filters.add(RowFilter.regexFilter(
            "(?i)" + java.util.regex.Pattern.quote(
                searchCategory.getText().trim()
            ), 4
        ));
    }

    if (favoritesOnly.isSelected()) {
        filters.add(RowFilter.regexFilter("^Yes$", 5));
    }

    if (filters.isEmpty()) {
        sorter.setRowFilter(null);
    } else {
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    JOptionPane.showMessageDialog(
        this,
        table.getRowCount() + " matching contacts found."
    );
}
private void exportContactsToText() {
    if (contacts.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "No contacts available to export."
        );
        return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save Contacts as Text File");
    chooser.setSelectedFile(new File("MyContacts.txt"));

    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = chooser.getSelectedFile();

    if (!file.getName().toLowerCase().endsWith(".txt")) {
        file = new File(file.getAbsolutePath() + ".txt");
    }

    try (java.io.PrintWriter writer =
             new java.io.PrintWriter(file)) {

        writer.println("CONTACT MANAGEMENT SYSTEM");
        writer.println("=========================");
        writer.println();

        for (Contact c : contacts) {
            writer.println("ID: " + c.getId());
            writer.println("Name: " + c.getName());
            writer.println("Phone: " + c.getPhone());
            writer.println("Email: " + c.getEmail());
            writer.println("Address: " + c.getAddress());
            writer.println("Category: " + c.getCategory());
            writer.println("Notes: " + c.getNotes());
            writer.println("-------------------------");
        }

        JOptionPane.showMessageDialog(
            this, "Contacts exported successfully!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Export failed: " + ex.getMessage()
        );
    }
}
private void importContactsFromText() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Import Contacts from Text File");

    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = chooser.getSelectedFile();
    int imported = 0;
    int skipped = 0;

    try (java.io.BufferedReader reader =
             new java.io.BufferedReader(new java.io.FileReader(file))) {

        String line;
        String name = "";
        String phone = "";
        String email = "";
        String address = "";
        String category = "";
        String notes = "";

        while ((line = reader.readLine()) != null) {

            if (line.startsWith("ID: ")) {
            } else if (line.startsWith("Name: ")) {
                name = line.substring(6).trim();
            } else if (line.startsWith("Phone: ")) {
                phone = line.substring(7).trim();
            } else if (line.startsWith("Email: ")) {
                email = line.substring(7).trim();
            } else if (line.startsWith("Address: ")) {
                address = line.substring(9).trim();
            } else if (line.startsWith("Category: ")) {
                category = line.substring(10).trim();
            } else if (line.startsWith("Notes: ")) {
                notes = line.substring(7).trim();
            } else if (line.startsWith("-------------------------")) {

                if (!name.isEmpty() && !phone.isEmpty()) {
                    boolean duplicate = false;

                    for (Contact c : contacts) {
                        if (phone.equalsIgnoreCase(c.getPhone())) {
                            duplicate = true;
                            break;
                        }
                    }

                    if (!duplicate) {
                        Contact c = new Contact(
    java.util.UUID.randomUUID().toString(),
    name,
    phone,
    email,
    address,
    "",
    category,
    notes,
    "",
    "",
    "",
    false,
    "",
    "",
    ""
);

                        contacts.add(c);
                        imported++;
                    } else {
                        skipped++;
                    }
                } else {
                    skipped++;
                }

                name = "";
                phone = "";
                email = "";
                address = "";
                category = "";
                notes = "";
            }
        }

        saveContacts();
        refreshTable();

        JOptionPane.showMessageDialog(
            this,
            "Import completed!\nImported: " + imported +
            "\nSkipped: " + skipped
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Import failed: " + ex.getMessage()
        );
    }
}
private void exportContactsToJSON() {
    if (contacts.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "No contacts available to export."
        );
        return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Export Contacts to JSON");
    chooser.setSelectedFile(new File("MyContacts.json"));

    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = chooser.getSelectedFile();

    if (!file.getName().toLowerCase().endsWith(".json")) {
        file = new File(file.getAbsolutePath() + ".json");
    }

    try (java.io.BufferedWriter writer =
             new java.io.BufferedWriter(new java.io.FileWriter(file))) {

        writer.write("[\n");

        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);

            writer.write("  {\n");
            writer.write("    \"id\": " + jsonValue(c.getId()) + ",\n");
            writer.write("    \"name\": " + jsonValue(c.getName()) + ",\n");
            writer.write("    \"phone\": " + jsonValue(c.getPhone()) + ",\n");
            writer.write("    \"email\": " + jsonValue(c.getEmail()) + ",\n");
            writer.write("    \"address\": " + jsonValue(c.getAddress()) + ",\n");
            writer.write("    \"alternatePhone\": " + jsonValue(c.getAlternatePhone()) + ",\n");
            writer.write("    \"category\": " + jsonValue(c.getCategory()) + ",\n");
            writer.write("    \"notes\": " + jsonValue(c.getNotes()) + ",\n");
            writer.write("    \"dob\": " + jsonValue(c.getDob()) + ",\n");
            writer.write("    \"anniversary\": " + jsonValue(c.getAnniversary()) + ",\n");
            writer.write("    \"company\": " + jsonValue(c.getCompany()) + ",\n");
            writer.write("    \"favorite\": " + c.isFavorite() + ",\n");
            writer.write("    \"relationship\": " + jsonValue(c.getRelationship()) + ",\n");
            writer.write("    \"tags\": " + jsonValue(c.getTags()) + ",\n");
            writer.write("    \"photoPath\": " + jsonValue(c.getPhotoPath()) + "\n");
            writer.write("  }");

            if (i < contacts.size() - 1) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("]\n");

        JOptionPane.showMessageDialog(
            this, "JSON export completed successfully!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "JSON export failed: " + ex.getMessage()
        );
    }
}

private String jsonValue(String value) {
    if (value == null) {
        return "\"\"";
    }

    StringBuilder result = new StringBuilder("\"");

    for (char ch : value.toCharArray()) {
        switch (ch) {
            case '"':
                result.append("\\\"");
                break;
            case '\\':
                result.append("\\\\");
                break;
            case '\n':
                result.append("\\n");
                break;
            case '\r':
                result.append("\\r");
                break;
            case '\t':
                result.append("\\t");
                break;
            default:
                if (ch < 32) {
                    result.append(String.format("\\u%04x", (int) ch));
                } else {
                    result.append(ch);
                }
        }
    }

    result.append("\"");
    return result.toString();
}
private void exportContactVCard() {
    Contact c = selectedContact();
    if (c == null) return;

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Export Contact as vCard");
    chooser.setSelectedFile(
        new File(c.getName().replaceAll("[\\\\/:*?\"<>|]", "_") + ".vcf")
    );

    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = chooser.getSelectedFile();

    if (!file.getName().toLowerCase().endsWith(".vcf")) {
        file = new File(file.getAbsolutePath() + ".vcf");
    }

    try (java.io.BufferedWriter writer =
             new java.io.BufferedWriter(
                 new java.io.OutputStreamWriter(
                     new java.io.FileOutputStream(file),
                     java.nio.charset.StandardCharsets.UTF_8
                 ))) {

        writer.write("BEGIN:VCARD\r\n");
        writer.write("VERSION:3.0\r\n");
        writer.write("FN:" + vCardEscape(c.getName()) + "\r\n");

        if (c.getPhone() != null && !c.getPhone().isEmpty()) {
            writer.write("TEL;TYPE=CELL:" +
                vCardEscape(c.getPhone()) + "\r\n");
        }

        if (c.getEmail() != null && !c.getEmail().isEmpty()) {
            writer.write("EMAIL:" +
                vCardEscape(c.getEmail()) + "\r\n");
        }

        if (c.getAddress() != null && !c.getAddress().isEmpty()) {
            writer.write("ADR;TYPE=HOME:;;" +
                vCardEscape(c.getAddress()) + ";;;;\r\n");
        }

        if (c.getCompany() != null && !c.getCompany().isEmpty()) {
            writer.write("ORG:" +
                vCardEscape(c.getCompany()) + "\r\n");
        }

        if (c.getNotes() != null && !c.getNotes().isEmpty()) {
            writer.write("NOTE:" +
                vCardEscape(c.getNotes()) + "\r\n");
        }

        writer.write("END:VCARD\r\n");

        JOptionPane.showMessageDialog(
            this, "Contact exported successfully as vCard!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Export failed: " + ex.getMessage()
        );
    }
}

private String vCardEscape(String value) {
    if (value == null) return "";

    return value.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace(",", "\\,")
                .replace(";", "\\;");
}
private void copyContactDetails() {
    Contact c = selectedContact();
    if (c == null) return;

    StringBuilder details = new StringBuilder();

    details.append("CONTACT DETAILS\n");
    details.append("-------------------------\n");
    details.append("Name: ").append(c.getName()).append("\n");
    details.append("Phone: ").append(c.getPhone()).append("\n");
    details.append("Email: ").append(c.getEmail()).append("\n");
    details.append("Address: ").append(c.getAddress()).append("\n");
    details.append("Alternate Phone: ")
           .append(c.getAlternatePhone()).append("\n");
    details.append("Category: ").append(c.getCategory()).append("\n");
    details.append("Company: ").append(c.getCompany()).append("\n");
    details.append("Notes: ").append(c.getNotes()).append("\n");

    try {
        java.awt.datatransfer.StringSelection selection =
            new java.awt.datatransfer.StringSelection(
                details.toString()
            );

        java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(selection, null);

        JOptionPane.showMessageDialog(
            this,
            "Contact details copied successfully!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            "Could not copy contact details: " + ex.getMessage()
        );
    }
}
private void printIndividualContact() {
    Contact c = selectedContact();
    if (c == null) return;

    StringBuilder details = new StringBuilder();

    details.append("CONTACT DETAILS\n");
    details.append("========================\n\n");
    details.append("ID: ").append(c.getId()).append("\n");
    details.append("Name: ").append(c.getName()).append("\n");
    details.append("Phone: ").append(c.getPhone()).append("\n");
    details.append("Email: ").append(c.getEmail()).append("\n");
    details.append("Address: ").append(c.getAddress()).append("\n");
    details.append("Alternate Phone: ")
           .append(c.getAlternatePhone()).append("\n");
    details.append("Category: ").append(c.getCategory()).append("\n");
    details.append("Company: ").append(c.getCompany()).append("\n");
    details.append("Relationship: ")
           .append(c.getRelationship()).append("\n");
    details.append("Date of Birth: ").append(c.getDob()).append("\n");
    details.append("Anniversary: ")
           .append(c.getAnniversary()).append("\n");
    details.append("Notes: ").append(c.getNotes()).append("\n");

    javax.swing.JTextArea printArea =
        new javax.swing.JTextArea(details.toString());

    printArea.setFont(new java.awt.Font(
        "Monospaced", java.awt.Font.PLAIN, 12
    ));

    try {
        boolean completed = printArea.print();

        if (completed) {
            JOptionPane.showMessageDialog(
                this, "Contact sent to printer successfully."
            );
        } else {
            JOptionPane.showMessageDialog(
                this, "Printing was cancelled."
            );
        }

    } catch (java.awt.print.PrinterException ex) {
        JOptionPane.showMessageDialog(
            this, "Printing failed: " + ex.getMessage()
        );
    }
}
private void toggleContactBlock() {
    Contact c = selectedContact();
    if (c == null) return;

    if (c.isBlocked()) {
        c.setBlocked(false);

        JOptionPane.showMessageDialog(
            this, "Contact unblocked successfully!"
        );
    } else {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Do you want to block " + c.getName() + "?",
            "Block Contact",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        c.setBlocked(true);

        JOptionPane.showMessageDialog(
            this, "Contact blocked successfully!"
        );
    }

    saveContacts();
    refreshTable();
}
private void showCategoryStatistics() {
    if (contacts.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "No contacts available."
        );
        return;
    }

    java.util.Map<String, Integer> statistics =
        new java.util.TreeMap<>(
            String.CASE_INSENSITIVE_ORDER
        );

    for (Contact c : contacts) {
        String category = c.getCategory();

        if (category == null || category.trim().isEmpty()) {
            category = "Uncategorized";
        } else {
            category = category.trim();
        }

        statistics.put(
            category,
            statistics.getOrDefault(category, 0) + 1
        );
    }

    StringBuilder report = new StringBuilder();

    report.append("CONTACT CATEGORY STATISTICS\n");
    report.append("============================\n\n");
    report.append("Total Contacts: ")
          .append(contacts.size()).append("\n\n");

    for (java.util.Map.Entry<String, Integer> entry
            : statistics.entrySet()) {

        report.append(entry.getKey())
              .append(": ")
              .append(entry.getValue())
              .append(" contacts\n");
    }

    JOptionPane.showMessageDialog(
        this,
        report.toString(),
        "Category Statistics",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void viewBlockedContacts() {
    StringBuilder report = new StringBuilder();

    report.append("BLOCKED CONTACTS\n");
    report.append("========================\n\n");

    int count = 0;

    for (Contact c : contacts) {
        if (c.isBlocked()) {
            count++;

            report.append("Name: ")
                  .append(c.getName()).append("\n");

            report.append("Phone: ")
                  .append(c.getPhone()).append("\n");

            report.append("Email: ")
                  .append(c.getEmail()).append("\n");

            report.append("------------------------\n");
        }
    }

    if (count == 0) {
        JOptionPane.showMessageDialog(
            this, "No blocked contacts found."
        );
        return;
    }

    report.append("\nTotal Blocked Contacts: ")
          .append(count);

    JTextArea area = new JTextArea(report.toString());
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(400, 300));

    JOptionPane.showMessageDialog(
        this,
        scrollPane,
        "Blocked Contacts",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void selectAllContacts() {
    int rowCount = table.getRowCount();

    if (rowCount == 0) {
        JOptionPane.showMessageDialog(
            this, "No contacts available to select."
        );
        return;
    }

    table.setRowSelectionInterval(0, rowCount - 1);

    JOptionPane.showMessageDialog(
        this,
        rowCount + " contacts selected successfully!"
    );
}
private void deselectAllContacts() {
    if (table.getSelectedRowCount() == 0) {
        JOptionPane.showMessageDialog(
            this, "No contacts are currently selected."
        );
        return;
    }

    table.clearSelection();

    JOptionPane.showMessageDialog(
        this, "All contact selections cleared."
    );
}
private void exportSelectedContacts() {
    int[] selectedRows = table.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please select at least one contact."
        );
        return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Export Selected Contacts");
    chooser.setSelectedFile(new File("SelectedContacts.txt"));

    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = chooser.getSelectedFile();

    if (!file.getName().toLowerCase().endsWith(".txt")) {
        file = new File(file.getAbsolutePath() + ".txt");
    }

    try (java.io.PrintWriter writer =
             new java.io.PrintWriter(file)) {

        writer.println("SELECTED CONTACTS");
        writer.println("========================");

        for (int row : selectedRows) {
            int modelRow = table.convertRowIndexToModel(row);
            Contact c = contacts.get(modelRow);

            writer.println();
            writer.println("Name: " + c.getName());
            writer.println("Phone: " + c.getPhone());
            writer.println("Email: " + c.getEmail());
            writer.println("Address: " + c.getAddress());
            writer.println("Category: " + c.getCategory());
            writer.println("------------------------");
        }

        JOptionPane.showMessageDialog(
            this,
            selectedRows.length +
            " contacts exported successfully!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Export failed: " + ex.getMessage()
        );
    }
}
private void sortContactsByPhone() {
    if (table.getRowCount() == 0) {
        JOptionPane.showMessageDialog(
            this, "No contacts available to sort."
        );
        return;
    }

    sorter.setSortKeys(
        java.util.Collections.singletonList(
            new javax.swing.RowSorter.SortKey(
                2,
                javax.swing.SortOrder.ASCENDING
            )
        )
    );

    sorter.sort();

    JOptionPane.showMessageDialog(
        this, "Contacts sorted by phone number!"
    );
}
private void sortContactsByEmail() {
    if (table.getRowCount() == 0) {
        JOptionPane.showMessageDialog(
            this, "No contacts available to sort."
        );
        return;
    }

    sorter.setSortKeys(
        java.util.Collections.singletonList(
            new javax.swing.RowSorter.SortKey(
                3,
                javax.swing.SortOrder.ASCENDING
            )
        )
    );

    sorter.sort();

    JOptionPane.showMessageDialog(
        this, "Contacts sorted by email successfully!"
    );
}
private void viewFavoriteContacts() {
    sorter.setRowFilter(
        RowFilter.regexFilter("^Yes$", 5)
    );

    int count = table.getRowCount();

    if (count == 0) {
        JOptionPane.showMessageDialog(
            this, "No favorite contacts found."
        );
        return;
    }

    JOptionPane.showMessageDialog(
        this,
        count + " favorite contacts found."
    );
}
private void copyPhoneNumber() {
    Contact c = selectedContact();
    if (c == null) return;

    String phone = c.getPhone();

    if (phone == null || phone.trim().isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "This contact has no phone number."
        );
        return;
    }

    try {
        java.awt.datatransfer.StringSelection selection =
            new java.awt.datatransfer.StringSelection(phone);

        java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(selection, null);

        JOptionPane.showMessageDialog(
            this, "Phone number copied: " + phone
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Copy failed: " + ex.getMessage()
        );
    }
}
private void copyEmailAddress() {
    Contact c = selectedContact();
    if (c == null) return;

    String email = c.getEmail();

    if (email == null || email.trim().isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "This contact has no email address."
        );
        return;
    }

    try {
        java.awt.datatransfer.StringSelection selection =
            new java.awt.datatransfer.StringSelection(email);

        java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(selection, null);

        JOptionPane.showMessageDialog(
            this, "Email copied successfully: " + email
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Copy failed: " + ex.getMessage()
        );
    }
}
private void showRecentContacts() {
    java.util.Calendar calendar =
        java.util.Calendar.getInstance();

    calendar.add(java.util.Calendar.DAY_OF_YEAR, -7);

    java.util.Date sevenDaysAgo = calendar.getTime();

    StringBuilder report = new StringBuilder();

    report.append("RECENTLY ADDED CONTACTS\n");
    report.append("========================\n\n");

    int count = 0;

    for (Contact c : contacts) {
        java.util.Date created = c.getCreatedDate();

        if (created != null && !created.before(sevenDaysAgo)) {
            count++;

            report.append("Name: ")
                  .append(c.getName()).append("\n");

            report.append("Phone: ")
                  .append(c.getPhone()).append("\n");

            report.append("Added: ")
                  .append(created).append("\n");

            report.append("------------------------\n");
        }
    }

    if (count == 0) {
        JOptionPane.showMessageDialog(
            this, "No contacts added in the last 7 days."
        );
        return;
    }

    report.append("\nTotal Recent Contacts: ").append(count);

    JTextArea area = new JTextArea(report.toString());
    area.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(450, 300));

    JOptionPane.showMessageDialog(
        this,
        scrollPane,
        "Recent Contacts",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void showRecentlyUpdatedContacts() {
    java.util.Calendar calendar =
        java.util.Calendar.getInstance();

    calendar.add(java.util.Calendar.DAY_OF_YEAR, -7);

    java.util.Date sevenDaysAgo = calendar.getTime();

    StringBuilder report = new StringBuilder();

    report.append("RECENTLY UPDATED CONTACTS\n");
    report.append("========================\n\n");

    int count = 0;

    for (Contact c : contacts) {
        java.util.Date updated = c.getUpdatedDate();

        if (updated != null && !updated.before(sevenDaysAgo)) {
            count++;

            report.append("Name: ")
                  .append(c.getName()).append("\n");

            report.append("Phone: ")
                  .append(c.getPhone()).append("\n");

            report.append("Last Updated: ")
                  .append(updated).append("\n");

            report.append("------------------------\n");
        }
    }

    if (count == 0) {
        JOptionPane.showMessageDialog(
            this, "No recently updated contacts found."
        );
        return;
    }

    report.append("\nTotal Updated Contacts: ").append(count);

    JTextArea area = new JTextArea(report.toString());
    area.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(450, 300));

    JOptionPane.showMessageDialog(
        this,
        scrollPane,
        "Recently Updated Contacts",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void showFavoriteCount() {
    int favoriteCount = 0;

    for (Contact c : contacts) {
        if (c.isFavorite()) {
            favoriteCount++;
        }
    }

    int totalContacts = contacts.size();
    int normalContacts = totalContacts - favoriteCount;

    String report =
        "CONTACT FAVORITE STATISTICS\n\n" +
        "Total Contacts: " + totalContacts + "\n" +
        "Favorite Contacts: " + favoriteCount + "\n" +
        "Normal Contacts: " + normalContacts;

    JOptionPane.showMessageDialog(
        this,
        report,
        "Favorite Statistics",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void findContactsWithoutEmail() {
    StringBuilder report = new StringBuilder();

    report.append("CONTACTS WITHOUT EMAIL\n");
    report.append("========================\n\n");

    int count = 0;

    for (Contact c : contacts) {
        String email = c.getEmail();

        if (email == null || email.trim().isEmpty()) {
            count++;

            report.append("Name: ")
                  .append(c.getName()).append("\n");

            report.append("Phone: ")
                  .append(c.getPhone()).append("\n");

            report.append("------------------------\n");
        }
    }

    if (count == 0) {
        JOptionPane.showMessageDialog(
            this, "All contacts have email addresses!"
        );
        return;
    }

    report.append("\nTotal Contacts Without Email: ")
          .append(count);

    JTextArea area = new JTextArea(report.toString());
    area.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(400, 300));

    JOptionPane.showMessageDialog(
        this,
        scrollPane,
        "Missing Email Contacts",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void findContactsWithoutAddress() {
    StringBuilder report = new StringBuilder();

    report.append("CONTACTS WITHOUT ADDRESS\n");
    report.append("========================\n\n");

    int count = 0;

    for (Contact c : contacts) {
        String address = c.getAddress();

        if (address == null || address.trim().isEmpty()) {
            count++;

            report.append("Name: ")
                  .append(c.getName()).append("\n");

            report.append("Phone: ")
                  .append(c.getPhone()).append("\n");

            report.append("------------------------\n");
        }
    }

    if (count == 0) {
        JOptionPane.showMessageDialog(
            this, "All contacts have addresses!"
        );
        return;
    }

    report.append("\nTotal Contacts Without Address: ")
          .append(count);

    JTextArea area = new JTextArea(report.toString());
    area.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(400, 300));

    JOptionPane.showMessageDialog(
        this,
        scrollPane,
        "Missing Address Contacts",
        JOptionPane.INFORMATION_MESSAGE
    );
}
private void importContactsFromJSON() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select JSON Contact File");

    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    try {
        String json = java.nio.file.Files.readString(
            chooser.getSelectedFile().toPath()
        );

        java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile(
                "\\{([^{}]*)\\}"
            );

        java.util.regex.Matcher matcher =
            pattern.matcher(json);

        int imported = 0;
        int skipped = 0;

        while (matcher.find()) {
            String object = matcher.group(1);

            String id = getJsonValue(object, "id");
            String name = getJsonValue(object, "name");
            String phone = getJsonValue(object, "phone");
            String email = getJsonValue(object, "email");
            String address = getJsonValue(object, "address");
            String category = getJsonValue(object, "category");
            String notes = getJsonValue(object, "notes");
            String dob = getJsonValue(object, "dob");
            String anniversary = getJsonValue(object, "anniversary");
            String company = getJsonValue(object, "company");
            String relationship = getJsonValue(object, "relationship");
            String tags = getJsonValue(object, "tags");
            String photoPath = getJsonValue(object, "photoPath");
            String favoriteValue = getJsonValue(object, "favorite");

            if (name.isBlank() || phone.isBlank()) {
                skipped++;
                continue;
            }

            boolean duplicate = contacts.stream()
                .anyMatch(c -> c.getPhone().equals(phone));

            if (duplicate) {
                skipped++;
                continue;
            }

            boolean favorite =
                "true".equalsIgnoreCase(favoriteValue);

            Contact contact = new Contact(
                (id.isBlank()
                    ? java.util.UUID.randomUUID().toString()
                    : id),
                name, phone, email, address, "",
                category, notes, dob, anniversary,
                company, favorite, relationship, tags,
                photoPath
            );

            contacts.add(contact);
            imported++;
        }

        saveContacts();
        refreshTable();

        JOptionPane.showMessageDialog(
            this,
            "Imported: " + imported +
            "\nSkipped: " + skipped
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            "JSON import failed: " + ex.getMessage()
        );
    }
}

private String getJsonValue(String object, String key) {
    java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile(
            "\"" + java.util.regex.Pattern.quote(key) +
            "\"\\s*:\\s*(?:\"((?:\\\\.|[^\"])*)\"|(true|false|null))"
        );

    java.util.regex.Matcher matcher = pattern.matcher(object);

    if (matcher.find()) {
        String value = matcher.group(1);

        if (value == null) {
            value = matcher.group(2);
        }

        if (value == null || "null".equals(value)) {
            return "";
        }

        return value
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n");
    }

    return "";
}
private void bulkDeleteContacts() {
    int[] selectedRows = table.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please select contacts to delete."
        );
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Delete " + selectedRows.length + " selected contacts?",
        "Confirm Bulk Delete",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    java.util.List<Contact> deletedContacts =
        new java.util.ArrayList<>();

    for (int row : selectedRows) {
        int modelRow = table.convertRowIndexToModel(row);
        deletedContacts.add(contacts.get(modelRow));
    }

    for (Contact contact : deletedContacts) {
        contacts.remove(contact);
        recycleBin.add(contact);
    }

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this,
        deletedContacts.size() + " contacts deleted."
    );
}
private void exportSelectedContactsToCSV() {
    int[] selectedRows = table.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please select contacts first."
        );
        return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save Selected Contacts CSV");
    chooser.setSelectedFile(
        new java.io.File("selected_contacts.csv")
    );

    if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    try (java.io.PrintWriter writer =
             new java.io.PrintWriter(
                 new java.io.FileWriter(
                     chooser.getSelectedFile()))) {

        writer.println(
            "ID,Name,Phone,Email,Address,Category"
        );

        for (int row : selectedRows) {
            int modelRow = table.convertRowIndexToModel(row);
            Contact c = contacts.get(modelRow);

            writer.println(
                csvCell(c.getId()) + "," +
                csvCell(c.getName()) + "," +
                csvCell(c.getPhone()) + "," +
                csvCell(c.getEmail()) + "," +
                csvCell(c.getAddress()) + "," +
                csvCell(c.getCategory())
            );
        }

        JOptionPane.showMessageDialog(
            this,
            selectedRows.length +
            " contacts exported successfully."
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Export failed: " + ex.getMessage()
        );
    }
}

private String csvCell(String value) {
    if (value == null) {
        value = "";
    }

    return "\"" + value.replace("\"", "\"\"") + "\"";
}
private void bulkUpdateCategory() {
    int[] selectedRows = table.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please select contacts first."
        );
        return;
    }

    String[] categories = {
        "Family", "Friends", "Work",
        "College", "Business", "Other"
    };

    String category = (String) JOptionPane.showInputDialog(
        this,
        "Select the new category:",
        "Bulk Update Category",
        JOptionPane.QUESTION_MESSAGE,
        null,
        categories,
        categories[0]
    );

    if (category == null) {
        return;
    }

    for (int row : selectedRows) {
        int modelRow = table.convertRowIndexToModel(row);
        Contact c = contacts.get(modelRow);
        c.setCategory(category);
    }

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this,
        selectedRows.length +
        " contacts updated to " + category + "."
    );
}
private void bulkAddTags() {
    int[] selectedRows = table.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please select contacts first."
        );
        return;
    }

    String enteredTag = JOptionPane.showInputDialog(
        this, "Enter a tag to add:"
    );

    if (enteredTag == null || enteredTag.trim().isEmpty()) {
        return;
    }

    final String newTag = enteredTag.trim();
    int updated = 0;

    for (int row : selectedRows) {
        int modelRow = table.convertRowIndexToModel(row);
        Contact c = contacts.get(modelRow);

        String oldTags = c.getTags();

        if (oldTags == null || oldTags.trim().isEmpty()) {
            c.setTags(newTag);
        } else {
            boolean alreadyExists =
                java.util.Arrays.stream(oldTags.split(","))
                .anyMatch(t ->
                    t.trim().equalsIgnoreCase(newTag)
                );

            if (!alreadyExists) {
                c.setTags(oldTags + ", " + newTag);
            }
        }

        updated++;
    }

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this, "Tags processed for " + updated + " contacts."
    );
}
private void bulkRemoveTags() {
    int[] selectedRows = table.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please select contacts first."
        );
        return;
    }

    String enteredTag = JOptionPane.showInputDialog(
        this, "Enter the tag to remove:"
    );

    if (enteredTag == null || enteredTag.trim().isEmpty()) {
        return;
    }

    String tagToRemove = enteredTag.trim();
    int updated = 0;

    for (int row : selectedRows) {
        int modelRow = table.convertRowIndexToModel(row);
        Contact c = contacts.get(modelRow);

        String oldTags = c.getTags();

        if (oldTags == null || oldTags.trim().isEmpty()) {
            continue;
        }

        StringBuilder remainingTags = new StringBuilder();

        for (String existingTag : oldTags.split(",")) {
            String currentTag = existingTag.trim();

            if (!currentTag.equalsIgnoreCase(tagToRemove)
                    && !currentTag.isEmpty()) {

                if (remainingTags.length() > 0) {
                    remainingTags.append(", ");
                }

                remainingTags.append(currentTag);
            }
        }

        String newTags = remainingTags.toString();

        if (!newTags.equals(oldTags)) {
            c.setTags(newTags);
            updated++;
        }
    }

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this,
        "Tag removed from " + updated + " contacts."
    );
}
private void clearAllFilters() {
    sorter.setRowFilter(null);
    refreshTable();

    JOptionPane.showMessageDialog(
        this, "All filters cleared!"
    );
}

private void showTotalContacts() {
    JOptionPane.showMessageDialog(
        this, "Total Contacts: " + contacts.size()
    );
}

private void showCategoryCount() {
    java.util.Map<String, Integer> counts =
        new java.util.TreeMap<>(
            String.CASE_INSENSITIVE_ORDER
        );

    for (Contact c : contacts) {
        String category = c.getCategory();

        if (category == null || category.isBlank()) {
            category = "Uncategorized";
        }

        counts.put(category, counts.getOrDefault(category, 0) + 1);
    }

    StringBuilder result =
        new StringBuilder("CONTACTS BY CATEGORY\n\n");

    for (String category : counts.keySet()) {
        result.append(category)
              .append(": ")
              .append(counts.get(category))
              .append("\n");
    }

    JOptionPane.showMessageDialog(this, result.toString());
}

private void searchByCompany() {
    String company = JOptionPane.showInputDialog(
        this, "Enter company name:"
    );

    if (company == null || company.isBlank()) return;

    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getCompany() != null &&
            c.getCompany().toLowerCase().contains(
                company.trim().toLowerCase()
            )) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void searchByInitial() {
    String initial = JOptionPane.showInputDialog(
        this, "Enter first letter of name:"
    );

    if (initial == null || initial.trim().isEmpty()) return;

    String letter = initial.trim().substring(0, 1);
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getName() != null &&
            c.getName().toLowerCase().startsWith(
                letter.toLowerCase()
            )) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No contacts found."
            : result.toString()
    );
}

private void searchByPhonePrefix() {
    String prefix = JOptionPane.showInputDialog(
        this, "Enter phone number prefix:"
    );

    if (prefix == null || prefix.trim().isEmpty()) return;

    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getPhone() != null &&
            c.getPhone().startsWith(prefix.trim())) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void searchByEmailDomain() {
    String domain = JOptionPane.showInputDialog(
        this, "Enter email domain (example: gmail.com):"
    );

    if (domain == null || domain.trim().isEmpty()) return;

    String searchDomain = domain.trim().toLowerCase();
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        String email = c.getEmail();

        if (email != null &&
            email.toLowerCase().endsWith("@" + searchDomain)) {
            result.append(c.getName())
                  .append(" - ")
                  .append(email)
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void showAlternatePhoneContacts() {
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        String phone = c.getAlternatePhone();

        if (phone != null && !phone.isBlank()) {
            result.append(c.getName())
                  .append(" - ")
                  .append(phone)
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No alternate phone contacts found."
            : result.toString()
    );
}

private void showBirthdayContacts() {
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        String dob = c.getDob();

        if (dob != null && !dob.isBlank()) {
            result.append(c.getName())
                  .append(" - Birthday: ")
                  .append(dob)
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No birthday details available."
            : result.toString()
    );
}

private void showAnniversaryContacts() {
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        String anniversary = c.getAnniversary();

        if (anniversary != null && !anniversary.isBlank()) {
            result.append(c.getName())
                  .append(" - Anniversary: ")
                  .append(anniversary)
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No anniversary details available."
            : result.toString()
    );
}

private void showEmergencyContacts() {
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.isEmergencyContact()) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this, result.length() == 0
            ? "No emergency contacts found."
            : result.toString()
    );
}
private void toggleEmergencyContact() {
    Contact c = selectedContactWithoutMessage();

    if (c == null) return;

    c.setEmergencyContact(!c.isEmergencyContact());

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this,
        c.isEmergencyContact()
            ? "Marked as emergency contact."
            : "Removed from emergency contacts."
    );
}

private void togglePrivateContact() {
    Contact c = selectedContactWithoutMessage();

    if (c == null) return;

    c.setPrivateContact(!c.isPrivateContact());

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this,
        c.isPrivateContact()
            ? "Contact marked private."
            : "Contact is no longer private."
    );
}

private void viewPrivateContacts() {
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.isPrivateContact()) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No private contacts found."
            : result.toString()
    );
}

private void toggleFavorite() {
    Contact c = selectedContactWithoutMessage();

    if (c == null) return;

    c.setFavorite(!c.isFavorite());

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this,
        c.isFavorite()
            ? "Added to favorites."
            : "Removed from favorites."
    );
}

private void searchByTag() {
    String tag = JOptionPane.showInputDialog(
        this, "Enter tag to search:"
    );

    if (tag == null || tag.trim().isEmpty()) return;

    String searchTag = tag.trim().toLowerCase();
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        String tags = c.getTags();

        if (tags != null) {
            for (String existingTag : tags.split(",")) {
                if (existingTag.trim().equalsIgnoreCase(searchTag)) {
                    result.append(c.getName())
                          .append(" - ")
                          .append(tags)
                          .append("\n");
                    break;
                }
            }
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void searchByRelationship() {
    String relationship = JOptionPane.showInputDialog(
        this, "Enter relationship:"
    );

    if (relationship == null || relationship.trim().isEmpty()) return;

    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getRelationship() != null &&
            c.getRelationship().equalsIgnoreCase(
                relationship.trim()
            )) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void searchByDepartment() {
    String department = JOptionPane.showInputDialog(
        this, "Enter department:"
    );

    if (department == null || department.trim().isEmpty()) return;

    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getDepartment() != null &&
            c.getDepartment().toLowerCase().contains(
                department.trim().toLowerCase()
            )) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getDepartment())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void showWebsiteContacts() {
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getWebsite() != null &&
            !c.getWebsite().trim().isEmpty()) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getWebsite())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No website details found."
            : result.toString()
    );
}

private void searchByNickname() {
    String nickname = JOptionPane.showInputDialog(
        this, "Enter nickname:"
    );

    if (nickname == null || nickname.trim().isEmpty()) return;

    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getNickname() != null &&
            c.getNickname().toLowerCase().contains(
                nickname.trim().toLowerCase()
            )) {
            result.append(c.getName())
                  .append(" (")
                  .append(c.getNickname())
                  .append(")\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void searchByJobTitle() {
    String jobTitle = JOptionPane.showInputDialog(
        this, "Enter job title:"
    );

    if (jobTitle == null || jobTitle.trim().isEmpty()) return;

    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getJobTitle() != null &&
            c.getJobTitle().toLowerCase().contains(
                jobTitle.trim().toLowerCase()
            )) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getJobTitle())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No matching contacts found."
            : result.toString()
    );
}

private void showContactsCreatedToday() {
    java.text.SimpleDateFormat sdf =
        new java.text.SimpleDateFormat("yyyyMMdd");

    String today = sdf.format(new java.util.Date());
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getCreatedDate() != null &&
            sdf.format(c.getCreatedDate()).equals(today)) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No contacts created today."
            : result.toString()
    );
}

private void showContactsUpdatedToday() {
    java.text.SimpleDateFormat sdf =
        new java.text.SimpleDateFormat("yyyyMMdd");

    String today = sdf.format(new java.util.Date());
    StringBuilder result = new StringBuilder();

    for (Contact c : contacts) {
        if (c.getUpdatedDate() != null &&
            sdf.format(c.getUpdatedDate()).equals(today)) {
            result.append(c.getName())
                  .append(" - ")
                  .append(c.getPhone())
                  .append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        result.length() == 0
            ? "No contacts updated today."
            : result.toString()
    );
}
private void sortContactsZA() {
    sorter.setSortKeys(
        java.util.Collections.singletonList(
            new javax.swing.RowSorter.SortKey(
                1, javax.swing.SortOrder.DESCENDING
            )
        )
    );
}

private void sortOldestContacts() {
    contacts.sort(
        java.util.Comparator.comparing(
            Contact::getCreatedDate,
            java.util.Comparator.nullsLast(
                java.util.Comparator.naturalOrder()
            )
        )
    );

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this, "Contacts sorted from oldest to newest."
    );
}

private void sortNewestContacts() {
    contacts.sort(
        java.util.Comparator.comparing(
            Contact::getCreatedDate,
            java.util.Comparator.nullsLast(
                java.util.Comparator.reverseOrder()
            )
        )
    );

    saveContacts();
    refreshTable();

    JOptionPane.showMessageDialog(
        this, "Contacts sorted from newest to oldest."
    );
}

private void exportContactsToXML() {
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(
        new java.io.File("contacts.xml")
    );

    if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    try {
        javax.xml.parsers.DocumentBuilderFactory factory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();

        javax.xml.parsers.DocumentBuilder builder =
            factory.newDocumentBuilder();

        org.w3c.dom.Document document =
            builder.newDocument();

        org.w3c.dom.Element root =
            document.createElement("contacts");

        document.appendChild(root);

        for (Contact c : contacts) {
            org.w3c.dom.Element contact =
                document.createElement("contact");

            root.appendChild(contact);

            addXmlElement(document, contact, "id", c.getId());
            addXmlElement(document, contact, "name", c.getName());
            addXmlElement(document, contact, "phone", c.getPhone());
            addXmlElement(document, contact, "email", c.getEmail());
            addXmlElement(document, contact, "address", c.getAddress());
            addXmlElement(document, contact, "category", c.getCategory());
            addXmlElement(document, contact, "notes", c.getNotes());
        }

        javax.xml.transform.Transformer transformer =
            javax.xml.transform.TransformerFactory
                .newInstance().newTransformer();

        transformer.setOutputProperty(
            javax.xml.transform.OutputKeys.INDENT, "yes"
        );

        transformer.transform(
            new javax.xml.transform.dom.DOMSource(document),
            new javax.xml.transform.stream.StreamResult(
                chooser.getSelectedFile()
            )
        );

        JOptionPane.showMessageDialog(
            this, "XML export completed successfully!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "XML export failed: " + ex.getMessage()
        );
    }
}

private void addXmlElement(
    org.w3c.dom.Document document,
    org.w3c.dom.Element parent,
    String tag,
    String value
) {
    org.w3c.dom.Element element =
        document.createElement(tag);

    element.setTextContent(value == null ? "" : value);
    parent.appendChild(element);
}

private void exportContactsToHTML() {
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(
        new java.io.File("contacts.html")
    );

    if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    try (java.io.PrintWriter writer =
             new java.io.PrintWriter(
                 chooser.getSelectedFile(),
                 "UTF-8"
             )) {

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head><meta charset='UTF-8'>");
        writer.println("<title>Contact Report</title></head><body>");
        writer.println("<h2>Contact Management Report</h2>");
        writer.println("<table border='1'>");
        writer.println(
            "<tr><th>Name</th><th>Phone</th>" +
            "<th>Email</th><th>Category</th></tr>"
        );

        for (Contact c : contacts) {
            writer.println(
                "<tr><td>" + htmlEscape(c.getName()) +
                "</td><td>" + htmlEscape(c.getPhone()) +
                "</td><td>" + htmlEscape(c.getEmail()) +
                "</td><td>" + htmlEscape(c.getCategory()) +
                "</td></tr>"
            );
        }

        writer.println("</table></body></html>");

        JOptionPane.showMessageDialog(
            this, "HTML export completed!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "HTML export failed: " + ex.getMessage()
        );
    }
}

private String htmlEscape(String value) {
    if (value == null) return "";

    return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
}

private void exportContactsToMarkdown() {
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(
        new java.io.File("contacts.md")
    );

    if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    try (java.io.PrintWriter writer =
             new java.io.PrintWriter(
                 chooser.getSelectedFile(),
                 "UTF-8"
             )) {

        writer.println("# Contact Management Report");
        writer.println();
        writer.println("| Name | Phone | Email | Category |");
        writer.println("|---|---|---|---|");

        for (Contact c : contacts) {
            writer.println(
                "| " + markdownEscape(c.getName()) +
                " | " + markdownEscape(c.getPhone()) +
                " | " + markdownEscape(c.getEmail()) +
                " | " + markdownEscape(c.getCategory()) +
                " |"
            );
        }

        JOptionPane.showMessageDialog(
            this, "Markdown export completed!"
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "Markdown export failed: " + ex.getMessage()
        );
    }
}

private String markdownEscape(String value) {
    if (value == null) return "";

    return value.replace("|", "\\|")
                .replace("\n", " ");
}
private void importContactsFromXML() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select XML Contact File");

    if (chooser.showOpenDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    try {
        javax.xml.parsers.DocumentBuilderFactory factory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();

        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl",
            true
        );

        javax.xml.parsers.DocumentBuilder builder =
            factory.newDocumentBuilder();

        org.w3c.dom.Document document =
            builder.parse(chooser.getSelectedFile());

        document.getDocumentElement().normalize();

        org.w3c.dom.NodeList nodes =
            document.getElementsByTagName("contact");

        int imported = 0;
        int skipped = 0;

        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Element element =
                (org.w3c.dom.Element) nodes.item(i);

            String id = xmlValue(element, "id");
            String name = xmlValue(element, "name");
            String phone = xmlValue(element, "phone");
            String email = xmlValue(element, "email");
            String address = xmlValue(element, "address");
            String category = xmlValue(element, "category");
            String notes = xmlValue(element, "notes");

            if (name.isBlank() || phone.isBlank()) {
                skipped++;
                continue;
            }

            boolean duplicate = contacts.stream()
                .anyMatch(c -> phone.equals(c.getPhone()));

            if (duplicate) {
                skipped++;
                continue;
            }

            Contact c = new Contact(
                id.isBlank()
                    ? java.util.UUID.randomUUID().toString()
                    : id,
                name, phone, email, address, "",
                category, notes, "", "", "",
                false, "", "", ""
            );

            contacts.add(c);
            imported++;
        }

        saveContacts();
        refreshTable();

        JOptionPane.showMessageDialog(
            this,
            "Imported: " + imported +
            "\nSkipped: " + skipped
        );

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this, "XML import failed: " + ex.getMessage()
        );
    }
}

private String xmlValue(
    org.w3c.dom.Element element,
    String tag
) {
    org.w3c.dom.NodeList nodes =
        element.getElementsByTagName(tag);

    if (nodes.getLength() == 0) return "";

    String value = nodes.item(0).getTextContent();

    return value == null ? "" : value;
}

private void validateAllContacts() {
    int valid = 0;
    int invalid = 0;

    StringBuilder report = new StringBuilder();

    for (Contact c : contacts) {
        boolean nameValid =
            c.getName() != null && !c.getName().isBlank();

        boolean phoneValid =
            c.getPhone() != null &&
            c.getPhone().matches("[0-9]{10}");

        boolean emailValid =
            c.getEmail() == null ||
            c.getEmail().isBlank() ||
            c.getEmail().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

        if (nameValid && phoneValid && emailValid) {
            valid++;
        } else {
            invalid++;

            report.append(c.getName())
                  .append(" - ");

            if (!nameValid) report.append("Invalid name; ");
            if (!phoneValid) report.append("Invalid phone; ");
            if (!emailValid) report.append("Invalid email; ");

            report.append("\n");
        }
    }

    JOptionPane.showMessageDialog(
        this,
        "Valid contacts: " + valid +
        "\nInvalid contacts: " + invalid +
        "\n\n" + (invalid == 0
            ? "All contacts passed validation."
            : report.toString()),
        "Contact Validation",
        JOptionPane.INFORMATION_MESSAGE
    );
}

private void findInvalidEmails() {
    StringBuilder result = new StringBuilder();
    int count = 0;

    for (Contact c : contacts) {
        String email = c.getEmail();

        if (email != null && !email.isBlank() &&
            !email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )) {

            result.append(c.getName())
                  .append(" - ")
                  .append(email)
                  .append("\n");

            count++;
        }
    }

    JOptionPane.showMessageDialog(
        this,
        count == 0
            ? "No invalid email addresses found."
            : "Invalid emails: " + count +
              "\n\n" + result.toString()
    );
}

private void findInvalidPhones() {
    StringBuilder result = new StringBuilder();
    int count = 0;

    for (Contact c : contacts) {
        String phone = c.getPhone();

        if (phone == null || !phone.matches("[0-9]{10}")) {
            result.append(c.getName())
                  .append(" - ")
                  .append(phone == null ? "Empty phone" : phone)
                  .append("\n");

            count++;
        }
    }

    JOptionPane.showMessageDialog(
        this,
        count == 0
            ? "No invalid phone numbers found."
            : "Invalid phone numbers: " + count +
              "\n\n" + result.toString()
    );
}

private void generateDatabaseReport() {
    int total = contacts.size();
    int favorites = 0;
    int blocked = 0;
    int emergency = 0;
    int privateCount = 0;
    int withEmail = 0;
    int withAddress = 0;

    for (Contact c : contacts) {
        if (c.isFavorite()) favorites++;
        if (c.isBlocked()) blocked++;
        if (c.isEmergencyContact()) emergency++;
        if (c.isPrivateContact()) privateCount++;

        if (c.getEmail() != null &&
            !c.getEmail().isBlank()) {
            withEmail++;
        }

        if (c.getAddress() != null &&
            !c.getAddress().isBlank()) {
            withAddress++;
        }
    }
   
    String report =
        "CONTACT DATABASE REPORT\n\n" +
        "Total Contacts: " + total + "\n" +
        "Favorite Contacts: " + favorites + "\n" +
        "Blocked Contacts: " + blocked + "\n" +
        "Emergency Contacts: " + emergency + "\n" +
        "Private Contacts: " + privateCount + "\n" +
        "Contacts with Email: " + withEmail + "\n" +
        "Contacts with Address: " + withAddress + "\n" +
        "Contacts without Email: " + (total - withEmail) + "\n" +
        "Contacts without Address: " + (total - withAddress);

    JOptionPane.showMessageDialog(
        this, report, "Database Report",
        JOptionPane.INFORMATION_MESSAGE
    );
}

    // ---------------- MAIN METHOD ----------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }

private String hashPassword(String password, String saltText)
        throws Exception {

    byte[] salt = Base64.getDecoder().decode(saltText);

    KeySpec spec = new PBEKeySpec(
        password.toCharArray(),
        salt,
        210000,
        256
    );

    SecretKeyFactory factory =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

    byte[] hash = factory.generateSecret(spec).getEncoded();

    return Base64.getEncoder().encodeToString(hash);
}

private String generateSalt() {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);

    return Base64.getEncoder().encodeToString(salt);
}

private void saveEncryptedPassword(String password) {
    try {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        try (java.io.PrintWriter writer =
                 new java.io.PrintWriter(PASSWORD_FILE)) {
            writer.println("PBKDF2");
            writer.println(salt);
            writer.println(hash);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            null, "Password saving failed: " + e.getMessage()
        );
    }
}

private boolean verifyEncryptedPassword(String password) {
    try (java.util.Scanner scanner =
             new java.util.Scanner(new java.io.File(PASSWORD_FILE))) {

        String format = scanner.nextLine();

        if (!format.equals("PBKDF2")) {
            return false;
        }

        String salt = scanner.nextLine();
        String savedHash = scanner.nextLine();
        String enteredHash = hashPassword(password, salt);

        return java.security.MessageDigest.isEqual(
            Base64.getDecoder().decode(savedHash),
            Base64.getDecoder().decode(enteredHash)
        );

    } catch (Exception e) {
        return false;
    }
}

private void changePassword() {
    String oldPassword = JOptionPane.showInputDialog(
        null, "Enter current password:"
    );

    if (oldPassword == null) return;

    if (!verifyEncryptedPassword(oldPassword)) {
        JOptionPane.showMessageDialog(
            null, "Incorrect current password."
        );
        return;
    }

    String newPassword = JOptionPane.showInputDialog(
        null, "Enter new password:"
    );

    if (newPassword == null || newPassword.trim().isEmpty()) {
        return;
    }

    saveEncryptedPassword(newPassword);

    JOptionPane.showMessageDialog(
        null, "Password changed successfully."
    );
}
}
