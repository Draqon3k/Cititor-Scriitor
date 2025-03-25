package org.example.cititorscriitor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class BookSimulation {
    @FXML private Button startButton;
    @FXML private Rectangle bookWriter1, bookWriter2;
    @FXML private Rectangle bookShelf1, bookShelf2, bookShelf3, bookShelf4;
    @FXML private Rectangle bookReader1, bookReader2;
    @FXML private Line hand1,hand2;
    @FXML private Circle ochi1, ochi2, ochi3, ochi4;

    private Set<Rectangle> readBooksReader1 = new HashSet<>();
    private Set<Rectangle> readBooksReader2 = new HashSet<>();

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
    private void startSimulation() {
        executorService.execute(() -> writeBook(bookWriter1, bookShelf1, hand1));
        executorService.execute(() -> writeBook(bookWriter2, bookShelf2, hand2));
        executorService.execute(() -> writeBook(bookWriter1, bookShelf3, hand1));
        executorService.execute(() -> writeBook(bookWriter2, bookShelf4, hand2));

        executorService.execute(() -> readBook(bookReader1, readBooksReader1, ochi1, ochi2));
        executorService.execute(() -> readBook(bookReader2, readBooksReader2, ochi3, ochi4));
    }



    private void writeBook(Rectangle writer, Rectangle shelfSlot, Line hand) {
        Platform.runLater(() -> writer.setVisible(true));
        moveHand(hand);

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        Platform.runLater(() -> {
            writer.setVisible(false);
            shelfSlot.setFill(Color.BLUE);
            shelfSlot.setVisible(true);
            if (!shelf.offer(shelfSlot)) {
                System.out.println("Queue is full, could not add book.");
            }
        });
    }

    private void moveHand(Line hand) {
        Platform.runLater(() -> {
            double originalStartX = hand.getStartX();
            double targetStartX = originalStartX + 30;

            new Thread(() -> {
                try {
                    // Animație spre dreapta
                    for (int i = 0; i < 25; i++) {
                        double newX = hand.getStartX() + 1;
                        Platform.runLater(() -> hand.setStartX(newX));
                        Thread.sleep(25);
                    }

                    Thread.sleep(500);

                    // Revine la poziția inițială
                    for (int i = 0; i < 25; i++) {
                        double newX = hand.getStartX() - 1;
                        Platform.runLater(() -> hand.setStartX(newX));
                        Thread.sleep(25);
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        });
    }


    private void readBook(Rectangle reader, Set<Rectangle> readBooks, Circle eye1, Circle eye2) {
        try {
            while (readBooks.size() < 4 && !shelf.isEmpty()) {
                Rectangle book = shelf.take();
                Platform.runLater(() -> {
                    if (!readBooks.contains(book)) {
                        book.setVisible(false);
                        reader.setFill(Color.GREEN);
                        reader.setVisible(true);
                        moveEyes(eye1, eye2);
                    }
                });
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    reader.setVisible(false);
                    readBooks.add(book);
                    book.setVisible(true);
                    book.setFill(Color.GRAY);
                });
            }
        } catch (InterruptedException ignored) {}
    }


    private void moveEyes(Circle eye1, Circle eye2) {
        new Thread(() -> {
            try {
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 5; i++) {
                        double newX = eye1.getCenterX() + 1;
                        Platform.runLater(() -> {
                            eye1.setCenterX(newX);
                            eye2.setCenterX(newX);
                        });
                        Thread.sleep(100);
                    }
                    for (int i = 0; i < 5; i++) {
                        double newX = eye1.getCenterX() - 1;
                        Platform.runLater(() -> {
                            eye1.setCenterX(newX);
                            eye2.setCenterX(newX);
                        });
                        Thread.sleep(100);
                    }
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }


}
