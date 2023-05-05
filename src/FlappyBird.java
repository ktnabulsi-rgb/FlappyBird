import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class FlappyBird extends ApplicationAdapter
{
    private OrthographicCamera camera; //the camera to our world
    private Viewport viewport; //maintains the ratios of your world
    private ShapeRenderer renderer; //used to draw textures and fonts
    private BitmapFont font; //used to draw fonts (text)
    private SpriteBatch batch; //also needed to draw fonts (text)
    private GlyphLayout layout; //needed to get the width and height of our text message

    private Array<Rectangle> tubes;

    private Circle bird;
    private float velY;
    private boolean started;
    private float time;
    private Sound clickSound;
    private Music thinkingMusic;
    private boolean menu;
    private boolean game;

    private Vector2 mousePos;
    private Circle circleMenu;
    private Texture birdFlap;
    private Texture packAPunch;
    private Texture background;
    private int ctr;
    private int ctr2;
    private int scoreCtr;

    public static final float WORLD_WIDTH = 600;
    public static final float WORLD_HEIGHT = 800;
    public static final float TUBE_WIDTH = 100;
    public static final float TUBE_BUFFER = 70;//buffer between the top and bottom of the screen
    //so tubes aren't created too high or too low
    public static final float TUBE_GAP_VERTICAL = 210; //gap between the tubes

    public static final float RADIUS = 35;
    public static final float SCROLL_SPEED = 4;//how fast the tubes move across the screen
    public static final float SPAWN_RATE = 1.8f;//how long it takes to spawn another set of tubes
    public static final float GRAVITY = WORLD_HEIGHT;
    public static final float JUMP_SPEED = WORLD_HEIGHT * 30f;
    public static final int RADIUS2 = 50;
    public static final float CENTER_X = WORLD_WIDTH / 2;
    public static final float CENTER_Y = WORLD_HEIGHT / 2;

    @Override//called once when we start the game
    public void create(){
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();
        font = new BitmapFont();
        batch = new SpriteBatch();//if you want to use images instead of using ShapeRenderer, also needed for text
        layout = new GlyphLayout();

        started = false;

        //generates a random y value for the bottom left of the top tube
        float y = generateRandomY();
        tubes = new Array<Rectangle>();

        //create the top tube rectangle
        tubes.add(new Rectangle(WORLD_WIDTH, y, TUBE_WIDTH, WORLD_HEIGHT - y));
        //create the bottom tube rectangle
        tubes.add(new Rectangle(WORLD_WIDTH, 0, TUBE_WIDTH, y - TUBE_GAP_VERTICAL));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("codMusic.mp3"));
        thinkingMusic = Gdx.audio.newMusic(Gdx.files.internal("codMusic2.mp3"));

        bird = new Circle(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, RADIUS);

        circleMenu = new Circle(CENTER_X, CENTER_Y,RADIUS2);
        birdFlap = new Texture(Gdx.files.internal("birdFlap.png"));
        packAPunch = new Texture(Gdx.files.internal("packAPunch.jpg"));
        mousePos = new Vector2();
        background = new Texture("background.jpg");

        velY = 0; //y velocity to add onto the bird whether it is jumping or falling due to gravity
        time = 0; //keep track of the time that has passed to spawn a new tube
        ctr = 0;
        ctr2 = 0;
        scoreCtr = 0;
        menu = true;
        game = false;
        thinkingMusic.setLooping(true);
    }

    @Override//called 60 times a second
    public void render(){

        viewport.apply();

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float delta = Gdx.graphics.getDeltaTime();//1/60

        float y = 0;

        int x1 = Gdx.input.getX();
        int y1 = Gdx.input.getY();
        mousePos = viewport.unproject(new Vector2(x1, y1));

        if(menu) {
            batch.begin();
            batch.draw(background, 0, 0);
            batch.end();

            if(ctr2 < 1) {
                thinkingMusic.play();
                ctr2++;
            }

            Circle tempMouse = new Circle(mousePos.x, mousePos.y, 1);

            if(Intersector.overlaps(tempMouse, circleMenu)) {

                renderer.setColor(new Color(126/255f, 148/255f, 139/255f, 1f));
                font.setColor(Color.PINK);
                if(Gdx.input.justTouched())//if we click start the game
                {
                    started = true;
                    game = true;
                    menu = false;
                }

            }
            else {
                renderer.setColor(new Color(50/255f, 168/255f, 166/255f, 1f));
                font.setColor(new Color(50/255f, 168/255f, 166/255f, 1f));
            }
            renderer.setProjectionMatrix(viewport.getCamera().combined);
            renderer.begin(ShapeType.Filled);
            renderer.setColor(new Color(50/255f, 168/255f, 166/255f, .5f));
            renderer.circle(circleMenu.x, circleMenu.y, circleMenu.radius);

            renderer.end();

            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();

            layout.setText(font, "START!");
            font.draw(batch, layout, WORLD_WIDTH / 2 - layout.width / 2, WORLD_HEIGHT / 2 + layout.height / 2);

            batch.end();

        }

        if(game == true) {


            if(ctr < 1) {
                thinkingMusic.stop();
                clickSound.play();
                ctr++;
            }

            if(started)
            {
                batch.begin();
                batch.draw(background, 0, 0);
                batch.end();
                time += delta; //add on 1/60 every time render is called

                //update the tubes
                for(Rectangle tube : tubes)
                {
                    tube.x -= SCROLL_SPEED;
                }

                //remove the tubes from the list if they go off the screen
                for(int i = 0; i < tubes.size; i++)
                {
                    Rectangle tube = tubes.get(i);
                    if(tube.x + TUBE_WIDTH < 0)
                    {
                        tubes.removeIndex(i);
                        i--;
                    }
                }

                //if a certain amount of time has passed create a new tube
                if(time > SPAWN_RATE)
                {
                    //create a new top and bottome Rectangle and reset the time
                    time = 0;
                    y = generateRandomY();
                    tubes.add(new Rectangle(WORLD_WIDTH, y, TUBE_WIDTH, WORLD_HEIGHT - y));
                    tubes.add(new Rectangle(WORLD_WIDTH, 0, TUBE_WIDTH, y - TUBE_GAP_VERTICAL));
                }

                //add GRAVITY to the y velocity so it it increases, causing the bird to fall faster
                velY -= GRAVITY * delta;
                if(Gdx.input.isKeyJustPressed(Keys.SPACE))
                {
                    velY = JUMP_SPEED * delta;//set the y velocity to the JUMP_SPEED if SPACE is pressed

                }
                bird.y += velY * delta; //change the y position of the bird based on the y velocity

                //TODO: loop through the tubes Array and check if any of the Rectangles overlaps
                //with the bird. Use the static method from the Intersector class, overlaps,
                //that has two parameters the first a Circle object and the second a Rectangle object
                //Intersector.overlaps(_Circle_, _Rectangle_)
                //if they intersect reset the game: started to false, the bird back to the middle of
                //the screen, velY to 0, and clear all the Rectangle objects from tubes Array.

                for(int i = 0; i < tubes.size; i++) {
                    if(Intersector.overlaps(bird, tubes.get(i))) { //LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOK HERE FOR ERRORS
                        started = false;
                        bird.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, RADIUS);
                        velY = 0;
                        tubes.clear();
                    }
                }

                //TODO check if the bird goes too high or too low. If it does, reset the game:
                //started to false, the bird back to the middle of
                //the screen, velY to 0, and clear all the Rectangle objects from tubes.
                for(int j = 0; j < tubes.size; j++) {
                    if(bird.y >= WORLD_HEIGHT - RADIUS || bird.y <= 0) {
                        started = false;
                        bird.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, RADIUS);
                        velY = 0;
                        tubes.clear();
                    }

                }

            }
            //draw everything on the screen
            renderer.setProjectionMatrix(viewport.getCamera().combined);
            renderer.setColor(Color.WHITE);
            renderer.begin(ShapeType.Filled);
            // renderer.circle(bird.x, bird.y, bird.radius);
            //TODO: draw the tubes rectangles and the bird (circle)

            for(int a = 0; a < tubes.size; a++) {
                Rectangle temp = tubes.get(a);
                renderer.rect(temp.x, temp.y, temp.width, temp.height);

            }

            renderer.end();

            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();

            if(!started)
            {
                batch.draw(background, 0, 0);


                Circle tempMouse = new Circle(mousePos.x, mousePos.y, 1);
                if(Intersector.overlaps(tempMouse, circleMenu)) {

                    renderer.setColor(new Color(126/255f, 148/255f, 139/255f, 1f));
                    font.setColor(Color.PINK);
                    layout.setText(font, "Click to start and SPACE to jump");
                    font.draw(batch, layout,
                            WORLD_WIDTH / 2 - layout.width / 2,
                            WORLD_HEIGHT/2 + layout.height / 2 + 20);
                    if(Gdx.input.justTouched())//if we click start the game
                    {
                        started = true;
                        game = true;
                        menu = false;
                    }

                }
                else {
                    renderer.setColor(new Color(50/255f, 168/255f, 166/255f, 1f));
                    font.setColor(new Color(50/255f, 168/255f, 166/255f, 1f));
                }

            }
            else {
                batch.draw(birdFlap, bird.x - bird.radius, bird.y - bird.radius, 2 * bird.radius, 2 * bird.radius);
                for(int a = 0; a < tubes.size; a++) {
                    Rectangle temp = tubes.get(a);
                    batch.draw(packAPunch, temp.x, temp.y, temp.width, temp.height);
                }
            }

            batch.end();
        }

    }

    public float generateRandomY()
    {

        //TODO: return a random y value from TUBE_BUFFER + TUBE_GAP_VAERTICAL to WORLD_HEIGHT - TUBE_BUFFER
        //Look up the random method from the MathUtils class in the libgdx library
        return MathUtils.random(TUBE_BUFFER + TUBE_GAP_VERTICAL, WORLD_HEIGHT - TUBE_BUFFER);
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    @Override
    public void dispose(){
        renderer.dispose();
        batch.dispose();
    }

}
