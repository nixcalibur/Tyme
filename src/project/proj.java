package project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class proj {

    private JFrame frame;
    private Map<String, DefaultTableModel> userTableModels;
    private JButton btnIn;
    private JButton btnOut;
    private JButton btnCalculate;
    private JButton btnSave;
    private JButton btnManageUsers;
    private JLabel lblSelectedUser;
    private boolean inButtonPressed;
    private JComboBox<String> manageUserComboBox = new JComboBox<>(); 
    private String selectedUser;
    private DefaultTableModel currentTableModel;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    proj window = new proj();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public proj() {
        userTableModels = new HashMap<>();
        initialize();
        loadUsersFromFile(); 
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        createTableModel();
        
        btnIn = new JButton("IN");
        btnIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recordTime("IN");
            }
        });

        btnIn.setFont(new Font("Lucida Grande", Font.PLAIN, 23));
        btnIn.setForeground(Color.BLUE);
        btnIn.setBackground(Color.DARK_GRAY);
        btnIn.setBounds(660, 80, 96, 71);
        frame.getContentPane().add(btnIn);

        btnOut = new JButton("OUT");
        btnOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                recordTime("OUT");
            }
        });

        btnOut.setForeground(Color.RED);
        btnOut.setFont(new Font("Lucida Grande", Font.PLAIN, 23));
        btnOut.setBackground(Color.DARK_GRAY);
        btnOut.setBounds(660, 195, 96, 71);
        frame.getContentPane().add(btnOut);

        btnCalculate = new JButton("Calculate");
        btnCalculate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculateTotalDuration();
            }
        });

        btnCalculate.setBounds(660, 330, 117, 29);
        frame.getContentPane().add(btnCalculate);

        inButtonPressed = false;

        btnSave = new JButton("SAVE");
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveRecords();
            }
        });
        btnSave.setBounds(6, 343, 85, 23);
        frame.getContentPane().add(btnSave);
        
        btnManageUsers = new JButton("USER");
        btnManageUsers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageUsers();
            }
        });
        btnManageUsers.setBounds(10, 10, 80, 35);
        frame.getContentPane().add(btnManageUsers);

        lblSelectedUser = new JLabel("Employee: ");
        lblSelectedUser.setBounds(100, 10, 200, 35);
        frame.getContentPane().add(lblSelectedUser);
    }

    private void recordTime(final String eventType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yy/MM/dd");
        final String currentTime = dateFormat.format(new java.util.Date());

        DefaultTableModel tableModel = createTableModel();

        if (eventType.equals("IN")) {
            if (!inButtonPressed) {
                tableModel.addRow(new Vector<String>() {
                    {
                        add(dateOnlyFormat.format(new java.util.Date()));
                        add(currentTime);
                        add("");
                        add("");
                    }
                });
                inButtonPressed = true;
            }
        } else if (eventType.equals("OUT")) {
            if (inButtonPressed && tableModel.getRowCount() > 0) {
                int lastRowIndex = tableModel.getRowCount() - 1;
                String inTime = tableModel.getValueAt(lastRowIndex, 1).toString();

                try {
                    java.util.Date inDate = dateFormat.parse(inTime);
                    java.util.Date outDate = dateFormat.parse(currentTime);

                    long duration = (outDate.getTime() - inDate.getTime()) / 1000;

                    int hours = (int) (duration / 3600);
                    int minutes = (int) ((duration % 3600) / 60);
                    int seconds = (int) (duration % 60);

                    String durationString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    tableModel.setValueAt(currentTime, lastRowIndex, 2);
                    tableModel.setValueAt(durationString, lastRowIndex, 3);
                    inButtonPressed = false;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    private DefaultTableModel createTableModel() {
        DefaultTableModel tableModel = userTableModels.get(selectedUser);

        if (tableModel == null) {
            tableModel = new DefaultTableModel();
            tableModel.addColumn("Date");
            tableModel.addColumn("IN");
            tableModel.addColumn("OUT");
            tableModel.addColumn("Duration");

            userTableModels.put(selectedUser, tableModel);

            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBounds(89, 41, 534, 289);
            frame.getContentPane().add(scrollPane);
            
            currentTableModel = tableModel;
        }

        return tableModel;
    }

    private void calculateTotalDuration() {
        DefaultTableModel tableModel = userTableModels.get(selectedUser);

        if (tableModel == null || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No time records found for " + selectedUser, "No Records", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 2).toString().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Incomplete time record found.", "Incomplete Record", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        long totalDuration = 0;

        if (tableModel != null) {
            int rowCount = tableModel.getRowCount();

            for (int i = 0; i < rowCount; i++) {
                String durationString = tableModel.getValueAt(i, 3).toString();
                String[] durationParts = durationString.split(":");
                int hours = Integer.parseInt(durationParts[0]);
                int minutes = Integer.parseInt(durationParts[1]);
                int seconds = Integer.parseInt(durationParts[2]);
                long durationInSeconds = hours * 3600 + minutes * 60 + seconds;
                totalDuration += durationInSeconds;
            }

            int totalHours = (int) (totalDuration / 3600);
            int totalMinutes = (int) ((totalDuration % 3600) / 60);
            int totalSeconds = (int) (totalDuration % 60);

            double durationInHours = (double) totalDuration / 3600;

            String totalDurationString = String.format("%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds);
            String stringDurationInHours = String.format("%.8f", durationInHours);
            
            final double wage = 10;
            
            double pay = (double) durationInHours * wage;
            String payFormat = String.format("%.2f", pay);

            JOptionPane.showMessageDialog(frame, "Wage is $10/h\n" + "Total Duration for " + selectedUser + ":\n" + totalDurationString + " hours\n"
                    + stringDurationInHours + " hours" + "\n\nPAY : " + payFormat + "$", "Wage Pay", JOptionPane.INFORMATION_MESSAGE);
        } else
			JOptionPane.showMessageDialog(frame, "No time records found for " + selectedUser, "No Records", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateTable() {
        DefaultTableModel tableModel = userTableModels.get(selectedUser);

        // Clear components related to the table
        Component[] components = frame.getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JScrollPane || (component instanceof JButton && !((JButton) component).getText().equals("USER"))) {
                frame.getContentPane().remove(component);
            }
        }

        if (tableModel != null) {
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBounds(89, 41, 534, 289);
            frame.getContentPane().add(scrollPane);

            JButton btnSave = new JButton("SAVE");
            btnSave.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveRecords();
                }
            });
            btnSave.setBounds(6, 343, 85, 23);
            frame.getContentPane().add(btnSave);
        } else {
            DefaultTableModel emptyTableModel = new DefaultTableModel();
            emptyTableModel.addColumn("Date");
            emptyTableModel.addColumn("IN");
            emptyTableModel.addColumn("OUT");
            emptyTableModel.addColumn("Duration");

            JTable emptyTable = new JTable(emptyTableModel);
            JScrollPane emptyScrollPane = new JScrollPane(emptyTable);
            emptyScrollPane.setBounds(89, 41, 534, 289);
            frame.getContentPane().add(emptyScrollPane);

            JButton btnSave = new JButton("SAVE");
            btnSave.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveRecords();
                }
            });
            btnSave.setBounds(6, 343, 85, 23);
            frame.getContentPane().add(btnSave);
        }

        // Add the label, USER button, and other buttons back
        frame.getContentPane().add(lblSelectedUser);
        frame.getContentPane().add(btnIn);
        frame.getContentPane().add(btnOut);
        frame.getContentPane().add(btnCalculate);
        frame.getContentPane().add(btnManageUsers);

        inButtonPressed = false;

        frame.revalidate();
        frame.repaint();
    }

    private void saveRecords() {
        File saves = new File("/Users/sduphy/Desktop/saves.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saves))) {
            DefaultTableModel tableModel = userTableModels.get(selectedUser);

            if (tableModel != null) {
                int rowCount = tableModel.getRowCount();
                int columnCount = tableModel.getColumnCount();

                for (int i = 0; i < columnCount; i++) {
                    writer.append(tableModel.getColumnName(i));
                    if (i < columnCount - 1) {
                        writer.append(" , ");
                    }
                }
                writer.write(" , User");
                writer.newLine();

                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        writer.append(tableModel.getValueAt(i, j).toString());
                        if (j < columnCount - 1) {
                            writer.append(" , ");
                        }
                    }
                    writer.append(" , " + selectedUser);
                    writer.newLine();
                }

                JOptionPane.showMessageDialog(frame, "Records saved successfully", "Save Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "No records to save", "No Records", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving records", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manageUsers() {
        JFrame userFrame = new JFrame("Manage Users");
        userFrame.setBounds(200, 200, 300, 200);
        userFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        userFrame.getContentPane().setLayout(new FlowLayout());

        manageUserComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedUser = manageUserComboBox.getSelectedItem().toString();
                    updateSelectedUserLabel();
                    createTableModelIfAbsent(selectedUser);
                    updateTable(); 
                }
            }
        });
        userFrame.getContentPane().add(manageUserComboBox);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newUser = JOptionPane.showInputDialog(userFrame, "Enter the new user name:");
                if (newUser != null && !newUser.isEmpty()) {
                    manageUserComboBox.addItem(newUser);
                    createTableModelIfAbsent(newUser);
                    selectedUser = newUser;
                    updateSelectedUserLabel();
                    updateTable();
                    saveUsersToFile();
                }
            }
        });
        userFrame.getContentPane().add(btnAdd);

        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String removedUser = manageUserComboBox.getSelectedItem().toString();
                int confirm = JOptionPane.showConfirmDialog(userFrame, "Are you sure you want to remove the user '" + removedUser + "'?", "Remove User", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    manageUserComboBox.removeItem(removedUser);
                    userTableModels.remove(removedUser);
                    selectedUser = null;
                    updateSelectedUserLabel();
                    updateTable();
                    saveUsersToFile();
                }
            }
        });
        userFrame.getContentPane().add(btnRemove);
        
        JButton btnDone = new JButton("Done");
        btnDone.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		userFrame.dispose();
        	}
        });
        userFrame.getContentPane().add(btnDone);
        userFrame.setVisible(true);
    }


    private void updateSelectedUserLabel() {
        Object selectedItem = manageUserComboBox.getSelectedItem();
        if (selectedItem != null) {
            selectedUser = selectedItem.toString();
            lblSelectedUser.setText("Employee : " + selectedUser);
        } else {
            System.err.println("Selected item is null.");
        }
    }
    
    private void createTableModelIfAbsent(String user) {
        DefaultTableModel tableModel = userTableModels.get(user);
        if (tableModel == null) {
            tableModel = new DefaultTableModel();
            tableModel.addColumn("Date");
            tableModel.addColumn("IN");
            tableModel.addColumn("OUT");
            tableModel.addColumn("Duration");
            userTableModels.put(user, tableModel);
        }
    }
    
    private void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/Users/sduphy/Desktop/users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                manageUserComboBox.addItem(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading users from file", "Load Users Error", JOptionPane.ERROR_MESSAGE);
        }

        /*// Load saved time from file
        selectedUser = manageUserComboBox.getItemAt(0).toString(); 
        DefaultTableModel tableModel = createTableModel();

        try (BufferedReader reader = new BufferedReader(new FileReader("/Users/sduphy/Desktop/saves.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tableModel.addRow(new Vector<String>() {
                    {
                        add(dateOnlyFormat.format(new java.util.Date()));
                        add(currentTime);
                        add("");
                        add("");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading saves from file", "Load Users Error", JOptionPane.ERROR_MESSAGE);
        }*/
    }

    
    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/sduphy/Desktop/users.txt"))) {
            for (int i = 0; i < manageUserComboBox.getItemCount(); i++) {
                Object item = manageUserComboBox.getItemAt(i);
                if (item != null) {
                    writer.write(item.toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving users to file", "Save Users Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
