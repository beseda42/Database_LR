import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Database implements Serializable {
    private HashMap<Integer, Order> ordersMap;
    private HashMap<String, List<Order>> addressIndex;
    private HashMap<String, List<Order>> dateIndex;
    private HashMap<Double, List<Order>> weightIndex;
    private String filePath;

    public Database() {
        this.ordersMap = new HashMap<>();
        this.addressIndex = new HashMap<>();
        this.dateIndex = new HashMap<>();
        this.weightIndex = new HashMap<>();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean addRecord(Order order) {
        if (ordersMap.containsKey(order.getId())) {
            return false;
        }
        ordersMap.put(order.getId(), order);
        addressIndex.computeIfAbsent(order.getAddress(), k -> new ArrayList<>()).add(order);
        dateIndex.computeIfAbsent(((order.getDate()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))), k -> new ArrayList<>()).add(order);
        weightIndex.computeIfAbsent(order.getWeight(), k -> new ArrayList<>()).add(order);
        return true;
    }

    public void removeRecord(int id) {
        Order order = ordersMap.remove(id);
        if (order != null) {
            List<Order> ordersAtAddress = addressIndex.get(order.getAddress());
            if (ordersAtAddress != null) {
                ordersAtAddress.remove(order);
                if (ordersAtAddress.isEmpty()) {
                    addressIndex.remove(order.getAddress());
                }
            }
            List<Order> ordersAtDate = dateIndex.get(order.getDate());
            if (ordersAtDate != null) {
                ordersAtDate.remove(order);
                if (ordersAtDate.isEmpty()) {
                    dateIndex.remove(order.getDate());
                }
            }
            List<Order> ordersAtWeight = weightIndex.get(order.getWeight());
            if (ordersAtWeight != null) {
                ordersAtWeight.remove(order);
                if (ordersAtWeight.isEmpty()) {
                    weightIndex.remove(order.getWeight());
                }
            }
        }
    }

    public void removeRecordsByAddress(String address) {
        List<Order> ordersToRemove = addressIndex.get(address);
        if (ordersToRemove != null) {
            for (Order order : new ArrayList<>(ordersToRemove)) {
                removeRecord(order.getId());
            }
        }
    }

    public void removeRecordsByDate(String date) {
        List<Order> ordersToRemove = dateIndex.get(date);
        if (ordersToRemove != null) {
            for (Order order : new ArrayList<>(ordersToRemove)) {
                removeRecord(order.getId());
            }
        }
    }

    public void removeRecordsByWeight(double weight) {
        List<Order> ordersToRemove = weightIndex.get(weight);
        if (ordersToRemove != null) {
            for (Order order : new ArrayList<>(ordersToRemove)) {
                removeRecord(order.getId());
            }
        }
    }

    public Order findById(int id) {
        return ordersMap.get(id);
    }

    public List<Order> findByAddress(String address) {
        return addressIndex.getOrDefault(address, new ArrayList<>());
    }

    public List<Order> findByDate(String date) {
        return dateIndex.getOrDefault(date, new ArrayList<>());
    }

    public List<Order> findByWeight(double weight) {
        return weightIndex.getOrDefault(weight, new ArrayList<>());
    }

    public boolean editRecord(int id, String newAddress, LocalDate newDate, double newWeight) {
        Order order = ordersMap.get(id);
        if (order == null) {
            return false;
        }
        removeRecord(id);
        order.setAddress(newAddress);
        order.setDate(newDate);
        order.setWeight(newWeight);
        addRecord(order);
        return true;
    }

    public void backupDatabase() throws IOException {
        String backupFilePath = filePath + ".bak";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(backupFilePath))) {
            oos.writeObject(new ArrayList<>(ordersMap.values()));
        }
    }

    public void restoreDatabase() throws IOException, ClassNotFoundException {
        String backupFilePath = filePath + ".bak";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(backupFilePath))) {
            List<Order> loadedOrders = (List<Order>) ois.readObject();
            clear();
            for (Order order : loadedOrders) {
                addRecord(order);
            }
        }
    }

    public void exportToXlsx(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Address");
        headerRow.createCell(2).setCellValue("Date");
        headerRow.createCell(3).setCellValue("Weight");
        int rowNum = 1;
        for (Order order : ordersMap.values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getAddress());
            row.createCell(2).setCellValue(order.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            row.createCell(3).setCellValue(order.getWeight());
        }
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }

    public void clear() {
        ordersMap.clear();
        addressIndex.clear();
        dateIndex.clear();
        weightIndex.clear();
    }

    public void saveDatabase() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(new ArrayList<>(ordersMap.values()));
        }
    }

    public void loadDatabase() throws IOException, ClassNotFoundException {
        clear();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            List<Order> loadedOrders = (List<Order>) ois.readObject();
            for (Order order : loadedOrders) {
                addRecord(order);
            }
        }
    }

    public List<Order> getRecords() {
        return new ArrayList<>(ordersMap.values());
    }
}
