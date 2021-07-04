package fr.miage.fsgbd;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 */
public class GUI extends JFrame implements ActionListener {

    TestInteger testInt = new TestInteger();
    BTreePlus<Integer> bInt;
    private JButton buttonClean, buttonRemove, buttonLoad, buttonSave, buttonAddMany, buttonAddItem, buttonRefresh, buttonLoadFile, buttonCreateRdmFile;
    private JTextField txtNbreItem, txtNbreSpecificItem, txtU, txtFile, removeSpecific;
    private JComboBox comboColumns;
    private final JTree tree = new JTree();

    private String[] csv_data_columns;
    private ArrayList<String[]> csv_data = new ArrayList<String[]>();
    BTreePlus<HashMap<String, Integer>> bcsv;

    public GUI() {
        super();
        build();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonLoad || e.getSource() == buttonClean || e.getSource() == buttonSave || e.getSource() == buttonRefresh) {
            if (e.getSource() == buttonLoad) {
                BDeserializer<Integer> load = new BDeserializer<Integer>();
                bInt = load.getArbre(txtFile.getText());
                if (bInt == null)
                    System.out.println("Echec du chargement.");

            } else if (e.getSource() == buttonClean) {
                if (Integer.parseInt(txtU.getText()) < 2)
                    System.out.println("Impossible de cr?er un arbre dont le nombre de cl?s est inf?rieur ? 2.");
                else
                    bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);
            } else if (e.getSource() == buttonSave) {
                BSerializer<Integer> save = new BSerializer<Integer>(bInt, txtFile.getText());
            } else if (e.getSource() == buttonRefresh) {
                tree.updateUI();
            }
        } else {
            if (bInt == null)
                bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);

            if (e.getSource() == buttonAddMany) {
                for (int i = 0; i < Integer.parseInt(txtNbreItem.getText()); i++) {
                    int valeur = (int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
                    boolean done = bInt.addValeur(valeur);

					/*
					  On pourrait forcer l'ajout mais on risque alors de tomber dans une boucle infinie sans "r?gle" faisant sens pour en sortir

					while (!done)
					{
						valeur =(int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
						done = bInt.addValeur(valeur);
					}
					 */
                }

            } else if (e.getSource() == buttonAddItem) {
                if (!bInt.addValeur(Integer.parseInt(txtNbreSpecificItem.getText())))
                    System.out.println("Tentative d'ajout d'une valeur existante : " + txtNbreSpecificItem.getText());
                txtNbreSpecificItem.setText(
                        String.valueOf(
                                Integer.parseInt(txtNbreSpecificItem.getText()) + 2
                        )
                );

            } else if (e.getSource() == buttonRemove) {
                bInt.removeValeur(Integer.parseInt(removeSpecific.getText()));
            }
        }

        if (e.getSource() == buttonCreateRdmFile) {
            try {
                this.generateRandomFile("fileRdm.csv");
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        }

        if (e.getSource() == buttonLoadFile) {
            try {
                this.readFileCSV("fileRdm.csv");
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        }

        if (e.getSource() == comboColumns) {
            // une colonne a été choisie, il faut remplir le b-tree avec les datas de la colonne choisie !
            String name_column = comboColumns.getSelectedItem().toString();
            int index_column = -1;
            for (int i = 0; i < csv_data_columns.length; i++) {
                if (csv_data_columns[i].equals(name_column)) {
                    index_column = i;
                    break;
                }
            }

            if (index_column >= 0 && index_column < csv_data_columns.length) {
                bcsv = new BTreePlus<HashMap<String, Integer>>(Integer.parseInt(txtU.getText()), new TestPointeur());
                for (int i = 0; i < csv_data.size(); i++) {
                    HashMap<String, Integer> v = new HashMap<String, Integer> ();
                    v.put(csv_data.get(i)[index_column], i+ 1);
                    boolean done = bcsv.addValeur(v);
                }
            }

            tree.setModel(new DefaultTreeModel(bcsv.bArbreToJTree()));
            for (int i = 0; i < tree.getRowCount(); i++)
                tree.expandRow(i);
            tree.updateUI();

        } else {
            tree.setModel(new DefaultTreeModel(bInt.bArbreToJTree()));
            for (int i = 0; i < tree.getRowCount(); i++)
                tree.expandRow(i);

            tree.updateUI();
        }
    }


    private String rdmString (int length) {
        if (length <= 0) {
            length = new Random().nextInt(10) + 5;
        }

        String chaine = "azertyuiopqsdfghjklmwxcvbn ";
        String res = "";
        for (int i=0; i<length; i++) {
            res += chaine.charAt(new Random().nextInt(chaine.length()));
        }
        return res;
    }

    private String rdmNum (int length) {
        String res = "";
        for (int i=0; i<length; i++) {
            res += new Random().nextInt(10);
        }
        return res;
    }

    private String rdmSexe () {
        if (new Random().nextInt(2) == 0) {
            return "M";
        }
        return "F";
    }

    private void generateRandomFile (String path) throws FileNotFoundException {
        csv_data.add(new String[] {
                "Num Securite Sociale", "Nom", "Prenom", "Sexe", "DateNaissance", "Adresse", "CP", "Ville"
        });
        for (int i=0; i<10000; i++) {
            csv_data.add(new String[] {
                this.rdmNum(15), this.rdmString(0),
                    this.rdmString(0), this.rdmSexe(),
                    this.rdmNum(8), this.rdmString(40),
                    this.rdmNum(5), this.rdmString(0)
            });
        }

        File csvOutputFile = new File(path);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            csv_data.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }

        System.out.println("CSV CREE");
    }

    private String convertToCSV(String[] data) {
        return Stream.of(data)
                .collect(Collectors.joining(","));
    }

    private void readFileCSV (String path) throws FileNotFoundException {
        String line = "\n";
        String splitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            int acc_first_line = 0;
            while ((line = br.readLine()) != null) {
                if (acc_first_line != 0) {  // la première ligne correspond aux noms des colonnes
                    csv_data.add(line.split(splitBy));
                } else {
                    acc_first_line = 1;
                    // on rempli le combobox avec les noms des colonnes
                    csv_data_columns = line.split(splitBy);
                    for (String name_column : csv_data_columns) {
                        comboColumns.addItem(name_column);
                    }
                }
            }

            System.out.println(csv_data.size());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void build() {
        setTitle("Indexation - B Arbre");
        setSize(760, 760);
        setLocationRelativeTo(this);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContentPane());
    }

    private JPanel buildContentPane() {
        GridBagLayout gLayGlob = new GridBagLayout();

        JPanel pane1 = new JPanel();
        pane1.setLayout(gLayGlob);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 2, 0);

        JLabel labelU = new JLabel("Nombre max de cl?s par noeud (2m): ");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        pane1.add(labelU, c);

        txtU = new JTextField("4", 7);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 2;
        pane1.add(txtU, c);

        JLabel labelBetween = new JLabel("Nombre de clefs ? ajouter:");
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(labelBetween, c);

        txtNbreItem = new JTextField("10000", 7);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(txtNbreItem, c);


        buttonAddMany = new JButton("Ajouter n ?l?ments al?atoires ? l'arbre");
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddMany, c);

        JLabel labelSpecific = new JLabel("Ajouter une valeur sp?cifique:");
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelSpecific, c);

        txtNbreSpecificItem = new JTextField("50", 7);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtNbreSpecificItem, c);

        buttonAddItem = new JButton("Ajouter l'?l?ment");
        c.gridx = 2;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddItem, c);

        JLabel labelRemoveSpecific = new JLabel("Retirer une valeur sp?cifique:");
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelRemoveSpecific, c);

        removeSpecific = new JTextField("54", 7);
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(removeSpecific, c);

        buttonRemove = new JButton("Supprimer l'?l?ment n de l'arbre");
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRemove, c);

        JLabel labelFilename = new JLabel("Nom de fichier : ");
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelFilename, c);

        txtFile = new JTextField("arbre.abr", 7);
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtFile, c);

        buttonSave = new JButton("Sauver l'arbre");
        c.gridx = 2;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonSave, c);

        buttonLoad = new JButton("Charger l'arbre");
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonLoad, c);

        buttonClean = new JButton("Reset");
        c.gridx = 2;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonClean, c);

        buttonRefresh = new JButton("Refresh");
        c.gridx = 2;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRefresh, c);

        buttonCreateRdmFile = new JButton ("Create CSV");
        c.gridx = 2;
        c.gridy = 8;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonCreateRdmFile, c);

        buttonLoadFile = new JButton ("Load CSV");
        c.gridx = 3;
        c.gridy = 8;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonLoadFile, c);

        comboColumns = new JComboBox ();
        c.gridx = 2;
        c.gridy = 9;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(comboColumns, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 400;       //reset to default
        c.weighty = 1.0;   //request any extra vertical space
        c.gridwidth = 4;   //2 columns wide
        c.gridx = 0;
        c.gridy = 10;

        JScrollPane scrollPane = new JScrollPane(tree);
        pane1.add(scrollPane, c);

        tree.setModel(new DefaultTreeModel(null));
        tree.updateUI();

        txtNbreItem.addActionListener(this);
        buttonAddItem.addActionListener(this);
        buttonAddMany.addActionListener(this);
        buttonLoad.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonRemove.addActionListener(this);
        buttonClean.addActionListener(this);
        buttonRefresh.addActionListener(this);
        buttonCreateRdmFile.addActionListener(this);
        buttonLoadFile.addActionListener(this);
        comboColumns.addActionListener(this);

        return pane1;
    }
}

