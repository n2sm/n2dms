/**
 * OpenKM, Open Document Management System (http://www.openkm.com)
 * Copyright (c) 2006-2013 Paco Avila & Josep Llort
 * 
 * No bytes were intentionally harmed during the development of this application.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.omr;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.filters.MedianFilter;
import net.sourceforge.jiu.geometry.ScaleReplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Aaditeshwar Seth
 */
public class ImageManipulation {
    private static Logger log = LoggerFactory
            .getLogger(ImageManipulation.class);

    Gray8Image grayimage, scaledImage;

    int height, width;

    ConcentricCircle topleftpos, bottomrightpos;

    int topleftX, topleftY, bottomrightX, bottomrightY;

    double currAngle, currDiag, realAngle, realDiag;

    int scaleFactor;

    int markLocations[], realMarkLocations[]; // x * 10000 + y

    int ascTemplateLocations[]; // same order as markLocations: x * 1000 + y

    int[][] ascTemplate;

    int nummarks, realNummarks;

    int numfields;

    Hashtable<Character, Field> fields;

    Field[] ascTemplateFields;

    public ImageManipulation(final Gray8Image grayimage) {
        this.grayimage = grayimage;

        height = grayimage.getHeight();
        width = grayimage.getWidth();

        // 1700 x 2339 --> 426 x 560
        log.debug("width = " + width + ": height = " + height);

        scaleFactor = width / 340; // 5 --> each mark becomes around 3 pixels wide
    }

    public void locateConcentricCircles() {
        final int[] topleft = new int[(height / 4 + 1) * (width / 4 + 1)];
        final int[] bottomright = new int[(height / 4 + 1) * (width / 4 + 1)];
        grayimage
                .getSamples(0, 0, 0, width / 4 + 1, height / 4 + 1, topleft, 0);
        grayimage.getSamples(0, width - width / 4 - 1, height - height / 4 - 1,
                width / 4 + 1, height / 4 + 1, bottomright, 0);

        final Gray8Image topleftimg = new MemoryGray8Image(width / 4 + 1,
                height / 4 + 1);
        topleftimg.putSamples(0, 0, 0, width / 4 + 1, height / 4 + 1, topleft,
                0);
        final Gray8Image bottomrightimg = new MemoryGray8Image(width / 4 + 1,
                height / 4 + 1);
        bottomrightimg.putSamples(0, 0, 0, width / 4 + 1, height / 4 + 1,
                bottomright, 0);

        topleftpos = new ConcentricCircle(topleftimg, width, height);
        topleftpos.process();
        bottomrightpos = new ConcentricCircle(bottomrightimg, width, height);
        bottomrightpos.process();

        // ImageUtil.saveImage(topleftpos.getImg(), "topleft.png");
        // ImageUtil.saveImage(bottomrightpos.getImg(), "bottomright.png");
        // ImageUtil.saveImage(topleftpos.getBestFit().getTemplate(), "template.png");

        bottomrightpos.getBestFit().setX(
                width - width / 4 - 1 + bottomrightpos.getBestFit().getX());
        bottomrightpos.getBestFit().setY(
                height - height / 4 - 1 + bottomrightpos.getBestFit().getY());

        topleftX = topleftpos.getBestFit().getX()
                + topleftpos.getBestFit().getTemplate().getWidth() / 2;
        topleftY = topleftpos.getBestFit().getY()
                + topleftpos.getBestFit().getTemplate().getHeight() / 2;
        ImageUtil.putMark(grayimage, topleftX, topleftY, true);

        bottomrightX = bottomrightpos.getBestFit().getX()
                + bottomrightpos.getBestFit().getTemplate().getWidth() / 2;
        bottomrightY = bottomrightpos.getBestFit().getY()
                + bottomrightpos.getBestFit().getTemplate().getHeight() / 2;
        ImageUtil.putMark(grayimage, bottomrightX, bottomrightY, true);

        log.debug(topleftX + ":" + topleftY + ":" + bottomrightX + ":"
                + bottomrightY);
        // ImageUtil.saveImage(grayimage, "grayimage.png");

        currAngle = Math.toDegrees(Math.atan2(bottomrightX - topleftX,
                bottomrightY - topleftY));
        currDiag = Math.sqrt(Math.pow(bottomrightY - topleftY, 2)
                + Math.pow(bottomrightX - topleftX, 2));
        log.debug("curr angle = " + currAngle);
        log.debug("curr diag = " + currDiag);
    }

    public void locateMarks() {
        rescale();

        // Gray8Image scaledImage = ImageUtil.readImage("scaled.png"); // XXX do not read from file
        final int scaledtopleftX = topleftX / scaleFactor;
        final int scaledtopleftY = topleftY / scaleFactor;
        final int scaledbottomrightX = bottomrightX / scaleFactor;
        final int scaledbottomrightY = bottomrightY / scaleFactor;

        log.debug("scaledtop: " + scaledtopleftX + ":" + scaledtopleftY);
        log.debug("scaledbot: " + scaledbottomrightX + ":" + scaledbottomrightY);

        int[] marks = new int[100 * 100 * 10];
        int nummarks = 0;
        for (int i = scaledtopleftX; i <= scaledbottomrightX; i++) {
            for (int j = scaledtopleftY + 20; j <= scaledbottomrightY - 20; j++) {
                final int val = (scaledImage.getSample(i, j)
                        + scaledImage.getSample(i - 1, j)
                        + scaledImage.getSample(i + 1, j)
                        + scaledImage.getSample(i, j - 1)
                        + scaledImage.getSample(i, j + 1)
                        + scaledImage.getSample(i - 1, j - 1)
                        + scaledImage.getSample(i + 1, j + 1)
                        + scaledImage.getSample(i + 1, j - 1) + scaledImage
                        .getSample(i - 1, j + 1)) / 9;
                if (val < 200) { // XXX
                    marks[nummarks++] = i * 1000 + j;
                }
            }
        }

        log.debug("nummarks = " + nummarks);

        final int[] dupmarks = new int[100 * 100 * 10];
        nummarks = filter(marks, dupmarks, nummarks);
        marks = dupmarks;

        log.debug("nummarks = " + nummarks);

        int t;
        final Gray8Image markedImage = (Gray8Image) grayimage.createCopy();
        final double approxXscale = width / ConcentricCircle.a4width; // 80.95 pixel/cm
        final double approxYscale = height / ConcentricCircle.a4height; // 78.75 pixel/cm
        final int markdispX = (int) (ConcentricCircle.markDiam * approxXscale / 4);
        final int markdispY = (int) (ConcentricCircle.markDiam * approxYscale / 4);

        for (int i = 0; i < nummarks; i++) {
            log.debug("{}", marks[i]);
            ImageUtil.putMark(scaledImage, marks[i] / 1000, marks[i] % 1000,
                    true);
            t = marks[i];
            marks[i] = (t / 1000 * scaleFactor + markdispX) * 10000 + t % 1000
                    * scaleFactor + markdispY; // XXX
            ImageUtil.putMark(markedImage, marks[i] / 10000, marks[i] % 10000,
                    false);
        }

        // ImageUtil.saveImage(scaledImage, "markedscaled.png");
        // ImageUtil.saveImage(markedImage, "marked.png");

        markLocations = marks;
        this.nummarks = nummarks;
    }

    public void writeAscTemplate(final String filename) {
        final int[] dupmarks = new int[nummarks];
        final int scaleFactor = this.scaleFactor * 3;
        final int width = (bottomrightX - topleftX) / scaleFactor;
        int height = (bottomrightY - topleftY) / scaleFactor;
        ascTemplate = new int[height][width];
        boolean linesOccupied[] = new boolean[height];
        ascTemplateLocations = new int[nummarks];

        for (int i = 0; i < nummarks; i++) {
            dupmarks[i] = (markLocations[i] / 10000 - topleftY) / scaleFactor
                    + (markLocations[i] % 10000 - topleftX) / scaleFactor
                    * 10000;
            ascTemplateLocations[i] = i;
        }
        sort(dupmarks, ascTemplateLocations, nummarks);

        log.debug("ascTemplateLocations -- ");
        for (int i = 0; i < nummarks; i++) {
            log.debug(ascTemplateLocations[i] + " ");
        }

        try {
            int prevrow = 0;
            int i = 0;
            while (i < nummarks) {
                for (int j = prevrow; j < dupmarks[i] / 10000; j++) {
                    linesOccupied[j] = false;
                    for (int k = 0; k < width; k++) {
                        ascTemplate[j][k] = -1;
                    }
                }

                int prevcol = 0;
                int j = i;
                prevrow = dupmarks[i] / 10000;

                for (; j < nummarks && dupmarks[j] / 10000 == prevrow; j++) {
                    for (int k = prevcol; k < dupmarks[j] % 10000; k++) {
                        ascTemplate[prevrow][k] = -1;
                    }

                    ascTemplate[prevrow][dupmarks[j] % 10000] = j;
                    linesOccupied[prevrow] = true;
                    prevcol = dupmarks[j] % 10000 + 1;
                }

                for (int k = prevcol; k < width; k++) {
                    ascTemplate[prevrow][k] = -1;
                }

                i = j;
                prevrow++;
            }

            for (int j = prevrow; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    ascTemplate[j][k] = -1;
                }
            }

            for (int m = 1; m < height; m++) {
                if (linesOccupied[m] && linesOccupied[m - 1]) {
                    for (int n = 0; n < width; n++) {
                        if (ascTemplate[m - 1][n] > -1) {
                            ascTemplate[m][n] = ascTemplate[m - 1][n];
                            ascTemplate[m - 1][n] = -1;
                        }
                    }
                    linesOccupied[m - 1] = false;
                }
            }

            final int[][] ascTemplateDup = new int[height][width];
            final boolean[] linesOccupiedDup = new boolean[height];
            int heightdup = height;
            boolean skip = false;

            for (int n = 0; n < width; n++) {
                ascTemplateDup[0][n] = ascTemplate[0][n];
            }

            int dupm = 0;
            for (int m = 1; m < height; m++) {
                if (!skip && !linesOccupied[m] && !linesOccupied[m - 1]) {
                    skip = true;
                    heightdup--;
                } else {
                    skip = false;
                    dupm++;
                    for (int n = 0; n < width; n++) {
                        ascTemplateDup[dupm][n] = ascTemplate[m][n];
                    }
                    linesOccupiedDup[dupm] = linesOccupied[m];
                }
            }

            ascTemplate = ascTemplateDup;
            linesOccupied = linesOccupiedDup;
            height = heightdup;

            dumpAscTemplate(ascTemplate, width, height, linesOccupied);

            final PrintWriter out = new PrintWriter(new FileOutputStream(
                    filename));
            final int[] ascTemplateLocationsDup = new int[nummarks];
            for (int m = 0; m < height; m++) {
                for (int n = 0; n < width; n++) {
                    if (ascTemplate[m][n] > -1) {
                        ascTemplateLocationsDup[ascTemplateLocations[ascTemplate[m][n]]] = m
                                * 1000 + n;
                        log.debug(markLocations[ascTemplateLocations[ascTemplate[m][n]]]
                                % 10000
                                + ":"
                                + markLocations[ascTemplateLocations[ascTemplate[m][n]]]
                                / 10000);
                        out.print("0");
                    } else {
                        out.print("-");
                    }
                }
                out.println();
            }

            out.close();
            ascTemplateLocations = ascTemplateLocationsDup;

            log.debug("ascTemplateLocations -- ");
            for (int q = 0; q < nummarks; q++) {
                log.debug(ascTemplateLocations[q] + " ");
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void dumpAscTemplate(final int[][] ascTemplate, final int width,
            final int height, final boolean[] linesOccupied) {
        for (int i = 0; i < height; i++) {
            log.debug(linesOccupied[i] + " ");

            for (int j = 0; j < width; j++) {
                if (ascTemplate[i][j] > -1) {
                    log.debug("{}", ascTemplate[i][j]);
                } else {
                    log.debug("-");
                }
            }
        }
    }

    private void sort(final int[] marks, final int[] ascTemplateLocations,
            final int nummarks) {
        int t;
        for (int i = 0; i < nummarks; i++) {
            for (int j = i + 1; j < nummarks; j++) {
                if (marks[i] > marks[j]) {
                    t = marks[i];
                    marks[i] = marks[j];
                    marks[j] = t;
                    t = ascTemplateLocations[i];
                    ascTemplateLocations[i] = ascTemplateLocations[j];
                    ascTemplateLocations[j] = t;
                }
            }
        }
    }

    private int filter(final int[] marks, final int[] dupmarks,
            final int nummarks) {
        int numdupmarks = 0;
        final int[] cluster = new int[50];
        int numin;
        int i = 0;

        while (i < nummarks) {
            numin = 0;
            cluster[numin++] = marks[i];
            int j = 0;
            log.debug("i->" + marks[i] + ":");
            marks[i] = -1;

            while (j < nummarks) {
                if (marks[j] != -1) {
                    int k = 0;
                    while (k < numin) {
                        if (Math.abs(marks[j] / 1000 - cluster[k] / 1000) < 2
                                && Math.abs(marks[j] % 1000 - cluster[k] % 1000) < 2) {
                            cluster[numin++] = marks[j];
                            log.debug("Found j->" + marks[j] + ":");
                            marks[j] = -1;
                            j = i + 1;
                            k = numin + 1;
                            break;
                        } else {
                            k++;
                        }
                    }
                }
                j++;
            }

            int sumx = 0, sumy = 0;
            for (int l = 0; l < numin; l++) {
                sumx += cluster[l] / 1000;
                sumy += cluster[l] % 1000;
            }
            sumx /= numin;
            sumy /= numin;
            dupmarks[numdupmarks++] = sumx * 1000 + sumy;

            i++;
            while (marks[i] == -1) {
                i++;
            }
            log.debug("New: i->" + marks[i]);
        }

        return numdupmarks;
    }

    private void rescale() {
        try {
            final MedianFilter filter = new MedianFilter();
            filter.setArea(width / 1700 * 25 / 2 * 2 + 1, height / 2339 * 25
                    / 2 * 2 + 1);
            filter.setInputImage(grayimage);
            filter.process();
            final Gray8Image medianimage = (Gray8Image) filter.getOutputImage();
            // ImageUtil.saveImage(medianimage, "median.png");

            final ScaleReplication scale = new ScaleReplication();
            scale.setInputImage(medianimage);
            scale.setSize(width / scaleFactor, height / scaleFactor);
            scale.process();
            final PixelImage scaledImage = scale.getOutputImage();

            final int scaledtopleftX = topleftX / scaleFactor;
            final int scaledtopleftY = topleftY / scaleFactor;
            final int scaledbottomrightX = bottomrightX / scaleFactor;
            final int scaledbottomrightY = bottomrightY / scaleFactor;

            ImageUtil.putMark((Gray8Image) scaledImage, scaledtopleftX,
                    scaledtopleftY, true);
            ImageUtil.putMark((Gray8Image) scaledImage, scaledbottomrightX,
                    scaledbottomrightY, true);

            // ImageUtil.saveImage(scaledImage, "scaled.png");
            this.scaledImage = (Gray8Image) scaledImage;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void writeConfig(final String filename) {
        try {
            final PrintWriter out = new PrintWriter(new FileOutputStream(
                    filename));
            out.println("#top left");
            out.println(topleftX + " " + topleftY);
            out.println("#bottom right");
            out.println(bottomrightX + " " + bottomrightY);
            out.println("#angle");
            out.println(currAngle);
            out.println("#diag");
            out.println(currDiag);
            out.println("#nummarks");
            out.println(nummarks);
            out.println("#marks");
            int x, y;
            double r1, theta1, r2, theta2;

            for (int i = 0; i < nummarks; i++) {
                x = markLocations[i] / 10000;
                y = markLocations[i] % 10000;
                r1 = Math.sqrt((x - topleftX) * (x - topleftX) + (y - topleftY)
                        * (y - topleftY));
                r2 = Math.sqrt((x - bottomrightX) * (x - bottomrightX)
                        + (y - bottomrightY) * (y - bottomrightY));
                theta1 = Math.toDegrees(Math.atan2(x - topleftX, y - topleftY));
                theta2 = Math.toDegrees(Math.atan2(bottomrightX - x,
                        bottomrightY - y));
                out.println(r1 + " " + theta1 + " " + r2 + " " + theta2 + " "
                        + ascTemplateLocations[i] / 1000 + " "
                        + ascTemplateLocations[i] % 1000);
            }
            out.close();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void readConfig(final InputStream is) {
        final int scaleFactor = this.scaleFactor * 3;

        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    is));
            in.readLine();
            final String topleft = in.readLine();
            int index = topleft.indexOf(" ");
            final int realTopleftX = Integer.parseInt(topleft.substring(0,
                    index));
            final int realTopleftY = Integer.parseInt(topleft
                    .substring(index + 1));
            in.readLine();
            final String bottomright = in.readLine();
            index = bottomright.indexOf(" ");
            final int realBottomrightX = Integer.parseInt(bottomright
                    .substring(0, index));
            final int realBottomrightY = Integer.parseInt(bottomright
                    .substring(index + 1));
            in.readLine();
            realAngle = Double.parseDouble(in.readLine());
            in.readLine();
            realDiag = Double.parseDouble(in.readLine());
            in.readLine();
            realNummarks = Integer.parseInt(in.readLine());
            in.readLine();
            String line;
            realMarkLocations = new int[realNummarks];

            ascTemplate = new int[(realBottomrightY - realTopleftY)
                    / scaleFactor][(realBottomrightX - realTopleftX)
                    / scaleFactor];
            for (int i = 0; i < (realBottomrightY - realTopleftY) / scaleFactor; i++) {
                for (int j = 0; j < (realBottomrightX - realTopleftX)
                        / scaleFactor; j++) {
                    ascTemplate[i][j] = -1;
                }
            }

            final Gray8Image markedImage = (Gray8Image) grayimage.createCopy();

            int i = 0;
            while ((line = in.readLine()) != null && !line.equals("")) {
                final StringTokenizer st = new StringTokenizer(line, " ");
                double r1 = Double.parseDouble(st.nextToken());
                double theta1 = Double.parseDouble(st.nextToken());
                double r2 = Double.parseDouble(st.nextToken());
                double theta2 = Double.parseDouble(st.nextToken());
                final int m = Integer.parseInt(st.nextToken());
                final int n = Integer.parseInt(st.nextToken());
                theta1 += currAngle - realAngle;
                theta2 += currAngle - realAngle;
                r1 *= currDiag / realDiag;
                r2 *= currDiag / realDiag;
                final int x1 = (int) (topleftX + r1
                        * Math.sin(Math.toRadians(theta1)));
                final int y1 = (int) (topleftY + r1
                        * Math.cos(Math.toRadians(theta1)));
                final int x2 = (int) (bottomrightX - r2
                        * Math.sin(Math.toRadians(theta2)));
                final int y2 = (int) (bottomrightY - r2
                        * Math.cos(Math.toRadians(theta2)));
                realMarkLocations[i++] = (x1 + x2) / 2 * 10000 + (y1 + y2) / 2;
                ascTemplate[m][n] = i - 1;
                ImageUtil.putMark(markedImage, (x1 + x2) / 2, (y1 + y2) / 2,
                        true);
            }

            in.close();
            // ImageUtil.saveImage(markedImage, "markedform.png");
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void readFields(final InputStream is) {
        String line;
        fields = new Hashtable<Character, Field>();

        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    is));
            while ((line = in.readLine()) != null && !line.equals("")) {
                final Field field = new Field(line);
                fields.put(new Character(field.getCh()), field);
            }

            in.close();
            numfields = fields.size();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void readAscTemplate(final InputStream is) {
        ascTemplateLocations = new int[realNummarks];
        ascTemplateFields = new Field[realNummarks];
        int m = 0, n;

        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    is));
            String line;
            while ((line = in.readLine()) != null && !line.equals("")) {
                n = 0;
                for (int i = 0; i < line.length(); i++) {
                    final char ch = line.charAt(i);
                    if (ch != '-' && ch != '0') {
                        ascTemplateLocations[ascTemplate[m][n]] = ch;
                        final Field field = fields.get(new Character(ch));
                        ascTemplateFields[ascTemplate[m][n]] = field;
                        field.addPos(ascTemplate[m][n]); // always added in row, column order
                        log.debug("added " + m + ":" + n + ":"
                                + ascTemplate[m][n] + ":"
                                + realMarkLocations[ascTemplate[m][n]] + ":"
                                + ch);
                    }

                    // else {
                    // ascTemplateLocations[ascTemplate[m][n]] = -1;
                    // }
                    n++;
                }

                m++;
            }

            in.close();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void searchMarks() {
        for (int i = 0; i < realNummarks; i++) {
            log.debug((char) ascTemplateLocations[i] + " ");
        }

        int x, y;
        final SolidMark mark = new SolidMark(grayimage, width
                / ConcentricCircle.a4width, height / ConcentricCircle.a4height);
        final Gray8Image markedImage = (Gray8Image) grayimage.createCopy();
        for (int i = 0; i < realNummarks; i++) {
            x = realMarkLocations[i] / 10000;
            y = realMarkLocations[i] % 10000;

            if (mark.isMark(x, y)) {
                final Field field = fields.get(new Character(
                        (char) ascTemplateLocations[i]));
                log.debug("*** " + i + ":" + (char) ascTemplateLocations[i]
                        + ":" + field);
                field.putValue(i);
                log.debug("Found mark at " + x + "," + y + ":"
                        + (char) ascTemplateLocations[i] + ":"
                        + field.getName() + "=" + field.getValue(i));
                mark.putMarkOnImage(markedImage);
            }
        }

        // ImageUtil.saveImage(markedImage, "marksfoundform.png");
    }

    public void saveData(final String filename) {
        try {
            final PrintWriter out = new PrintWriter(new FileOutputStream(
                    filename));
            final Enumeration<Character> e = fields.keys();

            while (e.hasMoreElements()) {
                final Field field = fields.get(e.nextElement());
                log.debug(field.getName() + "=" + field.getFieldValues());
                out.println(field.getName() + "=" + field.getFieldValues());
            }

            out.close();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    int ROW_CHOICE = 0, COLUMN_CHOICE = 1, GRID_CHOICE = 2;

    int SINGLE = 0, MULTIPLE = 1, COLUMN = 2, ROW = 3;

    class Field {
        char ch;

        int type, subtype;

        String name;

        String[] choices;

        Hashtable<Integer, Integer> positions;

        String[] values;

        int numValues = 0;

        boolean[] singleDone;

        public Field(final String line) {
            final StringTokenizer st = new StringTokenizer(line, " ");
            ch = st.nextToken().charAt(0);
            final String typestr = st.nextToken();

            if (typestr.equalsIgnoreCase("row")) {
                type = ROW_CHOICE;
            } else if (typestr.equalsIgnoreCase("column")) {
                type = COLUMN_CHOICE;
            } else if (typestr.equalsIgnoreCase("grid")) {
                type = GRID_CHOICE;
            }

            final String subtypestr = st.nextToken();
            if (subtypestr.equalsIgnoreCase("single")) {
                subtype = SINGLE;
            } else if (subtypestr.equalsIgnoreCase("multiple")) {
                subtype = MULTIPLE;
            } else if (subtypestr.equalsIgnoreCase("column")) {
                subtype = COLUMN;
            } else if (subtypestr.equalsIgnoreCase("row")) {
                subtype = ROW;
            }

            name = st.nextToken();
            final ArrayList<String> choicearr = new ArrayList<String>();

            while (st.hasMoreTokens()) {
                choicearr.add(st.nextToken());
            }

            choices = new String[choicearr.size()];
            for (int i = 0; i < choicearr.size(); i++) {
                choices[i] = choicearr.get(i);
            }

            if (type != GRID_CHOICE) {
                values = new String[choices.length];
                singleDone = null;
            } else {
                values = new String[100];
                singleDone = new boolean[100];
            }

            positions = new Hashtable<Integer, Integer>();
        }

        public char getCh() {
            return ch;
        }

        public int getType() {
            return type;
        }

        public int getSubtype() {
            return subtype;
        }

        public String getName() {
            return name;
        }

        public String[] getChoices() {
            return choices;
        }

        int currpos = 0;

        public void addPos(final int i) {
            if (type == ROW_CHOICE) {
                positions.put(new Integer(i), new Integer(currpos++));
            } else if (type == COLUMN_CHOICE) {
                positions.put(new Integer(i), new Integer(currpos++));
            } else if (type == GRID_CHOICE && subtype == ROW) {
                positions.put(new Integer(i), new Integer(currpos
                        % choices.length));
                currpos++;
            } else if (type == GRID_CHOICE && subtype == COLUMN) {
                log.debug("addpos -- " + i + ":" + currpos);
                positions.put(new Integer(i), new Integer(currpos++));
            }
        }

        public String getValue(final int i) {
            if (type == GRID_CHOICE && subtype == COLUMN) {
                final int mod = currpos / choices.length;
                return choices[positions.get(new Integer(i)).intValue() / mod];
            } else {
                return choices[positions.get(new Integer(i)).intValue()];
            }
        }

        public void putValue(final int i) {
            if (type == GRID_CHOICE && subtype == COLUMN) {
                final int posi = positions.get(new Integer(i)).intValue();
                log.debug("currpos = " + currpos + ":" + choices.length + ":"
                        + i + ":" + posi + ":" + posi
                        % (currpos / choices.length) + ":"
                        + singleDone[posi % (currpos / choices.length)]);
                if (!singleDone[posi % (currpos / choices.length)]) {
                    values[numValues++] = getValue(i);
                    singleDone[posi % (currpos / choices.length)] = true;
                }
            } else if (type == GRID_CHOICE && subtype == ROW) {
                final int posi = positions.get(new Integer(i)).intValue();
                if (!singleDone[posi / choices.length]) {
                    values[numValues++] = getValue(i);
                    singleDone[posi / choices.length] = true;
                }
            } else {
                values[numValues++] = getValue(i);
            }
        }

        public String getFieldValues() {
            if (subtype == SINGLE) {
                return values[0];
            } else if (subtype == MULTIPLE) {
                String retval = "";
                for (int i = 0; i < numValues; i++) {
                    retval = retval + " " + values[i];
                }
                if (retval.length() > 0) {
                    return retval.substring(1);
                } else {
                    return "";
                }
            } else if (type == GRID_CHOICE) {
                String retval = "";
                for (int i = 0; i < numValues; i++) {
                    retval = retval + values[i];
                }
                if (retval.length() > 0) {
                    return retval;
                } else {
                    return "";
                }
            }
            return "";
        }
    }
}
