import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    private JPanel background;
    private List<GameOBJ> objs = new ArrayList<>();

    public static Main game;

    public static class MEMORY {


        public static BufferedImage bombImage;
        public static BufferedImage explosion[];
        public static BufferedImage helicopter[];

        public static void init() {
            explosion = new BufferedImage[Explosion.FRAMES];
            helicopter = new BufferedImage[Helicopter.FRAMES];


            try {
                bombImage = ImageIO.read(new File("./img/bomb.png"));
                for (int i = 0; i < Helicopter.FRAMES; i++)
                    helicopter[i] = ImageIO.read(new File("./img/h" + (i + 1) + ".png"));
                for (int i = 0; i < Explosion.FRAMES; i++)
                    explosion[i] = ImageIO.read(new File("./img/" + (i + 1) + ".png"));


            } catch (Exception e) {
            }


        }

        public static void playSound1() {
            try {
                AudioInputStream audioInputStream1 = AudioSystem.getAudioInputStream(new File("./img/sound1.wav").getAbsoluteFile());
                Clip clipSound1 = AudioSystem.getClip();
                clipSound1.open(audioInputStream1);
                clipSound1.start();
            } catch (Exception e) {
            }
        }


    }

    public Main() {
        //

        setBounds(200, 200, 500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setLayout(null);


        background = new JPanel();
        background.setBounds(0, 0, 500, 500);
        background.setOpaque(true);
        background.setBackground(new Color(28, 28,28, 255));
        background.setLayout(null);


        add(background);


        Line line = new Line(50, 400, 400, 10);
        addObj(line);

        Helicopter helicopter = new Helicopter(100, 100);
        addObj(helicopter);


        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {


                Bomb bomb = new Bomb(helicopter.getObjX() + 32, helicopter.getObjY() + 32, 10);
                addObj(bomb);

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'a') helicopter.setDIRECTION(Helicopter.TYPE_DIRECTION.LEFT);
                else if (e.getKeyChar() == 'w') helicopter.setDIRECTION(Helicopter.TYPE_DIRECTION.TOP);
                else if (e.getKeyChar() == 'd') helicopter.setDIRECTION(Helicopter.TYPE_DIRECTION.RIGHT);
                else if (e.getKeyChar() == 's') helicopter.setDIRECTION(Helicopter.TYPE_DIRECTION.BOTTOM);


            }

            @Override
            public void keyReleased(KeyEvent e) {
                helicopter.setDIRECTION(Helicopter.TYPE_DIRECTION.STOP);
            }
        });

        //thread
        new Thread(() -> {

            while (true) {

                update();

                //delay
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }).start();


    }

    public void update() {

        try {

            for (GameOBJ s : objs)
                s.updateOBJ();


            for (int i = 0; i < objs.size(); i++) {

                for (int j = 0; j < objs.size(); j++) {

                    GameOBJ a = objs.get(i);
                    GameOBJ b = objs.get(j);

                    if (a == b)
                        continue;


                    if (a.isCollided(b)) {
                        a.stopOBJ();
                        a.goLastLocation();
                    }

                }
            }


            background.repaint();
        } catch (Exception e) {
            //for arraylist exception
        }

    }

    public synchronized void addObj(GameOBJ obj) {

        try {
            objs.add(obj);
            background.add(obj);
            background.repaint();
        } catch (Exception e) {
        }

    }

    public synchronized void removeObj(GameOBJ obj) {

        try {
            objs.remove(obj);
            background.remove(obj);
            background.repaint();
        } catch (Exception e) {
        }

    }

    public static void main(String[] args) {

        MEMORY.init();
        game = new Main();

    }




    public static class Bomb extends GameOBJ {

        public int speed;

        //size of snows
        public static final int SIZE = 32;

        public Bomb(int x, int y, int speed) {
            super(x, y, SIZE, SIZE);
            this.speed = speed;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(MEMORY.bombImage, 0, 0, SIZE, SIZE, null);
            } catch (Exception e) {
            }

        }

        public void setXY(int x, int y) {
            //!!!
            setPreX(getObjX());
            setPreY(getObjY());

            setObjX(x);
            setObjY(y);
            setLocation(x, y);
            //this is for linux (Graphic card) !!!
            Toolkit.getDefaultToolkit().sync();
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        @Override
        public void updateOBJ() {

            setXY(getObjX(), getObjY() + speed);

        }

        @Override
        public void stopOBJ() {
            super.stopOBJ();
            speed = 0;

            game.removeObj(this);
            Explosion explosion = new Explosion(getObjX() - Explosion.SIZE / 2, getObjY() - Explosion.SIZE / 2);
            game.addObj(explosion);

        }

        @Override
        public void goLastLocation() {
            super.goLastLocation();
            setXY(getPreX(), getPreY());
        }

        @Override
        public boolean isCollided(GameOBJ obj) {

            if (obj instanceof Explosion || obj instanceof Helicopter || obj instanceof Bomb)
                return false;

            return super.isCollided(obj);
        }
    }

    public static class Explosion extends GameOBJ {


        public int frame = 1;
        public final static int FRAMES = 48;

        public static final int SIZE = 50;

        public Explosion(int x, int y) {
            super(x, y, SIZE, SIZE);

            MEMORY.playSound1();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                BufferedImage image = ImageIO.read(new File("./img/" + frame + ".png"));
                g2.drawImage(MEMORY.explosion[frame - 1], 0, 0, SIZE, SIZE, null);
            } catch (Exception e) {
            }

        }


        @Override
        public void updateOBJ() {

            if (frame < FRAMES) {
                frame++;
            } else
                stopOBJ();

        }

        @Override
        public void stopOBJ() {
            super.stopOBJ();
            game.removeObj(this);

        }

        @Override
        public boolean isCollided(GameOBJ obj) {
            return false;
        }
    }

    public static class Helicopter extends GameOBJ {

        public int DIRECTION = 0;

        public static class TYPE_DIRECTION {
            public final static int LEFT = 1;
            public final static int TOP = 2;
            public final static int RIGHT = 3;
            public final static int BOTTOM = 4;
            public final static int STOP = 0;
        }

        public int frame = 1;
        public int speed = 5;
        public final static int FRAMES = 4;
        public static final int SIZE = 80;


        public Helicopter(int x, int y) {
            super(x, y, SIZE, SIZE);


        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(MEMORY.helicopter[frame - 1], 0, 0, SIZE, SIZE, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        @Override
        public void updateOBJ() {

            if (frame < FRAMES)
                frame++;
            else
                frame = 1;

            if (DIRECTION == TYPE_DIRECTION.RIGHT)
                setXY(getObjX() + speed, getObjY());
            else if (DIRECTION == TYPE_DIRECTION.LEFT)
                setXY(getObjX() - speed, getObjY());
            else if (DIRECTION == TYPE_DIRECTION.TOP)
                setXY(getObjX(), getObjY() - speed);
            else if (DIRECTION == TYPE_DIRECTION.BOTTOM)
                setXY(getObjX(), getObjY() + speed);
        }

        public void setDIRECTION(int DIRECTION) {
            this.DIRECTION = DIRECTION;
        }

        public void setXY(int x, int y) {
            //!!!
            setPreX(getObjX());
            setPreY(getObjY());

            setObjX(x);
            setObjY(y);
            setLocation(x, y);
            //this is for linux (Graphic card) !!!
            Toolkit.getDefaultToolkit().sync();
        }

        @Override
        public void goLastLocation() {
            super.goLastLocation();
            setXY(getPreX(), getPreY());
        }

        @Override
        public boolean isCollided(GameOBJ obj) {

            if (obj instanceof Bomb || obj instanceof Explosion)
                return false;

            return super.isCollided(obj);
        }
    }


    public static class Line extends GameOBJ {
        public Line(int objX, int objY, int WIDTH, int STROKE) {
            super(objX, objY, WIDTH, STROKE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);


            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GREEN);
            g2.setStroke(new BasicStroke(getObjHeight()));
            g2.drawLine(0, 0, getObjWidth(), 0);

        }
    }

    public static class GameOBJ extends JComponent {

        private int objX, objY, objWidth, objHeight;
        private int preX, preY;

        public GameOBJ(int objX, int objY, int objWidth, int objHeight) {
            this.objX = objX;
            this.objY = objY;
            this.objWidth = objWidth;
            this.objHeight = objHeight;

            setBounds(objX, objY, objWidth, objHeight);
            setLayout(null);
            setOpaque(true);
        }

        public void setObjX(int objX) {
            this.objX = objX;
        }

        public void setObjY(int objY) {
            this.objY = objY;
        }

        public void setObjWidth(int objWidth) {
            this.objWidth = objWidth;
        }

        public void setObjHeight(int objHeight) {
            this.objHeight = objHeight;
        }

        public int getObjX() {
            return objX;
        }

        public int getObjY() {
            return objY;
        }

        public int getObjWidth() {
            return objWidth;
        }

        public int getObjHeight() {
            return objHeight;
        }

        public void setPreX(int preX) {
            this.preX = preX;
        }

        public void setPreY(int preY) {
            this.preY = preY;
        }

        public int getPreX() {
            return preX;
        }

        public int getPreY() {
            return preY;
        }

        public void updateOBJ() {

        }

        public Rectangle getRectangle() {
            return new Rectangle(objX, objY, objWidth, objHeight);
        }

        public boolean isCollided(GameOBJ obj) {

            return getRectangle().intersects(obj.getRectangle());

        }

        public void stopOBJ() {

        }

        public void goLastLocation() {

        }
    }


}
