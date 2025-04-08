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
import java.util.concurrent.locks.ReentrantLock;

public class BookSimulation {
    @FXML
    private Button startButton;
    @FXML
    private Rectangle bookWriter1, bookWriter2;
    @FXML
    private Rectangle bookShelf1, bookShelf2, bookShelf3, bookShelf4;
    @FXML
    private Rectangle bookReader1, bookReader2;
    @FXML
    private Line hand1, hand2;
    @FXML
    private Circle ochi1, ochi2, ochi3, ochi4;

    private Set<Rectangle> readBooksReader1 = new HashSet<>();
    private Set<Rectangle> readBooksReader2 = new HashSet<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final LinkedBlockingQueue<Rectangle> shelf = new LinkedBlockingQueue<>(4);
    private final ReentrantLock lock = new ReentrantLock(); // Blocaj pentru exclusivitate mutuală

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
        Rectangle[] shelfSlots = {bookShelf1, bookShelf2, bookShelf3, bookShelf4};

        // Scriitor 1 scrie 2 cărți
        Writer writer1 = new Writer(this, 1, 2, bookWriter1, hand1, shelfSlots, 0);
        executorService.execute(writer1);

        // Scriitor 2 scrie 2 cărți
        Writer writer2 = new Writer(this, 2, 2, bookWriter2, hand2, shelfSlots, 2);
        executorService.execute(writer2);

        // Cititori
        executorService.execute(() -> readBook(bookReader1, readBooksReader1, ochi1, ochi2));
        executorService.execute(() -> readBook(bookReader2, readBooksReader2, ochi3, ochi4));
    }

    private void moveHand(Line hand) {
        Platform.runLater(() -> {
            double originalStartX = hand.getStartX();
            double targetStartX = originalStartX + 30;

            new Thread(() -> {
                try {
                    for (int i = 0; i < 25; i++) {
                        final double newXForward = originalStartX + i + 1;
                        Platform.runLater(() -> hand.setStartX(newXForward));
                        Thread.sleep(25);
                    }
                    Thread.sleep(500);
                    for (int i = 0; i < 25; i++) {
                        final double newXBack = targetStartX - i - 1;
                        Platform.runLater(() -> hand.setStartX(newXBack));
                        Thread.sleep(25);
                    }
                } catch (InterruptedException ignored) {
                }
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
        } catch (InterruptedException ignored) {
        }
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
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    class Writer implements Runnable {
        private final BookSimulation simulation;
        private final int writerId;
        private final int booksToWrite;
        private final Rectangle writerRectangle;
        private final Line hand;
        private final Rectangle[] shelfSlots;
        private final int startIndex;

        public Writer(BookSimulation simulation, int writerId, int booksToWrite,
                      Rectangle writerRectangle, Line hand, Rectangle[] shelfSlots, int startIndex) {
            this.simulation = simulation;
            this.writerId = writerId;
            this.booksToWrite = booksToWrite;
            this.writerRectangle = writerRectangle;
            this.hand = hand;
            this.shelfSlots = shelfSlots;
            this.startIndex = startIndex;
        }

        @Override
        public void run() {
            for (int i = 0; i < booksToWrite; i++) {
                simulation.lock.lock(); // Asigură exclusivitate mutuală
                try {
                    Platform.runLater(() -> {
                        writerRectangle.setVisible(true);
                        writerRectangle.setFill(Color.RED);
                    });
                    simulation.moveHand(hand);
                    Thread.sleep(2000);
                    Rectangle shelfSlot = shelfSlots[startIndex + i];
                    Platform.runLater(() -> {
                        writerRectangle.setVisible(false);
                        shelfSlot.setFill(Color.BLUE);
                        shelfSlot.setVisible(true);
                    });
                    simulation.shelf.put(shelfSlot); // Așteaptă până când raftul are spațiu
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Writer " + writerId + " was interrupted.");
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    simulation.lock.unlock(); // Eliberează blocajul
                }
            }
            System.out.println("Writer " + writerId + " finished writing " + booksToWrite + " books.");
        }
    }
}