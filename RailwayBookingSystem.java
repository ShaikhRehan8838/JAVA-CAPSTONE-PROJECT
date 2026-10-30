import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;

// ---------- Interface ----------
interface Ticket {
    String getType();
}

// ---------- General Ticket ----------
class General implements Ticket {
    public String getType() {
        return "General";
    }
}

// ---------- Sleeper Ticket ----------
class Sleeper implements Ticket {
    public String getType() {
        return "Sleeper";
    }
}

// ---------- Passenger ----------
class Passenger {
    int id;
    String name;
    int age;

    Passenger(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}

// ---------- Train ----------
class Train {
    int seats = 10;

    // synchronized = only one thread can book/cancel at a time
    synchronized boolean bookSeat() {
        if (seats > 0) {
            seats--;
            return true;
        }
        return false;
    }

    synchronized void cancelSeat() {
        if (seats < 10) {
            seats++;
        }
    }
}

// ---------- Main Window ----------
public class RailwayBookingSystem extends JFrame {

    JTextField idField, nameField, ageField;
    JComboBox<String> ticketBox;
    JTextArea output;
    JLabel seatLabel;

    Train train = new Train();

    // Collections
    ArrayList<Passenger> passengers = new ArrayList<Passenger>();
    HashMap<Integer, Passenger> records = new HashMap<Integer, Passenger>();

    // Colors
    Color blue = new Color(25, 70, 140);
    Color green = new Color(40, 160, 90);
    Color red = new Color(200, 60, 60);
    Color orange = new Color(230, 140, 30);
    Color light = new Color(240, 245, 255);

    RailwayBookingSystem() {

        setTitle("Railway Ticket Booking System");
        setSize(520, 620);
        setLocationRelativeTo(null); // open in center of screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(light);

        // ----- Top: Title + Seats -----
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setBackground(blue);
        top.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Railway Ticket Booking", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        seatLabel = new JLabel("", JLabel.CENTER);
        seatLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        seatLabel.setForeground(Color.YELLOW);

        top.add(title);
        top.add(seatLabel);
        add(top, BorderLayout.NORTH);

        // ----- Center: Form + Output -----
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(light);
        center.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new TitledBorder("Passenger Details"));

        idField = new JTextField();
        nameField = new JTextField();
        ageField = new JTextField();

        ticketBox = new JComboBox<String>();
        ticketBox.addItem("General");
        ticketBox.addItem("Sleeper");

        form.add(new JLabel("  Passenger ID"));
        form.add(idField);
        form.add(new JLabel("  Name"));
        form.add(nameField);
        form.add(new JLabel("  Age"));
        form.add(ageField);
        form.add(new JLabel("  Ticket Type"));
        form.add(ticketBox);

        output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(new TitledBorder("Messages"));

        center.add(form, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ----- Bottom: Buttons -----
        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 10));
        buttons.setBackground(light);
        buttons.setBorder(new EmptyBorder(0, 15, 15, 15));

        JButton bookButton = makeButton("Book Ticket", green);
        JButton cancelButton = makeButton("Cancel Ticket", red);
        JButton seatButton = makeButton("Check Seats", orange);

        buttons.add(bookButton);
        buttons.add(cancelButton);
        buttons.add(seatButton);
        add(buttons, BorderLayout.SOUTH);

        // ----- Button actions -----
        bookButton.addActionListener(e -> bookTicket());
        cancelButton.addActionListener(e -> cancelTicket());
        seatButton.addActionListener(e -> {
            showMessage("Available Seats: " + train.seats);
        });

        updateSeats();
        setVisible(true);
    }

    // Makes a colored button (so we don't repeat code)
    JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    // Shows a message in the output area
    void showMessage(String text) {
        output.append(text + "\n\n");
    }

    // Updates the seat label at the top
    void updateSeats() {
        seatLabel.setText("Available Seats: " + train.seats);
    }

    // Clears the text fields
    void clearFields() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
    }

    // ---------- Book Ticket ----------
    void bookTicket() {

        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());

            // Check duplicate ID
            if (records.containsKey(id)) {
                showMessage("ID already exists! Please use another ID.");
                return;
            }

            // Check invalid details
            if (name.equals("") || age <= 0) {
                showMessage("Invalid Passenger Details!");
                return;
            }

            // Make ticket object using the interface
            Ticket ticket;
            if (ticketBox.getSelectedItem().toString().equals("General")) {
                ticket = new General();
            } else {
                ticket = new Sleeper();
            }

            Passenger p = new Passenger(id, name, age);

            // Thread does the booking
            Thread t = new Thread(() -> {

                if (train.bookSeat()) {

                    passengers.add(p);
                    records.put(id, p);

                    String data = "Passenger ID: " + id
                            + "\nName: " + name
                            + "\nAge: " + age
                            + "\nTicket: " + ticket.getType()
                            + "\n------------------------";

                    showMessage("Ticket Booked Successfully!\n" + data);
                    updateSeats();
                    saveToFile(data);

                } else {
                    showMessage("No Seats Available!");
                }
            });
            t.start();

            clearFields();

        } catch (NumberFormatException e) {
            showMessage("Please enter valid ID and Age!");
        }
    }

    // ---------- Save to file ----------
    void saveToFile(String data) {
        try {
            FileWriter file = new FileWriter("bookings.txt", true);
            file.write(data + "\n");
            file.close();
        } catch (IOException ex) {
            showMessage("File Error!");
        }
    }

    // ---------- Cancel Ticket ----------
    void cancelTicket() {

        try {
            int id = Integer.parseInt(idField.getText().trim());

            if (records.containsKey(id)) {

                Passenger p = records.get(id);
                records.remove(id);
                passengers.remove(p); // also remove from the list

                train.cancelSeat();
                updateSeats();

                showMessage("Ticket Cancelled Successfully!\n"
                        + "Available Seats: " + train.seats);
                clearFields();

            } else {
                showMessage("Passenger ID not found!");
            }

        } catch (NumberFormatException e) {
            showMessage("Enter a valid Passenger ID!");
        }
    }

    // ---------- Main ----------
 nh   public static void main(String[] args) {
 
 new RailwayBookingSystem();
    }
}