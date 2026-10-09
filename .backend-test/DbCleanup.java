import java.sql.*;
public class DbCleanup {
  public static void main(String[] args) throws Exception {
    try (Connection c=DriverManager.getConnection("jdbc:mysql://localhost:3306/student_management", "root", "root")) {
      if(args[0].equals("inspect")) {
        try(ResultSet r=c.createStatement().executeQuery("SHOW COLUMNS FROM user LIKE 'role'")){while(r.next())System.out.println("role default="+r.getString("Default"));}
      } else {
        try(PreparedStatement p=c.prepareStatement("DELETE FROM student WHERE name LIKE ?")){p.setString(1,args[1]+"%");System.out.println("student cleanup="+p.executeUpdate());}
        try(PreparedStatement p=c.prepareStatement("DELETE FROM user WHERE username=?")){p.setString(1,args[1]);System.out.println("user cleanup="+p.executeUpdate());}
      }
    }
  }
}
