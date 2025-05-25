CREATE DATABASE student_courses;
USE student_courses;

CREATE TABLE students (
  student_id VARCHAR(20) PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE enrollments (
  student_id VARCHAR(20),
  course_code VARCHAR(20),
  PRIMARY KEY (student_id, course_code),
  FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
