import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class FoodTrackerApp extends JFrame {
    private DefaultTableModel tableModel;
    private JTable foodTable;
    private JTextField nameField, caloriesField, goalCaloriesField;
    private JComboBox<String> categoryBox, filterBox;
    private JLabel totalCaloriesLabel, goalLabel;
    private JProgressBar progressBar;
    private int goalCalories = 0;

    public FoodTrackerApp() {
        setTitle("Food Tracker");
        setSize(650, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 10, 5));
        nameField = new JTextField();
        caloriesField = new JTextField();
        goalCaloriesField = new JTextField();
        categoryBox = new JComboBox<>(new String[]{
                "Voće",
                "Povrće",
                "Slatkiši",
                "Meso",
                "Riba",
                "Tjestenina",
                "Ostalo",
                "Mliječni proizvodi",
                "Piće",
                "Zamrznuto",
                "Grickalice",
                "Kruh i pekarski proizvodi",
                "Žitarice i pahuljice",
                "Konzervirana hrana",
                "Začini i umaci",
                "Ulje i ocat",
                "Dodaci prehrani",
                "Dječja hrana"
        });

        JButton addButton = new JButton("Dodaj");
        JButton setGoalButton = new JButton("Postavi cilj");

        inputPanel.add(new JLabel("Hrana:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Kalorije:"));
        inputPanel.add(caloriesField);
        inputPanel.add(new JLabel("Kategorija:"));
        inputPanel.add(categoryBox);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addButton);

        inputPanel.add(new JLabel("Ciljane kalorije:"));
        inputPanel.add(goalCaloriesField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(setGoalButton);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Hrana", "Kalorije", "Kategorija"}, 0);
        foodTable = new JTable(tableModel);
        foodTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        add(new JScrollPane(foodTable), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));

        PieChartPanel pieChartPanel = new PieChartPanel();
        pieChartPanel.setPreferredSize(new Dimension(250, 350));
        add(pieChartPanel, BorderLayout.EAST);


        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBox = new JComboBox<>(new String[]{"Sve", "Voće", "Povrće", "Slatkiši", "Meso", "Ostalo"});
        JButton filterButton = new JButton("Filtriraj");
        JButton resetFilterButton = new JButton("Resetiraj");
        JButton deleteButton = new JButton("Obriši odabrano");
        totalCaloriesLabel = new JLabel("Ukupno kalorija: 0");

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterBox);
        filterPanel.add(filterButton);
        filterPanel.add(resetFilterButton);
        filterPanel.add(deleteButton);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(totalCaloriesLabel);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        goalLabel = new JLabel("Cilj: 0 kalorija");

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(goalLabel, BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        bottomPanel.add(filterPanel);
        bottomPanel.add(progressPanel);

        add(bottomPanel, BorderLayout.SOUTH);

        // Event listeners
        addButton.addActionListener(e -> addFood());
        deleteButton.addActionListener(e -> deleteSelectedRow());
        filterButton.addActionListener(e -> applyFilter());
        resetFilterButton.addActionListener(e -> resetFilter());
        setGoalButton.addActionListener(e -> setGoalCalories());

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

    private void setGoalCalories() {
        try {
            int value = Integer.parseInt(goalCaloriesField.getText().trim());
            if (value <= 0) {
                JOptionPane.showMessageDialog(this, "Cilj mora biti pozitivan broj.");
                return;
            }
            goalCalories = value;
            goalLabel.setText("Cilj: " + goalCalories + " kalorija");
            updateTotalCalories();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Unesite ispravnu vrijednost cilja.");
        }
    }

    private void updateTotalCalories() {
        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                total += Integer.parseInt(tableModel.getValueAt(i, 1).toString());
            } catch (NumberFormatException e) {
                // ignore
            }
            repaint(); // refresh pie chart

        }

        totalCaloriesLabel.setText("Ukupno kalorija: " + total);

        if (goalCalories > 0) {
            int percent = (int) ((total / (double) goalCalories) * 100);
            progressBar.setValue(Math.min(percent, 100));
            progressBar.setString(total + " / " + goalCalories + " kcal");

            if (total > goalCalories) {
                progressBar.setForeground(Color.RED);
            } else {
                progressBar.setForeground(Color.GREEN.darker());
            }
        } else {
            progressBar.setValue(0);
            progressBar.setString("Postavi cilj kalorija");
            progressBar.setForeground(Color.GRAY);
        }
    }

    private class PieChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Map<String, Integer> categoryCalories = new HashMap<>();
            int total = 0;

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String category = tableModel.getValueAt(i, 2).toString();
                int cal = Integer.parseInt(tableModel.getValueAt(i, 1).toString());

                categoryCalories.put(category, categoryCalories.getOrDefault(category, 0) + cal);
                total += cal;
            }

            if (total == 0) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 20, y = 20, diameter = 200;
            int startAngle = 0;
            Random rand = new Random();
            Map<String, Color> colorMap = new HashMap<>();

            for (Map.Entry<String, Integer> entry : categoryCalories.entrySet()) {
                String category = entry.getKey();
                int value = entry.getValue();

                int angle = (int) Math.round(360.0 * value / total);
                Color color = colorMap.computeIfAbsent(category, k -> new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));

                g2d.setColor(color);
                g2d.fillArc(x, y, diameter, diameter, startAngle, angle);
                startAngle += angle;
            }

            // Draw legend
            int legendY = y + diameter + 20;
            for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
                g2d.setColor(entry.getValue());
                g2d.fillRect(x, legendY, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawString(entry.getKey(), x + 30, legendY + 15);
                legendY += 25;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoodTrackerApp().setVisible(true));
    }
}
