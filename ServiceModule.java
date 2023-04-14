import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes.Name;
import java.sql.*;
import java.sql.Date;
import java.io.*;
import java.util.*;

class QueryRunner implements Runnable
{
    //  Declare socket for client access
    protected Socket socketConnection;
    static final String JDBC_DRIVER ="org.postgresql.Driver";
    static final String DB_URL="jdbc:postgresql://localhost:5432/new_rail";
 
    static final String USER="postgres";
    static final String PASS="root";

    public QueryRunner(Socket clientSocket)
    {
        this.socketConnection =  clientSocket;
    }

    public void run()
    {
      try
        {
            //  Reading data from client
            InputStreamReader inputStream = new InputStreamReader(socketConnection
                                                                  .getInputStream()) ;
            BufferedReader bufferedInput = new BufferedReader(inputStream) ;
            OutputStreamWriter outputStream = new OutputStreamWriter(socketConnection
                                                                     .getOutputStream()) ;
            BufferedWriter bufferedOutput = new BufferedWriter(outputStream) ;
            PrintWriter printWriter = new PrintWriter(bufferedOutput, true) ;
            String clientCommand = "" ;
            String responseQuery = "" ;
            ResultSet rs=null;
            
            // Read client query from the socket endpoint
            //clientCommand = bufferedInput.readLine(); 
            while(true)
            {
                
                System.out.println("Recieved data <" + clientCommand + "> from client : " 
                                    + socketConnection.getRemoteSocketAddress().toString());

                /*******************************************
                         Your DB code goes here
                ********************************************/
                
                // Dummy response send to client
                clientCommand = bufferedInput.readLine();

                if(clientCommand.equals("#")){
                
                    inputStream.close();
                    bufferedInput.close();
                    outputStream.close();
                    bufferedOutput.close();
                    printWriter.close();
                    socketConnection.close();
                return;
            }



                //StringTokenizer tokenizer = new StringTokenizer(clientCommand);   //tokenize each line

               //String[] tokarr = new String[clientCommand.length()];

               //int i=0;
               String[] tokarr = clientCommand.split("[ ]+"); 
                
                // while(tokenizer.hasMoreTokens())
                // {
                //    tokarr[i++] = tokenizer.nextToken();
                //     System.out.println(tokarr[i-1]);
                    
               
                // }
               
                String[] namearr=new String[Integer.parseInt(tokarr[0])];
                int st=Integer.parseInt(tokarr[0]);
                for(int p=1;p<=st;p++){

                   int len=tokarr[p].length();
                   if(p==st)
                   namearr[p-1]=tokarr[p];
                   else
                   namearr[p-1]=tokarr[p].substring(0, len-1);
                   
                  // System.out.println(namearr[p-1]);
                }


  
                try 
                {
                    Date dt= Date.valueOf(tokarr[st+2]);
                    System.out.println("conecting to database");

                    Connection c = DriverManager.getConnection(DB_URL,USER,PASS);
                    //String query = "call ticket_book(?,?,?,?::DATE,?,?) ";
                    //cs = con.prepareCall(query)
                    PreparedStatement cStmt=c.prepareCall("call ticket_book(?,?,?,?::DATE,?,?)");
                     

                    cStmt.setInt(1, Integer.parseInt(tokarr[0]));
                     cStmt.setObject(2, namearr);
                      cStmt.setInt(3,Integer.parseInt(tokarr[st+1]));
                      cStmt.setDate(4,dt);
                      cStmt.setString(5,tokarr[st+3]);        
                     cStmt.setInt(6,3);
                   rs=cStmt.executeQuery();
                    //  cStmt.execute();
                      System.out.println("Opened database successfully");
                      System.out.println("query update successful");
                         
                      
			
                      if(rs.next()){
                          int x=rs.getInt(1);
                          if(x==0){

                            responseQuery = "NO TRAIN AVAILABLE";
                           printWriter.println(responseQuery);
                          }
                          else if(x==1){

                            responseQuery = "NO TICKET AVAILABLE";
                           printWriter.println(responseQuery);
                          }
                          else {

                            responseQuery = tokarr[0]+" TICKETS BOOKED ";
                           printWriter.println(responseQuery);
                          }

                      } 
                      c.close();
                      cStmt.close();

                } 
                
                catch (Exception e) 
                {
                    System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                    System.out.println("database not connected  ");
                    System.exit(0);
                }

                System.out.println("Records updated successfully");

                try
                {
                Thread.sleep(60);
                } 
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }




                // Read next client query
               // clientCommand = bufferedInput.readLine(); 


            }


        }
        catch(IOException e)
        {
            return;
        }
    }
}

/**
 * Main Class to controll the program flow
 */
public class ServiceModule 
{
    // Server listens to port
    static int serverPort = 7008;
    // Max no of parallel requests the server can process
    static int numServerCores = 5 ;         
    //------------ Main----------------------
    public static void main(String[] args) throws IOException 
    {    
        // Creating a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numServerCores);
        
        try (//Creating a server socket to listen for clients
        ServerSocket serverSocket = new ServerSocket(serverPort)) {
            Socket socketConnection = null;
            
            // Always-ON server
            while(true)
            {
                System.out.println("Listening port : " + serverPort 
                                    + "\nWaiting for clients...");
                socketConnection = serverSocket.accept();   // Accept a connection from a client
                System.out.println("Accepted client :" 
                                    + socketConnection.getRemoteSocketAddress().toString() 
                                    + "\n");
                //  Create a runnable task
                Runnable runnableTask = new QueryRunner(socketConnection);
                //  Submit task for execution   
                executorService.submit(runnableTask);   
            }
        }
    }
}




