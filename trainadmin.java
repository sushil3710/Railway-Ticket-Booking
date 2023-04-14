
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.io.*;




public class trainadmin {

    public static void main(String[] args) throws SQLException 
    {
        try
        {
            final String DB_URL="jdbc:postgresql://localhost:5432/new_rail";
            final String USER="postgres";
            final String PASS="root";
        
            BufferedReader br = new BufferedReader(new FileReader( "./Trainschedule.txt"));
           

            String line = "";
            while ((line = br.readLine()) != "#") 
            {
                Connection c = DriverManager.getConnection(DB_URL,USER,PASS);
                String[] arr = line.split("[ ]+");                        
                PreparedStatement cStmt=c.prepareCall("call insert_train(?,?::DATE,?,?) ");
                Date dt= Date.valueOf(arr[1]);
                cStmt.setInt(1, Integer.parseInt(arr[0]));
                 cStmt.setDate(2, dt);
                  cStmt.setInt(3,Integer.parseInt(arr[2]));
                  cStmt.setInt(4,Integer.parseInt(arr[3]));
               

                    cStmt.execute();

                //stmt = c.createStatement();
                //stmt.executeUpdate(query);
                c.close();     
                cStmt.close();
            } 

            br.close();            
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

}
