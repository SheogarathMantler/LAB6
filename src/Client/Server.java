package Client;

import Client.Dragon;
import Client.FileCollectionReader;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.logging.Logger;

public class Server {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(5000);
        Logger logger = Logger.getLogger("server.main");
        while(true) {
            File file = null;
            // создаем сокет
            Socket server = serverSocket.accept();
            // создаем потоки
//            System.out.println("сокет создан");
            logger.info("сокет создан");
            DataOutputStream outputStream = new DataOutputStream(server.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(server.getInputStream());
            // считываем коллекцию из файла
            FileCollectionReader fileCollectionReader = new FileCollectionReader(file, outputStream);
            LinkedHashSet<Dragon> set = fileCollectionReader.readCollection(file);
            // исполняем команды
            if (server.isConnected()) {
//                System.out.println("okkkkkkk");
                logger.info("okkkkkkk");
                CommandExecutor executor = new CommandExecutor(set, false);
                executor.execute(inputStream, outputStream);
//                System.out.println("good bye");
                logger.info("good bye");
            }
        }
    }
}

