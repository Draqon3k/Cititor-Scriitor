package org.example.cititorscriitor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class BookSimulation {
    @FXML private Button startButton;  // This is connected to the button in the FXML
    @FXML private Rectangle bookWriter1, bookWriter2;
    @FXML private Rectangle bookShelf1, bookShelf2, bookShelf3, bookShelf4;
    @FXML private Rectangle bookReader1, bookReader2;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final LinkedBlockingQueue<Rectangle> shelf = new LinkedBlockingQueue<>(4);

    public void initialize() {
        bookWriter1.setVisible(false);
        bookWriter2.setVisible(false);
        bookShelf1.setVisible(false);
        bookShelf2.setVisible(false);
        bookShelf3.setVisible(false);
        bookShelf4.setVisible(false);
        bookReader1.setVisible(false);
        bookReader2.setVisible(false);
    }


    @FXML
    private void startSimulation() throws InterruptedException {
        // Scriem cărțile pe rafturi
        executorService.execute(() -> writeBook(bookWriter1, bookShelf1));
        executorService.execute(() -> writeBook(bookWriter2, bookShelf2));
        executorService.execute(() -> writeBook(bookWriter1, bookShelf3));
        executorService.execute(() -> writeBook(bookWriter2, bookShelf4));

        // Creăm o coadă pentru rafturi
        LinkedBlockingQueue<Rectangle> shelfQueue = new LinkedBlockingQueue<>();
        shelfQueue.put(bookShelf1);
        shelfQueue.put(bookShelf2);
        shelfQueue.put(bookShelf3);
        shelfQueue.put(bookShelf4);

        // Creăm liste pentru cărțile citite de fiecare cititor
        Set<Rectangle> readBooksReader1 = new HashSet<>();
        Set<Rectangle> readBooksReader2 = new HashSet<>();

        // Cititorii citesc cărțile
        executorService.execute(() -> readBook(bookReader1, readBooksReader1, shelfQueue));  // Cititorul 1
        executorService.execute(() -> readBook(bookReader2, readBooksReader2, shelfQueue));  // Cititorul 2
    }


    private void writeBook(Rectangle writer, Rectangle shelfSlot) {
        try {
            Thread.sleep(2000);  // Simulating writing time
        } catch (InterruptedException ignored) {}
        Platform.runLater(() -> {
            writer.setVisible(false);
            shelfSlot.setFill(Color.BLUE);
            shelfSlot.setVisible(true);
            shelf.add(shelfSlot);
        });
    }

    private void readBook(Rectangle reader, Set<Rectangle> readBooks, LinkedBlockingQueue<Rectangle> shelf) {
        try {
            while (readBooks.size() < 4) {  // Fiecare cititor citește 4 cărți
                Rectangle book = shelf.take();  // Ia o carte de pe raft
                Platform.runLater(() -> {
                    // Cititorul ia o carte pentru a o citi
                    book.setVisible(false);  // Ascunde cartea de pe raft
                    reader.setFill(Color.GREEN);  // Cititorul este evidențiat în verde
                    reader.setVisible(true);  // Arată cititorul cu cartea
                });
                Thread.sleep(3000);  // Citirea durează 3 secunde
                Platform.runLater(() -> {
                    reader.setVisible(false);  // Ascunde cititorul după citire
                    readBooks.add(book);  // Adaugă cartea în lista de cărți citite pentru acest cititor
                    // Cartea este complet citită
                    book.setVisible(true);  // Cartea devine vizibilă din nou pe raft
                    book.setFill(Color.GRAY);  // Resetăm culoarea cărții la gri
                    try {
                        shelf.put(book);  // Pune cartea înapoi pe raft pentru ca celălalt cititor să o poată citi
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (InterruptedException ignored) {}
    }


}
