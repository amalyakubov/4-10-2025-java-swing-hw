package tutorial;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class Country {
    private String name;
    private String code;
    private BufferedImage flag;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public BufferedImage getFlag() {
        return flag;
    }

    public URI getWiki() {
        return URI.create("https://pl.wikipedia.org/wiki/" + name.replace(" ", "_"));
    }

    Country(String name, String code) {
        this.name = name;
        this.code = code;
    }
}

class CountryTableModel extends AbstractTableModel {
    private String[] columnNames = { "Flaga", "Państwo", "Link do artykułu" };
    private ArrayList<Country> data = new ArrayList<Country>();

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return data.get(rowIndex).getFlag();
            case 1:
                return data.get(rowIndex).getName();
            case 2:
                return data.get(rowIndex).getWiki();
            default:
                break;
        }
        return columnIndex;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    public void addRow(Country c) {
        data.add(c);
    }
}

public class Tutorial {

    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tutorial().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Countries App - Tabela");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout(8, 8));

        // === SECTION 1: Podstawowe przyciski i pola tekstowe ===
        JPanel basicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // JButton z ActionListener
        JButton button = new JButton("Kliknij mnie");
        button.addActionListener(_ -> JOptionPane.showMessageDialog(frame, "Kliknięto przycisk!"));
        basicPanel.add(button);

        // JTextField z KeyListener
        JTextField textField = new JTextField(15);
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JOptionPane.showMessageDialog(frame, "Wpisano: " + textField.getText());
                }
            }
        });
        basicPanel.add(new JLabel("Pole tekstowe:"));
        basicPanel.add(textField);

        frame.add(basicPanel, BorderLayout.SOUTH);

        ArrayList<Country> countries = new ArrayList<Country>();

        HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://flagcdn.com/pl/codes.json"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .build();

        try {
            HttpResponse<String> response = client.sendAsync(request, BodyHandlers.ofString())
                    .join();

            String responseBody = response.body();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(responseBody,
                    new TypeReference<Map<String, String>>() {
                    });

            for (var country : map.entrySet()) {
                System.out.println(country);
                countries.add(new Country(country.getValue(), country.getKey()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // === SECTION 5: JTable ===

        CountryTableModel model = new CountryTableModel();
        JTable table = new JTable(model);
        for (Country c : countries) {
            model.addRow(c);
        }
        JScrollPane tableScroll = new JScrollPane(table);
        frame.add(tableScroll, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
