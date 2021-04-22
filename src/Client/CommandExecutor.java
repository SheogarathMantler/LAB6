package Client;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

// принимает от клиента объект Message, преобразовывает его в команду и выполняет её
public class CommandExecutor {
    private final LinkedHashSet<Dragon> set;
    private final boolean fromScript;
    private final Logger logger = Logger.getLogger("server.executor");
    public CommandExecutor(LinkedHashSet<Dragon> set, boolean fromScript) {
        this.set = set;
        this.fromScript = fromScript;
    }

    public void execute(ObjectInputStream inputStream, DataOutputStream outputStream) throws ClassNotFoundException, ParserConfigurationException {
        // принимаем сообщение
        boolean endOfStream = false;
        while (!endOfStream) {
            try {
                Message message = (Message) inputStream.readObject();
                logger.info("message recieved");
                if (message.isEnd) break; // если кто-то умный нажал Ctrl+D
                if (message.type == Command.CommandType.exit && !message.metaFromScript)
                    endOfStream = true; // заканчиваем принимать сообщения после команды exit не из скрипта
                //            System.out.println("Сообщение принято");
                Command command = new Command(outputStream, message.argument, message.dragon, set, fromScript);
                command.changeType(message.type);
                command.run();
            } catch (IOException e) {
                break;
            }
        }

    }

}

