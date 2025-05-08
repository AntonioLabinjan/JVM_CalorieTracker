import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class FoodTrackerApp extends JFrame {
    private DefaultTableModel tableModel;
    private JTable foodTable;
    private JTextField nameField, caloriesField;
    private JComboBox<String> categoryBox, filterBox;
    private JLabel totalCaloriesLabel;

    public FoodTrackerApp() {
        setTitle("Food Tracker ");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        nameField = new JTextField();
        caloriesField = new JTextField();
        categoryBox = new JComboBox<>(new String[]{"Voće", "Povrće", "Slatkiši", "Meso", "Ostalo"});
        JButton addButton = new JButton("Dodaj");

        inputPanel.add(new JLabel("Hrana:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Kalorije:"));
        inputPanel.add(caloriesField);
        inputPanel.add(new JLabel("Kategorija:"));
        inputPanel.add(categoryBox);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);


        tableModel = new DefaultTableModel(new String[]{"Hrana", "Kalorije", "Kategorija"}, 0);
        foodTable = new JTable(tableModel);

        foodTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        add(new JScrollPane(foodTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBox = new JComboBox<>(new String[]{"Sve", "Voće", "Povrće", "Slatkiši", "Meso", "Ostalo"});
        JButton filterButton = new JButton("Filtriraj");
        JButton resetFilterButton = new JButton("Resetiraj");
        JButton deleteButton = new JButton("Obriši odabrano");
        totalCaloriesLabel = new JLabel("Ukupno kalorija: 0");

        bottomPanel.add(new JLabel("Filter:"));
        bottomPanel.add(filterBox);
        bottomPanel.add(filterButton);
        bottomPanel.add(resetFilterButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(totalCaloriesLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addFood());
        deleteButton.addActionListener(e -> deleteSelectedRow());
        filterButton.addActionListener(e -> applyFilter());
        resetFilterButton.addActionListener(e -> resetFilter());

        foodTable.getModel().addTableModelListener(e -> updateTotalCalories());
    }

    private void addFood() {
        String name = nameField.getText().trim();
        String caloriesStr = caloriesField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();

        if (name.isEmpty() || caloriesStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Molimo unesite ime i kalorije.");
            return;
        }

        int calories;
        try {
            calories = Integer.parseInt(caloriesStr);
            if (calories < 0) {
                JOptionPane.showMessageDialog(this, "Kalorije ne mogu biti negativne.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Kalorije moraju biti cijeli broj.");
            return;
        }

        if (calories > 1000) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "UPOZORENJE: Kalorijska bomba detektirana!");
        }

        tableModel.addRow(new Object[]{name, calories, category});
        nameField.setText("");
        caloriesField.setText("");
        updateTotalCalories();
    }

    private void deleteSelectedRow() {
        int selectedRow = foodTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            updateTotalCalories();
        } else {
            JOptionPane.showMessageDialog(this, "Odaberite red za brisanje.");
        }
    }

    private void applyFilter() {
        String selectedCategory = (String) filterBox.getSelectedItem();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(foodTable.getModel());
        if (!selectedCategory.equals("Sve")) {
            sorter.setRowFilter(RowFilter.regexFilter(selectedCategory, 2));
        } else {
            sorter.setRowFilter(null);
        }
        foodTable.setRowSorter(sorter);
    }

    private void resetFilter() {
        filterBox.setSelectedItem("Sve");
        applyFilter();
    }

    private void updateTotalCalories() {
        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                total += Integer.parseInt(tableModel.getValueAt(i, 1).toString());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        totalCaloriesLabel.setText("Ukupno kalorija: " + total);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoodTrackerApp().setVisible(true));
    }
}
