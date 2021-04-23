package Client;

import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// считывает команды из консоли и принимает ответы с сервера, выводит их в консоль
public class CommandReader {

    InetSocketAddress address;
    SocketChannel channel;
    ByteArrayOutputStream byteArrayOutputStream;
    ObjectOutputStream objectOutputStream;
    boolean afterConnecting = false;
    public CommandReader(InetSocketAddress address) {
        this.address = address;
        connect();
    }
// подключение к серверу
void connect() {
    while (true) {
        try {
            channel = SocketChannel.open(address);
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            send(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.reset();
            return;
        } catch (IOException e) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            System.out.println("reconnecting");
        }
    }
}
// собственно загрузка байт в канал
    void send(byte[] message) throws IOException {
        int r = channel.write(ByteBuffer.wrap(message));
        if (r != message.length) {
            throw new IOException();
        }
    }
// функция, которая отправляет message и получает ответ
    String getResponse(Message message) {
        while (true) {
            try {
                byteArrayOutputStream.reset();
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
                send(byteArrayOutputStream.toByteArray());

                ByteBuffer shortBuffer = ByteBuffer.allocate(2);
                int r = channel.read(shortBuffer);
                if (r == -1) {
                    throw new IOException();
                }
                shortBuffer.flip();
                short len = shortBuffer.getShort();
                ByteBuffer buffer = ByteBuffer.allocate(len);
                r = channel.read(buffer);
                if (r == -1) {
                    throw new IOException();
                }
                buffer.flip();
                return StandardCharsets.UTF_8.decode(buffer).toString();
            } catch (IOException e) {
                connect();
            }
        }
    }
// основная функция взаимодействия (считывание команд и тд)
    public void read(Scanner scanner, boolean fromScript) throws IOException {
        boolean exitStatus = false;
        Dragon dragon = null;
        boolean wasEnter = false;
        while (!exitStatus) {
            afterConnecting = false;
            String[] text = null;
            Command.CommandType type = null;
            if (!fromScript && !wasEnter) System.out.println("Enter command");
            if (scanner.hasNext()) {
                String textline = scanner.nextLine();
                if (textline.trim().isEmpty()) {wasEnter = true; continue;}
                text = textline.replaceAll("^\\s+", "").split(" ", 2);
            } else {
                objectOutputStream.writeObject(new Message());
                objectOutputStream.flush();
                System.exit(0);
            }
            String word = text[0];
            String argument;
            try {
                argument = text[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                argument = null;
            }
            boolean normalCommand = true;
            switch (word) {
                case ("help"):
                    type = Command.CommandType.help;
                    break;
                case ("info"):
                    type = Command.CommandType.info;
                    break;
                case ("show"):
                    type = Command.CommandType.show;
                    break;
                case ("clear"):
                    type = Command.CommandType.clear;
                    break;
                case ("exit"):
                    exitStatus = true;
                    type = Command.CommandType.exit;
                    break;
                case ("print_field_descending_cave"):
                    type = Command.CommandType.print_field_descending_cave;
                    break;
                case ("add"):
                    type = Command.CommandType.add;
                    dragon = inputDragonFromConsole();
                    break;
                case ("add_if_max"):
                    type = Command.CommandType.add_if_max;
                    dragon = inputDragonFromConsole();
                    break;
                case ("add_if_min"):
                    type = Command.CommandType.add_if_min;
                    dragon = inputDragonFromConsole();
                    break;
                case ("remove_lower"):
                    type = Command.CommandType.remove_lower;
                    break;
                case ("update"):
                    type = Command.CommandType.update;
                    dragon = inputDragonFromConsole();
                    break;
                case ("remove_by_id"):
                    type = Command.CommandType.remove_by_id;
                    break;
                case ("execute_script"):
                    type = Command.CommandType.execute_script;
                    if (fromScript) {
                        System.out.println("Danger of recursion, skipping command");
                    }
                    else {
                        execute_script(argument);
                    }
                    break;
                case ("filter_starts_with_name"):
                    type = Command.CommandType.filter_starts_with_name;
                    break;
                case ("filter_less_than_age"):
                    type = Command.CommandType.filter_less_than_age;
                    break;
                default:
                    System.out.println("Invalid command. Try 'help' to see the list of commands");
                    normalCommand = false;
                    break;
            }
            try {
                if (normalCommand) {
                    Message message = new Message(dragon, type, argument, fromScript);
                    if (!(type == Command.CommandType.execute_script)) {
                        String response = getResponse(message);
                        System.out.println(response);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("I cant send message");
            }
            byteArrayOutputStream.reset();
        }
    }

    // всякие функции
    public void execute_script(String argument) throws IOException {
        System.out.println("argument : " + argument);
        try {
            File script = new File(argument);
            read(new Scanner(script), true);
        } catch (IOException e) {
            System.out.println("Script not found :(");
        }
    }
    public Dragon inputDragonFromConsole() throws NumberFormatException {
        Scanner consoleScanner = new Scanner(System.in);
        int exceptionStatus = 0; // для проверки на исключения парсинга и несоответсвия правилам
        System.out.println("Enter name");
        String name = "";
        while (exceptionStatus == 0){
            if (consoleScanner.hasNext()){
                name = consoleScanner.nextLine();
                if ((name != null) && (name.length() > 0)) {
                    exceptionStatus = 1;
                } else {
                    System.out.println("field can't be empty. Try again");
                }
            } else {
                System.exit(0);
            }
        }
        System.out.println("Enter x coordinate (long)");
        long x = inputLongField();
        System.out.println("Enter y coordinate (Double, not NULL ^_^ )");
        Double y = inputDoubleField();
        Coordinates coordinates = new Coordinates(x, y);
        System.out.println("Enter age (Long, positive)");
        Long age = inputPositiveLongField();
        System.out.println("Enter description (String)");
        String description = null;
        if (consoleScanner.hasNext()){
            description = consoleScanner.nextLine();
        } else {
            System.exit(0);
        }
        System.out.println("Enter wingspan (Double, positive)");
        Double wingspan = inputPositiveDoubleField();
        System.out.println("Enter type(UNDERGROUND, AIR, FIRE)");
        String dragonType = null;
        if (consoleScanner.hasNext()){
            dragonType = consoleScanner.nextLine();
        } else {
            System.exit(0);
        }
        DragonType type = inputDragonTypeField(dragonType);
        System.out.println("Enter depth of cave (double, positive)");
        double depth = inputPositiveDoubleField();
        System.out.println("Enter number Of Treasures in cave (Double, positive)");
        Double number = inputPositiveDoubleField();
        DragonCave cave = new DragonCave((int)depth, number);
        Dragon inputDragon = new Dragon(null, name, coordinates, null, age, description, wingspan, type, cave);
        return inputDragon;
    }
    public DragonType inputDragonTypeField(String type) {
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        DragonType dragonType = DragonType.AIR;
        while (exceptionStatus == 0){
            switch (type){
                case ("UNDERGROUND"):
                    dragonType = DragonType.UNDERGROUND;
                    exceptionStatus = 1;
                    break;
                case ("AIR"):
                    dragonType = DragonType.AIR;
                    exceptionStatus = 1;
                    break;
                case ("FIRE"):
                    dragonType = DragonType.FIRE;
                    exceptionStatus = 1;
                    break;
                default:
                    System.out.println("Invalid Dragon type. Try again");
                    type = inputScanner.nextLine();
                    break;
            }
        }
        return dragonType;
    }
    public Long inputLongField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus == 0){
                try {
                    x = Long.parseLong(inputScanner.nextLine());
                    exceptionStatus = 1;
                } catch (NumberFormatException e) {
                    System.out.println("Input must be Long. Try again");
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    public Long inputPositiveLongField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Long.parseLong(inputScanner.nextLine());
                    if (x <= 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be long. Try again.");
                        break;
                    case (2):
                        System.out.println("Input cant be <= 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }

        return x;
    }
    public Double inputDoubleField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus == 0){
                try {
                    x = Double.parseDouble(inputScanner.nextLine());
                    exceptionStatus = 1;
                } catch (NumberFormatException e) {
                    System.out.println("Input must be Double. Try again.");
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    public Double inputPositiveDoubleField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Double.parseDouble(inputScanner.nextLine());
                    if (x <= 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be Double. Try again.");
                        break;
                    case (2):
                        System.out.println("Input cant be < 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
}

