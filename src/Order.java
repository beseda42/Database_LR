import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Order implements Serializable {
    private int id;
    private String address;
    private LocalDate date;
    private double weight;

    public Order(int id, String address, LocalDate age, double salary) {
        this.id = id;
        this.address = address;
        this.date = age;
        this.weight = salary;
    }
    
    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return id + ": Address: " + address + ", Date: " + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ", Weight: " + weight;
    }
}
