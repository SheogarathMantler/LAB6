package Client;

import Client.Dragon;
import Client.FileCollectionReader;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.logging.Logger;

public class Server {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(5000);
        Logger logger = Logger.getLogger("server.main");
        while(true) {
            try {
                File file = null;
                // создаем сокет
                Socket server = serverSocket.accept();
                // создаем потоки

                DataOutputStream outputStream = new DataOutputStream(server.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(server.getInputStream());
                logger.info("сокет создан");
                // считываем коллекцию из файла
                FileCollectionReader fileCollectionReader = new FileCollectionReader(file, outputStream);
                LinkedHashSet<Dragon> set = fileCollectionReader.readCollection(file);
                // исполняем команды
                if (server.isConnected()) {
                    logger.info("server is connected");
                    CommandExecutor executor = new CommandExecutor(set, false);
                    executor.execute(inputStream, outputStream);
                    logger.info("session ended. Waiting for new session ... ");
                }
            } catch (SocketException e) {
                e.printStackTrace();
                System.out.println("something went wrong");
                Thread.sleep(100);
            }
        }
    }
}

