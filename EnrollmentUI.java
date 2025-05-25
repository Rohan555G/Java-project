package gui;

import dao.EnrollmentDAO;
import dao.StudentDAO;
import model.Enrollment;
import model.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;

public class EnrollmentUI extends JFrame {
    private JTextField txtStudentId, txtStudentName, txtCourseCode;
    private JTextArea textArea;
    private StudentDAO studentDAO = new StudentDAO();
    private EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    // In-memory HashMap for student enrollments
    private HashMap<String, List<String>> enrollmentMap = new HashMap<>();

    public EnrollmentUI() {
        setTitle("Student Course Enrollment");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblStudentId = new JLabel("Student ID:");
        JLabel lblStudentName = new JLabel("Student Name:");
        JLabel lblCourseCode = new JLabel("Course Code:");

        txtStudentId = new JTextField(15);
        txtStudentName = new JTextField(15);
        txtCourseCode = new JTextField(15);

        JButton btnAddStudent = new JButton("Add Student");
        JButton btnEnroll = new JButton("Enroll in Course");
        JButton btnDrop = new JButton("Drop Course");
        JButton btnSearch = new JButton("Search Courses");

        textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Layout placement
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblStudentId, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtStudentId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblStudentName, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtStudentName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblCourseCode, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtCourseCode, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(btnAddStudent, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(btnEnroll, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(btnDrop, gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(btnSearch, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        add(panel);

        // Button Listeners
        btnAddStudent.addActionListener(e -> addStudent());
        btnEnroll.addActionListener(e -> enrollCourse());
        btnDrop.addActionListener(e -> dropCourse());
        btnSearch.addActionListener(e -> searchCourses());
    }

    private void addStudent() {
        String studentId = txtStudentId.getText().trim();
        String studentName = txtStudentName.getText().trim();
        if (studentId.isEmpty() || studentName.isEmpty()) {
            showMessage("Student ID and Name cannot be empty.");
            return;
        }
        try {
            if (studentDAO.getStudent(studentId) != null) {
                showMessage("Student already exists.");
                return;
            }
            Student student = new Student(studentId, studentName);
            studentDAO.addStudent(student);
            showMessage("Student added successfully.");
            txtStudentId.setText("");
            txtStudentName.setText("");
        } catch (SQLException ex) {
            showMessage("Error adding student: " + ex.getMessage());
        }
    }

    private void enrollCourse() {
        String studentId = txtStudentId.getText().trim();
        String courseCode = txtCourseCode.getText().trim();
        if (studentId.isEmpty() || courseCode.isEmpty()) {
            showMessage("Student ID and Course Code cannot be empty.");
            return;
        }
        try {
            if (studentDAO.getStudent(studentId) == null) {
                showMessage("Student does not exist.");
                return;
            }
            enrollmentDAO.addEnrollment(new Enrollment(studentId, courseCode));

            // Update in-memory HashMap
            enrollmentMap.computeIfAbsent(studentId, k -> new java.util.ArrayList<>()).add(courseCode);

            showMessage("Enrolled in course successfully.");
            txtCourseCode.setText("");
        } catch (SQLException ex) {
            showMessage("Error enrolling course: " + ex.getMessage());
        }
    }

    private void dropCourse() {
        String studentId = txtStudentId.getText().trim();
        String courseCode = txtCourseCode.getText().trim();
        if (studentId.isEmpty() || courseCode.isEmpty()) {
            showMessage("Student ID and Course Code cannot be empty.");
            return;
        }
        try {
            enrollmentDAO.dropEnrollment(new Enrollment(studentId, courseCode));

            // Update in-memory HashMap
            List<String> courses = enrollmentMap.get(studentId);
            if (courses != null) {
                courses.remove(courseCode);
            }

            showMessage("Dropped course successfully.");
            txtCourseCode.setText("");
        } catch (SQLException ex) {
            showMessage("Error dropping course: " + ex.getMessage());
        }
    }

    private void searchCourses() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            showMessage("Student ID cannot be empty.");
            return;
        }
        try {
            List<String> courses = enrollmentDAO.getCoursesByStudent(studentId);
            enrollmentMap.put(studentId, courses); // sync HashMap with DB

            if (courses.isEmpty()) {
                textArea.setText("No courses found for student " + studentId);
            } else {
                StringBuilder sb = new StringBuilder("Courses enrolled by " + studentId + ":\n");
                for (String course : courses) {
                    sb.append("- ").append(course).append("\n");
                }
                textArea.setText(sb.toString());
            }
        } catch (SQLException ex) {
            showMessage("Error fetching courses: " + ex.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnrollmentUI ui = new EnrollmentUI();
            ui.setVisible(true);
        });
    }
}
