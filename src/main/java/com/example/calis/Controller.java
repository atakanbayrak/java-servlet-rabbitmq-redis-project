package com.example.calis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "Controller", urlPatterns = {"/controller"})
public class Controller extends HttpServlet {

    Publisher publisher = Publisher.getInstance();
    Consumer subscriber = Consumer.getInstance();
    Database dbprocess = Database.getInstance();

    @Override
    public void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)throws ServletException, IOException
    {
        try {
            publisher.createProcess(servletRequest.getParameter("tckn"));
            subscriber.useProcess();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        PrintWriter writer = servletResponse.getWriter();
        writer.println("<html><body>");
        writer.println("<h1>" + subscriber.getAnswer() + "</h1>");
        writer.println("</body></html>");
    }

    @Override
    public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
    {
        //burada da rabbitmq olması lazım
        PrintWriter writer = servletResponse.getWriter();

        try {
            // Buradaki connection bağlantısı kontrol edilmeli 1000 istek geldiğinde patlayabilir.
            Database.connect();
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