package com.uddernetworks.mspaint.ocr;

import com.uddernetworks.mspaint.main.ImageUtil;
import com.uddernetworks.mspaint.main.Letter;
import com.uddernetworks.mspaint.main.MainGUI;
import com.uddernetworks.mspaint.main.Probe;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageCompare {

    private Map<String, BufferedImage> images;
    private int totalIterations;
    private AtomicInteger currentIterations;
    private MainGUI mainGUI;
    private final AtomicBoolean loading = new AtomicBoolean(true);

    public LetterGrid getText(File inputImage, File objectFile, MainGUI mainGUI, Map<String, BufferedImage> images, boolean useProbe, boolean readFromFile, boolean saveCaches) {
        this.mainGUI = mainGUI;
        this.images = images;

        if (readFromFile) {
            System.out.println("Image not changed since file changed, so using file...");
        } else {
            System.out.println("Image has changed since last write to data file, reading image...");
        }

        try {
            BufferedImage image = ImageUtil.blackAndWhite(ImageIO.read(inputImage));

            if (objectFile != null && !objectFile.exists() && !objectFile.isFile()) {
                try {
                    readFromFile = objectFile.createNewFile();
                } catch (IOException ignored) {
                    readFromFile = false;
                }
            }

            LetterGrid grid;

            if (!readFromFile) {
                grid = new LetterGrid(image.getWidth(), image.getHeight());

                if (mainGUI != null) mainGUI.setStatusText("Probing...");

                AtomicInteger waitingFor = new AtomicInteger(images.keySet().size());

                Probe probe = new Probe(image, images.get("p"));

                int startY = (useProbe) ? probe.sendInProbe() : 0;
                int iterByY = (useProbe) ? 25 : 1;

                if (mainGUI != null) mainGUI.setStatusText("Scanning image " + inputImage.getName() + "...");

                System.out.println("Total images: " + images.keySet().size());

                int imageHeight = image.getHeight();

                totalIterations = 0;
                currentIterations = new AtomicInteger(0);

                for (String identifier : images.keySet()) {
                    int diffHeight = imageHeight - images.get(identifier).getHeight() - startY;

                    totalIterations += diffHeight / iterByY;
                }

                Thread loadingBarThread = new Thread(() -> {
                    while (loading.get()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (mainGUI != null) mainGUI.updateLoading(currentIterations.get(), totalIterations);
                    }
                });

                loadingBarThread.start();

                ExecutorService executor = Executors.newFixedThreadPool(10);

                for (String identifier : images.keySet()) {
                    executor.execute(() -> {
                        try {
                            searchFor(grid, identifier, image, startY, iterByY);
                            waitingFor.getAndDecrement();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    int count = executor.shutdownNow().size();
                    System.out.println("Warning: had to terminate " + count + " tasks");
                }

                while (true) {
                    if (waitingFor.get() == 0) {
                        break;
                    } else {
                        System.out.println("Waiting...");
                    }

                    Thread.sleep(1000);
                }

                if (saveCaches) {
                    if (mainGUI != null) mainGUI.setStatusText("Saving to cache file...");

                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(objectFile))) {
                        oos.writeObject(grid);
                    }
                }

                loading.set(false);
                loadingBarThread.join();
            } else {
                try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(objectFile))) {
                    grid = (LetterGrid) oi.readObject();
                }
            }

            if (mainGUI != null) {
                mainGUI.setStatusText("Compacting and processing collected data...");

                mainGUI.setIndeterminate(true);
            }

            grid.compact();

            if (mainGUI != null) mainGUI.setIndeterminate(false);

            return grid;

        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void searchFor(LetterGrid grid, String identifier, BufferedImage image, int startY, int iterYBy) {
        BufferedImage searching = images.get(identifier);

        int currentX = 0;
        int currentY = startY;

        while (currentY + searching.getHeight() <= image.getHeight()) {
            currentX = 0;
            while (currentX + searching.getWidth() <= image.getWidth()) {
                BufferedImage subImage = image.getSubimage(currentX, currentY, searching.getWidth(), searching.getHeight());
                if (identifier.equals("\'")) {
                    int topRightX = currentX + searching.getWidth();

                    boolean matches = true;

                    for (int i = 0; i < 3; i++) {
                        if (isInBounds(image, topRightX + i, currentY)) {
                            Color color = new Color(image.getRGB(topRightX + i, currentY));
                            if (!(color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
                                matches = false;
                            }
                        } else {
                            matches = false;
                        }
                    }

                    for (int i = 0; i < 2; i++) {
                        if (isInBounds(image, topRightX + i, currentY + 1)) {
                            Color color = new Color(image.getRGB(topRightX + i, currentY + 1));
                            if (!(color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
                                matches = false;
                            }
                        } else {
                            matches = false;
                        }
                    }

                    if (matches) {
                        currentX++;
                        continue;
                    }
                } else if (identifier.equals(".")) {
                    int bottomLeftX = currentX;
                    int bottomLeftY = currentY + searching.getHeight();

                    boolean matches = true;

                    if (isInBounds(image, bottomLeftX, bottomLeftY + 2)) {
                        Color color = new Color(image.getRGB(bottomLeftX, bottomLeftY + 2));
                        if ((color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
                            matches = false;
                        }
                    } else {
                        matches = false;
                    }

                    bottomLeftX++;

                    for (int i = 0; i < 3; i++) {
                        if (isInBounds(image, bottomLeftX + i, bottomLeftY + 2)) {
                            Color color = new Color(image.getRGB(bottomLeftX + i, bottomLeftY + 2));
                            if (!(color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
                                matches = false;
                            }
                        } else {
                            matches = false;
                        }
                    }

                    bottomLeftX = currentX;

                    if (matches) {
                        currentX++;
                        continue;
                    }

                    matches = true;

                    for (int i = 0; i < 4; i++) {
                        if (isInBounds(image, bottomLeftX + i, bottomLeftY + 2)) {
                            Color color = new Color(image.getRGB(bottomLeftX + i, bottomLeftY + 2));
                            if ((color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
                                matches = false;
                            }
                        } else {
                            matches = false;
                        }
                    }

                    if (!matches) {
                        currentX++;
                        continue;
                    }

                } else if (identifier.equals("f") || identifier.equals("t")) {
                    if (ImageUtil.equals(subImage, searching, Arrays.asList(new Point(0, 0), new Point(0, 1)))) {
                        grid.addLetter(new Letter(identifier, searching.getWidth(), searching.getHeight(), currentX, currentY));
                    }
                    currentX++;

                    continue;
                }

                if (ImageUtil.equals(subImage, searching)) {
                    grid.addLetter(new Letter(identifier, searching.getWidth(), searching.getHeight(), currentX, currentY));
                }
                currentX++;
            }

            currentIterations.incrementAndGet();
            currentY += iterYBy;
        }

    }

    private boolean isInBounds(BufferedImage image, int x, int y) {
        return x > 0 && y > 0 && image.getWidth() > x && image.getHeight() > y;
    }

}
