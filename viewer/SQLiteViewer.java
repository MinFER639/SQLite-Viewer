package viewer;

import org.sqlite.SQLiteDataSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.*;


public class SQLiteViewer extends JFrame {

    public SQLiteViewer() {
        super("SQLite Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        initComponents();
        setVisible(true);

    }

    private void initComponents() {
        JTextField fileName = new JTextField();
        fileName.setBounds(50, 20, 480, 30);
        fileName.setName("FileNameTextField");
        add(fileName);

        JTextArea textArea = new JTextArea();
        textArea.setBounds(50, 100, 480, 60);
        textArea.setName("QueryTextArea");
        textArea.setEnabled(false);
        add(textArea);

        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setBounds(50, 60, 600, 30);
        comboBox.setName("TablesComboBox");
        comboBox.addActionListener(e -> createQuery(textArea, comboBox));
        add(comboBox);

        DefaultTableModel tm = new DefaultTableModel();
        JTable table = new JTable(tm);
        table.setName("Table");
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(50, 180, 600, 550);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(sp);

        JButton runSQL = new JButton("Execute");
        runSQL.setBounds(550, 100, 100, 30);
        runSQL.setName("ExecuteQueryButton");
        runSQL.setEnabled(false);
        runSQL.addActionListener(e -> executeQuery(fileName, textArea, table));
        add(runSQL);

        JButton openFile = new JButton("Open");
        openFile.addActionListener(e -> openSql(fileName, comboBox, textArea, runSQL));
        openFile.setBounds(550, 20, 100, 30);
        openFile.setName("OpenFileButton");
        add(openFile);
    }

    private static void openSql(JTextField fileName, JComboBox<String> comboBox, JTextArea textArea, JButton runSQL) {
        comboBox.removeAllItems();
        String dbName = fileName.getText();
        String url = "jdbc:sqlite:" + dbName;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            if (con.isValid(5)) {
                String sql = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                try {
                    textArea.setText("SELECT * FROM " + rs.getString("name"));
                    while (rs.next()) {
                        comboBox.addItem(rs.getString("name"));
                    }
                    textArea.setEnabled(true);
                    runSQL.setEnabled(true);
                } catch (SQLException exception) {
                    textArea.setEnabled(false);
                    runSQL.setEnabled(false);
                    JOptionPane.showMessageDialog(new Frame(), "File doesn't exist!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void createQuery(JTextArea textArea, JComboBox<String> comboBox) {
        textArea.setText("SELECT * FROM " + comboBox.getSelectedItem() + ";");
    }

    private void executeQuery(JTextField fileName, JTextArea textArea, JTable table) {
        String dbName = fileName.getText();
        String url = "jdbc:sqlite:" + dbName;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            if (con.isValid(5)) {
                String sql = textArea.getText();
                Statement stmt = con.createStatement();

                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    ResultSetMetaData rsmd = rs.getMetaData();

                    DefaultTableModel tm = (DefaultTableModel) table.getModel();
                    TableColumnModel column = table.getColumnModel();
                    tm.setColumnCount(0);

                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        tm.addColumn(rsmd.getColumnName(i));
                    }

                    tm.setRowCount(0);

                    while (rs.next()) {
                        String[] a = new String[rsmd.getColumnCount()];
                        for (int i = 0; i < rsmd.getColumnCount(); i++) {
                            a[i] = rs.getString(i + 1);
                        }
                        tm.addRow(a);
                    }

                    tm.fireTableDataChanged();

                    rs.close();
                    stmt.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(new Frame(), "Wrong SQL query!");
                }


                /*
                TableModel tableModel = new TableModel();
                ArrayList<String> columns = new ArrayList<>();
                for (int i = 1; i <=; i++) {
                    columns.add(rsmd.getColumnName(i));
                }
                TableModel tableModel = new TableModel();
                tableModel.setColumns(columns);
                while (rs.next()) {
                    System.out.println(rs.getString("name"));
                }
                 */
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
