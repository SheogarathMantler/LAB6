package Client;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Dragon class describing elements of collection
 */
public class Dragon implements Serializable {
    private Integer id;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDateTime creationDate;
    private Long age;
    private String description;
    private Double wingspan;
    private DragonType type;
    private DragonCave cave;

    /**
     * Standard constructor
     * @param Id id of dragon, generated automatically
     * @param n name of dragon
     * @param coords coordinates of dragon
     * @param creationDate creation date of dragon (not birthday), generated automatically
     * @param age age of dragon
     * @param d description of dragon
     * @param w wingspan of dragon
     * @param t type of dragon
     * @param c cave where dragon lives
     */
    public Dragon(Integer Id, String n, Coordinates coords, java.time.LocalDateTime creationDate, Long age, String d,
                  Double w, DragonType t, DragonCave c ) {
        this.id = Id;
        if (Id == null) this.id = new Random().nextInt();
        if ((n != null) && (n.length() != 0)) {
            this.name = n;
        } else {
            throw new NumberFormatException();
        }
        if (coords != null) {
            this.coordinates = coords;
        } else {
            throw new NumberFormatException();
        }
        this.creationDate = creationDate;
        if (creationDate == null) this.creationDate = LocalDateTime.now();
        if ((age > 0)) {
            this.age = age;
        } else {
            throw new NumberFormatException();
        }
        this.description = d;
        if (w > 0) {
            this.wingspan = w;
        } else {
            throw new NumberFormatException();
        }
        this.type = t;
        this.cave = c;
    }
    public Integer getId() {return id;}
    public String getName() {return name;}
    public Coordinates getCoordinates() {return coordinates;}
    public Long getAge(){ return age; }
    public java.time.LocalDateTime getCreationDate() {return creationDate;}
    public String getDescription(){
        return description;
    }
    public Double getWingspan() {return wingspan;}
    public DragonType getType() {return type;}
    public DragonCave getCave(){ return cave; }

    /**
     * update of dragon, used when command 'update' called.
     * @param dragon the dragon that will change the dragon that has the method 'update()' called
     */
    public void update(Dragon dragon) {
        this.name = dragon.getName();
        this.coordinates = dragon.getCoordinates();
        this.age = dragon.getAge();
        this.creationDate = dragon.getCreationDate();
        this.description = dragon.getDescription();
        this.wingspan = dragon.getWingspan();
        this.type = dragon.getType();
        this.cave = dragon.getCave();
    }
    public void setId(Integer id){
        this.id = id;
    }
}

/**
 * class describing coordinates of dragon
 */
class Coordinates implements Serializable{
    private long x;
    private Double y; //Поле не может быть null
    public Coordinates(long x,Double y){
        this.x = x;
        if (y != null) {
            this.y = y;
        }
    }
    public long getX(){
        return x;
    }
    public Double getY(){
        return y;
    }
}
/**
 * class describing cave of dragon
 */
class DragonCave implements Serializable{
    private int depth;
    private Double numberOfTreasures; //Поле не может быть null, Значение поля должно быть больше 0
    public DragonCave(int d, Double n) {
        this.depth = d;
        if (n > 0) {
            this.numberOfTreasures = n;
        }
    }
    public int getDepth(){
        return depth;
    }
    public Double getNumberOfTreasures() {
        return numberOfTreasures;
    }
}
/**
 * class describing type of dragon
 */
enum DragonType implements Serializable{
    UNDERGROUND,
    AIR,
    FIRE;
}


