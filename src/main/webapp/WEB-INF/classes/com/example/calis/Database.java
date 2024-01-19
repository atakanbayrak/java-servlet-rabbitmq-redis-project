package com.example.calis;

import java.sql.*;

public class Database {

    // Bunun tekil oluşması lazım multithread yüzünden patlayabiliyor. Valotile, Synchronize
    private static Database process = new Database();

    private Database()
    {
        System.out.println("Database Process sınıfını Singleton olarak oluşturuldu.");
    }
    public static Database getInstance()
    {
        return process;
    }

    private static final String url = "jdbc:postgresql://localhost:5432/servlet";
    private static final String user = "postgres";
    private static final String password = "12345";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }

    // process null check yapılması lazım
    String qry = "insert into users (fullname,tc) values (?,?)";
    public void saveToDatabase(String fullname, String tckn)
    {
        try {
            Connection connection = Database.connect();
            PreparedStatement insert = connection.prepareStatement(qry);
            insert.setString(1,fullname);
            insert.setString(2,tckn);
            insert.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFromDatabase(String tckn)
    {
        System.out.println("GET TCKN: "+ tckn);
        ResultSet result = null;
        String slct = "SELECT * FROM users WHERE tc = '"+tckn+"'";
        try {
            Connection connection = Database.connect();
            PreparedStatement select = connection.prepareStatement(slct);
            result = select.executeQuery();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            while(result.next())
            {
                if(result.getString(2).equals(tckn))
                {
                    return tckn;
                }
                else
                {
                    return "Empty";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "Empty";
    }

    public boolean checkDatabase(String name, String tckn) throws SQLException {
        System.out.println(name + " " + tckn);
        ResultSet result = null;
        String slct = "SELECT * FROM users WHERE fullname = '"+name+"' AND tc = '"+tckn+"'";
        try {
            Connection connection = Database.connect();
            PreparedStatement select = connection.prepareStatement(slct);
            result = select.executeQuery();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            while(result.next())
            {
                if(result.getString(1).equals(name))
                {
                    System.out.println("Burada "+result.getString(1).equals(name));
                    return true;
                }
                else
                {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
