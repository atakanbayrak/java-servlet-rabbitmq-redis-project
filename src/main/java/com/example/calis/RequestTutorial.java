package com.example.calis;

import com.rabbitmq.client.ConnectionFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "RequestTutorial", urlPatterns = {"/request-tutorial"})
public class RequestTutorial extends HttpServlet {
    IdentityPublisher publisher = IdentityPublisher.getInstance();
    IdentitySubscriber subscriber = IdentitySubscriber.getInstance();
    DatabaseProcess dbprocess = DatabaseProcess.getInstance();
    @Override
    public void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)throws ServletException, IOException
    {
        PrintWriter writer = servletResponse.getWriter();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            // servletRequest.getParameter("fullname"),servletRequest.getParameter("tckn")
            publisher.createProcess(servletRequest.getParameter("fullname"), servletRequest.getParameter("tckn"), factory);
            subscriber.setFactory(factory);
            subscriber.useProcess();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        writer.println("<html><body>");
        writer.println("<h1>" + subscriber.getTckn() + "</h1>");
        writer.println("</body></html>");
    }
    @Override
    public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        PrintWriter writer = servletResponse.getWriter();

        try {
            DatabaseConnection.connect();
            boolean cond = dbprocess.checkDatabase(servletRequest.getParameter("fullname"),servletRequest.getParameter("tckn"));
            if(cond)
            {
                System.out.println("Kayıt mevcut");
            }
            else
            {
                System.out.println("Kayıt mevcut degildi veritabanına kaydedildi");
                dbprocess.saveToDatabase(servletRequest.getParameter("fullname"),servletRequest.getParameter("tckn"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        writer.println("<html><body>");
        writer.println("<h1>" + "Okundu" + "</h1>");
        writer.println("</body></html>");
    }

}